package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyPrayerSchedule
import com.example.data.model.DistrictInfo
import com.example.data.model.PrayerName
import com.example.ui.theme.PrimaryGreen
import com.example.utils.PrayerTimesCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesDetailSheet(
    schedule: DailyPrayerSchedule,
    isHanafi: Boolean,
    onDistrictSelected: (DistrictInfo) -> Unit,
    onHanafiChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var showDistrictPicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredDistricts = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            PrayerTimesCalculator.BANGLADESH_DISTRICTS
        } else {
            PrayerTimesCalculator.BANGLADESH_DISTRICTS.filter {
                it.nameBn.contains(searchQuery, ignoreCase = true) ||
                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                it.divisionBn.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "নামাজের সময়সূচি",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = schedule.dateStrBn,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // District selector chip
                Surface(
                    onClick = { showDistrictPicker = true },
                    shape = RoundedCornerShape(100.dp),
                    color = PrimaryGreen.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = schedule.district.nameBn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showDistrictPicker) {
                // District Search & Picker View
                Text(
                    text = "জেলা নির্বাচন করুন (৬৪ জেলা):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("জেলার নাম দিয়ে খুঁজুন...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(filteredDistricts) { district ->
                        val isSelected = district.id == schedule.district.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryGreen.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable {
                                    onDistrictSelected(district)
                                    showDistrictPicker = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = district.nameBn,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${district.divisionBn} বিভাগ • ${district.nameEn}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showDistrictPicker = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("সম্পন্ন", color = Color.White)
                }
            } else {
                // Status banner (Countdown & Current Prayer)
                val statusGradient = Brush.horizontalGradient(
                    colors = listOf(PrimaryGreen, PrimaryGreen.copy(alpha = 0.8f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(statusGradient)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val activeTitle = if (schedule.isForbiddenTimeNow) {
                                schedule.forbiddenTimeReason ?: "মাকরূহ ওয়াক্ত"
                            } else if (schedule.currentPrayer != null) {
                                "${schedule.currentPrayer.name.nameBn}-এর ওয়াক্ত চলছে"
                            } else {
                                "নামাজের প্রস্তুতি"
                            }

                            Text(
                                text = activeTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            val nextInfo = if (schedule.nextPrayer != null) {
                                "${schedule.nextPrayer.name.nameBn}: ${schedule.nextPrayer.timeFormatted} (${schedule.remainingTimeToNextFormatted})"
                            } else {
                                schedule.remainingTimeToNextFormatted
                            }

                            Text(
                                text = nextInfo,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // Icon/Badge
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = schedule.currentPrayer?.name?.icon ?: "🕌",
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5 Prayers Grid/List
                Text(
                    text = "ওয়াক্তসমূহ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(vertical = 4.dp)
                ) {
                    schedule.prayers.forEachIndexed { index, prayer ->
                        val isLast = index == schedule.prayers.size - 1
                        val isHighlighted = prayer.isCurrent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isHighlighted) PrimaryGreen.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(prayer.name.icon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = prayer.name.nameBn,
                                    fontSize = 14.sp,
                                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isHighlighted) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                                )
                                if (isHighlighted) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PrimaryGreen)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "বর্তমান",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Text(
                                text = prayer.timeFormatted,
                                fontSize = 14.sp,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isHighlighted) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (!isLast) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Extra Sunnah & Nofol times (Sahri, Iftar, Tahajjud, Ishraq)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sahri & Iftar Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🌙 সাহরি ও ইফতার", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("সাহরি শেষ: ${schedule.sahriEndTimeFormatted}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("ইফতার: ${schedule.iftarTimeFormatted}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Tahajjud & Ishraq Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("✨ তাহাজ্জুদ ও ইশরাক", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("তাহাজ্জুদ শেষ: ${schedule.tahajjudEndTimeFormatted}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("ইশরাক শুরু: ${schedule.ishraqStartTimeFormatted}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Asr Calculation Method Switch (Hanafi / Shafi'i)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "আসরের ওয়াক্ত পদ্ধতি: ${if (isHanafi) "হানাফী (মিছলে সানি)" else "শাফেয়ী/জমহুর"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick = { onHanafiChanged(!isHanafi) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isHanafi) "পরিবর্তন" else "হানাফী করুন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                }
            }
        }
    }
}
