package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.PrayerName
import com.example.utils.DateUtil
import com.example.utils.HijriCalendarUtil
import kotlinx.coroutines.delay
import java.util.Calendar

private val GoldAccent = Color(0xFFD4AF37)
private val GoldBright = Color(0xFFFFDF78)
private val SoftWhite = Color(0xFFD6EAE0)
private val GlassBg = Color.White.copy(alpha = 0.08f)
private val GlassBorder = Color.White.copy(alpha = 0.14f)

data class VisualPrayerPoint(
    val name: String,
    val bengaliName: String,
    val timeMinutes: Int,
    val timeString: String,
    val iconType: PrayerName
)

@Composable
fun PrayerSunPathCard(
    schedule: DailyPrayerSchedule,
    modifier: Modifier = Modifier,
    onDetailsClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentDate = remember(schedule) { schedule.dateStrBn }
    val hijriDate = remember { DateUtil.getTodayHijriDateStr(0) }

    val selectedLocation = if (schedule.district.countryBn == "বাংলাদেশ") {
        schedule.district.nameBn
    } else {
        "${schedule.district.nameBn}, ${schedule.district.countryBn}"
    }

    val prayers = remember(schedule) {
        val list = mutableListOf<VisualPrayerPoint>()
        schedule.prayers.forEach { prayerTime ->
            if (prayerTime.name != PrayerName.SAHRI && prayerTime.name != PrayerName.IFTAR) {
                val cal = Calendar.getInstance().apply { timeInMillis = prayerTime.timestampMillis }
                val minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                
                list.add(
                    VisualPrayerPoint(
                        name = prayerTime.name.name,
                        bengaliName = prayerTime.name.nameBn,
                        timeMinutes = minutes,
                        timeString = "${prayerTime.timeDigits} ${prayerTime.amPm}",
                        iconType = prayerTime.name
                    )
                )
            }
        }
        list.sortedBy { it.timeMinutes }
    }

    val currentMinutes by rememberCurrentMinutes()
    val currentIndex = findCurrentPrayerIndex(prayers, currentMinutes)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(158.dp) // Bound height exactly as the existing cards do
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDetailsClick
            )
    ) {
        // Transparent container because parent card already has gradient
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header (Date + Hijri + Location)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left: Golden Calendar Icon + Bangla Date Capsule
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 9.dp, vertical = 2.5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentDate,
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                // Top-Right: Golden Location Pin + District Capsule
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 9.dp, vertical = 2.5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = selectedLocation,
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 2. Visual Sun/Moon Path
            VisualSunPathSection(
                prayers = prayers,
                currentIndex = currentIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // 3. Current Waqt & Progress Bar
            if (currentIndex in 0 until prayers.size) {
                val current = prayers[currentIndex]
                val next = prayers.getOrNull((currentIndex + 1) % prayers.size) ?: prayers.first()
                
                val totalMinutes = if (next.timeMinutes > current.timeMinutes) {
                    next.timeMinutes - current.timeMinutes
                } else {
                    (24 * 60 - current.timeMinutes) + next.timeMinutes
                }
                val elapsedMinutes = if (currentMinutes >= current.timeMinutes) {
                    currentMinutes - current.timeMinutes
                } else {
                    (24 * 60 - current.timeMinutes) + currentMinutes
                }
                val progress = (elapsedMinutes.toFloat() / totalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বর্তমান ওয়াক্ত:",
                            color = SoftWhite.copy(alpha = 0.8f),
                            fontSize = 9.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${current.bengaliName} • ${current.timeString}",
                            color = GoldBright,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(GoldAccent, Color(0xFF34D399))
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Elapsed & Remaining Times
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "শুরু: ${formatDurationBangla(elapsedMinutes)} আগে",
                            color = SoftWhite.copy(alpha = 0.9f),
                            fontSize = 8.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = Color(0xFF6EE7B7),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "বাকি: ${formatDurationBangla(totalMinutes - elapsedMinutes)}",
                            color = GoldBright,
                            fontSize = 8.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 4. Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SoftWhite.copy(alpha = 0.7f),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "ইসলামিক ফাউন্ডেশন অনুযায়ী",
                    color = SoftWhite.copy(alpha = 0.75f),
                    fontSize = 7.5.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.clickable(onClick = onDetailsClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বিস্তারিত দেখুন",
                        color = GoldBright,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualSunPathSection(
    prayers: List<VisualPrayerPoint>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val width = maxWidth
        val height = maxHeight

        val infiniteTransition = rememberInfiniteTransition(label = "sunPulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.90f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        // Arc & Nodes Drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (prayers.size < 2) return@Canvas

            val pathStart = Offset(x = size.width * 0.05f, y = size.height * 0.78f)
            val pathEnd = Offset(x = size.width * 0.95f, y = size.height * 0.78f)
            val peak = Offset(x = size.width * 0.50f, y = size.height * 0.08f)

            val path = Path().apply {
                moveTo(pathStart.x, pathStart.y)
                quadraticBezierTo(peak.x, peak.y, pathEnd.x, pathEnd.y)
            }

            // Glow path behind
            drawPath(
                path = path,
                color = GoldAccent.copy(alpha = 0.12f),
                style = Stroke(width = 6.dp.toPx())
            )

            // Dashed celestial path
            drawPath(
                path = path,
                color = GoldAccent.copy(alpha = 0.85f),
                style = Stroke(
                    width = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(6.dp.toPx(), 4.dp.toPx())
                    )
                )
            )

            // Points along the arc
            prayers.forEachIndexed { index, _ ->
                val fraction = index.toFloat() / (prayers.size - 1).coerceAtLeast(1)
                val x = pathStart.x + (pathEnd.x - pathStart.x) * fraction
                val curve = 4f * (1f - fraction) * fraction
                val y = pathStart.y - size.height * 0.70f * curve

                val isCurrent = index == currentIndex

                if (isCurrent) {
                    drawCircle(
                        color = GoldAccent.copy(alpha = 0.35f),
                        radius = 8.dp.toPx() * pulseScale,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = GoldBright,
                        radius = 4.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                } else {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = 2.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Labels & Icons along the path
        prayers.forEachIndexed { index, prayer ->
            val fraction = index.toFloat() / (prayers.size - 1).coerceAtLeast(1)
            val isCurrent = index == currentIndex

            val verticalY = when (index) {
                0, prayers.lastIndex -> 22.dp
                1, 4 -> 4.dp
                else -> (-10).dp
            }

            val xOffset = width * fraction

            Column(
                modifier = Modifier
                    .offset(x = xOffset - 28.dp, y = verticalY)
                    .width(56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = prayer.bengaliName,
                    color = if (isCurrent) GoldBright else Color.White,
                    fontSize = if (isCurrent) 8.5.sp else 7.5.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = prayer.timeString.replace(" ", "\n"),
                    color = if (isCurrent) Color.White else SoftWhite.copy(alpha = 0.85f),
                    fontSize = if (isCurrent) 7.5.sp else 6.5.sp,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 8.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Minimal icon circle
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 16.dp else 13.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent) GoldAccent.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.08f))
                        .border(
                            width = if (isCurrent) 1.5.dp else 0.8.dp,
                            color = if (isCurrent) GoldBright else Color.White.copy(alpha = 0.35f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Minimal fallback icon here to avoid dependency issues if GoldenPrayerIcon isn't easily accessible
                    val fallbackIcon = when (prayer.iconType) {
                        PrayerName.FAJR -> Icons.Default.Nightlight
                        PrayerName.SUNRISE -> Icons.Default.WbSunny
                        PrayerName.DHUHR -> Icons.Default.WbSunny
                        PrayerName.ASR -> Icons.Default.WbSunny
                        PrayerName.MAGHRIB -> Icons.Default.WbSunny
                        PrayerName.ISHA -> Icons.Default.Nightlight
                        else -> Icons.Default.Schedule
                    }
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = null,
                        tint = if (isCurrent) GoldBright else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(if (isCurrent) 10.dp else 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberCurrentMinutes(): State<Int> {
    val state = remember {
        val cal = Calendar.getInstance()
        mutableIntStateOf(cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE))
    }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            state.intValue = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            delay(30_000L) // update every 30s
        }
    }
    return state
}

private fun findCurrentPrayerIndex(prayers: List<VisualPrayerPoint>, currentMinutes: Int): Int {
    if (prayers.isEmpty()) return -1
    for (i in 0 until prayers.lastIndex) {
        val start = prayers[i].timeMinutes
        val end = prayers[i + 1].timeMinutes
        if (currentMinutes in start until end) {
            return i
        }
    }
    return prayers.lastIndex
}

private fun formatDurationBangla(minutes: Int): String {
    val safe = minutes.coerceAtLeast(0)
    val hours = safe / 60
    val mins = safe % 60
    return when {
        hours > 0 && mins > 0 -> "${DateUtil.toBengaliNumerals(hours)} ঘণ্টা ${DateUtil.toBengaliNumerals(mins)} মি."
        hours > 0 -> "${DateUtil.toBengaliNumerals(hours)} ঘণ্টা"
        else -> "${DateUtil.toBengaliNumerals(mins)} মিনিট"
    }
}
