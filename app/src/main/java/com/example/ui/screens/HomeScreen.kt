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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

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
    var selectedTab by remember { mutableStateOf(0) } // 0 for Surah, 1 for Para

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .background(White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("কুরআন", fontWeight = FontWeight.Bold, color = DarkText, fontSize = 20.sp)
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
                    IconButton(onClick = { /* Dark Mode toggle */ }) {
                        Icon(Icons.Outlined.DarkMode, contentDescription = "Dark Mode", tint = GrayText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OffWhite,
                )
            )
        },
        containerColor = OffWhite
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
                    Spacer(modifier = Modifier.height(12.dp))
                    QuickSurahPills(
                        onSurahClick = onNavigateToSurah,
                        onAyatulKursiClick = {
                            onNavigateToSurahWithAyah(2, "MUSHAF", 255)
                        }
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
            .background(White, androidx.compose.foundation.shape.CircleShape)
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
                .background(White, RoundedCornerShape(100.dp))
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
                    Text(lastReadSurahName, color = DarkText, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, lineHeight = 12.sp)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrayText, modifier = Modifier.size(16.dp))
            }
        }

        // Toggle Buttons
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, RoundedCornerShape(100.dp))
                .background(White, RoundedCornerShape(100.dp))
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

@Composable
fun QuickSurahPills(
    onSurahClick: (Int) -> Unit,
    onAyatulKursiClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .shadow(2.dp, RoundedCornerShape(100.dp))
                .background(White, RoundedCornerShape(100.dp))
                .clickable { onSurahClick(18) } // Surah Kahf
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(OrangeAccent, RoundedCornerShape(50)))
                Spacer(modifier = Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text("জুমার আমল", color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, lineHeight = 10.sp)
                    Text("সূরা কাহফ", color = DarkText, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
                }
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .shadow(2.dp, RoundedCornerShape(100.dp))
                .background(White, RoundedCornerShape(100.dp))
                .clickable { onAyatulKursiClick() } // Ayatul Kursi click action
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(BlueDot, RoundedCornerShape(50)))
                Spacer(modifier = Modifier.width(8.dp))
                Text("আয়াতুল কুরসী", color = DarkText, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .shadow(2.dp, RoundedCornerShape(100.dp))
                .background(White, RoundedCornerShape(100.dp))
                .clickable { onSurahClick(67) } // Surah Mulk
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(GreenDot, RoundedCornerShape(50)))
                Spacer(modifier = Modifier.width(8.dp))
                Text("সূরা মুলক", color = DarkText, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
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
            .background(White, RoundedCornerShape(16.dp))
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
                        color = DarkText, 
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

// Removed old BottomNavBar
