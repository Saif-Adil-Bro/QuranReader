package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.model.CombinedAyah
import com.example.data.repository.QuranRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.AiRepository
import com.example.data.repository.AudioRepository
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File
import java.net.URL

enum class PlaybackMode { AYAH, SURAH }

class SurahDetailViewModel(
    private val repository: QuranRepository,
    private val settingsRepository: SettingsRepository,
    private val aiRepository: AiRepository,
    val audioRepository: AudioRepository,
    private val bookmarkDao: BookmarkDao
) : ViewModel() {

    private var currentSelectedQariId = "ar.alafasy"

    init {
        viewModelScope.launch {
            settingsRepository.selectedQariIdFlow.collect { qariId ->
                currentSelectedQariId = qariId
            }
        }
    }

    private val _uiState = MutableStateFlow<UiState<List<CombinedAyah>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<CombinedAyah>>> = _uiState.asStateFlow()

    private val _tafsirState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val tafsirState: StateFlow<UiState<String>> = _tafsirState.asStateFlow()

    // --- Manual offline caching state for current surah ---
    private val _isDownloadingOffline = MutableStateFlow(false)
    val isDownloadingOffline: StateFlow<Boolean> = _isDownloadingOffline.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0) // 0 to 100%
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val _downloadStatus = MutableStateFlow<String?>(null)
    val downloadStatus: StateFlow<String?> = _downloadStatus.asStateFlow()

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError.asStateFlow()

    private var downloadJob: Job? = null

    // Observe translation toggle
    val showTranslation: StateFlow<Boolean> = settingsRepository.showTranslationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = true
        )
        
    val showTransliteration: StateFlow<Boolean> = settingsRepository.showTransliterationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )

    val showTajweed: StateFlow<Boolean> = settingsRepository.showTajweedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )

    // Observe Font Sizes
    val arabicFontSize: StateFlow<Float> = settingsRepository.arabicFontSizeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = 24f
        )

    val bengaliFontSize: StateFlow<Float> = settingsRepository.bengaliFontSizeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = 16f
        )

    val arabicFontName: StateFlow<String> = settingsRepository.arabicFontNameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = "Amiri Quran"
        )

    val tanzilTextStyle: StateFlow<String> = settingsRepository.tanzilTextStyleFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = "quran-simple"
        )

    val arabicLineSpacing: StateFlow<Float> = settingsRepository.arabicLineSpacingFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = 2.0f
        )

    val showWaqfSigns: StateFlow<Boolean> = settingsRepository.showWaqfSignsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = true
        )

    // Playback state and Mode
    val isPlaying: StateFlow<Boolean> = audioRepository.isPlaying
    val currentPlayingAyahNumber: StateFlow<Int?> = audioRepository.currentPlayingAyahNumber

    private val _playbackMode = MutableStateFlow(PlaybackMode.SURAH)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun toggleBookmark(ayah: CombinedAyah, defaultSurahNumber: Int) {
        viewModelScope.launch {
            val existing = bookmarkDao.getBookmark("AYAH", ayah.number)
            if (existing != null) {
                bookmarkDao.deleteBookmark(existing)
            } else {
                val sNum = if (ayah.surahNumber > 0) ayah.surahNumber else defaultSurahNumber
                val surahPair = com.example.data.QuranData.surahNames.find { it.first == sNum }
                val surahName = surahPair?.second?.first ?: "সুরা $sNum"
                val bookmarkName = "$surahName: আয়াত ${ayah.numberInSurah}"
                bookmarkDao.insertBookmark(
                    BookmarkEntity(
                        type = "AYAH",
                        referenceId = ayah.number,
                        name = bookmarkName
                    )
                )
            }
        }
    }

    fun setPlaybackMode(mode: PlaybackMode) {
        _playbackMode.value = mode
    }

    fun playAyah(ayah: CombinedAyah, surahNumber: Int) {
        val audioUrl = ayah.audioUrl ?: "https://cdn.islamic.network/quran/audio/128/$currentSelectedQariId/${ayah.number}.mp3"
        audioRepository.playAudio(audioUrl, ayah.numberInSurah)
        
        // Setup completion callback for continuous play
        audioRepository.onPlaybackEnded = {
            if (_playbackMode.value == PlaybackMode.SURAH) {
                playNextAyah(ayah, surahNumber)
            }
        }
    }

    private fun playNextAyah(currentAyah: CombinedAyah, surahNumber: Int) {
        val state = _uiState.value
        if (state is UiState.Success) {
            val currentIndex = state.data.indexOfFirst { it.number == currentAyah.number }
            if (currentIndex != -1 && currentIndex + 1 < state.data.size) {
                val nextAyah = state.data[currentIndex + 1]
                playAyah(nextAyah, surahNumber)
            }
        }
    }

    fun playWord(url: String) {
        audioRepository.onPlaybackEnded = null
        audioRepository.playAudio(url, -1)
    }

    fun togglePlayPause(currentAyah: CombinedAyah?, surahNumber: Int) {
        val playingAyahNum = audioRepository.currentPlayingAyahNumber.value
        if (currentAyah != null && playingAyahNum == currentAyah.numberInSurah) {
            if (audioRepository.isPlaying.value) {
                audioRepository.pauseAudio()
            } else {
                audioRepository.resumeAudio()
            }
        } else if (currentAyah != null) {
            playAyah(currentAyah, surahNumber)
        } else {
            // If nothing playing, play first ayah from loaded list
            val state = _uiState.value
            if (state is UiState.Success && state.data.isNotEmpty()) {
                playAyah(state.data.first(), surahNumber)
            }
        }
    }

    fun playPrevious(currentAyah: CombinedAyah?, surahNumber: Int) {
        val state = _uiState.value
        if (state is UiState.Success && currentAyah != null) {
            val currentIndex = state.data.indexOfFirst { it.number == currentAyah.number }
            if (currentIndex > 0) {
                val prevAyah = state.data[currentIndex - 1]
                playAyah(prevAyah, surahNumber)
            }
        }
    }

    fun playNext(currentAyah: CombinedAyah?, surahNumber: Int) {
        val state = _uiState.value
        if (state is UiState.Success && currentAyah != null) {
            val currentIndex = state.data.indexOfFirst { it.number == currentAyah.number }
            if (currentIndex != -1 && currentIndex + 1 < state.data.size) {
                val nextAyah = state.data[currentIndex + 1]
                playAyah(nextAyah, surahNumber)
            }
        }
    }

    fun setArabicFontSize(size: Float) {
        viewModelScope.launch {
            settingsRepository.setArabicFontSize(size)
        }
    }

    fun setBengaliFontSize(size: Float) {
        viewModelScope.launch {
            settingsRepository.setBengaliFontSize(size)
        }
    }

    fun setArabicFontName(fontName: String) {
        viewModelScope.launch {
            settingsRepository.setArabicFontName(fontName)
        }
    }

    fun setTanzilTextStyle(style: String) {
        viewModelScope.launch {
            settingsRepository.setTanzilTextStyle(style)
        }
    }

    fun setArabicLineSpacing(spacing: Float) {
        viewModelScope.launch {
            settingsRepository.setArabicLineSpacing(spacing)
        }
    }

    fun toggleTranslation() {
        viewModelScope.launch {
            settingsRepository.setShowTranslation(!showTranslation.value)
        }
    }

    fun setShowWaqfSigns(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowWaqfSigns(show)
        }
    }

    fun loadSurah(surahNumber: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val combinedAyahs = repository.getSurahDetailsCombined(surahNumber, tanzilTextStyle.value)
                _uiState.value = UiState.Success(combinedAyahs)
                settingsRepository.setLastReadSurah(surahNumber)
                settingsRepository.setLastReadMode("DETAIL")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load Surah details")
            }
        }
    }

    fun loadJuz(juzNumber: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val combinedAyahs = repository.getJuzCombined(juzNumber)
                _uiState.value = UiState.Success(combinedAyahs)
                combinedAyahs.firstOrNull()?.let {
                    settingsRepository.setLastReadSurah(it.surahNumber)
                    settingsRepository.setLastReadMode("DETAIL")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load Juz details")
            }
        }
    }

    fun getTafsir(surahName: String, ayahNumber: Int, ayahText: String) {
        viewModelScope.launch {
            _tafsirState.value = UiState.Loading
            val result = aiRepository.getTafsir(surahName, ayahNumber, ayahText)
            if (result.isSuccess) {
                _tafsirState.value = UiState.Success(result.getOrNull() ?: "")
            } else {
                _tafsirState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun downloadSurahOffline(surahNumber: Int, surahName: String) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _isDownloadingOffline.value = true
            _downloadProgress.value = 0
            _downloadStatus.value = "সুরা ডাটা ও অনুবাদ ডাউনলোড হচ্ছে..."
            _downloadError.value = null
            try {
                // 1. Pre-cache text data
                val combinedAyahs = repository.getSurahDetailsCombined(surahNumber, tanzilTextStyle.value)
                val totalAyahs = combinedAyahs.size
                if (totalAyahs == 0) {
                    _downloadStatus.value = "কোনো আয়াত পাওয়া যায়নি"
                    return@launch
                }

                _downloadStatus.value = "অডিও ফাইল ডাউনলোড হচ্ছে..."
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
                                }
                            }
                        }
                        val progress = ((index + 1) * 100) / totalAyahs
                        _downloadProgress.value = progress
                    }
                }
                _downloadStatus.value = "সম্পূর্ণ সুরা অফলাইন ডাউনলোড সফল হয়েছে!"
            } catch (e: Exception) {
                _downloadError.value = e.localizedMessage ?: "ডাউনলোড ব্যর্থ হয়েছে"
                _downloadStatus.value = null
            } finally {
                _isDownloadingOffline.value = false
            }
        }
    }

    fun cancelOfflineDownload() {
        downloadJob?.cancel()
        _isDownloadingOffline.value = false
        _downloadStatus.value = "ডাউনলোড বাতিল করা হয়েছে"
    }

    override fun onCleared() {
        super.onCleared()
        downloadJob?.cancel()
        audioRepository.onPlaybackEnded = null
        audioRepository.stopAudio()
    }
}
