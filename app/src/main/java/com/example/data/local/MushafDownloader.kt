package com.example.data.local

import android.content.Context
import com.example.data.model.DownloadState
import com.example.data.model.DownloadStatus
import com.example.util.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.isActive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class MushafDownloader(
    private val context: Context,
    private val storageManager: StorageManager
) {
    private val client = OkHttpClient()
    private val activeDownloads = mutableMapOf<String, Job>()
    private val pausedDownloads = mutableSetOf<String>()
    
    suspend fun downloadMushaf(
        mushafId: String,
        baseUrl: String,
        totalPages: Int,
        onProgress: (DownloadStatus) -> Unit,
        scope: CoroutineScope
    ) {
        val job = scope.launch(Dispatchers.IO) {
            pausedDownloads.remove(mushafId)
            val semaphore = Semaphore(3) // Max 3 concurrent downloads
            
            var downloadedCount = storageManager.getDownloadedPagesCount(mushafId)
            
            if (downloadedCount == totalPages) {
                onProgress(DownloadStatus(mushafId, DownloadState.Downloaded, 100, downloadedCount, totalPages))
                return@launch
            }
            
            onProgress(DownloadStatus(mushafId, DownloadState.Downloading, (downloadedCount * 100) / totalPages, downloadedCount, totalPages))
            
            val pagesToDownload = (1..totalPages).filter { !storageManager.isPageDownloaded(mushafId, it) }
            
            val downloadChannel = Channel<Boolean>(Channel.UNLIMITED)
            
            for (page in pagesToDownload) {
                if (!isActive || pausedDownloads.contains(mushafId)) {
                    break
                }
                
                launch {
                    semaphore.withPermit {
                        if (!isActive || pausedDownloads.contains(mushafId)) return@withPermit
                        val success = downloadPage(mushafId, baseUrl, page)
                        downloadChannel.send(success)
                    }
                }
            }
            
            var allSuccessful = true
            var processed = 0
            
            while (processed < pagesToDownload.size && isActive && !pausedDownloads.contains(mushafId)) {
                val success = downloadChannel.receive()
                if (success) {
                    downloadedCount++
                    val progress = (downloadedCount * 100) / totalPages
                    onProgress(DownloadStatus(mushafId, DownloadState.Downloading, progress, downloadedCount, totalPages))
                } else {
                    allSuccessful = false
                }
                processed++
            }
            
            if (pausedDownloads.contains(mushafId)) {
                // Was paused
            } else if (downloadedCount == totalPages) {
                onProgress(DownloadStatus(mushafId, DownloadState.Downloaded, 100, downloadedCount, totalPages))
            } else {
                onProgress(DownloadStatus(mushafId, DownloadState.Failed, (downloadedCount * 100) / totalPages, downloadedCount, totalPages))
            }
        }
        activeDownloads[mushafId] = job
    }
    
    private suspend fun downloadPage(mushafId: String, baseUrl: String, page: Int): Boolean = withContext(Dispatchers.IO) {
        var attempts = 0
        while (attempts < 3) {
            try {
                val page3Str = String.format("%03d", page)
                val url = baseUrl.replace("{page}", page.toString()).replace("{page3}", page3Str)
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val body = response.body
                    if (body != null) {
                        val file = storageManager.getPageFile(mushafId, page)
                        val inputStream: InputStream = body.byteStream()
                        val outputStream = FileOutputStream(file)
                        
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        
                        outputStream.flush()
                        outputStream.close()
                        inputStream.close()
                        return@withContext true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            attempts++
            delay(1000)
        }
        return@withContext false
    }
    
    fun pauseDownload(mushafId: String) {
        pausedDownloads.add(mushafId)
        activeDownloads[mushafId]?.cancel()
        activeDownloads.remove(mushafId)
    }
    
    fun cancelDownload(mushafId: String) {
        pausedDownloads.remove(mushafId)
        activeDownloads[mushafId]?.cancel()
        activeDownloads.remove(mushafId)
        storageManager.deleteMushaf(mushafId)
    }
}
