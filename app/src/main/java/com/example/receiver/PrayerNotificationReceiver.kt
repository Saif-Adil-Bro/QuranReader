package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.PrayerName
import com.example.utils.DateUtil
import com.example.utils.PrayerNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == Intent.ACTION_BOOT_COMPLETED || action == "com.example.ACTION_REFRESH_PRAYER_ALARMS") {
            PrayerNotificationHelper.scheduleNextPrayerAlarms(context)
            return
        }

        if (action == "com.example.ACTION_PRAYER_NOTIFICATION") {
            val prayerNameStr = intent.getStringExtra("prayer_name") ?: return
            val prayerName = try {
                PrayerName.valueOf(prayerNameStr)
            } catch (e: Exception) {
                null
            } ?: return

            if (!PrayerNotificationHelper.isMasterEnabled(context) ||
                !PrayerNotificationHelper.isPrayerEnabled(context, prayerName)
            ) {
                // Reschedule next ones and return
                PrayerNotificationHelper.scheduleNextPrayerAlarms(context)
                return
            }

            // Deduplication Guard: Do not show notification for the same prayer within 30 minutes
            val prefs = context.getSharedPreferences("prayer_notification_prefs", Context.MODE_PRIVATE)
            val lastNotifiedKey = "last_notified_${prayerName.name}"
            val lastNotifiedTime = prefs.getLong(lastNotifiedKey, 0L)
            val nowTime = System.currentTimeMillis()

            if (nowTime - lastNotifiedTime < 30 * 60 * 1000L) {
                // Already notified within the last 30 minutes, skip sending again
                PrayerNotificationHelper.scheduleNextPrayerAlarms(context)
                return
            }
            prefs.edit().putLong(lastNotifiedKey, nowTime).apply()

            val prayerTimeFormatted = intent.getStringExtra("prayer_time_formatted") ?: ""
            val districtNameBn = intent.getStringExtra("district_name_bn") ?: "ঢাকা"

            val isFriday = LocalDate.now().dayOfWeek == DayOfWeek.FRIDAY
            val isDhuhrOnFriday = isFriday && prayerName == PrayerName.DHUHR

            val title = when (prayerName) {
                PrayerName.FAJR -> "ফজরের ওয়াক্ত শুরু হয়েছে 🕌"
                PrayerName.DHUHR -> if (isDhuhrOnFriday) "পবিত্র জুমুআর ওয়াক্ত শুরু হয়েছে 🕌✨" else "যুহরের ওয়াক্ত শুরু হয়েছে 🕌"
                PrayerName.ASR -> "আসরের ওয়াক্ত শুরু হয়েছে 🕌"
                PrayerName.MAGHRIB -> "মাগরিবের ওয়াক্ত শুরু হয়েছে 🕌"
                PrayerName.ISHA -> "এশার ওয়াক্ত শুরু হয়েছে 🌙"
                PrayerName.SUNRISE -> "সূর্যোদয় হয়েছে ☀️"
            }

            val prayerDisplayTitle = when (prayerName) {
                PrayerName.FAJR -> "ফজর"
                PrayerName.DHUHR -> if (isDhuhrOnFriday) "জুমুআ" else "যুহর"
                PrayerName.ASR -> "আসর"
                PrayerName.MAGHRIB -> "মাগরিব"
                PrayerName.ISHA -> "এশা"
                PrayerName.SUNRISE -> "সূর্যোদয়"
            }

            val message = if (prayerTimeFormatted.isNotBlank()) {
                "$prayerDisplayTitle সালাতের সময়: $prayerTimeFormatted ($districtNameBn)। ওয়াক্তমত সালাত আদায় করার প্রস্তুতি নিন।"
            } else {
                "$prayerDisplayTitle সালাতের সময় হয়েছে ($districtNameBn)। ওয়াক্তমত সালাত আদায় করার প্রস্তুতি নিন।"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "prayer_times_notification_channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "ওয়াক্ত শুরুর নোটিফিকেশন",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "প্রতিটি ওয়াক্তের সালাত শুরু হলে স্মরণ করিয়ে দেওয়া হয়"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("navigate_to", "prayer_times")
                putExtra("target_screen", "prayer_times")
            }

            val notifId = PrayerNotificationHelper.getRequestCodeForPrayer(prayerName)
            val pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Save in local in-app notification database
            try {
                val db = com.example.data.local.NotificationDatabase.getDatabase(context)
                val entity = com.example.data.local.entity.LocalNotificationEntity(
                    title = title,
                    content = message,
                    category = "সালাত রিমাইন্ডার",
                    author = "নামাজের সময়সূচি",
                    timestamp = System.currentTimeMillis()
                )
                GlobalScope.launch(Dispatchers.IO) {
                    db.localNotificationDao().insertNotification(entity)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val iconRes = R.mipmap.ic_launcher
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)

            if (PrayerNotificationHelper.isSoundEnabled(context)) {
                builder.setSound(defaultSoundUri)
            }

            notificationManager.notify(notifId, builder.build())

            // Reschedule subsequent prayer alarms
            PrayerNotificationHelper.scheduleNextPrayerAlarms(context)
        }
    }
}
