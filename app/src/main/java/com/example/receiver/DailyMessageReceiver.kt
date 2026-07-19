package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.util.Calendar

class DailyMessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sharedPrefs = context.getSharedPreferences("quran_menu_prefs", Context.MODE_PRIVATE)
            val enabled = sharedPrefs.getBoolean("daily_message_enabled", false)
            if (enabled) {
                scheduleNextAlarm(context)
            }
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_islamic_message"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Islamic Message",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily Ayah, Hadith or Islamic reminder"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2001,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messages = listOf(
            Pair("আজকের আয়াত", "নিশ্চয়ই কষ্টের সাথে স্বস্তি রয়েছে। (সূরা ইনশিরাহ: ৫)"),
            Pair("আজকের হাদিস", "যে ব্যক্তি জ্ঞান অর্জনের উদ্দেশ্যে কোনো পথ অবলম্বন করে, আল্লাহ তার জন্য জান্নাতের পথ সহজ করে দেন। (মুসলিম)"),
            Pair("কুরআনিক দুআ", "হে আমাদের রব! আমাদেরকে দুনিয়াতে কল্যাণ দান করুন এবং আখিরাতেও কল্যাণ দান করুন। (সূরা বাকারা: ২০১)"),
            Pair("ইসলামিক বার্তা", "আল্লাহর জিকিরে অন্তর প্রশান্তি লাভ করে। (সূরা রাদ: ২৮)"),
            Pair("আজকের সুন্নাহ", "হাসিমুখে কথা বলাও একটি সাদাকাহ। (তিরমিযী)"),
            Pair("দ্বীনি বার্তা", "নামাজ কায়েম করুন, নিশ্চয়ই নামাজ অশ্লীল ও মন্দ কাজ থেকে বিরত রাখে। (সূরা আনকাবুত: ৪৫)")
        )
        
        val randomMessage = messages.random()

        val iconRes = com.example.R.mipmap.ic_launcher
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(randomMessage.first)
            .setContentText(randomMessage.second)
            .setStyle(NotificationCompat.BigTextStyle().bigText(randomMessage.second))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(2001, notification)
        
        scheduleNextAlarm(context)
    }

    companion object {
        fun scheduleNextAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, DailyMessageReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val sharedPrefs = context.getSharedPreferences("quran_menu_prefs", Context.MODE_PRIVATE)
            val hour = sharedPrefs.getInt("daily_message_hour", 8)
            val minute = sharedPrefs.getInt("daily_message_minute", 0)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }

        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, DailyMessageReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
