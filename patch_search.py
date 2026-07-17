with open("app/src/main/java/com/example/ui/screens/SearchScreen.kt", "r") as f:
    content = f.read()

target_signature = """fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSurah: (Int) -> Unit
) {"""

replacement_signature = """fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSurah: (Int) -> Unit,
    onNavigateToAyah: (Int, Int) -> Unit = { _, _ -> }
) {"""

content = content.replace(target_signature, replacement_signature)

target_click = """                                            is SearchResultItemType.AyahItem -> {
                                                SearchResultItem(
                                                    match = item.match, 
                                                    searchQuery = searchQuery,
                                                    isDark = isDark,
                                                    arabicText = item.arabicText,
                                                    banglaText = item.banglaText,
                                                    arabicFontName = arabicFont,
                                                    onClick = {
                                                        onNavigateToSurah(item.match.surah.number)
                                                    }
                                                )
                                            }"""

replacement_click = """                                            is SearchResultItemType.AyahItem -> {
                                                SearchResultItem(
                                                    match = item.match, 
                                                    searchQuery = searchQuery,
                                                    isDark = isDark,
                                                    arabicText = item.arabicText,
                                                    banglaText = item.banglaText,
                                                    arabicFontName = arabicFont,
                                                    onClick = {
                                                        onNavigateToAyah(item.match.surah.number, item.match.numberInSurah)
                                                    }
                                                )
                                            }"""

content = content.replace(target_click, replacement_click)

with open("app/src/main/java/com/example/ui/screens/SearchScreen.kt", "w") as f:
    f.write(content)
