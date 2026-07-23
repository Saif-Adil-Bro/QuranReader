package com.example.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.model.BlogPost
import com.example.data.model.ShortPost
import com.example.utils.PostNotificationHelper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DataSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences("posts_sync_prefs", Context.MODE_PRIVATE)
    }

    override suspend fun doWork(): Result {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val lastSync = prefs.getLong("last_sync_timestamp", 0L)
            val currentMills = System.currentTimeMillis()

            val effectiveLastSync = if (lastSync == 0L) {
                currentMills - (3600 * 1000L) // Default 1 hour ago for new install
            } else {
                lastSync
            }

            var maxTimestampSeen = effectiveLastSync

            // 1. Sync new blog_posts
            val blogSnapshot = firestore.collection("blog_posts")
                .get()
                .await()

            for (doc in blogSnapshot.documents) {
                val ts = extractTimestamp(doc)
                if (ts > effectiveLastSync) {
                    val title = doc.getString("title") ?: doc.getString("name") ?: ""
                    val content = doc.getString("content") ?: doc.getString("text") ?: doc.getString("body") ?: ""
                    if (title.isNotBlank() || content.isNotBlank()) {
                        val blogPost = BlogPost(
                            id = doc.id,
                            title = title.ifBlank { "নতুন ইসলামিক পোস্ট" },
                            content = content,
                            author = doc.getString("author") ?: "ইসলামিক এডমিন",
                            category = doc.getString("category") ?: "সাধারণ",
                            imageUrl = doc.getString("imageUrl") ?: doc.getString("image") ?: "",
                            readTime = doc.getString("readTime") ?: "২ মিনিট",
                            timestamp = ts
                        )
                        PostNotificationHelper.showBlogPostNotification(appContext, blogPost)
                        if (ts > maxTimestampSeen) maxTimestampSeen = ts
                    }
                }
            }

            // 2. Sync new articles
            val articleSnapshot = firestore.collection("articles")
                .get()
                .await()

            for (doc in articleSnapshot.documents) {
                val ts = extractTimestamp(doc)
                if (ts > effectiveLastSync) {
                    val title = doc.getString("title") ?: doc.getString("name") ?: ""
                    val content = doc.getString("content") ?: doc.getString("text") ?: doc.getString("body") ?: ""
                    if (title.isNotBlank() || content.isNotBlank()) {
                        val blogPost = BlogPost(
                            id = doc.id,
                            title = title.ifBlank { "নতুন নিবন্ধ" },
                            content = content,
                            author = doc.getString("author") ?: "ইসলামিক এডমিন",
                            category = doc.getString("category") ?: "সাধারণ",
                            imageUrl = doc.getString("imageUrl") ?: doc.getString("image") ?: "",
                            readTime = doc.getString("readTime") ?: "২ মিনিট",
                            timestamp = ts
                        )
                        PostNotificationHelper.showBlogPostNotification(appContext, blogPost)
                        if (ts > maxTimestampSeen) maxTimestampSeen = ts
                    }
                }
            }

            // 3. Sync new short_posts
            val shortSnapshot = firestore.collection("short_posts")
                .get()
                .await()

            for (doc in shortSnapshot.documents) {
                val ts = extractTimestamp(doc)
                if (ts > effectiveLastSync) {
                    val text = doc.getString("text") ?: doc.getString("content") ?: doc.getString("title") ?: ""
                    if (text.isNotBlank()) {
                        val shortPost = ShortPost(
                            id = doc.id,
                            text = text,
                            reference = doc.getString("reference") ?: doc.getString("ref") ?: "",
                            category = doc.getString("category") ?: "দৈনিক নসীহত",
                            author = doc.getString("author") ?: "ইসলামিক স্কলার",
                            timestamp = ts
                        )
                        PostNotificationHelper.showPhotoCardNotification(appContext, shortPost)
                        if (ts > maxTimestampSeen) maxTimestampSeen = ts
                    }
                }
            }

            // Update last sync time
            prefs.edit().putLong("last_sync_timestamp", maxOf(maxTimestampSeen, currentMills)).apply()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun extractTimestamp(doc: DocumentSnapshot): Long {
        return try {
            val raw = doc.get("timestamp") ?: doc.get("createdAt") ?: doc.get("date")
            when (raw) {
                is Timestamp -> raw.toDate().time
                is Number -> raw.toLong()
                is String -> raw.toLongOrNull() ?: System.currentTimeMillis()
                else -> System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
