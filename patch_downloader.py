import re

with open("app/src/main/java/com/example/data/local/MushafDownloader.kt", "r") as f:
    content = f.read()

content = content.replace("downloadPdfFile(mushafId, baseUrl, onProgress)", "downloadPdfFile(mushafId, baseUrl, totalPages, onProgress)")

old_sig = """    private suspend fun downloadPdfFile(
        mushafId: String,
        url: String,
        onProgress: (DownloadStatus) -> Unit
    ) = withContext(Dispatchers.IO) {"""
new_sig = """    private suspend fun downloadPdfFile(
        mushafId: String,
        url: String,
        totalPages: Int,
        onProgress: (DownloadStatus) -> Unit
    ) = withContext(Dispatchers.IO) {"""
content = content.replace(old_sig, new_sig)

# Replace 604 with totalPages inside downloadPdfFile. Wait, there are multiple 604s.
# 1. 100, 604, 604
content = content.replace("100, 604, 604))", "100, totalPages, totalPages))")
# 2. 0, 0, 604
content = content.replace("0, 0, 604))", "0, 0, totalPages))")
# 3. fakePageCount = (percent * 604) / 100
content = content.replace("val fakePageCount = (percent * 604) / 100", "val fakePageCount = (percent * totalPages) / 100")
# 4. percent, fakePageCount, 604
content = content.replace("percent, fakePageCount, 604))", "percent, fakePageCount, totalPages))")

with open("app/src/main/java/com/example/data/local/MushafDownloader.kt", "w") as f:
    f.write(content)
