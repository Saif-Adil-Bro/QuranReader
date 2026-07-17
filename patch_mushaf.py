with open("app/src/main/java/com/example/ui/viewmodels/MushafViewerViewModel.kt", "r") as f:
    content = f.read()

target = """    fun jumpToPage(pageNumber: Int) {
        _currentPageNumber.value = pageNumber
        viewModelScope.launch(Dispatchers.IO) {
            val path = repository.getMushafPagePath(currentMushafId, pageNumber, _pdfPageOffset.value)
            _currentPagePath.value = path
        }
    }"""

replacement = """    fun jumpToPage(pageNumber: Int) {
        _currentPageNumber.value = pageNumber
        viewModelScope.launch(Dispatchers.IO) {
            val path = repository.getMushafPagePath(currentMushafId, pageNumber, _pdfPageOffset.value)
            _currentPagePath.value = path
            
            // Save last read state
            settingsRepository.setLastReadMushaf(currentMushafId, pageNumber)
            settingsRepository.setLastReadMode("MUSHAF")
        }
    }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/viewmodels/MushafViewerViewModel.kt", "w") as f:
    f.write(content)
