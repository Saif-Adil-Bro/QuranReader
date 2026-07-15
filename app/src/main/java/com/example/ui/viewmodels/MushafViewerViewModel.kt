package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MushafRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MushafViewerViewModel(
    private val repository: MushafRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val theme: StateFlow<String> = settingsRepository.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = "Light"
    )

    val scrollDirection: StateFlow<String> = settingsRepository.mushafScrollDirectionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = "Horizontal"
    )

    private val _currentPagePath = MutableStateFlow<String?>(null)
    val currentPagePath: StateFlow<String?> = _currentPagePath.asStateFlow()

    private val _currentPageNumber = MutableStateFlow(1)
    val currentPageNumber: StateFlow<Int> = _currentPageNumber.asStateFlow()
    
    private val _pdfPageOffset = MutableStateFlow(0)
    val pdfPageOffset: StateFlow<Int> = _pdfPageOffset.asStateFlow()
    
    private val _isPdf = MutableStateFlow(false)
    val isPdf: StateFlow<Boolean> = _isPdf.asStateFlow()

    private val _totalPages = MutableStateFlow(604)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private var currentMushafId: String = ""

    fun initMushaf(mushafId: String, initialPage: Int) {
        currentMushafId = mushafId
        val defaultStyle = repository.getAvailableMushafs().find { it.id == mushafId }
        _isPdf.value = defaultStyle?.isPdf == true
        _totalPages.value = defaultStyle?.totalPages ?: 604
        val defaultOffset = defaultStyle?.pdfPageOffset ?: 0
        _currentPageNumber.value = initialPage
        
        viewModelScope.launch {
            val savedOffset = settingsRepository.getMushafOffset(mushafId).first()
            val activeOffset = if (savedOffset != -1) savedOffset else defaultOffset
            _pdfPageOffset.value = activeOffset
            jumpToPage(initialPage)
        }
    }

    fun nextPage() {
        if (_currentPageNumber.value < _totalPages.value) {
            jumpToPage(_currentPageNumber.value + 1)
        }
    }

    fun previousPage() {
        if (_currentPageNumber.value > 1) {
            jumpToPage(_currentPageNumber.value - 1)
        }
    }

    fun jumpToPage(pageNumber: Int) {
        if (pageNumber in 1.._totalPages.value) {
            _currentPageNumber.value = pageNumber
            _currentPagePath.value = repository.getMushafPagePath(currentMushafId, pageNumber, _pdfPageOffset.value)
            viewModelScope.launch {
                if (currentMushafId.isNotEmpty()) {
                    settingsRepository.setLastReadMushaf(currentMushafId, pageNumber)
                    settingsRepository.setLastReadMode("MUSHAF")
                }
            }
        }
    }
    
    fun getPagePath(mushafId: String, pageNumber: Int): String? {
        return repository.getMushafPagePath(mushafId, pageNumber, _pdfPageOffset.value)
    }

    suspend fun downloadPageOnDemand(mushafId: String, pageNumber: Int): Boolean {
        return repository.downloadSinglePage(mushafId, pageNumber)
    }

    fun adjustOffset(increment: Int) {
        val newOffset = _pdfPageOffset.value + increment
        _pdfPageOffset.value = newOffset
        viewModelScope.launch {
            settingsRepository.setMushafOffset(currentMushafId, newOffset)
            repository.clearRenderedPages(currentMushafId)
            jumpToPage(_currentPageNumber.value)
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = settingsRepository.themeFlow.first()
            val next = if (current == "Dark") "Light" else "Dark"
            settingsRepository.setTheme(next)
        }
    }

    fun setScrollDirection(direction: String) {
        viewModelScope.launch {
            settingsRepository.setMushafScrollDirection(direction)
        }
    }
}
