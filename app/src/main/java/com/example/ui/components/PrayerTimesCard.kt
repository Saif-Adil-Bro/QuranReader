package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.SinglePrayerTime

// Design Constants matching exact prompt & mockup specs
private val PrimaryGreenDark = Color(0xFF1B4D3E)
private val PrimaryGreenLight = Color(0xFF2E7D32)
private val AccentGold = Color(0xFFD4AF37)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFD7E8DF)

@Composable
fun PrayerTimesBannerSlide(
    schedule: DailyPrayerSchedule,
    onClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
        // High-Fidelity 3D-Illuminated Mosque Silhouette Watermark in Background
        MosqueSilhouetteBackground(
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header (Date Capsule on Left, Location Capsule on Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                            tint = AccentGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = schedule.dateStrBn,
                            color = TextPrimary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Top-Right: Golden Location Pin + District Capsule
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(100.dp))
                        .clickable { onLocationClick() }
                        .padding(horizontal = 9.dp, vertical = 2.5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        val locationName = if (schedule.district.countryBn == "বাংলাদেশ") {
                            schedule.district.nameBn
                        } else {
                            "${schedule.district.nameBn}, ${schedule.district.countryBn}"
                        }
                        Text(
                            text = locationName,
                            color = TextPrimary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 2. Middle Section (5 Horizontal Prayer Items + Next Prayer Highlight Card)
            val mainPrayers = schedule.prayers.filter { it.name != PrayerName.SUNRISE }
            val nextPrayer = schedule.nextPrayer ?: mainPrayers.firstOrNull { it.name == PrayerName.ISHA } ?: mainPrayers.last()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 5 prayers grid
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    mainPrayers.forEachIndexed { index, prayer ->
                        PrayerColumnItem(
                            prayer = prayer,
                            modifier = Modifier.weight(1f)
                        )

                        if (index < mainPrayers.size - 1) {
                            // Thin vertical separator
                            Box(
                                modifier = Modifier
                                    .width(0.7.dp)
                                    .height(34.dp)
                                    .background(Color.White.copy(alpha = 0.18f))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right: Next Prayer Highlighted Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E8566).copy(alpha = 0.50f))
                        .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "পরবর্তী",
                            color = AccentGold,
                            fontSize = 8.5.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = nextPrayer.name.nameBn,
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = nextPrayer.timeDigits,
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = nextPrayer.amPm,
                            color = TextSecondary,
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 3. Footer (Thin divider, Left Info disclaimer, Right "বিস্তারিত দেখুন →")
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.6.dp)
                        .background(Color.White.copy(alpha = 0.22f))
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "সময়সূচী: ইসলামিক ফাউন্ডেশন (বাংলাদেশ) অনুযায়ী",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "বিস্তারিত দেখুন →",
                            color = AccentGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerColumnItem(
    prayer: SinglePrayerTime,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Golden minimal Islamic prayer icon
        GoldenPrayerIcon(
            prayerName = prayer.name,
            modifier = Modifier.size(15.dp)
        )

        Spacer(modifier = Modifier.height(1.dp))

        // Bengali Name (Tight line spacing)
        Text(
            text = prayer.name.nameBn,
            color = TextSecondary,
            fontSize = 10.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Medium
        )

        // Large clear time digits
        Text(
            text = prayer.timeDigits,
            color = TextPrimary,
            fontSize = 11.5.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold
        )

        // AM / PM
        Text(
            text = prayer.amPm,
            color = TextSecondary.copy(alpha = 0.9f),
            fontSize = 7.5.sp,
            lineHeight = 8.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GoldenPrayerIcon(
    prayerName: PrayerName,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val goldColor = AccentGold

        when (prayerName) {
            PrayerName.FAJR -> {
                // Rising sun with horizontal horizon and rays
                drawLine(
                    color = goldColor,
                    start = Offset(0f, height * 0.75f),
                    end = Offset(width, height * 0.75f),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
                drawArc(
                    color = goldColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(width * 0.25f, height * 0.35f),
                    size = Size(width * 0.5f, height * 0.5f),
                    style = Stroke(width = 1.5f)
                )
                // Rays
                drawLine(goldColor, Offset(width * 0.5f, height * 0.1f), Offset(width * 0.5f, height * 0.25f), 1.3f, StrokeCap.Round)
                drawLine(goldColor, Offset(width * 0.2f, height * 0.25f), Offset(width * 0.32f, height * 0.38f), 1.3f, StrokeCap.Round)
                drawLine(goldColor, Offset(width * 0.8f, height * 0.25f), Offset(width * 0.68f, height * 0.38f), 1.3f, StrokeCap.Round)
            }
            PrayerName.DHUHR, PrayerName.SUNRISE -> {
                // High bright midday sun with radial rays
                drawCircle(
                    color = goldColor,
                    radius = width * 0.22f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
                val rayLen = width * 0.12f
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    val startR = width * 0.30f
                    val endR = startR + rayLen
                    drawLine(
                        color = goldColor,
                        start = Offset((center.x + startR * Math.cos(angle)).toFloat(), (center.y + startR * Math.sin(angle)).toFloat()),
                        end = Offset((center.x + endR * Math.cos(angle)).toFloat(), (center.y + endR * Math.sin(angle)).toFloat()),
                        strokeWidth = 1.3f,
                        cap = StrokeCap.Round
                    )
                }
            }
            PrayerName.ASR -> {
                // Afternoon sun with tilted rays
                drawArc(
                    color = goldColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(width * 0.22f, height * 0.32f),
                    size = Size(width * 0.56f, height * 0.56f),
                    style = Stroke(width = 1.5f)
                )
                drawLine(goldColor, Offset(width * 0.1f, height * 0.75f), Offset(width * 0.9f, height * 0.75f), 1.5f, StrokeCap.Round)
                drawLine(goldColor, Offset(width * 0.5f, height * 0.12f), Offset(width * 0.5f, height * 0.24f), 1.3f, StrokeCap.Round)
                drawLine(goldColor, Offset(width * 0.22f, height * 0.2f), Offset(width * 0.32f, height * 0.32f), 1.3f, StrokeCap.Round)
                drawLine(goldColor, Offset(width * 0.78f, height * 0.2f), Offset(width * 0.68f, height * 0.32f), 1.3f, StrokeCap.Round)
            }
            PrayerName.MAGHRIB -> {
                // Sunset (Sun dipping into horizon)
                drawLine(goldColor, Offset(0f, height * 0.65f), Offset(width, height * 0.65f), 1.5f, StrokeCap.Round)
                drawLine(goldColor, Offset(width * 0.15f, height * 0.82f), Offset(width * 0.85f, height * 0.82f), 1.1f, StrokeCap.Round)
                drawArc(
                    color = goldColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(width * 0.28f, height * 0.32f),
                    size = Size(width * 0.44f, height * 0.44f),
                    style = Stroke(width = 1.5f)
                )
                drawLine(goldColor, Offset(width * 0.5f, height * 0.14f), Offset(width * 0.5f, height * 0.24f), 1.3f, StrokeCap.Round)
            }
            PrayerName.ISHA -> {
                // Golden Crescent Moon & Star
                val moonPath = Path().apply {
                    moveTo(width * 0.55f, height * 0.15f)
                    cubicTo(
                        width * 0.15f, height * 0.25f,
                        width * 0.15f, height * 0.75f,
                        width * 0.55f, height * 0.85f
                    )
                    cubicTo(
                        width * 0.32f, height * 0.7f,
                        width * 0.32f, height * 0.3f,
                        width * 0.55f, height * 0.15f
                    )
                    close()
                }
                drawPath(moonPath, goldColor, style = Fill)

                // Little star
                val starCenter = Offset(width * 0.75f, height * 0.4f)
                drawCircle(goldColor, radius = width * 0.08f, center = starCenter)
            }
        }
    }
}

@Composable
private fun MosqueSilhouetteBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Soft atmospheric light glow radiating from the central mosque dome
        val glowCenter = Offset(w * 0.72f, h * 0.45f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.18f),
                    Color(0xFF81C784).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = glowCenter,
                radius = w * 0.35f
            ),
            radius = w * 0.35f,
            center = glowCenter
        )

        // Shading colors for 3D depth matching the reference image
        val lightSilhouette = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.20f),
                Color(0xFF66BB6A).copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.06f)
            ),
            start = Offset(w * 0.65f, 0f),
            end = Offset(w * 0.85f, h)
        )
        val deepSilhouette = Color.White.copy(alpha = 0.10f)
        val outerMinaretColor = Color.White.copy(alpha = 0.07f)
        val goldFinialColor = AccentGold.copy(alpha = 0.65f)

        // 2. Far Outer Minarets (Layer 1 - Background)
        // Left Far Minaret
        val farLeftM = w * 0.52f
        drawRect(
            color = outerMinaretColor,
            topLeft = Offset(farLeftM, h * 0.32f),
            size = Size(w * 0.016f, h * 0.50f)
        )
        val farLeftCap = Path().apply {
            moveTo(farLeftM - 1f, h * 0.32f)
            lineTo(farLeftM + w * 0.008f, h * 0.23f)
            lineTo(farLeftM + w * 0.016f + 1f, h * 0.32f)
            close()
        }
        drawPath(farLeftCap, outerMinaretColor)

        // Right Far Minaret
        val farRightM = w * 0.91f
        drawRect(
            color = outerMinaretColor,
            topLeft = Offset(farRightM, h * 0.32f),
            size = Size(w * 0.016f, h * 0.50f)
        )
        val farRightCap = Path().apply {
            moveTo(farRightM - 1f, h * 0.32f)
            lineTo(farRightM + w * 0.008f, h * 0.23f)
            lineTo(farRightM + w * 0.016f + 1f, h * 0.32f)
            close()
        }
        drawPath(farRightCap, outerMinaretColor)

        // 3. Middle Side Domes
        // Left Mid Dome
        val leftDomeX = w * 0.63f
        val leftDomeBaseY = h * 0.72f
        val leftDomeR = w * 0.055f
        val leftDomePath = Path().apply {
            moveTo(leftDomeX - leftDomeR, leftDomeBaseY)
            cubicTo(
                leftDomeX - leftDomeR * 0.9f, leftDomeBaseY - leftDomeR * 1.3f,
                leftDomeX, leftDomeBaseY - leftDomeR * 1.6f,
                leftDomeX, leftDomeBaseY - leftDomeR * 1.7f
            )
            cubicTo(
                leftDomeX, leftDomeBaseY - leftDomeR * 1.6f,
                leftDomeX + leftDomeR * 0.9f, leftDomeBaseY - leftDomeR * 1.3f,
                leftDomeX + leftDomeR, leftDomeBaseY
            )
            close()
        }
        drawPath(leftDomePath, deepSilhouette)

        // Right Mid Dome
        val rightDomeX = w * 0.81f
        val rightDomeBaseY = h * 0.72f
        val rightDomeR = w * 0.055f
        val rightDomePath = Path().apply {
            moveTo(rightDomeX - rightDomeR, rightDomeBaseY)
            cubicTo(
                rightDomeX - rightDomeR * 0.9f, rightDomeBaseY - rightDomeR * 1.3f,
                rightDomeX, rightDomeBaseY - rightDomeR * 1.6f,
                rightDomeX, rightDomeBaseY - rightDomeR * 1.7f
            )
            cubicTo(
                rightDomeX, rightDomeBaseY - rightDomeR * 1.6f,
                rightDomeX + rightDomeR * 0.9f, rightDomeBaseY - rightDomeR * 1.3f,
                rightDomeX + rightDomeR, rightDomeBaseY
            )
            close()
        }
        drawPath(rightDomePath, deepSilhouette)

        // 4. Main Grand Dome (Layer 2 - Central 3D Volumetric Dome)
        val domeCenterX = w * 0.72f
        val domeBaseY = h * 0.75f
        val domeRadius = w * 0.095f

        // Dome Base / Drum
        drawRect(
            brush = lightSilhouette,
            topLeft = Offset(domeCenterX - domeRadius * 0.92f, domeBaseY - h * 0.06f),
            size = Size(domeRadius * 1.84f, h * 0.08f)
        )

        // Drum arched window slits
        val windowColor = Color(0xFF1B4D3E).copy(alpha = 0.35f)
        for (i in -2..2) {
            val winX = domeCenterX + i * (domeRadius * 0.32f) - 3f
            drawRoundRect(
                color = windowColor,
                topLeft = Offset(winX, domeBaseY - h * 0.045f),
                size = Size(6f, h * 0.035f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
        }

        // 3D Pointed Grand Dome Path
        val domePath = Path().apply {
            moveTo(domeCenterX - domeRadius, domeBaseY - h * 0.06f)
            cubicTo(
                domeCenterX - domeRadius * 0.95f, domeBaseY - domeRadius * 1.4f,
                domeCenterX - domeRadius * 0.15f, domeBaseY - domeRadius * 1.85f,
                domeCenterX, domeBaseY - domeRadius * 2.05f
            )
            cubicTo(
                domeCenterX + domeRadius * 0.15f, domeBaseY - domeRadius * 1.85f,
                domeCenterX + domeRadius * 0.95f, domeBaseY - domeRadius * 1.4f,
                domeCenterX + domeRadius, domeBaseY - h * 0.06f
            )
            close()
        }
        drawPath(domePath, lightSilhouette)

        // Golden Crescent Finial at Dome Top Peak
        val finialTopY = domeBaseY - domeRadius * 2.05f
        drawLine(
            color = goldFinialColor,
            start = Offset(domeCenterX, finialTopY),
            end = Offset(domeCenterX, finialTopY - h * 0.07f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
        // Crescent on finial
        drawArc(
            color = goldFinialColor,
            startAngle = 130f,
            sweepAngle = 260f,
            useCenter = false,
            topLeft = Offset(domeCenterX - 4.5f, finialTopY - h * 0.08f),
            size = Size(9f, 9f),
            style = Stroke(width = 1.3f)
        )

        // 5. Inner Grand Minarets (Layer 3)
        // Left Main Minaret
        val min1X = w * 0.58f
        drawRect(
            brush = lightSilhouette,
            topLeft = Offset(min1X, h * 0.25f),
            size = Size(w * 0.022f, h * 0.55f)
        )
        // Balcony 1
        drawRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(min1X - 2.5f, h * 0.36f),
            size = Size(w * 0.022f + 5f, 3.5f)
        )
        // Balcony 2
        drawRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(min1X - 2.5f, h * 0.25f),
            size = Size(w * 0.022f + 5f, 3.5f)
        )
        // Conical Roof + Needle
        val min1Roof = Path().apply {
            moveTo(min1X - 1.5f, h * 0.25f)
            lineTo(min1X + w * 0.011f, h * 0.15f)
            lineTo(min1X + w * 0.022f + 1.5f, h * 0.25f)
            close()
        }
        drawPath(min1Roof, lightSilhouette)
        drawLine(
            color = goldFinialColor,
            start = Offset(min1X + w * 0.011f, h * 0.15f),
            end = Offset(min1X + w * 0.011f, h * 0.11f),
            strokeWidth = 1.2f
        )

        // Right Main Minaret
        val min2X = w * 0.86f
        drawRect(
            brush = lightSilhouette,
            topLeft = Offset(min2X, h * 0.25f),
            size = Size(w * 0.022f, h * 0.55f)
        )
        // Balcony 1
        drawRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(min2X - 2.5f, h * 0.36f),
            size = Size(w * 0.022f + 5f, 3.5f)
        )
        // Balcony 2
        drawRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(min2X - 2.5f, h * 0.25f),
            size = Size(w * 0.022f + 5f, 3.5f)
        )
        // Conical Roof + Needle
        val min2Roof = Path().apply {
            moveTo(min2X - 1.5f, h * 0.25f)
            lineTo(min2X + w * 0.011f, h * 0.15f)
            lineTo(min2X + w * 0.022f + 1.5f, h * 0.25f)
            close()
        }
        drawPath(min2Roof, lightSilhouette)
        drawLine(
            color = goldFinialColor,
            start = Offset(min2X + w * 0.011f, h * 0.15f),
            end = Offset(min2X + w * 0.011f, h * 0.11f),
            strokeWidth = 1.2f
        )

        // 6. Base Arcade Arches
        val archBaseY = h * 0.77f
        val archW = w * 0.035f
        for (i in 0..7) {
            val startArchX = w * 0.56f + i * (archW * 1.35f)
            val archPath = Path().apply {
                moveTo(startArchX, h)
                lineTo(startArchX, archBaseY + archW * 0.5f)
                cubicTo(
                    startArchX, archBaseY,
                    startArchX + archW, archBaseY,
                    startArchX + archW, archBaseY + archW * 0.5f
                )
                lineTo(startArchX + archW, h)
                close()
            }
            drawPath(archPath, Color.White.copy(alpha = 0.05f))
        }
    }
}
