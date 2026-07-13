package com.example.data.repository

import com.example.data.model.DownloadStatus
import com.example.data.model.MushafStyle
import com.example.data.local.MushafDownloader
import com.example.util.StorageManager
import kotlinx.coroutines.CoroutineScope

class MushafRepository(
    private val downloader: MushafDownloader,
    private val storageManager: StorageManager
) {

    fun getAvailableMushafs(): List<MushafStyle> {
        return listOf(
            MushafStyle(
                id = "madani",
                name = "Mushaf Al-Madani",
                nameBengali = "মুসহাফ আল-মাদানী",
                description = "Standard Uthmani script used worldwide",
                descriptionBengali = "সারা বিশ্বে সর্বাধিক ব্যবহৃত আদর্শ উসমানী লিপি",
                fileSizeMB = 250,
                thumbnailUrl = "https://cdn.islamic.network/quran/images/1_1.png",
                baseUrl = "https://android.quran.com/data/width_1024/page{page3}.png"
            ),
            MushafStyle(
                id = "makkah",
                name = "Mushaf Al-Makkah",
                nameBengali = "মুসহাফ মক্কা",
                description = "Clear and large font from Makkah",
                descriptionBengali = "মক্কা থেকে প্রকাশিত স্পষ্ট ও বড় ফন্ট",
                fileSizeMB = 320,
                thumbnailUrl = "https://cdn.islamic.network/quran/images/2_1.png",
                baseUrl = "https://android.quran.com/data/width_1024/page{page3}.png"
            ),
            MushafStyle(
                id = "indopak",
                name = "Indo-Pak Script",
                nameBengali = "ইন্দো-পাক লিপি",
                description = "Popular Nastaliq style in India/Pakistan",
                descriptionBengali = "ভারত-পাকিস্তানে জনপ্রিয় নস্তালিক স্টাইল",
                fileSizeMB = 280,
                thumbnailUrl = "https://cdn.islamic.network/quran/images/3_1.png",
                baseUrl = "https://android.quran.com/data/width_1024/page{page3}.png"
            ),
            MushafStyle(
                id = "simplified",
                name = "Simplified Mushaf",
                nameBengali = "সহজ মুসহাফ",
                description = "Easy to read for beginners",
                descriptionBengali = "শিক্ষানবিসদের জন্য সহজ পাঠ্য",
                fileSizeMB = 180,
                thumbnailUrl = "https://cdn.islamic.network/quran/images/4_1.png",
                baseUrl = "https://android.quran.com/data/width_1024/page{page3}.png"
            ),
            MushafStyle(
                id = "hafs_tajweed",
                name = "Hafs Tajweed",
                nameBengali = "হাফস তাজবীদ",
                description = "Color-coded Tajweed Mushaf from easyquran.com",
                descriptionBengali = "ইজি-কুরআন ডট কম থেকে রঙিন তাজবীদ মুসহাফ",
                fileSizeMB = 180,
                thumbnailUrl = "https://raw.githubusercontent.com/QuranHub/quran-pages-images/main/easyquran.com/hafs-tajweed/1.jpg",
                baseUrl = "https://raw.githubusercontent.com/QuranHub/quran-pages-images/main/easyquran.com/hafs-tajweed/{page}.jpg"
            )
        )
    }

    suspend fun downloadMushaf(
        mushaf: MushafStyle,
        scope: CoroutineScope,
        onProgress: (DownloadStatus) -> Unit
    ) {
        downloader.downloadMushaf(
            mushaf.id,
            mushaf.baseUrl,
            mushaf.totalPages,
            onProgress,
            scope
        )
    }

    fun pauseDownload(mushafId: String) {
        downloader.pauseDownload(mushafId)
    }

    fun cancelDownload(mushafId: String) {
        downloader.cancelDownload(mushafId)
    }

    fun deleteMushaf(mushafId: String): Boolean {
        return storageManager.deleteMushaf(mushafId)
    }

    fun isMushafDownloaded(mushafId: String): Boolean {
        val count = storageManager.getDownloadedPagesCount(mushafId)
        // Assume at least 1 page downloaded means it exists. Better check all pages or downloaded count.
        return count >= 604
    }

    fun getDownloadedPagesCount(mushafId: String): Int {
        return storageManager.getDownloadedPagesCount(mushafId)
    }

    fun getMushafPagePath(mushafId: String, pageNumber: Int): String? {
        val file = storageManager.getPageFile(mushafId, pageNumber)
        return if (file.exists()) file.absolutePath else null
    }
}
