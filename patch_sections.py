import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    'fun QuickAccessSection(\n    selectedTab: Int,\n    lastReadSurah: Int,\n    lastReadPage: Int,\n    lastReadMode: String,\n    lastReadMushafId: String?,\n    lastReadMushafPage: Int,\n    onTabSelected: (Int) -> Unit,',
    'fun QuickAccessSection(\n    selectedTab: Int,\n    lastReadSurah: Int,\n    lastReadPage: Int,\n    lastReadMode: String,\n    lastReadMushafId: String?,\n    lastReadMushafPage: Int,\n    defaultMushafId: String,\n    onTabSelected: (Int) -> Unit,'
)

content = content.replace(
    'QuickAccessSection(\n                        selectedTab = selectedTab,\n                        lastReadSurah = lastReadSurah,\n                        lastReadPage = lastReadPage,\n                        lastReadMode = lastReadMode,\n                        lastReadMushafId = lastReadMushafId,\n                        lastReadMushafPage = lastReadMushafPage,\n                        onTabSelected =',
    'QuickAccessSection(\n                        selectedTab = selectedTab,\n                        lastReadSurah = lastReadSurah,\n                        lastReadPage = lastReadPage,\n                        lastReadMode = lastReadMode,\n                        lastReadMushafId = lastReadMushafId,\n                        lastReadMushafPage = lastReadMushafPage,\n                        defaultMushafId = defaultMushafId,\n                        onTabSelected ='
)

content = content.replace(
    'fun BookmarksAndLastReadSection(\n    lastReadSurah: Int,\n    lastReadPage: Int,\n    lastReadMode: String,\n    lastReadMushafId: String?,\n    lastReadMushafPage: Int,\n    lastReadMushafName: String,\n    bookmarks: List<com.example.data.local.entity.BookmarkEntity>,',
    'fun BookmarksAndLastReadSection(\n    lastReadSurah: Int,\n    lastReadPage: Int,\n    lastReadMode: String,\n    lastReadMushafId: String?,\n    lastReadMushafPage: Int,\n    lastReadMushafName: String,\n    defaultMushafId: String,\n    bookmarks: List<com.example.data.local.entity.BookmarkEntity>,'
)

content = content.replace(
    'BookmarksAndLastReadSection(\n                        lastReadSurah = lastReadSurah,\n                        lastReadPage = lastReadPage,\n                        lastReadMode = lastReadMode,\n                        lastReadMushafId = lastReadMushafId,\n                        lastReadMushafPage = lastReadMushafPage,\n                        lastReadMushafName = viewModel.getMushafStyle(lastReadMushafId?.takeIf { it.isNotEmpty() } ?: defaultMushafId)?.nameBengali ?: (lastReadMushafId?.takeIf { it.isNotEmpty() } ?: defaultMushafId),\n                        bookmarks = bookmarks,',
    'BookmarksAndLastReadSection(\n                        lastReadSurah = lastReadSurah,\n                        lastReadPage = lastReadPage,\n                        lastReadMode = lastReadMode,\n                        lastReadMushafId = lastReadMushafId,\n                        lastReadMushafPage = lastReadMushafPage,\n                        lastReadMushafName = viewModel.getMushafStyle(lastReadMushafId?.takeIf { it.isNotEmpty() } ?: defaultMushafId)?.nameBengali ?: (lastReadMushafId?.takeIf { it.isNotEmpty() } ?: defaultMushafId),\n                        defaultMushafId = defaultMushafId,\n                        bookmarks = bookmarks,'
)


with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
