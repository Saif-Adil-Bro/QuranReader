package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CombinedAyah
import com.example.data.repository.QuranRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.AiRepository
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SurahDetailViewModel(
    private val repository: QuranRepository,
    settingsRepository: SettingsRepository,
    private val aiRepository: AiRepository
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
}
