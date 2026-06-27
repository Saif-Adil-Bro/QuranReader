package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.SearchMatch
import com.example.data.repository.QuranRepository
import com.example.ui.state.UiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: QuranRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<List<SearchMatch>>>(UiState.Success(emptyList()))
    val uiState: StateFlow<UiState<List<SearchMatch>>> = _uiState.asStateFlow()

    init {
        setupSearch()
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _uiState.value = UiState.Success(emptyList())
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = repository.searchQuran(query)
                _uiState.value = UiState.Success(response.matches)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Search failed")
            }
        }
    }
}
