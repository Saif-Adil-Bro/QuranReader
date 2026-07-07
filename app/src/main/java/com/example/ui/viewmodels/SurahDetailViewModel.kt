package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

enum class PlaybackMode { AYAH, SURAH }

class SurahDetailViewModel(
    private val repository: QuranRepository,
    private val settingsRepository: SettingsRepository,
    private val aiRepository: AiRepository,
    val audioRepository: AudioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<CombinedAyah>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<CombinedAyah>>> = _uiState.asStateFlow()

    private val _tafsirState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val tafsirState: StateFlow<UiState<String>> = _tafsirState.asStateFlow()

    // Observe translation toggle
    val showTranslation: StateFlow<Boolean> = settingsRepository.showTranslationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Observe Font Sizes
    val arabicFontSize: StateFlow<Float> = settingsRepository.arabicFontSizeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 24f
        )

    val bengaliFontSize: StateFlow<Float> = settingsRepository.bengaliFontSizeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 16f
        )

    val arabicFontName: StateFlow<String> = settingsRepository.arabicFontNameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Amiri Quran"
        )

    // Playback state and Mode
    val isPlaying: StateFlow<Boolean> = audioRepository.isPlaying
    val currentPlayingAyahNumber: StateFlow<Int?> = audioRepository.currentPlayingAyahNumber

    private val _playbackMode = MutableStateFlow(PlaybackMode.SURAH)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    fun setPlaybackMode(mode: PlaybackMode) {
        _playbackMode.value = mode
    }

    fun playAyah(ayah: CombinedAyah, surahNumber: Int) {
        val audioUrl = ayah.audioUrl ?: "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${ayah.number}.mp3"
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

    fun toggleTranslation() {
        viewModelScope.launch {
            settingsRepository.setShowTranslation(!showTranslation.value)
        }
    }

    fun loadSurah(surahNumber: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val combinedAyahs = repository.getSurahDetailsCombined(surahNumber)
                _uiState.value = UiState.Success(combinedAyahs)
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

    override fun onCleared() {
        super.onCleared()
        audioRepository.onPlaybackEnded = null
        audioRepository.stopAudio()
    }
}
