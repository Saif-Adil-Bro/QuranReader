import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace ?: with takeIf { it.isNotEmpty() } ?: 
content = content.replace(
    'viewModel.getMushafStyle(lastReadMushafId ?: defaultMushafId)?.nameBengali ?: (lastReadMushafId ?: defaultMushafId)',
    'viewModel.getMushafStyle(lastReadMushafId?.takeIf { it.isNotEmpty() } ?: defaultMushafId)?.nameBengali ?: (lastReadMushafId?.takeIf { it.isNotEmpty() } ?: defaultMushafId)'
)

content = content.replace(
    'onNavigateToMushafPage(lastReadMushafId ?: defaultMushafId, lastReadMushafPage, false)',
    'onNavigateToMushafPage(lastReadMushafId?.takeIf { it.isNotEmpty() } ?: defaultMushafId, lastReadMushafPage, false)'
)

content = content.replace(
    'if (lastReadMushafId != null) {\n                                onNavigateToMushafPage(lastReadMushafId, lastReadMushafPage, false)\n                            } else {\n                                onNavigateToHafeziMode(lastReadPage)\n                            }',
    'if (!lastReadMushafId.isNullOrEmpty()) {\n                                onNavigateToMushafPage(lastReadMushafId, lastReadMushafPage, false)\n                            } else {\n                                onNavigateToHafeziMode(lastReadPage)\n                            }'
)

content = content.replace(
    'if (lastReadMushafId != null) {\n                                    onNavigateToMushafPage(lastReadMushafId, lastReadMushafPage, false)\n                                } else {\n                                    onNavigateToHafeziMode(lastReadPage) // Fallback\n                                }',
    'if (!lastReadMushafId.isNullOrEmpty()) {\n                                    onNavigateToMushafPage(lastReadMushafId, lastReadMushafPage, false)\n                                } else {\n                                    onNavigateToHafeziMode(lastReadPage) // Fallback\n                                }'
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
