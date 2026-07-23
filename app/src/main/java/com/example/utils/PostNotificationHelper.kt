package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.BlogPost
import com.example.data.model.ShortPost
import com.example.receiver.PostNotificationShareReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PostNotificationHelper {

    private const val CHANNEL_ID = "islamic_posts_channel"
    private const val CHANNEL_NAME = "ইসলামিক পোস্ট ও নসীহত"
    private const val CHANNEL_DESC = "নতুন ইসলামিক আপডেট, ব্লগ ও ফটো কার্ডের নোটিফিকেশন"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPhotoCardNotification(context: Context, post: ShortPost) {
        createNotificationChannel(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cardBitmap = PostShareUtil.generateCardBitmap(
                    context = context,
                    post = post,
                    theme = PostShareUtil.CardTheme.EMERALD,
                    bgImageUrl = null,
                    overlayAlpha = 0.65f,
                    textAlignName = "CENTER",
                    fontName = "SolaimanLipi",
                    fontSizeSp = 22f,
                    customCategory = post.category,
                    customText = post.text,
                    customRef = post.reference,
                    showLogo = true,
                    showWatermark = true
                )

                val reqId = if (post.id.isNotBlank()) post.id.hashCode() else (post.category + post.text).hashCode()

                // Intent for Opening Customizer (used by Notification Click, Share, and Edit)
                val openCustomizerIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("navigate_to", "posts")
                    putExtra("open_photo_card_edit", true)
                    putExtra("post_id", post.id)
                    putExtra("post_text", post.text)
                    putExtra("post_ref", post.reference)
                    putExtra("post_category", post.category)
                    putExtra("post_author", post.author)
                }

                val openCustomizerPendingIntent = PendingIntent.getActivity(
                    context,
                    reqId,
                    openCustomizerIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val titleText = if (post.category.isNotBlank()) "নতুন ফটো কার্ড: ${post.category}" else "নতুন ইসলামিক ফটো কার্ড"

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(titleText)
                    .setContentText(post.text)
                    .setLargeIcon(cardBitmap)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(cardBitmap)
                            .bigLargeIcon(null as Bitmap?)
                            .setBigContentTitle(titleText)
                            .setSummaryText(post.text)
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(openCustomizerPendingIntent)
                    .addAction(
                        android.R.drawable.ic_menu_share,
                        "Share",
                        openCustomizerPendingIntent
                    )
                    .addAction(
                        android.R.drawable.ic_menu_edit,
                        "Edit",
                        openCustomizerPendingIntent
                    )
                    .build()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(reqId, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showBlogPostNotification(context: Context, post: BlogPost) {
        createNotificationChannel(context)

        val reqId = if (post.id.isNotBlank()) post.id.hashCode() else (post.title + post.content).hashCode()

        val openDetailIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", "posts")
            putExtra("open_blog_post_detail", true)
            putExtra("blog_post_id", post.id)
            putExtra("blog_post_title", post.title)
            putExtra("blog_post_content", post.content)
            putExtra("blog_post_category", post.category)
            putExtra("blog_post_author", post.author)
            putExtra("blog_post_read_time", post.readTime)
            putExtra("blog_post_image_url", post.imageUrl)
            putExtra("blog_post_timestamp", post.timestamp)
        }

        val openDetailPendingIntent = PendingIntent.getActivity(
            context,
            reqId,
            openDetailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titleText = "নতুন ব্লগ পোস্ট: ${post.title}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titleText)
            .setContentText(post.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(post.content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(openDetailPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "পড়ুন",
                openDetailPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_share,
                "শেয়ার",
                openDetailPendingIntent
            )
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reqId, notification)
    }

    fun showPostNotification(context: Context, title: String, message: String, postId: String = "") {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "posts")
            putExtra("post_id", postId)
        }

        val notificationId = if (postId.isNotBlank()) postId.hashCode() else (title + message).hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
