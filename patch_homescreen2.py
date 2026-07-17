with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# For actionTextForHero
content = content.replace(
    '                            "MUSHAF" -> "সর্বশেষ পঠিত পৃষ্ঠা: ${com.example.utils.DateUtil.toBengaliNumerals(lastReadMushafPage)} (সূরা $lastReadSurahNameForHero)"',
    '                            "MUSHAF" -> "সর্বশেষ পঠিত পৃষ্ঠা: ${com.example.utils.DateUtil.toBengaliNumerals(lastReadMushafPage)}"'
)

# For subtitleText
content = content.replace(
    '                        "MUSHAF" -> "মুসহাফ: ${lastReadMushafPage.toBengaliNumerals()} • সূরা: $lastReadSurahName"',
    '                        "MUSHAF" -> "মুসহাফ: ${lastReadMushafPage.toBengaliNumerals()}"'
)

# For cardSubtitleText
content = content.replace(
    '                        "MUSHAF" -> "পৃষ্ঠা: ${lastReadMushafPage.toBengaliNumerals()} • সূরা: $lastReadSurahName"',
    '                        "MUSHAF" -> "পৃষ্ঠা: ${lastReadMushafPage.toBengaliNumerals()}"'
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
