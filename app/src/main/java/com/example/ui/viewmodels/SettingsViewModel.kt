package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.CancellationException
import java.net.URL

import com.example.data.repository.QuranRepository
import com.example.data.repository.AudioRepository
import com.example.data.model.Surah
import java.io.File

data class UserNote(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long
)


enum class GamePhase { SETUP, LOADING, PLAYING, RESULT }
enum class GameSource { ENTIRE_QURAN, SPECIFIC_SURAH }
enum class GameType { ARABIC_TO_BENGALI, BENGALI_TO_ARABIC }

data class WordGameConfig(
    val source: GameSource = GameSource.ENTIRE_QURAN,
    val selectedSurah: Int = 1,
    val type: GameType = GameType.ARABIC_TO_BENGALI,
    val totalQuestions: Int = 10
)

data class WordQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val bookmarkDao: BookmarkDao,
    val quranRepository: QuranRepository,
    val audioRepository: AudioRepository
) : ViewModel() {


    private val _downloadingTafsirIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingTafsirIds: StateFlow<Set<String>> = _downloadingTafsirIds.asStateFlow()

    private val _downloadedTafsirIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedTafsirIds: StateFlow<Set<String>> = _downloadedTafsirIds.asStateFlow()

    private val _tafsirDownloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val tafsirDownloadProgress: StateFlow<Map<String, Float>> = _tafsirDownloadProgress.asStateFlow()
    
    fun updateDownloadedTafsirs() {
        viewModelScope.launch {
            val available = _availableTafsirs.value
            val downloaded = available.filter { quranRepository.isTafsirDownloaded(it.id.toString()) }.map { it.id.toString() }.toSet()
            _downloadedTafsirIds.value = downloaded
            
            // Auto-select 164 if nothing is selected and it's downloaded
            val currentSelected = selectedTafsirIds.value
            if (currentSelected.isEmpty() && downloaded.contains("164")) {
                setSelectedTafsirIds(setOf("164"))
            }
        }
    }

    fun downloadTafsir(id: String) {
        if (_downloadingTafsirIds.value.contains(id)) return
        _downloadingTafsirIds.value = _downloadingTafsirIds.value + id
        _tafsirDownloadProgress.value = _tafsirDownloadProgress.value.toMutableMap().apply { put(id, 0f) }
        
        viewModelScope.launch {
            try {
                quranRepository.downloadTafsir(id) { progress ->
                    _tafsirDownloadProgress.value = _tafsirDownloadProgress.value.toMutableMap().apply { put(id, progress) }
                }
                updateDownloadedTafsirs()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _downloadingTafsirIds.value = _downloadingTafsirIds.value - id
                _tafsirDownloadProgress.value = _tafsirDownloadProgress.value.toMutableMap().apply { remove(id) }
            }
        }
    }

    // ---------------- Offline Sync States ----------------
    private val _isDownloadingQuran = MutableStateFlow(false)
    val isDownloadingQuran: StateFlow<Boolean> = _isDownloadingQuran.asStateFlow()

    private val _quranDownloadProgress = MutableStateFlow(0) // 0 to 114
    val quranDownloadProgress: StateFlow<Int> = _quranDownloadProgress.asStateFlow()

    private val _quranDownloadError = MutableStateFlow<String?>(null)
    val quranDownloadError: StateFlow<String?> = _quranDownloadError.asStateFlow()

    private val _downloadedSurahsCount = MutableStateFlow(0)
    val downloadedSurahsCount: StateFlow<Int> = _downloadedSurahsCount.asStateFlow()

    private val _audioCacheSize = MutableStateFlow(0L)
    val audioCacheSize: StateFlow<Long> = _audioCacheSize.asStateFlow()

    // --- Audio manual download states ---
    private val _isDownloadingAudio = MutableStateFlow(false)
    val isDownloadingAudio: StateFlow<Boolean> = _isDownloadingAudio.asStateFlow()

    private val _audioDownloadProgress = MutableStateFlow(0) // 0 to 100%
    val audioDownloadProgress: StateFlow<Int> = _audioDownloadProgress.asStateFlow()

    private val _audioDownloadStatus = MutableStateFlow<String?>(null)
    val audioDownloadStatus: StateFlow<String?> = _audioDownloadStatus.asStateFlow()

    private val _audioDownloadError = MutableStateFlow<String?>(null)
    val audioDownloadError: StateFlow<String?> = _audioDownloadError.asStateFlow()

    private val _surahList = MutableStateFlow<List<Surah>>(emptyList())
    val surahList: StateFlow<List<Surah>> = _surahList.asStateFlow()

    private var audioDownloadJob: Job? = null

    fun loadSurahList() {
        viewModelScope.launch {
            try {
                _surahList.value = quranRepository.getSurahs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun downloadAudioForSurah(surahNumber: Int, surahName: String) {
        audioDownloadJob?.cancel()
        audioDownloadJob = viewModelScope.launch {
            _isDownloadingAudio.value = true
            _audioDownloadProgress.value = 0
            _audioDownloadStatus.value = "সুরা $surahName-এর অডিও ফাইল ডাউনলোড হচ্ছে..."
            _audioDownloadError.value = null
            try {
                // Ensure surah details are cached or loaded
                val combinedAyahs = quranRepository.getSurahDetailsCombined(surahNumber)
                val totalAyahs = combinedAyahs.size
                if (totalAyahs == 0) {
                    _audioDownloadStatus.value = "কোনো আয়াত পাওয়া যায়নি"
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    for ((index, ayah) in combinedAyahs.withIndex()) {
                        val url = ayah.audioUrl
                        if (url != null && url.isNotEmpty()) {
                            val localFile = audioRepository.getLocalAudioFile(url)
                            if (!localFile.exists() || localFile.length() == 0L) {
                                val tempFile = File(localFile.parent, localFile.name + ".temp")
                                try {
                                    URL(url).openStream().use { input ->
                                        tempFile.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    if (tempFile.exists() && tempFile.length() > 0) {
                                        tempFile.renameTo(localFile)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    if (tempFile.exists()) tempFile.delete()
                                    // Let's not crash/stop download of subsequent files for minor errors, but log
                                }
                            }
                        }
                        val progress = ((index + 1) * 100) / totalAyahs
                        _audioDownloadProgress.value = progress
                        updateAudioCacheSize()
                    }
                }
                _audioDownloadStatus.value = "সুরা $surahName-এর অডিও ডাউনলোড সফল হয়েছে!"
            } catch (e: Exception) {
                _audioDownloadError.value = e.localizedMessage ?: "অডিও ডাউনলোড ব্যর্থ হয়েছে"
                _audioDownloadStatus.value = null
            } finally {
                _isDownloadingAudio.value = false
                updateAudioCacheSize()
            }
        }
    }

    fun cancelAudioDownload() {
        audioDownloadJob?.cancel()
        _isDownloadingAudio.value = false
        _audioDownloadStatus.value = "ডাউনলোড বাতিল করা হয়েছে"
        updateAudioCacheSize()
    }

    fun updateDownloadedSurahsCount() {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                quranRepository.getDownloadedSurahsCount()
            }
            _downloadedSurahsCount.value = count
        }
    }

    fun updateAudioCacheSize() {
        val dir = File(repository.context.filesDir, "quran_audio")
        _audioCacheSize.value = getFolderSize(dir)
    }

    private fun getFolderSize(folder: File): Long {
        var length = 0L
        val files = folder.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    length += file.length()
                } else {
                    length += getFolderSize(file)
                }
            }
        }
        return length
    }

    private var downloadJob: Job? = null

    fun stopQuranDownload() {
        downloadJob?.cancel()
        _isDownloadingQuran.value = false
    }

    fun downloadAllQuranData() {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            _isDownloadingQuran.value = true
            _quranDownloadProgress.value = 0
            _quranDownloadError.value = null
            try {
                // First download Surah list
                quranRepository.getSurahs()
                
                // Then download each of the 114 Surahs
                for (i in 1..114) {
                    ensureActive()
                    if (!quranRepository.isSurahDownloaded(i)) {
                        try {
                            val edition = tanzilTextStyle.value
                            quranRepository.getSurahDetailsCombined(i, edition)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    _quranDownloadProgress.value = i
                    _downloadedSurahsCount.value = i
                }
            } catch (e: CancellationException) {
                // Ignored - job was cancelled
            } catch (e: Exception) {
                _quranDownloadError.value = e.localizedMessage ?: "ডাউনলোড ব্যর্থ হয়েছে"
            } finally {
                _isDownloadingQuran.value = false
                updateDownloadedSurahsCount()
            }
        }
    }

    fun deleteDownloadedQuranData() {
        viewModelScope.launch {
            quranRepository.deleteDownloadedSurahs()
            updateDownloadedSurahsCount()
        }
    }

    fun clearAudioCache() {
        viewModelScope.launch {
            val dir = File(repository.context.filesDir, "quran_audio")
            if (dir.exists()) {
                dir.deleteRecursively()
            }
            updateAudioCacheSize()
        }
    }

    val hijriOffset: StateFlow<Int> = repository.hijriOffsetFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )

    fun setHijriOffset(offset: Int) {
        viewModelScope.launch {
            repository.setHijriOffset(offset)
        }
    }

    val showTranslation: StateFlow<Boolean> = repository.showTranslationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = true
        )

    val showTransliteration: StateFlow<Boolean> = repository.showTransliterationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )

    val showTajweed: StateFlow<Boolean> = repository.showTajweedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )

    val keepScreenOnFlow: StateFlow<Boolean> = repository.keepScreenOnFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )

    fun setKeepScreenOn(keep: Boolean) {
        viewModelScope.launch {
            repository.setKeepScreenOn(keep)
        }
    }
        
    fun setShowTransliteration(show: Boolean) {
        viewModelScope.launch { repository.setShowTransliteration(show) }
    }
    fun setShowTajweed(show: Boolean) {
        viewModelScope.launch { repository.setShowTajweed(show) }
    }

    private val _availableTafsirs = MutableStateFlow<List<com.example.data.model.TafsirResourceDto>>(emptyList())
    val availableTafsirs: StateFlow<List<com.example.data.model.TafsirResourceDto>> = _availableTafsirs.asStateFlow()

    val selectedTafsirIds: StateFlow<Set<String>> = repository.selectedTafsirIdsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = setOf("164")
        )

    fun setSelectedTafsirIds(ids: Set<String>) {
        viewModelScope.launch { repository.setSelectedTafsirIds(ids) }
    }

    val selectedQariId: StateFlow<String> = repository.selectedQariIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = "ar.alafasy"
        )

    fun setSelectedQariId(qariId: String) {
        viewModelScope.launch { repository.setSelectedQariId(qariId) }
    }
    
    fun toggleTafsir(id: String) {
        val current = selectedTafsirIds.value.toMutableSet()
        val isDownloaded = downloadedTafsirIds.value.contains(id)

        if (current.contains(id)) {
            if (current.size > 1) { // ensure at least one is selected
                current.remove(id)
                setSelectedTafsirIds(current)
            }
        } else {
            if (!isDownloaded) {
                current.clear()
                current.add(id)
            } else {
                val nonDownloaded = current.filter { !downloadedTafsirIds.value.contains(it) }
                current.removeAll(nonDownloaded.toSet())
                
                if (current.size >= 3) {
                    current.remove(current.first())
                }
                current.add(id)
            }
            setSelectedTafsirIds(current)
        }
    }

    val tanzilTextStyle: StateFlow<String> = repository.tanzilTextStyleFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = "quran-simple"
        )

    fun toggleTranslation(show: Boolean) {
        viewModelScope.launch {
            repository.setShowTranslation(show)
        }
    }

    fun setTanzilTextStyle(style: String) {
        viewModelScope.launch {
            repository.setTanzilTextStyle(style)
        }
    }

    // 1. Bookmarks flow
    val bookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmark(bookmark)
        }
    }

    // 2. Profile & Notes Storage
    private val sharedPrefs by lazy {
        repository.context.getSharedPreferences("quran_menu_prefs", Context.MODE_PRIVATE)
    }

    // Profile State
    private val _username = MutableStateFlow("দ্বীনদার বান্দা")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _readingTimeMinutes = MutableStateFlow(60) // Default 1 hour reading
    val readingTimeMinutes: StateFlow<Int> = _readingTimeMinutes.asStateFlow()

    fun updateUsername(newName: String) {
        _username.value = newName
        sharedPrefs.edit().putString("username", newName).apply()
    }

    fun addReadingTime(mins: Int) {
        _readingTimeMinutes.value += mins
        sharedPrefs.edit().putInt("reading_time", _readingTimeMinutes.value).apply()
    }

    // Notes State
    private val _notes = MutableStateFlow<List<UserNote>>(emptyList())
    val notes: StateFlow<List<UserNote>> = _notes.asStateFlow()

    fun addNote(title: String, content: String) {
        val newNote = UserNote(
            id = System.currentTimeMillis().toString(),
            title = title,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        val updated = _notes.value + newNote
        _notes.value = updated
        saveNotesToPrefs(updated)
    }

    fun deleteNote(noteId: String) {
        val updated = _notes.value.filterNot { it.id == noteId }
        _notes.value = updated
        saveNotesToPrefs(updated)
    }

    private fun loadNotesFromPrefs() {
        val notesSet = sharedPrefs.getStringSet("user_notes_set", emptySet()) ?: emptySet()
        val loaded = notesSet.mapNotNull { noteStr ->
            val parts = noteStr.split("|||")
            if (parts.size >= 4) {
                UserNote(
                    id = parts[0],
                    title = parts[1],
                    content = parts[2],
                    timestamp = parts[3].toLongOrNull() ?: System.currentTimeMillis()
                )
            } else null
        }.sortedByDescending { it.timestamp }
        _notes.value = loaded
    }

    private fun saveNotesToPrefs(notesList: List<UserNote>) {
        val notesSet = notesList.map { "${it.id}|||${it.title}|||${it.content}|||${it.timestamp}" }.toSet()
        sharedPrefs.edit().putStringSet("user_notes_set", notesSet).apply()
    }

    // 3. Word Game State
    private val _gamePhase = MutableStateFlow(GamePhase.SETUP)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _gameConfig = MutableStateFlow(WordGameConfig())
    val gameConfig: StateFlow<WordGameConfig> = _gameConfig.asStateFlow()

    private val _dynamicQuestions = MutableStateFlow<List<WordQuestion>>(emptyList())
    val dynamicQuestions: StateFlow<List<WordQuestion>> = _dynamicQuestions.asStateFlow()

    private val _gameScore = MutableStateFlow(0)
    val gameScore: StateFlow<Int> = _gameScore.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _lastAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val lastAnswerCorrect: StateFlow<Boolean?> = _lastAnswerCorrect.asStateFlow()
    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer: StateFlow<String?> = _selectedAnswer.asStateFlow()

    fun updateGameConfig(config: WordGameConfig) {
        _gameConfig.value = config
    }
    
    fun setGamePhase(phase: GamePhase) {
        _gamePhase.value = phase
    }

    fun startDynamicGame() {
        _gamePhase.value = GamePhase.LOADING
        viewModelScope.launch {
            try {
                val config = _gameConfig.value
                val surahToFetch = if (config.source == GameSource.ENTIRE_QURAN) {
                    (1..114).random()
                } else {
                    config.selectedSurah
                }
                
                val ayahs = quranRepository.getSurahDetailsCombined(surahToFetch)
                val allWords = mutableListOf<com.example.data.model.QuranComWord>()
                for (ayah in ayahs) {
                    allWords.addAll(ayah.words.filter { it.charTypeName == "word" && it.translation?.text != null && it.textUthmani != null && it.translation.text.isNotBlank() && it.textUthmani.isNotBlank() })
                }
                
                // If not enough words, fallback to Surah Al-Baqarah
                val finalWords = if (allWords.size < config.totalQuestions) {
                    val fallbackAyahs = quranRepository.getSurahDetailsCombined(2)
                    allWords.clear()
                    for (ayah in fallbackAyahs) {
                        allWords.addAll(ayah.words.filter { it.charTypeName == "word" && it.translation?.text != null && it.textUthmani != null && it.translation.text.isNotBlank() && it.textUthmani.isNotBlank() })
                    }
                    allWords
                } else {
                    allWords
                }
                
                val selectedWords = finalWords.shuffled().take(config.totalQuestions)
                
                val generatedQuestions = selectedWords.map { word ->
                    val isArabicToBengali = config.type == GameType.ARABIC_TO_BENGALI
                    val numRegex = Regex("[0-9০-৯٠-٩]")
                    val questionTextRaw = if (isArabicToBengali) "${word.textUthmani}" else "${word.translation?.text}"
                    val correctAnsRaw = if (isArabicToBengali) "${word.translation?.text}" else "${word.textUthmani}"
                    
                    val questionText = questionTextRaw.replace(numRegex, "").trim()
                    val correctAns = correctAnsRaw.replace(numRegex, "").trim()
                    
                    // Pick 3 random wrong answers
                    val wrongWords = finalWords.filter { it.id != word.id }.shuffled().take(3)
                    val wrongAns = wrongWords.map { 
                        val raw = if (isArabicToBengali) "${it.translation?.text}" else "${it.textUthmani}"
                        raw.replace(numRegex, "").trim()
                    }.toMutableList()
                    
                    // Ensure unique options
                    var options = (wrongAns + correctAns).distinct()
                    while(options.size < 4 && finalWords.size > 4) {
                       val extraWord = finalWords.random()
                       val extraOptRaw = if (isArabicToBengali) "${extraWord.translation?.text}" else "${extraWord.textUthmani}"
                       val extraOpt = extraOptRaw.replace(numRegex, "").trim()
                       if (!options.contains(extraOpt)) {
                           options = options + extraOpt
                       }
                    }
                    
                    WordQuestion(questionText, options.shuffled(), correctAns)
                }
                
                _dynamicQuestions.value = generatedQuestions
                _gameScore.value = 0
                _currentQuestionIndex.value = 0
                _lastAnswerCorrect.value = null
                _selectedAnswer.value = null
                _gamePhase.value = GamePhase.PLAYING
                
            } catch (e: Exception) {
                e.printStackTrace()
                _gamePhase.value = GamePhase.SETUP // go back on error
            }
        }
    }

    fun submitAnswer(selectedAnswer: String) {
        _selectedAnswer.value = selectedAnswer
        val currentQ = _dynamicQuestions.value[_currentQuestionIndex.value]
        val isCorrect = selectedAnswer == currentQ.correctAnswer
        _lastAnswerCorrect.value = isCorrect
        if (isCorrect) {
            _gameScore.value += 1
        }
    }

    fun nextQuestion() {
        _lastAnswerCorrect.value = null
        _selectedAnswer.value = null
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < _dynamicQuestions.value.size) {
            _currentQuestionIndex.value = nextIdx
        } else {
            _gamePhase.value = GamePhase.RESULT
        }
    }

    fun resetGame() {
        _gamePhase.value = GamePhase.SETUP
        _gameScore.value = 0
        _currentQuestionIndex.value = 0
        _lastAnswerCorrect.value = null
        _selectedAnswer.value = null
        _dynamicQuestions.value = emptyList()
    }

    // 4. Quran Planner State
    private val _plannerTarget = MutableStateFlow("৩০ দিনে খতম")
    val plannerTarget: StateFlow<String> = _plannerTarget.asStateFlow()

    private val _plannerPagesRead = MutableStateFlow(0)
    val plannerPagesRead: StateFlow<Int> = _plannerPagesRead.asStateFlow()

    private val _plannerStartDate = MutableStateFlow(System.currentTimeMillis())
    val plannerStartDate: StateFlow<Long> = _plannerStartDate.asStateFlow()
    
    private val _plannerStreak = MutableStateFlow(0)
    val plannerStreak: StateFlow<Int> = _plannerStreak.asStateFlow()
    
    private val _plannerReminderEnabled = MutableStateFlow(false)
    val plannerReminderEnabled: StateFlow<Boolean> = _plannerReminderEnabled.asStateFlow()

    fun updatePlannerTarget(target: String) {
        _plannerTarget.value = target
        _plannerPagesRead.value = 0
        _plannerStartDate.value = System.currentTimeMillis()
        _plannerStreak.value = 0
        sharedPrefs.edit()
            .putString("planner_target", target)
            .putInt("planner_pages_read", 0)
            .putLong("planner_start_date", _plannerStartDate.value)
            .putInt("planner_streak", 0)
            .apply()
    }

    fun addPlannerPages(pages: Int) {
        val total = (_plannerPagesRead.value + pages).coerceAtMost(604).coerceAtLeast(0)
        _plannerPagesRead.value = total
        
        // Simple streak logic for demo: increment streak if adding pages
        if (pages > 0) {
            _plannerStreak.value += 1
            sharedPrefs.edit().putInt("planner_streak", _plannerStreak.value).apply()
        }
        
        sharedPrefs.edit().putInt("planner_pages_read", total).apply()
    }
    
    fun togglePlannerReminder(enabled: Boolean) {
        _plannerReminderEnabled.value = enabled
        sharedPrefs.edit().putBoolean("planner_reminder", enabled).apply()
    }

    // 5. Quran Hifz State
    private val _hifzProgress = MutableStateFlow<Map<String, String>>(emptyMap())
    val hifzProgress: StateFlow<Map<String, String>> = _hifzProgress.asStateFlow()

    fun updateHifzProgress(surahName: String, status: String) {
        val current = _hifzProgress.value.toMutableMap()
        current[surahName] = status
        _hifzProgress.value = current
        val serializedMap = current.entries.joinToString(";") { "${it.key}:${it.value}" }
        sharedPrefs.edit().putString("hifz_progress_map", serializedMap).apply()
    }

    init {
        _username.value = sharedPrefs.getString("username", "দ্বীনদার বান্দা") ?: "দ্বীনদার বান্দা"
        _readingTimeMinutes.value = sharedPrefs.getInt("reading_time", 60)
        _plannerTarget.value = sharedPrefs.getString("planner_target", "৩০ দিনে খতম") ?: "৩০ দিনে খতম"
        _plannerPagesRead.value = sharedPrefs.getInt("planner_pages_read", 0)
        _plannerStartDate.value = sharedPrefs.getLong("planner_start_date", System.currentTimeMillis())
        _plannerStreak.value = sharedPrefs.getInt("planner_streak", 0)
        _plannerReminderEnabled.value = sharedPrefs.getBoolean("planner_reminder", false)
        
        val hifzStr = sharedPrefs.getString("hifz_progress_map", "") ?: ""
        if (hifzStr.isNotEmpty()) {
            val hifzMap = hifzStr.split(";").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) parts[0] to parts[1] else null
            }.toMap()
            _hifzProgress.value = hifzMap
        }

        loadNotesFromPrefs()
        updateDownloadedSurahsCount()
        updateAudioCacheSize()
        
        viewModelScope.launch {
            _availableTafsirs.value = quranRepository.getAvailableTafsirs("bn")
            updateDownloadedTafsirs()
        }
    }
}
