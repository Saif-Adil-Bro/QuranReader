package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.data.repository.MushafRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MushafViewerViewModel(
    private val repository: MushafRepository
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
        }
    }
    
    fun getPagePath(pageNumber: Int): String? {
        return repository.getMushafPagePath(currentMushafId, pageNumber)
    }
}
