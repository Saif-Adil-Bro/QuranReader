package com.example.utils

import com.example.data.model.DistrictInfo
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.SinglePrayerTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import kotlin.math.*

object PrayerTimesCalculator {

    // 64 Districts of Bangladesh with accurate Coordinates
    val BANGLADESH_DISTRICTS = listOf(
        DistrictInfo("dhaka", "Dhaka", "ঢাকা", "ঢাকা", 23.8103, 90.4125),
        DistrictInfo("chattogram", "Chattogram", "চট্টগ্রাম", "চট্টগ্রাম", 22.3569, 91.7832),
        DistrictInfo("sylhet", "Sylhet", "সিলেট", "সিলেট", 24.8949, 91.8687),
        DistrictInfo("rajshahi", "Rajshahi", "রাজশাহী", "রাজশাহী", 24.3636, 88.6241),
        DistrictInfo("khulna", "Khulna", "খুলনা", "খুলনা", 22.8456, 89.5403),
        DistrictInfo("barishal", "Barishal", "বরিশাল", "বরিশাল", 22.7010, 90.3535),
        DistrictInfo("rangpur", "Rangpur", "রংপুর", "রংপুর", 25.7439, 89.2752),
        DistrictInfo("mymensingh", "Mymensingh", "ময়মনসিংহ", "ময়মনসিংহ", 24.7471, 90.4203),
        DistrictInfo("cumilla", "Cumilla", "কুমিল্লা", "চট্টগ্রাম", 23.4682, 91.1788),
        DistrictInfo("gazipur", "Gazipur", "গাজীপুর", "ঢাকা", 24.0023, 90.4267),
        DistrictInfo("narayanganj", "Narayanganj", "নারায়ণগঞ্জ", "ঢাকা", 23.6238, 90.5000),
        DistrictInfo("bogra", "Bogura", "বগুড়া", "রাজশাহী", 24.8465, 89.3777),
        DistrictInfo("dinajpur", "Dinajpur", "দিনাজপুর", "রংপুর", 25.6217, 88.6355),
        DistrictInfo("faridpur", "Faridpur", "ফরিদপুর", "ঢাকা", 23.6071, 89.8429),
        DistrictInfo("jashore", "Jashore", "যশোর", "খুলনা", 23.1664, 89.2081),
        DistrictInfo("coxsbazar", "Cox's Bazar", "কক্সবাজার", "চট্টগ্রাম", 21.4272, 92.0058),
        DistrictInfo("noakhali", "Noakhali", "নোয়াখালী", "চট্টগ্রাম", 22.8696, 91.0998),
        DistrictInfo("feni", "Feni", "ফেনী", "চট্টগ্রাম", 23.0186, 91.3966),
        DistrictInfo("brahmanbaria", "Brahmanbaria", "ব্রাহ্মণবাড়িয়া", "চট্টগ্রাম", 23.9571, 91.1115),
        DistrictInfo("chandpur", "Chandpur", "চাঁদপুর", "চট্টগ্রাম", 23.2333, 90.6667),
        DistrictInfo("lakshmipur", "Lakshmipur", "লক্ষ্মীপুর", "চট্টগ্রাম", 22.9425, 90.8412),
        DistrictInfo("tangail", "Tangail", "টাঙ্গাইল", "ঢাকা", 24.2513, 89.9167),
        DistrictInfo("kishoreganj", "Kishoreganj", "কিশোরগঞ্জ", "ঢাকা", 24.4449, 90.7766),
        DistrictInfo("manikganj", "Manikganj", "মানিকগঞ্জ", "ঢাকা", 23.8617, 90.0003),
        DistrictInfo("munshiganj", "Munshiganj", "মুন্সিগঞ্জ", "ঢাকা", 23.5422, 90.5305),
        DistrictInfo("narsingdi", "Narsingdi", "নরসিংদী", "ঢাকা", 23.9322, 90.7154),
        DistrictInfo("gopalganj", "Gopalganj", "গোপালগঞ্জ", "ঢাকা", 23.0051, 89.8266),
        DistrictInfo("madaripur", "Madaripur", "মাদারীপুর", "ঢাকা", 23.1641, 90.1897),
        DistrictInfo("shariatpur", "Shariatpur", "শরীয়তপুর", "ঢাকা", 23.2423, 90.4348),
        DistrictInfo("rajbari", "Rajbari", "রাজবাড়ী", "ঢাকা", 23.7574, 89.6445),
        DistrictInfo("netrokona", "Netrokona", "নেত্রকোণা", "ময়মনসিংহ", 24.8709, 90.7279),
        DistrictInfo("jamalpur", "Jamalpur", "জামালপুর", "ময়মনসিংহ", 24.9375, 89.9378),
        DistrictInfo("sherpur", "Sherpur", "শেরপুর", "ময়মনসিংহ", 25.0205, 90.0153),
        DistrictInfo("habiganj", "Habiganj", "হবিগঞ্জ", "সিলেট", 24.3749, 91.4155),
        DistrictInfo("moulvibazar", "Moulvibazar", "মৌলভীবাজার", "সিলেট", 24.4829, 91.7774),
        DistrictInfo("sunamganj", "Sunamganj", "সুনামগঞ্জ", "সিলেট", 25.0658, 91.3950),
        DistrictInfo("sirajganj", "Sirajganj", "সিরাজগঞ্জ", "রাজশাহী", 24.4534, 89.7008),
        DistrictInfo("pabna", "Pabna", "পাবনা", "রাজশাহী", 24.0064, 89.2372),
        DistrictInfo("natore", "Natore", "নাটোর", "রাজশাহী", 24.4206, 88.9320),
        DistrictInfo("naogaon", "Naogaon", "নওগাঁ", "রাজশাহী", 24.7936, 88.9318),
        DistrictInfo("chapainawabganj", "Chapai Nawabganj", "চাঁপাইনবাবগঞ্জ", "রাজশাহী", 24.5965, 88.2775),
        DistrictInfo("joypurhat", "Joypurhat", "জয়পুরহাট", "রাজশাহী", 25.1015, 89.0277),
        DistrictInfo("kushtia", "Kushtia", "কুষ্টিয়া", "খুলনা", 23.9013, 89.1205),
        DistrictInfo("meherpur", "Meherpur", "মেহেরপুর", "খুলনা", 23.7622, 88.6318),
        DistrictInfo("chuadanga", "Chuadanga", "চুয়াডাঙ্গা", "খুলনা", 23.6402, 88.8418),
        DistrictInfo("jhenaidah", "Jhenaidah", "ঝিনাইদহ", "খুলনা", 23.5450, 89.1726),
        DistrictInfo("magura", "Magura", "মাগুরা", "খুলনা", 23.4873, 89.4199),
        DistrictInfo("narail", "Narail", "নড়াইল", "খুলনা", 23.1725, 89.5127),
        DistrictInfo("satkhira", "Satkhira", "সাতক্ষীরা", "খুলনা", 22.7185, 89.0705),
        DistrictInfo("bagerhat", "Bagerhat", "বাগেরহাট", "খুলনা", 22.6516, 89.7859),
        DistrictInfo("patuakhali", "Patuakhali", "পটুয়াখালী", "বরিশাল", 22.3596, 90.3299),
        DistrictInfo("bhola", "Bhola", "ভোলা", "বরিশাল", 22.6859, 90.6481),
        DistrictInfo("pirojpur", "Pirojpur", "পিরোজপুর", "বরিশাল", 22.5841, 89.9720),
        DistrictInfo("jhalokathi", "Jhalokathi", "ঝালকাঠি", "বরিশাল", 22.6406, 90.1987),
        DistrictInfo("barguna", "Barguna", "বরগুনা", "বরিশাল", 22.0953, 90.1121),
        DistrictInfo("gaibandha", "Gaibandha", "গাইবান্ধা", "রংপুর", 25.3288, 89.5408),
        DistrictInfo("kurigram", "Kurigram", "কুড়িগ্রাম", "রংপুর", 25.8054, 89.6362),
        DistrictInfo("lalmonirhat", "Lalmonirhat", "লালমনিরহাট", "রংপুর", 25.9923, 89.2847),
        DistrictInfo("nilphamari", "Nilphamari", "নীলফামারী", "রংপুর", 25.9318, 88.8560),
        DistrictInfo("panchagarh", "Panchagarh", "পঞ্চগড়", "রংপুর", 26.3411, 88.5542),
        DistrictInfo("thakurgaon", "Thakurgaon", "ঠাকুরগাঁও", "রংপুর", 26.0337, 88.4617),
        DistrictInfo("khagrachhari", "Khagrachhari", "খাগড়াছড়ি", "চট্টগ্রাম", 23.1193, 91.9847),
        DistrictInfo("rangamati", "Rangamati", "রাঙ্গামাটি", "চট্টগ্রাম", 22.7324, 92.2985),
        DistrictInfo("bandarban", "Bandarban", "বান্দরবান", "চট্টগ্রাম", 22.1953, 92.2184)
    )

    fun getDefaultDistrict(): DistrictInfo = BANGLADESH_DISTRICTS.first() // Dhaka

    fun findDistrictById(id: String): DistrictInfo {
        return BANGLADESH_DISTRICTS.find { it.id.equals(id, ignoreCase = true) } ?: getDefaultDistrict()
    }

    /**
     * Calculates the daily prayer schedule using astronomical solar formulas.
     * Method: Karachi / Islamic Foundation Bangladesh (Fajr: 18.0°, Isha: 18.0°, Asr: Hanafi / factor 2).
     */
    fun calculatePrayerSchedule(
        date: LocalDate = LocalDate.now(),
        district: DistrictInfo = getDefaultDistrict(),
        isHanafi: Boolean = true,
        fajrAngle: Double = 18.0,
        ishaAngle: Double = 18.0
    ): DailyPrayerSchedule {
        val lat = district.latitude
        val lng = district.longitude
        val timeZone = 6.0 // Bangladesh Standard Time (UTC+6)

        val dayOfYear = date.dayOfYear

        // Solar Declination & Equation of Time
        val b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0
        val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b) // minutes
        val declination = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))) // degrees

        // Solar Noon (Dhuhr) in local time hours
        val solarNoon = 12.0 + (timeZone * 15.0 - lng) / 15.0 - (eot / 60.0)

        // Helper to calculate Hour Angle
        fun getHourAngle(angle: Double): Double {
            val latRad = Math.toRadians(lat)
            val decRad = Math.toRadians(declination)
            val angRad = Math.toRadians(angle)

            val cosH = (sin(angRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
            return if (cosH > 1.0) 0.0 else if (cosH < -1.0) Math.PI else acos(cosH)
        }

        // Sunrise & Sunset angle is typically -0.833° (atmospheric refraction + sun disk radius)
        val sunriseHourAngle = Math.toDegrees(getHourAngle(-0.833)) / 15.0
        val fajrHourAngle = Math.toDegrees(getHourAngle(-fajrAngle)) / 15.0
        val ishaHourAngle = Math.toDegrees(getHourAngle(-ishaAngle)) / 15.0

        // Asr Angle calculation (Hanafi shadow = 2, Shafi'i/Standard shadow = 1)
        val asrFactor = if (isHanafi) 2.0 else 1.0
        val latMinusDec = Math.abs(lat - declination)
        val asrAltitude = Math.toDegrees(atan(1.0 / (asrFactor + tan(Math.toRadians(latMinusDec)))))
        val asrHourAngle = Math.toDegrees(getHourAngle(asrAltitude)) / 15.0

        val fajrDecimal = solarNoon - fajrHourAngle
        val sunriseDecimal = solarNoon - sunriseHourAngle
        val dhuhrDecimal = solarNoon + (2.0 / 60.0) // 2 minutes added after zawal for safety
        val asrDecimal = solarNoon + asrHourAngle
        val maghribDecimal = solarNoon + sunriseHourAngle + (2.0 / 60.0) // 2 minutes safety margin for sunset
        val ishaDecimal = solarNoon + ishaHourAngle

        // Format to LocalTime
        fun decimalToLocalTime(decimal: Double): LocalTime {
            var hours = decimal.toInt()
            var minutes = ((decimal - hours) * 60.0).roundToInt()
            if (minutes >= 60) {
                hours += 1
                minutes -= 60
            }
            if (hours >= 24) hours %= 24
            if (hours < 0) hours = (hours + 24) % 24
            return LocalTime.of(hours, minutes)
        }

        val fajrTime = decimalToLocalTime(fajrDecimal)
        val sunriseTime = decimalToLocalTime(sunriseDecimal)
        val dhuhrTime = decimalToLocalTime(dhuhrDecimal)
        val asrTime = decimalToLocalTime(asrDecimal)
        val maghribTime = decimalToLocalTime(maghribDecimal)
        val ishaTime = decimalToLocalTime(ishaDecimal)

        // Extra times
        val sahriEndTime = fajrTime.minusMinutes(3) // 3 mins precaution before Fajr
        val iftarTime = maghribTime
        val tahajjudEndTime = fajrTime.minusMinutes(15)
        val ishraqStartTime = sunriseTime.plusMinutes(15)

        // Convert to millisecond timestamps for today
        val zoneId = ZoneId.systemDefault()
        fun toMillis(lt: LocalTime): Long {
            return LocalDateTime.of(date, lt).atZone(zoneId).toInstant().toEpochMilli()
        }

        val now = LocalDateTime.now()
        val nowMillis = System.currentTimeMillis()

        fun createSinglePrayer(name: PrayerName, time: LocalTime): SinglePrayerTime {
            return SinglePrayerTime(
                name = name,
                timeDigits = formatTimeDigits(time),
                amPm = formatAmPm(time),
                timeFormatted = formatTimeBn(time),
                timestampMillis = toMillis(time)
            )
        }

        val rawPrayers = listOf(
            createSinglePrayer(PrayerName.FAJR, fajrTime),
            createSinglePrayer(PrayerName.SUNRISE, sunriseTime),
            createSinglePrayer(PrayerName.DHUHR, dhuhrTime),
            createSinglePrayer(PrayerName.ASR, asrTime),
            createSinglePrayer(PrayerName.MAGHRIB, maghribTime),
            createSinglePrayer(PrayerName.ISHA, ishaTime)
        )

        // Determine current & next prayer
        val isToday = (date == LocalDate.now())
        var currentPrayer: SinglePrayerTime? = null
        var nextPrayer: SinglePrayerTime? = null
        var remainingMillis = 0L

        if (isToday) {
            val fMillis = toMillis(fajrTime)
            val sMillis = toMillis(sunriseTime)
            val dMillis = toMillis(dhuhrTime)
            val aMillis = toMillis(asrTime)
            val mMillis = toMillis(maghribTime)
            val iMillis = toMillis(ishaTime)

            when {
                nowMillis < fMillis -> {
                    // Before Fajr (Night time)
                    currentPrayer = null
                    nextPrayer = rawPrayers[0] // Fajr
                    remainingMillis = fMillis - nowMillis
                }
                nowMillis < sMillis -> {
                    // Fajr
                    currentPrayer = rawPrayers[0]
                    nextPrayer = rawPrayers[1] // Sunrise
                    remainingMillis = sMillis - nowMillis
                }
                nowMillis < dMillis -> {
                    // Sunrise to Dhuhr (Ishraq/Chasht)
                    currentPrayer = rawPrayers[1]
                    nextPrayer = rawPrayers[2] // Dhuhr
                    remainingMillis = dMillis - nowMillis
                }
                nowMillis < aMillis -> {
                    // Dhuhr
                    currentPrayer = rawPrayers[2]
                    nextPrayer = rawPrayers[3] // Asr
                    remainingMillis = aMillis - nowMillis
                }
                nowMillis < mMillis -> {
                    // Asr
                    currentPrayer = rawPrayers[3]
                    nextPrayer = rawPrayers[4] // Maghrib
                    remainingMillis = mMillis - nowMillis
                }
                nowMillis < iMillis -> {
                    // Maghrib
                    currentPrayer = rawPrayers[4]
                    nextPrayer = rawPrayers[5] // Isha
                    remainingMillis = iMillis - nowMillis
                }
                else -> {
                    // After Isha
                    currentPrayer = rawPrayers[5]
                    // Next is tomorrow's Fajr
                    val tomorrowFajrMillis = LocalDateTime.of(date.plusDays(1), fajrTime).atZone(zoneId).toInstant().toEpochMilli()
                    nextPrayer = SinglePrayerTime(
                        name = PrayerName.FAJR,
                        timeDigits = formatTimeDigits(fajrTime),
                        amPm = formatAmPm(fajrTime),
                        timeFormatted = formatTimeBn(fajrTime),
                        timestampMillis = tomorrowFajrMillis
                    )
                    remainingMillis = tomorrowFajrMillis - nowMillis
                }
            }
        }

        val markedPrayers = rawPrayers.map { p ->
            p.copy(
                isCurrent = isToday && (currentPrayer?.name == p.name),
                isNext = isToday && (nextPrayer?.name == p.name)
            )
        }

        // Check forbidden prayer times (মাকরূহ সময়)
        // 1. Sunrise (Sunrise to Sunrise + 15 min)
        // 2. Solar Noon / Zawal (10 min before Dhuhr)
        // 3. Sunset (15 min before Maghrib till Maghrib)
        var isForbidden = false
        var forbiddenReason: String? = null

        if (isToday) {
            val currentTime = now.toLocalTime()
            val sunrisePlus15 = sunriseTime.plusMinutes(15)
            val dhuhrMinus10 = dhuhrTime.minusMinutes(10)
            val maghribMinus15 = maghribTime.minusMinutes(15)

            if (currentTime.isAfter(sunriseTime) && currentTime.isBefore(sunrisePlus15)) {
                isForbidden = true
                forbiddenReason = "সূর্যোদয়ের নিষিদ্ধ সময় (মাকরূহ)"
            } else if (currentTime.isAfter(dhuhrMinus10) && currentTime.isBefore(dhuhrTime)) {
                isForbidden = true
                forbiddenReason = "দ্বিপ্রহরের নিষিদ্ধ সময় (মাকরূহ)"
            } else if (currentTime.isAfter(maghribMinus15) && currentTime.isBefore(maghribTime)) {
                isForbidden = true
                forbiddenReason = "সূর্যাস্তের নিষিদ্ধ সময় (মাকরূহ)"
            }
        }

        return DailyPrayerSchedule(
            dateStrBn = DateUtil.getTodayEnglishDateStr(),
            district = district,
            prayers = markedPrayers,
            currentPrayer = currentPrayer,
            nextPrayer = nextPrayer,
            remainingTimeToNextFormatted = formatRemainingTimeBn(remainingMillis),
            isForbiddenTimeNow = isForbidden,
            forbiddenTimeReason = forbiddenReason,
            sahriEndTimeFormatted = formatTimeBn(sahriEndTime),
            iftarTimeFormatted = formatTimeBn(iftarTime),
            tahajjudEndTimeFormatted = formatTimeBn(tahajjudEndTime),
            ishraqStartTimeFormatted = formatTimeBn(ishraqStartTime)
        )
    }

    fun formatTimeDigits(time: LocalTime): String {
        var hour = time.hour
        val minute = time.minute
        if (hour > 12) hour -= 12
        if (hour == 0) hour = 12

        val hourStr = toBengaliDigits(hour)
        val minuteStr = toBengaliDigits(minute).padStart(2, '০')
        return "$hourStr:$minuteStr"
    }

    fun formatAmPm(time: LocalTime): String {
        return if (time.hour >= 12) "PM" else "AM"
    }

    fun formatTimeBn(time: LocalTime): String {
        var hour = time.hour
        val minute = time.minute
        val isPm = hour >= 12
        val amPmBn = if (hour in 0..3) "রাত" else if (hour in 4..11) "সকাল" else if (hour in 12..15) "দুপুর" else if (hour in 16..17) "বিকাল" else if (hour in 18..19) "সন্ধ্যা" else "রাত"

        if (hour > 12) hour -= 12
        if (hour == 0) hour = 12

        val hourStr = toBengaliDigits(hour)
        val minuteStr = toBengaliDigits(minute).padStart(2, '০')

        return "$hourStr:$minuteStr $amPmBn"
    }

    fun formatRemainingTimeBn(millis: Long): String {
        if (millis <= 0) return "ওয়াক্ত শুরু হয়েছে"
        val totalMinutes = millis / (1000 * 60)
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60

        return when {
            hours > 0 && mins > 0 -> "${toBengaliDigits(hours.toInt())} ঘণ্টা ${toBengaliDigits(mins.toInt())} মিনিট বাকি"
            hours > 0 -> "${toBengaliDigits(hours.toInt())} ঘণ্টা বাকি"
            mins > 0 -> "${toBengaliDigits(mins.toInt())} মিনিট বাকি"
            else -> "কয়েক সেকেন্ড বাকি"
        }
    }

    fun toBengaliDigits(number: Int): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return number.toString().map { if (it.isDigit()) bnDigits[it - '0'] else it }.joinToString("")
    }
}
