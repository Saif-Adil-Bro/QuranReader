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
