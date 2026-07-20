package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title 
            ?: remoteMessage.data["title"] 
            ?: "ইসলামিক বার্তা"
            
        val body = remoteMessage.notification?.body 
            ?: remoteMessage.data["body"] 
            ?: remoteMessage.data["message"] 
            ?: remoteMessage.data["text"] 
            ?: ""

        val imageUrl = remoteMessage.notification?.imageUrl?.toString()
            ?: remoteMessage.data["image"]
            ?: remoteMessage.data["imageUrl"]
            ?: remoteMessage.data["image_url"]
            ?: remoteMessage.data["img_url"]

        if (!imageUrl.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = fetchBitmap(imageUrl)
                withContext(Dispatchers.Main) {
                    sendNotification(title, body, bitmap)
                }
            }
        } else {
            sendNotification(title, body, null)
        }
    }

    override fun onNewToken(token: String) {
        // Handle token refresh if needed
        super.onNewToken(token)
    }

    private suspend fun fetchBitmap(url: String): Bitmap? {
        return try {
            val loader = ImageLoader(this)
            val request = ImageRequest.Builder(this)
                .data(url)
                .allowHardware(false) // Crucial for notification/widget bitmaps to prevent rendering crashes
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun sendNotification(title: String, messageBody: String, bitmap: Bitmap?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "fcm_default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (bitmap != null) {
            notificationBuilder.setLargeIcon(bitmap)
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null as Bitmap?) // Hide thumbnail when expanded
                    .setSummaryText(messageBody)
            )
        } else {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Push Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(3001, notificationBuilder.build())
    }
}
