with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# For actionTextForHero
content = content.replace(
    '                            "HAFEZI" -> "সর্বশেষ পঠিত পৃষ্ঠা: ${com.example.utils.DateUtil.toBengaliNumerals(lastReadPage)}"\n                            "TAJWEED" -> "সর্বশেষ পঠিত পৃষ্ঠা: ${com.example.utils.DateUtil.toBengaliNumerals(lastReadPage)}"\n                            "MUSHAF" -> "সর্বশেষ পঠিত পৃষ্ঠা: ${com.example.utils.DateUtil.toBengaliNumerals(lastReadMushafPage)}"',
    '                            "HAFEZI" -> "সর্বশেষ পঠিত পৃষ্ঠা: ${com.example.utils.DateUtil.toBengaliNumerals(lastReadPage)}"\n                            "TAJWEED" -> "সর্বশেষ পঠিত পৃষ্ঠা: ${com.example.utils.DateUtil.toBengaliNumerals(lastReadPage)} (সূরা $lastReadSurahNameForHero)"\n                            "MUSHAF" -> "সর্বশেষ পঠিত পৃষ্ঠা: ${com.example.utils.DateUtil.toBengaliNumerals(lastReadMushafPage)} (সূরা $lastReadSurahNameForHero)"'
)

# For subtitleText around line 940
content = content.replace(
    '                        "HAFEZI" -> "হাফেজী: ${lastReadPage.toBengaliNumerals()}"\n                        "TAJWEED" -> "তাজবীদ: ${lastReadPage.toBengaliNumerals()}"\n                        "MUSHAF" -> "মুসহাফ: ${lastReadMushafPage.toBengaliNumerals()}"',
    '                        "HAFEZI" -> "হাফেজী: ${lastReadPage.toBengaliNumerals()}"\n                        "TAJWEED" -> "তাজবীদ: ${lastReadPage.toBengaliNumerals()} • সূরা: $lastReadSurahName"\n                        "MUSHAF" -> "মুসহাফ: ${lastReadMushafPage.toBengaliNumerals()} • সূরা: $lastReadSurahName"'
)

# For cardSubtitleText
content = content.replace(
    '                        "HAFEZI" -> "পৃষ্ঠা: ${lastReadPage.toBengaliNumerals()}"\n                        "TAJWEED" -> "পৃষ্ঠা: ${lastReadPage.toBengaliNumerals()}"\n                        "MUSHAF" -> "পৃষ্ঠা: ${lastReadMushafPage.toBengaliNumerals()}"',
    '                        "HAFEZI" -> "পৃষ্ঠা: ${lastReadPage.toBengaliNumerals()}"\n                        "TAJWEED" -> "পৃষ্ঠা: ${lastReadPage.toBengaliNumerals()} • সূরা: $lastReadSurahName"\n                        "MUSHAF" -> "পৃষ্ঠা: ${lastReadMushafPage.toBengaliNumerals()} • সূরা: $lastReadSurahName"'
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
