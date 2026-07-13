package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MushafRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MushafViewerViewModel(
    private val repository: MushafRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currentPagePath = MutableStateFlow<String?>(null)
    val currentPagePath: StateFlow<String?> = _currentPagePath.asStateFlow()

    private val _currentPageNumber = MutableStateFlow(1)
    val currentPageNumber: StateFlow<Int> = _currentPageNumber.asStateFlow()
    
    private var currentMushafId: String = ""

    fun initMushaf(mushafId: String, initialPage: Int) {
        currentMushafId = mushafId
        jumpToPage(initialPage)
    }

    fun nextPage() {
        if (_currentPageNumber.value < 604) {
            jumpToPage(_currentPageNumber.value + 1)
        }
    }

    fun previousPage() {
        if (_currentPageNumber.value > 1) {
            jumpToPage(_currentPageNumber.value - 1)
        }
    }

    fun jumpToPage(pageNumber: Int) {
        if (pageNumber in 1..604) {
            _currentPageNumber.value = pageNumber
            _currentPagePath.value = repository.getMushafPagePath(currentMushafId, pageNumber)
            viewModelScope.launch {
                if (currentMushafId.isNotEmpty()) {
                    settingsRepository.setLastReadMushaf(currentMushafId, pageNumber)
                }
            }
        }
    }
    
    fun getPagePath(mushafId: String, pageNumber: Int): String? {
        return repository.getMushafPagePath(mushafId, pageNumber)
    }
}
