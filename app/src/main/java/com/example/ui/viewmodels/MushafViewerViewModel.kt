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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    var currentMushafId = ""

    fun initMushaf(mushafId: String, initialPage: Int = 1) {
        currentMushafId = mushafId
        val style = repository.getAvailableMushafs().find { it.id == mushafId }
        _isPdf.value = style?.isPdf == true
        _totalPages.value = style?.totalPages ?: 604

        viewModelScope.launch {
            val customOffset = settingsRepository.getMushafOffset(mushafId).first()
            _pdfPageOffset.value = customOffset ?: style?.pdfPageOffset ?: 0
            jumpToPage(initialPage)
            _isReady.value = true
        }
    }

    fun jumpToPage(pageNumber: Int) {
        _currentPageNumber.value = pageNumber
        viewModelScope.launch(Dispatchers.IO) {
            val path = repository.getMushafPagePath(currentMushafId, pageNumber, _pdfPageOffset.value)
            _currentPagePath.value = path
            
            // Save last read state
            settingsRepository.setLastReadMushaf(currentMushafId, pageNumber)
            settingsRepository.setLastReadMode("MUSHAF")
        }
    }

    suspend fun getPagePath(mushafId: String, pageNumber: Int): String? = withContext(Dispatchers.IO) {
        repository.getMushafPagePath(mushafId, pageNumber, _pdfPageOffset.value)
    }

    suspend fun downloadPageOnDemand(mushafId: String, pageNumber: Int): Boolean {
        return repository.downloadSinglePage(mushafId, pageNumber)
    }

    fun adjustOffset(increment: Int) {
        val newOffset = _pdfPageOffset.value + increment
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.clearRenderedPages(currentMushafId)
            }
            _pdfPageOffset.value = newOffset
            settingsRepository.setMushafOffset(currentMushafId, newOffset)
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

    fun prefetchPages(mushafId: String, currentPage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            for (i in 1..2) {
                if (currentPage + i <= _totalPages.value) {
                    repository.getMushafPagePath(mushafId, currentPage + i, _pdfPageOffset.value)
                }
                if (currentPage - i > 0) {
                    repository.getMushafPagePath(mushafId, currentPage - i, _pdfPageOffset.value)
                }
            }
        }
    }
}
