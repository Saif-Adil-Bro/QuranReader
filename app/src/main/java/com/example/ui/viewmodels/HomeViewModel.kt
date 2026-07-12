package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.QuranRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.MushafRepository
import com.example.data.model.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val quranRepository: QuranRepository,
    private val mushafRepository: MushafRepository
) : ViewModel() {

    val lastReadSurah: StateFlow<Int> = settingsRepository.lastReadSurahFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1
        )

    val lastReadPage: StateFlow<Int> = settingsRepository.lastReadPageFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1
        )

    val theme: StateFlow<String> = settingsRepository.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Light"
        )

    val hasAskedDownloadPrompt: StateFlow<Boolean> = settingsRepository.hasAskedDownloadPromptFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError

    fun setHasAskedDownloadPrompt() {
        viewModelScope.launch {
            settingsRepository.setHasAskedDownloadPrompt(true)
        }
    }

    fun downloadAllQuranData() {
        viewModelScope.launch {
            _isDownloading.value = true
            _downloadProgress.value = 0
            _downloadError.value = null
            try {
                // First download Surah list
                quranRepository.getSurahs()
                
                // Then download each of the 114 Surahs
                for (i in 1..114) {
                    if (!quranRepository.isSurahDownloaded(i)) {
                        try {
                            quranRepository.getSurahDetailsCombined(i)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    _downloadProgress.value = i
                }
            } catch (e: Exception) {
                _downloadError.value = e.localizedMessage ?: "ডাউনলোড ব্যর্থ হয়েছে"
            } finally {
                _isDownloading.value = false
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val nextTheme = if (theme.value == "Dark") "Light" else "Dark"
            settingsRepository.setTheme(nextTheme)
        }
    }

    private val _surahs = MutableStateFlow<List<Surah>>(emptyList())
    val surahs: StateFlow<List<Surah>> = _surahs

    init {
        loadSurahs()
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            try {
                _surahs.value = quranRepository.getSurahs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isMushafDownloaded(mushafId: String): Boolean {
        return mushafRepository.isMushafDownloaded(mushafId)
    }
}
