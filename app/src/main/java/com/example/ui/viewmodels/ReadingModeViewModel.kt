package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CombinedAyah
import com.example.domain.usecase.GetSurahDetailsUseCase
import com.example.data.repository.SettingsRepository
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReadingModeViewModel(
    private val getSurahDetailsUseCase: GetSurahDetailsUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<CombinedAyah>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<CombinedAyah>>> = _uiState.asStateFlow()

    val arabicFontSize: StateFlow<Float> = settingsRepository.arabicFontSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 24f)

    val bengaliFontSize: StateFlow<Float> = settingsRepository.bengaliFontSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16f)

    val tanzilTextStyle: StateFlow<String> = settingsRepository.tanzilTextStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "quran-uthmani")

    val theme: StateFlow<String> = settingsRepository.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Light")

    val autoScrollSpeed: StateFlow<Float> = settingsRepository.autoScrollSpeedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1f)

    fun loadSurah(surahNumber: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val style = settingsRepository.tanzilTextStyleFlow.first()
                val ayahs = getSurahDetailsUseCase(surahNumber, style)
                _uiState.value = UiState.Success(ayahs)
                settingsRepository.setLastReadSurah(surahNumber)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load Surah details")
            }
        }
    }

    fun setTanzilTextStyle(style: String) {
        viewModelScope.launch { settingsRepository.setTanzilTextStyle(style) }
    }

    fun setArabicFontSize(size: Float) {
        viewModelScope.launch { settingsRepository.setArabicFontSize(size) }
    }

    fun setBengaliFontSize(size: Float) {
        viewModelScope.launch { settingsRepository.setBengaliFontSize(size) }
    }

    fun setTheme(newTheme: String) {
        viewModelScope.launch { settingsRepository.setTheme(newTheme) }
    }

    fun setAutoScrollSpeed(speed: Float) {
        viewModelScope.launch { settingsRepository.setAutoScrollSpeed(speed) }
    }
}
