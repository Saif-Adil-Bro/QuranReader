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
            initialValue = setOf("164", "169")
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
        if (current.contains(id)) {
            if (current.size > 1) { // ensure at least one is selected
                current.remove(id)
                setSelectedTafsirIds(current)
            }
        } else {
            if (current.size < 3) {
                current.add(id)
                setSelectedTafsirIds(current)
            }
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
    val questions = listOf(
        WordQuestion("আলামীন (العالمين)", listOf("সৃষ্টিজগৎ", "মানুষ", "ফেরেশতা", "নক্ষত্র"), "সৃষ্টিজগৎ"),
        WordQuestion("রাহমান (الرحمن)", listOf("পরম দয়ালু", "বিচারক", "স্রষ্টা", "মালিক"), "পরম দয়ালু"),
        WordQuestion("মা’বুদ (المعبود)", listOf("উপাস্য", "বন্ধু", "সাহায্যকারী", "শাসক"), "উপাস্য"),
        WordQuestion("ইয়াকীন (اليقين)", listOf("নিশ্চয়তা/বিশ্বাস", "সন্দেহ", "ভয়", "আশা"), "নিশ্চয়তা/বিশ্বাস"),
        WordQuestion("হুদা (هدى)", listOf("পথপ্রদর্শন", "জ্ঞান", "আলো", "পরিত্রাণ"), "পথপ্রদর্শন"),
        WordQuestion("সওম (الصوم)", listOf("রোজা/বিরত থাকা", "নামাজ", "হজ", "দান"), "রোজা/বিরত থাকা")
    )

    private val _gameScore = MutableStateFlow(0)
    val gameScore: StateFlow<Int> = _gameScore.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _lastAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val lastAnswerCorrect: StateFlow<Boolean?> = _lastAnswerCorrect.asStateFlow()

    fun submitAnswer(selectedAnswer: String) {
        val currentQ = questions[_currentQuestionIndex.value]
        val isCorrect = selectedAnswer == currentQ.correctAnswer
        _lastAnswerCorrect.value = isCorrect
        if (isCorrect) {
            _gameScore.value += 10
        }
    }

    fun nextQuestion() {
        _lastAnswerCorrect.value = null
        _currentQuestionIndex.value = (_currentQuestionIndex.value + 1) % questions.size
    }

    fun resetGame() {
        _gameScore.value = 0
        _currentQuestionIndex.value = 0
        _lastAnswerCorrect.value = null
    }

    // 4. Quran Planner State
    private val _plannerTarget = MutableStateFlow("১ পৃষ্ঠা")
    val plannerTarget: StateFlow<String> = _plannerTarget.asStateFlow()

    private val _plannerProgress = MutableStateFlow(setOf<String>())
    val plannerProgress: StateFlow<Set<String>> = _plannerProgress.asStateFlow()

    fun updatePlannerTarget(target: String) {
        _plannerTarget.value = target
        sharedPrefs.edit().putString("planner_target", target).apply()
    }

    fun togglePlannerDay(day: String) {
        val current = _plannerProgress.value.toMutableSet()
        if (current.contains(day)) {
            current.remove(day)
        } else {
            current.add(day)
        }
        _plannerProgress.value = current
        sharedPrefs.edit().putStringSet("planner_progress", current).apply()
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
        _plannerTarget.value = sharedPrefs.getString("planner_target", "১ পৃষ্ঠা") ?: "১ পৃষ্ঠা"
        _plannerProgress.value = sharedPrefs.getStringSet("planner_progress", emptySet()) ?: emptySet()
        
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
