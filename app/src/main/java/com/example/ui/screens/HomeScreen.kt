package com.example.ui.screens

import com.example.data.QuranData
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.HomeViewModel
import androidx.compose.animation.core.*
import java.util.Calendar
import kotlinx.coroutines.delay
import androidx.compose.foundation.border
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector

fun String.toArabicNumerals(): String {
    val englishNumerals = "0123456789"
    val arabicNumerals = "٠١٢٣٤٥٦٧٨٩"
    return this.map { char ->
        val index = englishNumerals.indexOf(char)
        if (index != -1) arabicNumerals[index] else char
    }.joinToString("")
}

fun String.toBengaliNumerals(): String {
    val englishNumerals = "0123456789"
    val bengaliNumerals = "০১২৩৪৫৬৭৮৯"
    return this.map { char ->
        val index = englishNumerals.indexOf(char)
        if (index != -1) bengaliNumerals[index] else char
    }.joinToString("")
}

fun Int.toBengaliNumerals(): String {
    return this.toString().toBengaliNumerals()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSurah: (Int) -> Unit,
    onNavigateToJuz: (Int) -> Unit,
    onNavigateToNormalMode: () -> Unit,
    onNavigateToReadingMode: () -> Unit,
    onNavigateToHafeziMode: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateToMushaf: () -> Unit,
    onNavigateToMushafPage: (String, Int) -> Unit,
    onNavigateToSurahWithAyah: (Int, String, Int) -> Unit
) {
    val context = LocalContext.current
    val lastReadSurah by viewModel.lastReadSurah.collectAsState()
    val lastReadPage by viewModel.lastReadPage.collectAsState()
    val surahList by viewModel.surahs.collectAsState()
    val currentTheme by viewModel.theme.collectAsState()
    val isDark = currentTheme == "Dark"
    var selectedTab by remember { mutableStateOf(0) } // 0 for Surah, 1 for Para

    val hasAskedDownloadPrompt by viewModel.hasAskedDownloadPrompt.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadError by viewModel.downloadError.collectAsState()

    // Show first-time download prompt dialog
    if (!hasAskedDownloadPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.setHasAskedDownloadPrompt() },
            title = {
                Text(
                    text = "অফলাইন কুরআন ডাটা",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "আপনি কি অফলাইনে পড়ার জন্য কুরআনের সব সুরা ও অনুবাদ ডাটা ডাউনলোড করতে চান? ডাউনলোড করলে কোনো ইন্টারনেট ছাড়াই শব্দার্থ ও সুরা অফলাইনে পড়তে পারবেন।\n\n(অন্যথায় ক্যাশিং সিস্টেম অনুযায়ী আপনি যখন যে সুরা ওপেন করবেন, সেটি স্বয়ংক্রিয়ভাবে সেভ হয়ে যাবে।)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setHasAskedDownloadPrompt()
                        viewModel.downloadAllQuranData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("হ্যাঁ, ডাউনলোড করুন", color = White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.setHasAskedDownloadPrompt() }
                ) {
                    Text("না, পরে করব", color = GrayText)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Show downloading progress dialog
    if (isDownloading) {
        AlertDialog(
            onDismissRequest = { /* Non-dismissable */ },
            title = {
                Text(
                    text = "ডাটা ডাউনলোড হচ্ছে...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = PrimaryGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "সুরা ডাউনলোড হয়েছে: ${downloadProgress.toBengaliNumerals()} / ১১৪",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress / 114f },
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryGreen,
                        trackColor = PrimaryGreen.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "অনুগ্রহ করে অপেক্ষা করুন, ডাটা ডাউনলোড সম্পন্ন হচ্ছে। ডাউনলোড শেষ হলে সব ফিচার অফলাইনে ব্যবহার করতে পারবেন।",
                        fontSize = 12.sp,
                        color = GrayText,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {},
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Show download error toast if any
    LaunchedEffect(downloadError) {
        downloadError?.let {
            Toast.makeText(context, "ডাউনলোড ত্রুটি: $it", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("কুরআন", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(PrimaryGreen, RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("BN", color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (isDark) "Light Mode" else "Dark Mode",
                            tint = if (isDark) OrangeAccent else GrayText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isTablet = maxWidth > 600.dp
            val horizontalPadding = if (isTablet) 32.dp else 0.dp

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = horizontalPadding)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        HeroSection()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .offset(y = 24.dp)
                        ) {
                            SearchSection(onNavigateToSearch)
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                    QuickAccessSection(
                        selectedTab = selectedTab,
                        lastReadSurah = lastReadSurah,
                        onTabSelected = { selectedTab = it },
                        onSurahClick = onNavigateToSurah
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ReadingModesSection(
                        onNavigateToReadingMode = onNavigateToReadingMode,
                        onNavigateToHafeziMode = { onNavigateToHafeziMode(1) },
                        onNavigateToMushaf = onNavigateToMushaf
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    QuickSurahPills(
                        onSurahClick = onNavigateToSurah,
                        onNavigateToSurahWithAyah = onNavigateToSurahWithAyah
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (selectedTab == 0) {
                        SurahGridSection(surahList = surahList, onSurahClick = onNavigateToSurah)
                    } else {
                        ParaGridSection(onNavigateToJuz)
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.verticalGradient(listOf(PrimaryGreen, DarkGreen)))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = White, modifier = Modifier.size(24.dp))
            }
            Text(
                text = "আল-কুরআন",
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            )
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "\" ... আমি কোরআনকে বুঝার জন্য সহজ করে দিয়েছি... \" (৫৪:১৭)",
                    color = White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SearchSection(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(6.dp, androidx.compose.foundation.shape.CircleShape)
            .background(MaterialTheme.colorScheme.surface, androidx.compose.foundation.shape.CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = GrayText)
            Spacer(modifier = Modifier.width(12.dp))
            Text("সূরা খুঁজুন...", color = GrayText, fontSize = 16.sp)
        }
    }
}

@Composable
fun QuickAccessSection(
    selectedTab: Int,
    lastReadSurah: Int,
    onTabSelected: (Int) -> Unit,
    onSurahClick: (Int) -> Unit
) {
    val lastReadSurahName = QuranData.surahNames.find { it.first == lastReadSurah }?.second?.first ?: "আল ফাতিহা"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Last Read Card
        Box(
            modifier = Modifier
                .weight(1.1f)
                .shadow(2.dp, RoundedCornerShape(100.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(100.dp))
                .clickable { onSurahClick(lastReadSurah) }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(BackgroundGreen, RoundedCornerShape(100.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text("সর্বশেষ পঠিত", color = GrayText, fontSize = 9.sp, lineHeight = 10.sp, maxLines = 1)
                    Text(lastReadSurahName, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, lineHeight = 12.sp)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrayText, modifier = Modifier.size(16.dp))
            }
        }

        // Toggle Buttons
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, RoundedCornerShape(100.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(100.dp))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(0) }
                        .background(
                            if (selectedTab == 0) PrimaryGreen else Color.Transparent, 
                            RoundedCornerShape(100.dp)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = if (selectedTab == 0) White else GrayText, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সূরা", color = if (selectedTab == 0) White else GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(1) }
                        .background(
                            if (selectedTab == 1) PrimaryGreen else Color.Transparent, 
                            RoundedCornerShape(100.dp)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = if (selectedTab == 1) White else GrayText, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("পারা", color = if (selectedTab == 1) White else GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class AmaliSurah(
    val title: String,
    val subtitle: String,
    val surahId: Int,
    val startAyah: Int? = null,
    val dotColor: Color,
    val isActive: (Calendar) -> Boolean
)

@Composable
fun QuickSurahPills(
    onSurahClick: (Int) -> Unit,
    onNavigateToSurahWithAyah: (Int, String, Int) -> Unit
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // check every 10 seconds to keep dynamic cards live
            currentTime = Calendar.getInstance()
        }
    }
    
    val isDark = MaterialTheme.colorScheme.surface.let { (it.red + it.green + it.blue) < 1.5f }
    
    val amaliList = remember {
        listOf(
            AmaliSurah(
                title = "সূরা কাহফ",
                subtitle = "জুমার আমল",
                surahId = 18,
                dotColor = OrangeAccent,
                isActive = { cal ->
                    val day = cal.get(Calendar.DAY_OF_WEEK)
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val minute = cal.get(Calendar.MINUTE)
                    day == Calendar.FRIDAY && (hour in 5..12 || (hour == 13 && minute <= 30))
                }
            ),
            AmaliSurah(
                title = "আয়াতুল কুরসী",
                subtitle = "ফরজ সালাত পর",
                surahId = 2,
                startAyah = 255,
                dotColor = BlueDot,
                isActive = { cal ->
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val minute = cal.get(Calendar.MINUTE)
                    val timeInMinutes = hour * 60 + minute
                    (timeInMinutes in 315..360) || // 5:15 - 6:00
                    (timeInMinutes in 810..855) || // 13:30 - 14:15
                    (timeInMinutes in 1005..1050) || // 16:45 - 17:30
                    (timeInMinutes in 1140..1185) || // 19:00 - 19:45
                    (timeInMinutes in 1230..1275)    // 20:30 - 21:15
                }
            ),
            AmaliSurah(
                title = "সূরা ইয়াসিন",
                subtitle = "ফজরের আমল",
                surahId = 36,
                dotColor = Color(0xFF8B5CF6),
                isActive = { cal ->
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val minute = cal.get(Calendar.MINUTE)
                    (hour == 5) || (hour == 6 && minute <= 30)
                }
            ),
            AmaliSurah(
                title = "সূরা আর-রহমান",
                subtitle = "আসর আমল",
                surahId = 55,
                dotColor = Color(0xFFF97316),
                isActive = { cal ->
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val minute = cal.get(Calendar.MINUTE)
                    (hour == 16 && minute >= 30) || (hour == 17)
                }
            ),
            AmaliSurah(
                title = "সূরা ওয়াক্বিয়া",
                subtitle = "মাগরিবের আমল",
                surahId = 56,
                dotColor = Color(0xFFEC4899),
                isActive = { cal ->
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val minute = cal.get(Calendar.MINUTE)
                    (hour == 18 && minute >= 30) || (hour == 19)
                }
            ),
            AmaliSurah(
                title = "সূরা মুলক",
                subtitle = "ঘুমানোর আমল",
                surahId = 67,
                dotColor = GreenDot,
                isActive = { cal ->
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    hour >= 22 || hour < 4
                }
            ),
            AmaliSurah(
                title = "সূরা দুখান",
                subtitle = "বৃহস্পতিবার রাত",
                surahId = 44,
                dotColor = Color(0xFF06B6D4),
                isActive = { cal ->
                    val day = cal.get(Calendar.DAY_OF_WEEK)
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    (day == Calendar.THURSDAY && hour >= 18 && (hour > 18 || cal.get(Calendar.MINUTE) >= 30)) ||
                    (day == Calendar.FRIDAY && hour < 4)
                }
            ),
            AmaliSurah(
                title = "বাকারার শেষ ২ আয়াত",
                subtitle = "রাতের আমল",
                surahId = 2,
                startAyah = 285,
                dotColor = Color(0xFF14B8A6),
                isActive = { cal ->
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    hour in 19..21
                }
            )
        )
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "amal_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    val sortedAmaliList = remember(currentTime) {
        amaliList.sortedByDescending { it.isActive(currentTime) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        sortedAmaliList.forEach { item ->
            val isActive = item.isActive(currentTime)
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .shadow(if (isActive) 4.dp else 2.dp, RoundedCornerShape(100.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .then(
                        if (isActive) {
                            Modifier.border(
                                width = 1.5.dp,
                                color = item.dotColor.copy(alpha = borderAlpha),
                                shape = RoundedCornerShape(100.dp)
                            )
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(100.dp)
                            )
                        }
                    )
                    .clickable {
                        if (item.startAyah != null) {
                            onNavigateToSurahWithAyah(item.surahId, "MUSHAF", item.startAyah)
                        } else {
                            onSurahClick(item.surahId)
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size((12 * pulseScale).dp)
                                    .background(item.dotColor.copy(alpha = pulseAlpha), RoundedCornerShape(50))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(item.dotColor, RoundedCornerShape(50))
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.subtitle,
                                color = if (isActive) item.dotColor else GrayText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 10.sp
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(item.dotColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "চলমান",
                                        color = item.dotColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        lineHeight = 8.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SurahGridSection(surahList: List<com.example.data.model.Surah>, onSurahClick: (Int) -> Unit) {
    val dummySurahs = QuranData.surahNames
    
    BoxWithConstraints(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        val columns = maxOf(2, (maxWidth / 160.dp).toInt())
        val itemWidth = (maxWidth - (12.dp * (columns - 1))) / columns

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            dummySurahs.chunked(columns).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { surahPair ->
                        val surahId = surahPair.first
                        val apiSurah = surahList.find { it.number == surahId }
                        val ayahCount = apiSurah?.numberOfAyahs ?: when(surahId) {
                            1 -> 7
                            2 -> 286
                            3 -> 200
                            4 -> 176
                            5 -> 120
                            6 -> 165
                            7 -> 206
                            8 -> 75
                            9 -> 129
                            10 -> 109
                            11 -> 123
                            12 -> 111
                            13 -> 43
                            14 -> 52
                            15 -> 99
                            16 -> 128
                            17 -> 111
                            18 -> 110
                            19 -> 98
                            20 -> 135
                            21 -> 112
                            22 -> 78
                            23 -> 118
                            24 -> 64
                            25 -> 77
                            26 -> 227
                            27 -> 93
                            28 -> 88
                            29 -> 69
                            30 -> 60
                            31 -> 34
                            32 -> 30
                            33 -> 73
                            34 -> 54
                            35 -> 45
                            36 -> 83
                            37 -> 182
                            38 -> 88
                            39 -> 75
                            40 -> 85
                            41 -> 54
                            42 -> 53
                            43 -> 89
                            44 -> 59
                            45 -> 37
                            46 -> 35
                            47 -> 38
                            48 -> 29
                            49 -> 18
                            50 -> 45
                            51 -> 60
                            52 -> 49
                            53 -> 62
                            54 -> 55
                            55 -> 78
                            56 -> 96
                            57 -> 29
                            58 -> 22
                            59 -> 24
                            60 -> 13
                            61 -> 14
                            62 -> 11
                            63 -> 11
                            64 -> 18
                            65 -> 12
                            66 -> 12
                            67 -> 30
                            68 -> 52
                            69 -> 52
                            70 -> 44
                            71 -> 28
                            72 -> 28
                            73 -> 20
                            74 -> 56
                            75 -> 40
                            76 -> 31
                            77 -> 50
                            78 -> 40
                            79 -> 46
                            80 -> 42
                            81 -> 29
                            82 -> 19
                            83 -> 36
                            84 -> 25
                            85 -> 22
                            86 -> 17
                            87 -> 19
                            88 -> 26
                            89 -> 30
                            90 -> 20
                            91 -> 15
                            92 -> 21
                            93 -> 11
                            94 -> 8
                            95 -> 8
                            96 -> 19
                            97 -> 5
                            98 -> 8
                            99 -> 8
                            100 -> 11
                            101 -> 11
                            102 -> 8
                            103 -> 3
                            104 -> 9
                            105 -> 5
                            106 -> 4
                            107 -> 7
                            108 -> 3
                            109 -> 6
                            110 -> 3
                            111 -> 5
                            112 -> 4
                            113 -> 5
                            114 -> 6
                            else -> 7
                        }
                        
                        val rawType = apiSurah?.revelationType
                        val revelationType = if (rawType != null) {
                            if (rawType.equals("Meccan", ignoreCase = true)) "মাক্কী" else "মাদানী"
                        } else {
                            com.example.data.QuranData.getSurahType(surahId)
                        }

                        SurahCard(
                            modifier = Modifier.width(itemWidth),
                            number = surahId.toString(), 
                            title = surahPair.second.first, 
                            translation = surahPair.second.second, 
                            ayahCount = ayahCount,
                            revelationType = revelationType,
                            onClick = { onSurahClick(surahId) }
                        )
                    }
                    val emptySpots = columns - rowItems.size
                    repeat(emptySpots) {
                        Spacer(modifier = Modifier.width(itemWidth))
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
fun ParaGridSection(onParaClick: (Int) -> Unit) {
    val dummyParas = (1..30).toList()
    
    BoxWithConstraints(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        val columns = maxOf(2, (maxWidth / 160.dp).toInt())
        val itemWidth = (maxWidth - (12.dp * (columns - 1))) / columns

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            dummyParas.chunked(columns).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { para ->
                        SurahCard( // Reusing SurahCard for Para
                            modifier = Modifier.width(itemWidth),
                            number = para.toString(), 
                            title = "পারা $para", 
                            translation = "Juz $para", 
                            onClick = { onParaClick(para) }
                        )
                    }
                    val emptySpots = columns - rowItems.size
                    repeat(emptySpots) {
                        Spacer(modifier = Modifier.width(itemWidth))
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
fun SurahCard(
    modifier: Modifier = Modifier, 
    number: String, 
    title: String, 
    translation: String, 
    ayahCount: Int? = null,
    revelationType: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BackgroundGreen, RoundedCornerShape(8.dp))
                    )
                    Text(number, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        title, 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 14.sp, 
                        maxLines = 1, 
                        lineHeight = 14.sp
                    )
                    Text(
                        translation, 
                        color = GrayText, 
                        fontSize = 10.sp, 
                        maxLines = 1, 
                        lineHeight = 10.sp,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }
            }
            if (ayahCount != null && revelationType != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Border)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = GrayText, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${ayahCount.toBengaliNumerals()} আয়াত", color = GrayText, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(3.dp).background(GrayText, RoundedCornerShape(50)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(revelationType, color = GrayText, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun ReadingModesSection(
    onNavigateToReadingMode: () -> Unit,
    onNavigateToHafeziMode: () -> Unit,
    onNavigateToMushaf: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "পঠন মোডসমূহ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Paragraph Reading Mode Card
            ModeCard(
                title = "প্যারাগ্রাফ মোড",
                subtitle = "টানা পড়ার জন্য",
                icon = Icons.Default.ChromeReaderMode,
                backgroundColor = Color(0xFF10B981).copy(alpha = 0.08f),
                iconColor = Color(0xFF10B981),
                onClick = onNavigateToReadingMode,
                modifier = Modifier.weight(1f)
            )

            // Hafezi Mode Card
            ModeCard(
                title = "হাফেজী মোড",
                subtitle = "১৫ লাইন ফরম্যাট",
                icon = Icons.Default.AutoStories,
                backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.08f),
                iconColor = Color(0xFF3B82F6),
                onClick = onNavigateToHafeziMode,
                modifier = Modifier.weight(1f)
            )

            // Mushaf Mode Card
            ModeCard(
                title = "ডিজিটাল মুসহাফ",
                subtitle = "আসল প্রিন্ট পেজ",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                backgroundColor = Color(0xFFF59E0B).copy(alpha = 0.08f),
                iconColor = Color(0xFFF59E0B),
                onClick = onNavigateToMushaf,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = GrayText,
                maxLines = 1,
                lineHeight = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Removed old BottomNavBar
