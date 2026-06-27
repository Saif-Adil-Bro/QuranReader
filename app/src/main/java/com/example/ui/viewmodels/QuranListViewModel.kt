package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Surah
import com.example.data.repository.QuranRepository
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuranListViewModel(
    private val repository: QuranRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Surah>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Surah>>> = _uiState.asStateFlow()

    private var allSurahs: List<Surah> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadSurahs()
    }

    fun loadSurahs() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val surahs = repository.getSurahs()
                allSurahs = surahs
                _uiState.value = UiState.Success(surahs)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterSurahs(query)
    }

    private fun filterSurahs(query: String) {
        if (query.isBlank()) {
            _uiState.value = UiState.Success(allSurahs)
        } else {
            val filteredList = allSurahs.filter {
                it.englishName.contains(query, ignoreCase = true) ||
                it.name.contains(query) ||
                it.englishNameTranslation.contains(query, ignoreCase = true)
            }
            _uiState.value = UiState.Success(filteredList)
        }
    }
}
