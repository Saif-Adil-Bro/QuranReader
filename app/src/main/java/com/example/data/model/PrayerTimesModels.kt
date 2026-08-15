package com.example.data.model

data class DistrictInfo(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val divisionBn: String,
    val latitude: Double,
    val longitude: Double
)

enum class PrayerName(val id: String, val nameBn: String, val nameEn: String, val icon: String) {
    FAJR("fajr", "ফজর", "Fajr", "🌅"),
    SUNRISE("sunrise", "সূর্যোদয়", "Sunrise", "☀️"),
    DHUHR("dhuhr", "যোহর", "Dhuhr", "☀️"),
    ASR("asr", "আসর", "Asr", "🌤️"),
    MAGHRIB("maghrib", "মাগরিব", "Maghrib", "🌇"),
    ISHA("isha", "এশা", "Isha", "🌙")
}

data class SinglePrayerTime(
    val name: PrayerName,
    val timeDigits: String,         // e.g. "৪:৩১"
    val amPm: String,               // "AM" or "PM"
    val timeFormatted: String,      // e.g. "০৪:২২ AM"
    val timestampMillis: Long,
    val isCurrent: Boolean = false,
    val isNext: Boolean = false
)

data class DailyPrayerSchedule(
    val dateStrBn: String,
    val district: DistrictInfo,
    val prayers: List<SinglePrayerTime>,
    val currentPrayer: SinglePrayerTime?,
    val nextPrayer: SinglePrayerTime?,
    val remainingTimeToNextFormatted: String, // e.g. "৩৫ মিনিট বাকি" or "১ ঘণ্টা ২০ মিনিট"
    val isForbiddenTimeNow: Boolean,
    val forbiddenTimeReason: String?,
    val sahriEndTimeFormatted: String,
    val iftarTimeFormatted: String,
    val tahajjudEndTimeFormatted: String,
    val ishraqStartTimeFormatted: String
)
