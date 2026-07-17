import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace the fallback block for "MUSHAF"
content = content.replace(
    '''                            "MUSHAF" -> {
                                if (!lastReadMushafId.isNullOrEmpty()) {
                                    onNavigateToMushafPage(lastReadMushafId, lastReadMushafPage, false)
                                } else {
                                    onNavigateToHafeziMode(lastReadPage)
                                }
                            }''',
    '''                            "MUSHAF" -> {
                                val targetMushafId = lastReadMushafId?.takeIf { it.isNotEmpty() } ?: defaultMushafId
                                onNavigateToMushafPage(targetMushafId, lastReadMushafPage, false)
                            }'''
)

content = content.replace(
    '''                            "MUSHAF" -> {
                                if (!lastReadMushafId.isNullOrEmpty()) {
                                    onNavigateToMushafPage(lastReadMushafId, lastReadMushafPage, false)
                                } else {
                                    onNavigateToHafeziMode(lastReadPage) // Fallback
                                }
                            }''',
    '''                            "MUSHAF" -> {
                                val targetMushafId = lastReadMushafId?.takeIf { it.isNotEmpty() } ?: defaultMushafId
                                onNavigateToMushafPage(targetMushafId, lastReadMushafPage, false)
                            }'''
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
