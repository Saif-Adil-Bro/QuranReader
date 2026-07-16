package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BookmarkEntity
import com.example.data.model.Surah
import com.example.ui.theme.*
import com.example.ui.viewmodels.GamePhase
import com.example.ui.viewmodels.GameSource
import com.example.ui.viewmodels.GameType
import com.example.ui.viewmodels.WordGameConfig
import com.example.ui.viewmodels.SettingsViewModel
import com.example.ui.viewmodels.UserNote
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSurah: (Int) -> Unit = {},
    onNavigateToPage: (Int) -> Unit = {},
    onNavigateToJuz: (Int) -> Unit = {},
    onNavigateToAyah: (Int, Int) -> Unit = { _, _ -> },
    onNavigateToPlayer: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val showTranslation by viewModel.showTranslation.collectAsState()
    val showTransliteration by viewModel.showTransliteration.collectAsState()
    val showTajweed by viewModel.showTajweed.collectAsState()
    val hijriOffset by viewModel.hijriOffset.collectAsState()
    val tanzilTextStyle by viewModel.tanzilTextStyle.collectAsState()
    val username by viewModel.username.collectAsState()
    val readingTime by viewModel.readingTimeMinutes.collectAsState()
    val bookmarkList by viewModel.bookmarks.collectAsState(initial = emptyList())
    
    var activeDialog by remember { mutableStateOf<String?>(null) }
    
    val menuItems = listOf(
        MenuItem("bookmark", "বুকমার্ক", Icons.Default.Bookmark, Color(0xFFEF4444)),
        MenuItem("note", "নোট", Icons.Default.Edit, Color(0xFF0D9488)),
        MenuItem("planner", "কুরআন প্ল্যানার", Icons.Default.DateRange, Color(0xFF10B981)),
        MenuItem("subjectwise", "বিষয়ভিত্তিক কুরআন", Icons.Default.Category, Color(0xFF3B82F6)),
        MenuItem("dua", "কুরআনিক দুআ", Icons.Default.Schedule, Color(0xFF8B5CF6)),
        MenuItem("game", "কুরআনিক ওয়ার্ড গেম", Icons.Default.PlayCircle, Color(0xFFEC4899)),
        MenuItem("player", "কুরআন প্লেয়ার", Icons.Default.MusicNote, Color(0xFF06B6D4)),
        MenuItem("hifz", "কুরআন হিফজ", Icons.Default.CheckCircle, Color(0xFF6366F1)),
        MenuItem("learn", "কুরআন শিক্ষা", Icons.Default.Book, Color(0xFF4F46E5)),
        MenuItem("video", "কুরআন ভিডিও", Icons.Default.Videocam, Color(0xFFEF4444)),
        MenuItem("offline_sync", "অফলাইন ডাউনলোড", Icons.Default.Download, Color(0xFFF59E0B)),
        MenuItem("backup", "ক্লাউড ব্যাকআপ", Icons.Default.Cloud, Color(0xFF6B7280))
    )
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মেনু অপশন",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = GrayText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                HorizontalDivider(color = Border, thickness = 1.dp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, PrimaryGreen.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .clickable { activeDialog = "profile" },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Icon
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(PrimaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Profile Details
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = username,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${bookmarkList.size} বুকমার্ক",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val hoursText = if (readingTime >= 60) {
                                    val hrs = readingTime / 60
                                    val mins = readingTime % 60
                                    if (mins > 0) "$hrs ঘণ্টা $mins মি. পড়া" else "$hrs ঘণ্টা পড়া"
                                } else {
                                    "$readingTime মিনিট পড়া"
                                }
                                Text(
                                    text = hoursText,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Right arrow
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Edit Profile",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // 2. Menu Items Grid (Custom Chunked Layout)
            val chunkedItems = menuItems.chunked(3)
            chunkedItems.forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    if (item.id == "player") {
                                        onNavigateToPlayer()
                                    } else {
                                        activeDialog = item.id
                                    }
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(item.color.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = item.color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    // Symmetrical spaces if chunk contains less than 3 items
                    if (rowItems.size < 3) {
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Border, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            
            // 3. Settings Segment (Backward Compatibility)
            Text(
                text = "অ্যাপ সেটিংস",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Hijri Date Adjustment
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "হিজরি তারিখ সমন্বয়",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "বর্তমান আরবি তারিখ: ${com.example.utils.DateUtil.getTodayHijriDateStr(hijriOffset)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.setHijriOffset(hijriOffset - 1) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.background, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = if (hijriOffset > 0) "+${com.example.utils.DateUtil.toBengaliNumerals(hijriOffset)}" 
                                   else if (hijriOffset < 0) "-${com.example.utils.DateUtil.toBengaliNumerals(-hijriOffset)}" 
                                   else "০",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { viewModel.setHijriOffset(hijriOffset + 1) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.background, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অনুবাদ প্রদর্শন করুন (Show Translation)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "আরবি আয়াতের নিচে বাংলা অনুবাদ প্রদর্শন করুন",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showTranslation,
                        onCheckedChange = { viewModel.toggleTranslation(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "শব্দে শব্দে উচ্চারণ (Word Transliteration)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "প্রতিটি শব্দের নিচে বাংলা উচ্চারণ প্রদর্শন করুন",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showTransliteration,
                        onCheckedChange = { viewModel.setShowTransliteration(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "তাজবীদ কালার (Tajweed Colors)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "আরবি আয়াতে তাজবীদের নিয়ম অনুযায়ী বিভিন্ন রঙ প্রদর্শন করুন",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showTajweed,
                        onCheckedChange = { viewModel.setShowTajweed(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            val availableTafsirs by viewModel.availableTafsirs.collectAsState()
            val selectedTafsirIds by viewModel.selectedTafsirIds.collectAsState()
            val downloadedTafsirIds by viewModel.downloadedTafsirIds.collectAsState()
            val downloadingTafsirIds by viewModel.downloadingTafsirIds.collectAsState()
            val tafsirDownloadProgress by viewModel.tafsirDownloadProgress.collectAsState()
            val selectedQariId by viewModel.selectedQariId.collectAsState()
            var showTafsirDialog by remember { mutableStateOf(false) }
            var showQariDialog by remember { mutableStateOf(false) }

            val qariList = listOf(
                "ar.alafasy" to "Mishary Rashid Alafasy",
                "ar.abdulbasitmurattal" to "Abdul Basit",
                "ar.abdullahbasfar" to "Abdullah Basfar",
                "ar.abdurrahmaansudais" to "Abdurrahmaan As-Sudais",
                "ar.hudhaify" to "Ali Al-Hudhaify",
                "ar.husary" to "Mahmoud Khalil Al-Husary",
                "ar.husarymujawwad" to "Mahmoud Khalil Al-Husary Mujawwad",
                "ar.mahermuaiqly" to "Maher Al Muaiqly",
                "ar.minshawi" to "Mohamed Siddiq al-Minshawi",
                "ar.muhammadayyoub" to "Muhammad Ayyoub",
                "ar.muhammadjibreel" to "Muhammad Jibreel"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Qari Selection
                    Text(
                        text = "ক্বারী নির্বাচন করুন (Qari)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showQariDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkText)
                    ) {
                        val selectedQariName = qariList.find { it.first == selectedQariId }?.second ?: "Mishary Rashid Alafasy"
                        Text(text = selectedQariName, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Qari")
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "তাফসীর নির্বাচন করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "একসাথে সর্বোচ্চ ৩টি তাফসীর নির্বাচন করতে পারবেন",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { showTafsirDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkText)
                    ) {
                        val selectedCount = selectedTafsirIds.size
                        Text(text = "$selectedCount টি তাফসীর নির্বাচিত", modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Tafsir")
                    }
                    
                    if (showQariDialog) {
                        AlertDialog(
                            onDismissRequest = { showQariDialog = false },
                            title = { Text("ক্বারী নির্বাচন করুন") },
                            text = {
                                LazyColumn {
                                    items(qariList) { qari ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setSelectedQariId(qari.first)
                                                    showQariDialog = false
                                                }
                                                .padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selectedQariId == qari.first,
                                                onClick = {
                                                    viewModel.setSelectedQariId(qari.first)
                                                    showQariDialog = false
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = qari.second)
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                androidx.compose.material3.TextButton(onClick = { showQariDialog = false }) {
                                    Text("বন্ধ করুন")
                                }
                            }
                        )
                    }

                    if (showTafsirDialog) {
                        AlertDialog(
                            onDismissRequest = { showTafsirDialog = false },
                            title = {
                                Text("তাফসীর নির্বাচন করুন")
                            },
                            text = {
                                LazyColumn {
                                    items(availableTafsirs) { tafsir ->
                                        val tafsirId = tafsir.id.toString()
                                        val isSelected = selectedTafsirIds.contains(tafsirId)
                                        val isDownloaded = downloadedTafsirIds.contains(tafsirId)
                                        val isDownloading = downloadingTafsirIds.contains(tafsirId)
                                        val progress = tafsirDownloadProgress[tafsirId] ?: 0f

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.toggleTafsir(tafsirId) }
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isDownloaded) {
                                                androidx.compose.material3.Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = { viewModel.toggleTafsir(tafsirId) },
                                                    colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                                                )
                                            } else {
                                                androidx.compose.material3.RadioButton(
                                                    selected = isSelected,
                                                    onClick = { viewModel.toggleTafsir(tafsirId) },
                                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = tafsir.name ?: "Unknown", 
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.weight(1f),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = tafsir.languageName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                                            fontSize = 10.sp,
                                                            color = PrimaryGreen,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Text(text = tafsir.authorName ?: "Unknown", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            if (!isDownloaded) {
                                                if (isDownloading) {
                                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp).padding(2.dp)) {
                                                        androidx.compose.material3.CircularProgressIndicator(
                                                            progress = progress,
                                                            color = PrimaryGreen,
                                                            strokeWidth = 2.dp
                                                        )
                                                    }
                                                } else {
                                                    IconButton(
                                                        onClick = { viewModel.downloadTafsir(tafsirId) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            androidx.compose.material.icons.Icons.Default.Share,
                                                            contentDescription = "Download Tafsir",
                                                            tint = PrimaryGreen
                                                        )
                                                    }
                                                }
                                            } else {
                                                Icon(
                                                    androidx.compose.material.icons.Icons.Default.CheckCircle,
                                                    contentDescription = "Downloaded",
                                                    tint = PrimaryGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (availableTafsirs.isEmpty()) {
                                        item {
                                            Text("তাফসীর লোড হচ্ছে...", modifier = Modifier.padding(16.dp))
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                androidx.compose.material3.TextButton(onClick = { showTafsirDialog = false }) {
                                    Text("বন্ধ করুন", color = PrimaryGreen)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "তানজিল কুরআন স্ক্রিপ্ট স্টাইল",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "পঠন মোড ও সার্চের জন্য তানজিল.নেট স্ক্রিপ্ট অপশন নির্বাচন করুন",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val scriptOptions = listOf(
                        Pair("quran-uthmani", "উসমানী স্ক্রিপ্ট (Uthmani)"),
                        Pair("quran-simple", "সহজ স্ক্রিপ্ট (Simple)"),
                        Pair("quran-simple-clean", "হরকত ছাড়া ক্লিন (Simple Clean)"),
                        Pair("quran-simple-plain", "প্লেইন স্ক্রিপ্ট (Simple Plain)")
                    )

                    scriptOptions.forEach { (styleId, styleName) ->
                        val isSelected = tanzilTextStyle == styleId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryGreen.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { viewModel.setTanzilTextStyle(styleId) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = styleName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setTanzilTextStyle(styleId) },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // --- DIALOGS AND BOTTOM SHEETS ---
    if (activeDialog != null) {
        MenuDetailDialog(
            type = activeDialog!!,
            viewModel = viewModel,
            onDismiss = { activeDialog = null },
            onNavigateToSurah = onNavigateToSurah,
            onNavigateToPage = onNavigateToPage,
            onNavigateToJuz = onNavigateToJuz,
            onNavigateToAyah = onNavigateToAyah
        )
    }
}

@Composable
fun MenuDetailDialog(
    type: String,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit,
    onNavigateToSurah: (Int) -> Unit = {},
    onNavigateToPage: (Int) -> Unit = {},
    onNavigateToJuz: (Int) -> Unit = {},
    onNavigateToAyah: (Int, Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Header
                val title = when (type) {
                    "profile" -> "আমার প্রোফাইল"
                    "bookmark" -> "বুকমার্ক তালিকা"
                    "note" -> "আমার নোটপ্যাড"
                    "planner" -> "কুরআন প্ল্যানার"
                    "subjectwise" -> "বিষয়ভিত্তিক কুরআন"
                    "dua" -> "কুরআনিক দুআ"
                    "game" -> "কুরআনিক ওয়ার্ড গেম"
                    "player" -> "কুরআন অডিও প্লেয়ার"
                    "hifz" -> "হিফজ ট্র্যাকার"
                    "learn" -> "কুরআন শিক্ষা"
                    "video" -> "ভিডিও ক্লাস"
                    "offline_sync" -> "কুরআন অফলাইন ডাউনলোড"
                    "backup" -> "ক্লাউড ব্যাকআপ"
                    else -> "বিস্তারিত"
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.size(48.dp)) // Symmetrical spacer
                }
                
                HorizontalDivider(color = Border)
                
                // Dialog Content Body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (type) {
                        "profile" -> ProfileDialogContent(viewModel)
                        "bookmark" -> BookmarkDialogContent(
                            viewModel = viewModel,
                            onBookmarkClick = { bookmark ->
                                onDismiss()
                                when (bookmark.type) {
                                    "SURAH" -> onNavigateToSurah(bookmark.referenceId)
                                    "PAGE" -> onNavigateToPage(bookmark.referenceId)
                                    "JUZ" -> onNavigateToJuz(bookmark.referenceId)
                                    "AYAH" -> {
                                        val (surahNum, ayahNum) = com.example.data.QuranData.getSurahAndAyahFromGlobal(bookmark.referenceId)
                                        onNavigateToAyah(surahNum, ayahNum)
                                    }
                                }
                            }
                        )
                        "note" -> NotepadDialogContent(viewModel)
                        "planner" -> PlannerDialogContent(viewModel)
                        "subjectwise" -> SubjectwiseDialogContent()
                        "dua" -> DuaDialogContent()
                        "game" -> GameDialogContent(viewModel)
                        "player" -> PlayerDialogContent()
                        "hifz" -> HifzDialogContent(viewModel)
                        "learn" -> LearnDialogContent()
                        "video" -> VideoDialogContent()
                        "offline_sync" -> OfflineSyncDialogContent(viewModel)
                        "backup" -> BackupDialogContent()
                    }
                }
            }
        }
    }
}

// --- 1. PROFILE DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDialogContent(viewModel: SettingsViewModel) {
    val username by viewModel.username.collectAsState()
    val readingMins by viewModel.readingTimeMinutes.collectAsState()
    var tempName by remember { mutableStateOf(username) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(PrimaryGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "প্রোফাইল পরিবর্তন করুন",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = tempName,
            onValueChange = { tempName = it },
            label = { Text("ব্যবহারকারীর নাম") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                focusedLabelColor = PrimaryGreen
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = { viewModel.updateUsername(tempName) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("নাম পরিবর্তন করুন", color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Border)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "পড়ার সময় বৃদ্ধি করুন (সিমুলেটর)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.addReadingTime(15) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                modifier = Modifier.weight(1f)
            ) {
                Text("+১৫ মিনিট", color = Color.White, fontSize = 12.sp)
            }
            Button(
                onClick = { viewModel.addReadingTime(30) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1f)
            ) {
                Text("+৩০ মিনিট", color = Color.White, fontSize = 12.sp)
            }
            Button(
                onClick = { viewModel.addReadingTime(60) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                modifier = Modifier.weight(1f)
            ) {
                Text("+১ ঘণ্টা", color = Color.White, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("মোট অধ্যয়নকাল", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (readingMins >= 60) "${readingMins / 60} ঘণ্টা ${readingMins % 60} মিনিট" else "$readingMins মিনিট",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }
    }
}

// --- 2. BOOKMARK DIALOG ---
@Composable
fun BookmarkDialogContent(
    viewModel: SettingsViewModel,
    onBookmarkClick: (BookmarkEntity) -> Unit = {}
) {
    val bookmarks by viewModel.bookmarks.collectAsState(initial = emptyList())
    
    if (bookmarks.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = GrayText.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "কোনো বুকমার্ক পাওয়া যায়নি!",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "সুরা বা পৃষ্ঠা পড়ার সময় উপরে বুকমার্ক বাটনে ক্লিক করুন।",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bookmarks) { bookmark ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookmarkClick(bookmark) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bookmark.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val displayType = when (bookmark.type) {
                                "SURAH" -> "সুরা"
                                "PAGE" -> "পৃষ্ঠা"
                                "JUZ" -> "পারা"
                                "AYAH" -> "আয়াত"
                                else -> bookmark.type
                            }
                            Text(
                                text = "প্রকার: $displayType • আইডি: ${bookmark.referenceId}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.removeBookmark(bookmark) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

// --- 3. NOTEPAD DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadDialogContent(viewModel: SettingsViewModel) {
    val notes by viewModel.notes.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Add Note Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("নতুন নোট লিখুন", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("নোটের শিরোনাম") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("এখানে বিস্তারিত লিখুন...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            viewModel.addNote(title, content)
                            title = ""
                            content = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নোট যুক্ত করুন", color = Color.White)
                }
            }
        }
        
        // Notes List
        Text(
            text = "নোটের তালিকা (${notes.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("কোনো নোট পাওয়া যায়নি!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(note.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                IconButton(onClick = { viewModel.deleteNote(note.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(note.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(6.dp))
                            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            Text(
                                text = sdf.format(Date(note.timestamp)),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 4. PLANNER DIALOG ---
@Composable
fun PlannerDialogContent(viewModel: SettingsViewModel) {
    val target by viewModel.plannerTarget.collectAsState()
    val pagesRead by viewModel.plannerPagesRead.collectAsState()
    val startDate by viewModel.plannerStartDate.collectAsState()
    val streak by viewModel.plannerStreak.collectAsState()
    val reminderEnabled by viewModel.plannerReminderEnabled.collectAsState()

    val khatamPlans = listOf(
        Pair("৩০ দিনে খতম", 30),
        Pair("৬০ দিনে খতম", 60),
        Pair("৯০ দিনে খতম", 90),
        Pair("৬ মাসে খতম", 180),
        Pair("১ বছরে খতম", 365)
    )
    
    val selectedPlan = khatamPlans.find { it.first == target } ?: khatamPlans.first()
    val totalDays = selectedPlan.second
    val passedDays = maxOf(0, ((System.currentTimeMillis() - startDate) / (1000 * 60 * 60 * 24)).toInt())
    val remainingDays = maxOf(1, totalDays - passedDays)
    val remainingPages = maxOf(0, 604 - pagesRead)
    
    // Dynamic daily target adjustment
    val dailyTargetPages = maxOf(1, kotlin.math.ceil(remainingPages.toDouble() / remainingDays).toInt())
    
    val progressPercentage = pagesRead.toFloat() / 604f

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        
        // --- 1. Dynamic Progress & Streak Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Circular Progress
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(72.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                            strokeWidth = 6.dp,
                        )
                        CircularProgressIndicator(
                            progress = { progressPercentage },
                            modifier = Modifier.size(72.dp),
                            color = PrimaryGreen,
                            strokeWidth = 6.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(progressPercentage * 100).toInt()}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(selectedPlan.first, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("মোট পড়া: $pagesRead / ৬০৪ পৃষ্ঠা", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("টানা পড়া: $streak দিন (Streak)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- 2. Dynamic Daily Target ---
        Text("আজকের লক্ষ্য", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$dailyTargetPages", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("পৃষ্ঠা পড়তে হবে", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = "বাকি দিন: $remainingDays | বাকি পৃষ্ঠা: $remainingPages",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = { viewModel.addPlannerPages(dailyTargetPages) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("সম্পন্ন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- 3. Milestones & Badges ---
        Text("মাইলফলক ও ব্যাজ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        
        val totalJuz = 30
        val completedJuz = pagesRead / 20 // Approx 20 pages per juz
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(10) { i ->
                val juzTarget = (i + 1) * 3
                val isUnlocked = completedJuz >= juzTarget
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isUnlocked) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${juzTarget} পারা", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // --- 4. Select Plan ---
        Text("লক্ষ্য পরিবর্তন করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(khatamPlans) { plan ->
                val isSel = target == plan.first
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) PrimaryGreen else MaterialTheme.colorScheme.surface)
                        .border(1.dp, if (isSel) PrimaryGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.updatePlannerTarget(plan.first) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(plan.first, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- 5. Smart Reminder ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("স্মার্ট রিমাইন্ডার", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("পড়ার সময় মনে করিয়ে দিতে নোটিফিকেশন", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = reminderEnabled,
                onCheckedChange = { viewModel.togglePlannerReminder(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- 5. SUBJECTWISE DIALOG ---
@Composable
fun SubjectwiseDialogContent() {
    val topics = listOf(
        Pair("ঈমান ও বিশ্বাস", "সূরা আল-মুমিনুন: ১-২ • \"নিশ্চয়ই মুমিনরা সফলকাম হয়েছে, যারা নিজেদের নামাজে নম্র ও বিনয়ী...\""),
        Pair("সালাত ও ইবাদত", "সূরা আল-আনকাবুত: ৪৫ • \"নিশ্চয়ই নামাজ মানুষকে অশ্লীল ও মন্দ কাজ থেকে বিরত রাখে...\""),
        Pair("সবর ও ধৈর্য", "সূরা আল-বাকারা: ১৫৩ • \"হে মুমিনগণ! তোমরা ধৈর্য ও সালাতের মাধ্যমে সাহায্য প্রার্থনা করো। নিশ্চয়ই আল্লাহ ধৈর্যশীলদের সাথে আছেন।\""),
        Pair("নৈতিকতা ও চরিত্র", "সূরা আল-বাকারা: ৮৩ • \"তোমরা মানুষের সাথে উত্তম ও নম্রভাবে কথা বলো এবং সালাত কায়েম করো...\"")
    )
    
    var expandedTopic by remember { mutableStateOf<String?>(null) }
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        items(topics) { (title, verse) ->
            val isExp = expandedTopic == title
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedTopic = if (isExp) null else title },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Icon(
                            imageVector = if (isExp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = PrimaryGreen
                        )
                    }
                    if (isExp) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(verse, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// --- 6. DUA DIALOG ---
@Composable
fun DuaDialogContent() {
    val duas = com.example.data.DuaData.dailyDuas
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        items(duas) { (title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(desc.replace("\n", " • "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                }
            }
        }
    }
}

// --- 7. WORD GAME DIALOG ---
@Composable
fun GameDialogContent(viewModel: SettingsViewModel) {
    val phase by viewModel.gamePhase.collectAsState()
    
    when (phase) {
        com.example.ui.viewmodels.GamePhase.SETUP -> GameSetupScreen(viewModel)
        com.example.ui.viewmodels.GamePhase.LOADING -> GameLoadingScreen()
        com.example.ui.viewmodels.GamePhase.PLAYING -> GamePlayingScreen(viewModel)
        com.example.ui.viewmodels.GamePhase.RESULT -> GameResultScreen(viewModel)
    }
}

@Composable
fun GameSetupScreen(viewModel: SettingsViewModel) {
    val config by viewModel.gameConfig.collectAsState()
    val surahs = com.example.data.surahInfoList
    var isSurahDropdownExpanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Text("১. কিসের উপর গেম খেলতে চান?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f).clickable { viewModel.updateGameConfig(config.copy(source = com.example.ui.viewmodels.GameSource.ENTIRE_QURAN)) },
                colors = CardDefaults.cardColors(containerColor = if (config.source == com.example.ui.viewmodels.GameSource.ENTIRE_QURAN) Color(0xFFFDE6B0) else Color.White),
                border = BorderStroke(1.dp, if (config.source == com.example.ui.viewmodels.GameSource.ENTIRE_QURAN) PrimaryGreen else Border)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(androidx.compose.material.icons.Icons.Default.MenuBook, contentDescription = null, tint = if (config.source == com.example.ui.viewmodels.GameSource.ENTIRE_QURAN) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("সম্পূর্ণ কুরআন", fontSize = 13.sp, fontWeight = if (config.source == com.example.ui.viewmodels.GameSource.ENTIRE_QURAN) FontWeight.Bold else FontWeight.Medium, color = if (config.source == com.example.ui.viewmodels.GameSource.ENTIRE_QURAN) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
                }
            }
            Card(
                modifier = Modifier.weight(1f).clickable { viewModel.updateGameConfig(config.copy(source = com.example.ui.viewmodels.GameSource.SPECIFIC_SURAH)) },
                colors = CardDefaults.cardColors(containerColor = if (config.source == com.example.ui.viewmodels.GameSource.SPECIFIC_SURAH) Color(0xFFFDE6B0) else Color.White),
                border = BorderStroke(1.dp, if (config.source == com.example.ui.viewmodels.GameSource.SPECIFIC_SURAH) PrimaryGreen else Border)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(androidx.compose.material.icons.Icons.Default.Description, contentDescription = null, tint = if (config.source == com.example.ui.viewmodels.GameSource.SPECIFIC_SURAH) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("নির্দিষ্ট সূরা", fontSize = 13.sp, fontWeight = if (config.source == com.example.ui.viewmodels.GameSource.SPECIFIC_SURAH) FontWeight.Bold else FontWeight.Medium, color = if (config.source == com.example.ui.viewmodels.GameSource.SPECIFIC_SURAH) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        if (config.source == com.example.ui.viewmodels.GameSource.SPECIFIC_SURAH) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().clickable { isSurahDropdownExpanded = true },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Border)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val selectedName = surahs.find { it.first == config.selectedSurah }?.second?.arabicName ?: "সূরা নির্বাচন করুন"
                    Text("নির্বাচিত সূরা: $selectedName", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Icon(androidx.compose.material.icons.Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            androidx.compose.material3.DropdownMenu(
                expanded = isSurahDropdownExpanded,
                onDismissRequest = { isSurahDropdownExpanded = false }
            ) {
                surahs.forEach { surahInfo ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("${surahInfo.first}. ${surahInfo.second.arabicName}") },
                        onClick = { 
                            viewModel.updateGameConfig(config.copy(selectedSurah = surahInfo.first))
                            isSurahDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("২. গেমের ধরণ নির্ধারণ করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f).clickable { viewModel.updateGameConfig(config.copy(type = com.example.ui.viewmodels.GameType.ARABIC_TO_BENGALI)) },
                colors = CardDefaults.cardColors(containerColor = if (config.type == com.example.ui.viewmodels.GameType.ARABIC_TO_BENGALI) Color(0xFFD1FAF5) else Color.White),
                border = BorderStroke(1.dp, if (config.type == com.example.ui.viewmodels.GameType.ARABIC_TO_BENGALI) PrimaryGreen else Border)
            ) {
                Text("আরবি -> বাংলা", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = if (config.type == com.example.ui.viewmodels.GameType.ARABIC_TO_BENGALI) FontWeight.Bold else FontWeight.Medium, color = if (config.type == com.example.ui.viewmodels.GameType.ARABIC_TO_BENGALI) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
            }
            Card(
                modifier = Modifier.weight(1f).clickable { viewModel.updateGameConfig(config.copy(type = com.example.ui.viewmodels.GameType.BENGALI_TO_ARABIC)) },
                colors = CardDefaults.cardColors(containerColor = if (config.type == com.example.ui.viewmodels.GameType.BENGALI_TO_ARABIC) Color(0xFFD1FAF5) else Color.White),
                border = BorderStroke(1.dp, if (config.type == com.example.ui.viewmodels.GameType.BENGALI_TO_ARABIC) PrimaryGreen else Border)
            ) {
                Text("বাংলা -> আরবি", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = if (config.type == com.example.ui.viewmodels.GameType.BENGALI_TO_ARABIC) FontWeight.Bold else FontWeight.Medium, color = if (config.type == com.example.ui.viewmodels.GameType.BENGALI_TO_ARABIC) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("৩. মোট কতটি প্রশ্ন?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 20, 30).forEach { count ->
                Card(
                    modifier = Modifier.weight(1f).clickable { viewModel.updateGameConfig(config.copy(totalQuestions = count)) },
                    colors = CardDefaults.cardColors(containerColor = if (config.totalQuestions == count) Color(0xFFFDE6B0) else Color.White),
                    border = BorderStroke(1.dp, if (config.totalQuestions == count) PrimaryGreen else Border)
                ) {
                    Text("$count টি", modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = if (config.totalQuestions == count) FontWeight.Bold else FontWeight.Medium, color = if (config.totalQuestions == count) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.startDynamicGame() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)) // Orange like in image
        ) {
            Text("গেম শুরু করুন", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(androidx.compose.material.icons.Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun GameLoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PrimaryGreen)
            Spacer(modifier = Modifier.height(16.dp))
            Text("কুরআন থেকে শব্দ সংগ্রহ করা হচ্ছে...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GamePlayingScreen(viewModel: SettingsViewModel) {
    val score by viewModel.gameScore.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val lastCorrect by viewModel.lastAnswerCorrect.collectAsState()
    val questions by viewModel.dynamicQuestions.collectAsState()
    
    if (questions.isEmpty()) return
    
    val question = questions[currentIndex]
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("স্কোর: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryGreen)
            Text("প্রশ্ন: ${currentIndex + 1}/${questions.size}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Question Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("নিচের শব্দটির সঠিক অর্থ নির্বাচন করুন:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(question.question, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Options List
        question.options.forEach { opt ->
            val isCorrectOpt = opt == question.correctAnswer
            val isSelected = lastCorrect != null // once answered, show correct/wrong
            
            val borderCol = when {
                isSelected && isCorrectOpt -> Color(0xFF10B981)
                lastCorrect == false && !isCorrectOpt -> Border
                else -> Border
            }
            val bgCol = when {
                isSelected && isCorrectOpt -> Color(0xFFD1FAF5)
                else -> Color.White
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable(enabled = lastCorrect == null) { viewModel.submitAnswer(opt) },
                colors = CardDefaults.cardColors(containerColor = bgCol),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(opt, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (isSelected && isCorrectOpt) {
                        Icon(androidx.compose.material.icons.Icons.Default.Check, contentDescription = null, tint = PrimaryGreen)
                    }
                }
            }
        }
        
        if (lastCorrect != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.nextQuestion() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(if (currentIndex == questions.size - 1) "ফলাফল দেখুন" else "পরবর্তী প্রশ্ন", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun GameResultScreen(viewModel: SettingsViewModel) {
    val score by viewModel.gameScore.collectAsState()
    val total = viewModel.dynamicQuestions.value.size
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val comment = when {
        score == total -> "মাশাআল্লাহ! অসাধারণ!"
        score >= total * 0.8 -> "আলহামদুলিল্লাহ! খুব ভালো!"
        score >= total * 0.5 -> "ভালো চেষ্টা, আরো চর্চা করুন!"
        else -> "ইনশাআল্লাহ! পরবর্তীতে আরো ভালো হবে।"
    }
    
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("কুরআন শব্দ গেইম", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PrimaryGreen)
                Text("quranbn.com", fontSize = 12.sp, color = PrimaryGreen.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha=0.2f))
                ) {
                   Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                       Text("আপনার স্কোর", fontSize = 16.sp, color = PrimaryGreen)
                       Spacer(modifier = Modifier.height(8.dp))
                       Row(verticalAlignment = Alignment.Bottom) {
                           Text("$score", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                           Text("/$total", fontSize = 24.sp, color = PrimaryGreen, modifier = Modifier.padding(bottom = 6.dp))
                       }
                       Spacer(modifier = Modifier.height(16.dp))
                       Text(comment, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryGreen)
                   }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* Share link */ },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                ) {
                    Text("আপনিও খেলুন: quranbn.com/game", fontSize = 12.sp, color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { 
                android.widget.Toast.makeText(context, "ফলাফল কার্ড ডাউনলোড শুরু হয়েছে", android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ফলাফল কার্ড ডাউনলোড করুন", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.resetGame() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
            border = BorderStroke(1.dp, PrimaryGreen)
        ) {
            Text("পুনরায় খেলুন", fontWeight = FontWeight.Bold)
        }
    }
}

// --- 8. AUDIO PLAYER DIALOG ---
@Composable
fun PlayerDialogContent() {
    var isPlaying by remember { mutableStateOf(false) }
    var currentReciter by remember { mutableStateOf("মিশারি রাশিদ আল-আফাসি") }
    var speed by remember { mutableStateOf(1f) }
    var sliderVal by remember { mutableStateOf(0.3f) }
    
    val reciters = listOf("মিশারি রাশিদ আল-আফাসি", "আব্দুল বাসিত আব্দুস সামাদ", "মাহের আল-মুআইকিলী")
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ক্বারী বা তেলাওয়াতকারী নির্বাচন করুন", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        reciters.forEach { r ->
            val isSel = currentReciter == r
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) PrimaryGreen.copy(alpha = 0.1f) else Color.White)
                    .border(1.dp, if (isSel) PrimaryGreen else Border, RoundedCornerShape(8.dp))
                    .clickable { currentReciter = r }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(r, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (isSel) PrimaryGreen else DarkText)
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Player Controller Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("সুরা আল-ফাতিহা", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(currentReciter, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Slider
                Slider(
                    value = sliderVal,
                    onValueChange = { sliderVal = it },
                    colors = SliderDefaults.colors(thumbColor = PrimaryGreen, activeTrackColor = PrimaryGreen)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0:45", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("2:30", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = DarkText, modifier = Modifier.size(32.dp))
                    }
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(56.dp)
                            .background(PrimaryGreen, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, tint = DarkText, modifier = Modifier.size(32.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("গতি: ${String.format("%.1fx", speed)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = speed,
                        onValueChange = { speed = it },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.width(100.dp),
                        colors = SliderDefaults.colors(thumbColor = PrimaryGreen, activeTrackColor = PrimaryGreen)
                    )
                }
            }
        }
    }
}

// --- 9. HIFZ DIALOG ---
@Composable
fun HifzDialogContent(viewModel: SettingsViewModel) {
    val hifzProgress by viewModel.hifzProgress.collectAsState()
    
    val surahs = listOf(
        "সুরা আল-ফাতিহা", "সুরা আন-নাস", "সুরা আল-ফালাক", "সুরা আল-ইখলাস",
        "সুরা আল-লাহাব", "সুরা আন-নসর", "সুরা আল-কাফিরুন", "সুরা আল-কাওসার"
    )
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(surahs) { surah ->
            val status = hifzProgress[surah] ?: "শুরু করা হয়নি"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(surah, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("চলছে", "হিফজ").forEach { label ->
                            val activeLabel = if (label == "হিফজ") "হিফজ করা হয়েছে" else "চলছে"
                            val active = status == activeLabel
                            val col = if (label == "হিফজ") Color(0xFF10B981) else Color(0xFFFBBF24)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) col else OffWhite)
                                    .border(1.dp, if (active) col else Border, RoundedCornerShape(6.dp))
                                    .clickable {
                                        val newStatus = if (active) "শুরু করা হয়নি" else activeLabel
                                        viewModel.updateHifzProgress(surah, newStatus)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(label, fontSize = 11.sp, color = if (active) Color.White else GrayText, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 10. LEARN DIALOG ---
@Composable
fun LearnDialogContent() {
    val lessons = listOf(
        Pair("পাঠ ১: আরবী হরফ পরিচিতি", "আরবী ভাষার হরফ বা বর্ণ মোট ২৯টি। এগুলো ডানদিক থেকে বামদিকে পড়তে হয়। যেমন: আলিফ (ا), বা (ب), তা (ت), ছা (ث), জীম (ج), হা (ح), খা (خ)..."),
        Pair("পাঠ ২: হরকত শিক্ষা", "জের ( ِ ), জবর ( َ ), পেশ ( ُ ) কে হরকত বলা হয়। এক জবর, এক জের ও এক পেশের উচ্চারণ তাড়াতাড়ি করতে হয়। যেমন: আ, ই, উ।"),
        Pair("পাঠ ৩: তানভীন পরিচয়", "দুই জবর, দুই জের ও দুই পেশকে তানভীন বলা হয়। তানভীনের উচ্চারণে শেষে 'ন' ধ্বনি আসে। যেমন: আন, ইন, উন।"),
        Pair("পাঠ ৪: মাখরাজ ও উচ্চারণস্থল", "আরবী হরফ উচ্চারণের মোট ১৭টি সুনির্দিষ্ট স্থান রয়েছে, একে মাখরাজ বলে। যেমন: ১ নং মাখরাজ- হলকের (কণ্ঠনালীর) শুরু হইতে হামযাহ ও হা উচ্চারিত হয়।")
    )
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        items(lessons) { (title, content) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                }
            }
        }
    }
}

// --- 11. VIDEO CLASSES DIALOG ---
@Composable
fun VideoDialogContent() {
    val classes = listOf(
        Pair("তাজবিদ পাঠ ১: আরবী উচ্চারণের নিয়মাবলী", "১০:১৫ মিনিট • ট্রেইনার: হাফেজ মাওলানা আব্দুর রহমান"),
        Pair("তাজবিদ পাঠ ২: সহজ উপায়ে মাখরাজ শিক্ষা", "১২:৪০ মিনিট • ট্রেইনার: হাফেজ মাওলানা আব্দুর রহমান"),
        Pair("তাফসির: সুরা ফাতিহার তাফসির ও বিশ্লেষণ", "২৫:৩০ মিনিট • তাফসিরকারী: ড. আবু বকর মুহাম্মাদ যাকারিয়া"),
        Pair("কুরআন তিলাওয়াত শুদ্ধিকরণ কর্মশালা", "১৮:৪৫ মিনিট • তেলাওয়াতকারী: ক্বারী আশরাফ আলী")
    )
    
    val context = LocalContext.current
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        items(classes) { (title, subtitle) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp, 50.dp)
                            .background(Color.LightGray, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    IconButton(onClick = { Toast.makeText(context, "ভিডিও লোড হচ্ছে...", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = PrimaryGreen)
                    }
                }
            }
        }
    }
}

// --- 12. CLOUD BACKUP DIALOG ---
@Composable
fun BackupDialogContent() {
    var isBackingUp by remember { mutableStateOf(false) }
    var lastBackupTime by remember { mutableStateOf("আজ সকাল ১০:৩০") }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Cloud, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("ক্লাউড ব্যাকআপ অ্যান্ড রিস্টোর", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text("আপনার বুকমার্ক ও নোট সুরক্ষিত রাখতে ব্যাকআপ নিন।", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("সর্বশেষ ব্যাকআপের সময়:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(lastBackupTime, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isBackingUp) {
            CircularProgressIndicator(color = PrimaryGreen)
            Spacer(modifier = Modifier.height(8.dp))
            Text("সার্ভারে ডাটা পাঠানো হচ্ছে...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Button(
                onClick = {
                    isBackingUp = true
                    scope.launch {
                        delay(2500) // Simulate cloud delay
                        isBackingUp = false
                        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        lastBackupTime = sdf.format(Date())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ব্যাকআপ নিন", color = Color.White)
            }
        }
    }
}

// --- OFFLINE SYNC DIALOG ---
@Composable
fun OfflineSyncDialogContent(viewModel: SettingsViewModel) {
    val isDownloading by viewModel.isDownloadingQuran.collectAsState()
    val progress by viewModel.quranDownloadProgress.collectAsState()
    val error by viewModel.quranDownloadError.collectAsState()
    val downloadedCount by viewModel.downloadedSurahsCount.collectAsState()
    val audioCacheSize by viewModel.audioCacheSize.collectAsState()

    // Audio Manual Download States
    val surahList by viewModel.surahList.collectAsState()
    val isDownloadingAudio by viewModel.isDownloadingAudio.collectAsState()
    val audioDownloadProgress by viewModel.audioDownloadProgress.collectAsState()
    val audioDownloadStatus by viewModel.audioDownloadStatus.collectAsState()
    val audioDownloadError by viewModel.audioDownloadError.collectAsState()

    var showSurahSelectorSheet by remember { mutableStateOf(false) }

    // Refresh states
    LaunchedEffect(Unit) {
        viewModel.updateDownloadedSurahsCount()
        viewModel.updateAudioCacheSize()
        viewModel.loadSurahList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CloudQueue,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "কুরআন অফলাইন ডাউনলোড ও ক্যাশ",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "আপনার কুরআন রিডিং ডাটা এবং অডিও অফলাইন ব্যবহারের জন্য ডাউনলোড করে রাখুন যাতে ইন্টারনেট না থাকলেও পড়তে ও শুনতে পারেন।",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Quran Texts Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "কুরআন রিডিং ডাটা (সুরা ও অর্থ)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "১১৪টি সুরার আরবি ও বাংলা অনুবাদ ডাটা",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Download Status UI
                val statusText: String
                val statusColor: Color
                val statusIcon: ImageVector

                if (downloadedCount == 114) {
                    statusText = "সম্পূর্ণ ডাউনলোড করা হয়েছে (১১৪টি সুরা)"
                    statusColor = PrimaryGreen
                    statusIcon = Icons.Default.CheckCircle
                } else if (downloadedCount > 0) {
                    statusText = "আংশিক ডাউনলোড হয়েছে ($downloadedCount/১১৪ সুরা)"
                    statusColor = Color(0xFFF59E0B)
                    statusIcon = Icons.Default.Warning
                } else {
                    statusText = "কোনো অফলাইন ডাটা নেই"
                    statusColor = Color.Red
                    statusIcon = Icons.Default.Info
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(statusColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সুরা ডাউনলোড হচ্ছে...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$progress / 114",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val progressPct = progress.toFloat() / 114f
                        LinearProgressIndicator(
                            progress = { progressPct },
                            modifier = Modifier.fillMaxWidth(),
                            color = PrimaryGreen,
                            trackColor = Border
                        )
                    }
                }

                error?.let { err ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ত্রুটি: $err",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isDownloading) {
                        Button(
                            onClick = { viewModel.stopQuranDownload() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ডাউনলোড বন্ধ করুন", color = Color.White, fontSize = 12.sp, maxLines = 1)
                        }
                    } else {
                        if (downloadedCount < 114) {
                            Button(
                                onClick = { viewModel.downloadAllQuranData() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ডাউনলোড শুরু করুন", color = Color.White, fontSize = 12.sp, maxLines = 1)
                            }
                        }

                        if (downloadedCount > 0) {
                            OutlinedButton(
                                onClick = { viewModel.deleteDownloadedQuranData() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("মুছে ফেলুন", fontSize = 12.sp, color = Color.Red, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Audio Cache Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF06B6D4).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "অডিও প্লেব্যাক অফলাইন ক্যাশ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "প্লে হওয়া আয়াতে অফলাইন ফাইল সংরক্ষণ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ব্যবহৃত ক্যাশ মেমোরি:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val formattedSize = formatBytesLocal(audioCacheSize)
                        Text(
                            text = formattedSize,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF06B6D4)
                        )
                    }

                    if (audioCacheSize > 0) {
                        OutlinedButton(
                            onClick = { viewModel.clearAudioCache() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ক্যাশ মুছুন", fontSize = 12.sp, color = Color.Red)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Border)
                Spacer(modifier = Modifier.height(16.dp))

                // Manual Audio Download Progress & Status
                if (isDownloadingAudio) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF06B6D4).copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = audioDownloadStatus ?: "অডিও ফাইল ডাউনলোড করা হচ্ছে...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "অগ্রগতি:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$audioDownloadProgress%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF06B6D4)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { audioDownloadProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF06B6D4),
                            trackColor = Border
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.cancelAudioDownload() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ডাউনলোড বাতিল করুন", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    // Show last status/result if downloaded successfully
                    audioDownloadStatus?.let { status ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PrimaryGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = status,
                                color = PrimaryGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Show error if failed
                    audioDownloadError?.let { err ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Red.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "ত্রুটি: $err",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Button to manually select and download surah audio
                    Button(
                        onClick = { showSurahSelectorSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ম্যানুয়ালি সুরা অডিও ডাউনলোড করুন", fontSize = 12.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "নিয়ম: অ্যাপে যেকোনো সুরা বা আয়াত শোনার সময় সেটি স্বয়ংক্রিয়ভাবে ব্যাকগ্রাউন্ডে ক্যাশ হয়ে যাবে। তবে আপনি চাইলে উপরোক্ত বাটন ব্যবহার করে যেকোনো সুরার সম্পূর্ণ অডিও আগে থেকেই অফলাইনে প্লে করার জন্য ডাউনলোড করে রাখতে পারবেন।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Searchable Surah Selector Dialog
    if (showSurahSelectorSheet) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredSurahs = if (searchQuery.isEmpty()) {
            surahList
        } else {
            surahList.filter { 
                (it.name ?: "").contains(searchQuery, ignoreCase = true) || 
                it.englishName.contains(searchQuery, ignoreCase = true) || 
                it.number.toString() == searchQuery
            }
        }
        
        AlertDialog(
            onDismissRequest = { showSurahSelectorSheet = false },
            title = {
                Column {
                    Text(
                        text = "অডিও ডাউনলোডের জন্য সুরা নির্বাচন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("সুরা খুঁজুন (যেমন: ফাতিহা বা 1)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF06B6D4),
                            unfocusedBorderColor = Border
                        ),
                        singleLine = true
                    )
                }
            },
            text = {
                if (filteredSurahs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("কোনো সুরা পাওয়া যায়নি", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredSurahs) { surah ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.downloadAudioForSurah(surah.number, surah.name ?: "Unknown")
                                        showSurahSelectorSheet = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color(0xFF06B6D4).copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = surah.number.toString(),
                                            color = Color(0xFF06B6D4),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = surah.name ?: "Unknown",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${surah.englishName} • ${surah.numberOfAyahs} আয়াত",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color(0xFF06B6D4),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            HorizontalDivider(color = Border)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSurahSelectorSheet = false }) {
                    Text("বন্ধ করুন", color = Color(0xFF06B6D4))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

fun formatBytesLocal(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val i = (java.lang.Math.log10(bytes.toDouble()) / java.lang.Math.log10(1024.0)).toInt()
    val cappedI = if (i >= units.size) units.size - 1 else i
    return String.format(java.util.Locale.US, "%.1f %s", bytes / java.lang.Math.pow(1024.0, cappedI.toDouble()), units[cappedI])
}
