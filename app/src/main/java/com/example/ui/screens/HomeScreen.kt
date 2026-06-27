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

fun String.toArabicNumerals(): String {
    val englishNumerals = "0123456789"
    val arabicNumerals = "٠١٢٣٤٥٦٧٨٩"
    return this.map { char ->
        val index = englishNumerals.indexOf(char)
        if (index != -1) arabicNumerals[index] else char
    }.joinToString("")
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
    onSettingsClick: () -> Unit
) {
    val lastReadSurah by viewModel.lastReadSurah.collectAsState()
    val lastReadPage by viewModel.lastReadPage.collectAsState()
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
        // bottomBar = {
        //     BottomNavBar()
        // },
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
                    QuickAccessSection(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                }
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    QuickSurahPills()
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (selectedTab == 0) {
                        SurahGridSection(onNavigateToSurah)
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
fun QuickAccessSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
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
                .clickable { }
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
                    Text("আল বাকারা . আয়াত ৬", color = DarkText, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, lineHeight = 12.sp)
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
fun QuickSurahPills() {
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
                .clickable { }
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
                .clickable { }
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
                .clickable { }
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
fun SurahGridSection(onSurahClick: (Int) -> Unit) {
    val dummySurahs = QuranData.surahNames
    
    BoxWithConstraints(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        val columns = maxOf(2, (maxWidth / 160.dp).toInt())
        val itemWidth = (maxWidth - (12.dp * (columns - 1))) / columns

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            dummySurahs.chunked(columns).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { surah ->
                        SurahCard(
                            modifier = Modifier.width(itemWidth),
                            number = surah.first.toString(), 
                            title = surah.second.first, 
                            translation = surah.second.second, 
                            onClick = { onSurahClick(surah.first) }
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
fun SurahCard(modifier: Modifier = Modifier, number: String, title: String, translation: String, onClick: () -> Unit) {
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
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Border)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = GrayText, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("৭ আয়াত", color = GrayText, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(3.dp).background(GrayText, RoundedCornerShape(50)))
                Spacer(modifier = Modifier.width(6.dp))
                Text("মাক্কী", color = GrayText, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun BottomNavBar() {
    NavigationBar(
        containerColor = White,
        tonalElevation = 8.dp,
        modifier = Modifier.shadow(8.dp)
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("হোম") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                indicatorColor = BackgroundGreen,
                unselectedIconColor = GrayText,
                unselectedTextColor = GrayText
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("সার্চ") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GrayText,
                unselectedTextColor = GrayText
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("প্রোফাইল") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GrayText,
                unselectedTextColor = GrayText
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
            label = { Text("মেনু") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GrayText,
                unselectedTextColor = GrayText
            )
        )
    }
}
