package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import kotlinx.coroutines.launch
import com.example.data.model.CombinedAyah
import com.example.ui.state.UiState
import com.example.ui.theme.*
import com.example.ui.viewmodels.SurahDetailViewModel

enum class ViewMode { LIST, READING, MUSHAF, TAFSIR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    surahNumber: Int,
    isJuz: Boolean = false,
    viewModel: SurahDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val showTranslation by viewModel.showTranslation.collectAsState()
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    LaunchedEffect(surahNumber, isJuz) {
        if (isJuz) {
            viewModel.loadJuz(surahNumber)
        } else {
            viewModel.loadSurah(surahNumber)
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
                    IconButton(onClick = { /* Dark Mode */ }) {
                        Icon(Icons.Outlined.DarkMode, contentDescription = "Dark Mode", tint = GrayText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OffWhite,
                )
            )
        },
        bottomBar = {
            FloatingAudioPlayer()
        },
        containerColor = OffWhite
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryGreen)
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { 
                            if (isJuz) {
                                viewModel.loadJuz(surahNumber)
                            } else {
                                viewModel.loadSurah(surahNumber) 
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                            Text("Retry", color = White)
                        }
                    }
                }
                is UiState.Success -> {
                    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()
                    var searchQuery by remember { mutableStateOf("") }
                    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            val surahData = com.example.data.QuranData.surahNames.find { it.first == surahNumber }
                            val surahName = surahData?.second?.first ?: "সূরা $surahNumber"
                            val title = if (isJuz) "পারা $surahNumber" else surahName
                            val subtitle = if (isJuz) "" else (surahData?.second?.second ?: "")
                            val info1 = if (isJuz) "পারা: $surahNumber" else "সূরা: $surahNumber"
                            val info2 = if (isJuz) "" else com.example.data.QuranData.getSurahType(surahNumber)
                            val info3 = "মোট আয়াত: ${state.data.size}"
                            
                            HeaderCard(
                                title = title,
                                subtitle = subtitle,
                                info1 = info1,
                                info2 = info2,
                                info3 = info3,
                                viewMode = viewMode, 
                                onModeChange = { viewMode = it },
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                onSearch = {
                                    if (viewMode != ViewMode.MUSHAF) {
                                        val ayahIndex = state.data.indexOfFirst { it.numberInSurah.toString() == searchQuery }
                                        if (ayahIndex != -1) {
                                            val headerCount = if (!isJuz && surahNumber != 1 && surahNumber != 9) 2 else 1
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(ayahIndex + headerCount)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        if (!isJuz && surahNumber != 1 && surahNumber != 9) {
                            item {
                                BismillahSection()
                            }
                        }
                        if (viewMode == ViewMode.MUSHAF) {
                            val ayahsByPage = state.data.groupBy { it.page }
                            ayahsByPage.forEach { (page, ayahs) ->
                                item {
                                    MushafPageView(page, ayahs)
                                }
                            }
                        } else {
                            items(state.data) { ayah ->
                                AyahCard(ayah, viewMode)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCard(
    title: String, 
    subtitle: String, 
    info1: String, 
    info2: String, 
    info3: String, 
    viewMode: ViewMode, 
    onModeChange: (ViewMode) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .background(White, RoundedCornerShape(20.dp))
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkText)
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, fontSize = 14.sp, color = PrimaryGreen)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(100.dp))
                        .background(White, RoundedCornerShape(100.dp))
                        .border(0.5.dp, Border, RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(info1, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                }
                
                if (info2.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(100.dp))
                            .background(White, RoundedCornerShape(100.dp))
                            .border(0.5.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(info2, fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (info3.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(100.dp))
                            .background(White, RoundedCornerShape(100.dp))
                            .border(0.5.dp, Border, RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(info3, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(100.dp))
                    .background(PrimaryGreen, RoundedCornerShape(100.dp))
                    .clickable {  }
                    .padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("প্লেয়ার", color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .width(60.dp)
                            .background(OffWhite, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = DarkText),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onSearch() }),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("আয়াত", color = GrayText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward, 
                        contentDescription = "Search Ayah", 
                        tint = GrayText, 
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onSearch() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(100.dp))
                    .border(1.dp, Border, RoundedCornerShape(100.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ViewModeToggle("লিস্ট", Icons.Default.List, viewMode == ViewMode.LIST) { onModeChange(ViewMode.LIST) }
                ViewModeToggle("শব্দার্থ", Icons.Outlined.Book, viewMode == ViewMode.READING) { onModeChange(ViewMode.READING) }
                ViewModeToggle("তাফসির", Icons.Outlined.Info, viewMode == ViewMode.TAFSIR) { onModeChange(ViewMode.TAFSIR) }
                ViewModeToggle("মুসহাফ", Icons.Outlined.MenuBook, viewMode == ViewMode.MUSHAF) { onModeChange(ViewMode.MUSHAF) }
            }
        }
    }
}

@Composable
fun InfoChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(
        modifier = Modifier
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryGreen)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(text, fontSize = 12.sp, color = DarkText)
    }
}

@Composable
fun RowScope.ViewModeToggle(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(if (isSelected) PrimaryGreen else White, RoundedCornerShape(100.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isSelected) White else GrayText, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = if (isSelected) White else GrayText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BismillahSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .background(White, RoundedCornerShape(20.dp))
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            fontSize = 36.sp,
            color = DarkText,
            textAlign = TextAlign.Center,
            fontFamily = com.example.ui.theme.amiriQuranFont
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AyahCard(ayah: CombinedAyah, viewMode: ViewMode) {
    var showTafsirDialog by remember { mutableStateOf(false) }

    if (showTafsirDialog) {
        AlertDialog(
            onDismissRequest = { showTafsirDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f),
            title = {
                Column {
                    Text(text = "তাফসীরে ইবনে কাসীর", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DarkText)
                    Text(text = "আয়াত ${ayah.numberInSurah}", fontSize = 14.sp, color = GrayText)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (ayah.tafsirText != null) {
                        val paragraphs = ayah.tafsirText.split(Regex("\\n+"))
                        paragraphs.forEach { paragraph ->
                            if (paragraph.isNotBlank()) {
                                Text(
                                    text = paragraph.trim(),
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = DarkText,
                                    textAlign = TextAlign.Justify
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    } else {
                        Text(text = "এই আয়াতের তাফসীর পাওয়া যায়নি।", fontSize = 16.sp, color = GrayText)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTafsirDialog = false }) {
                    Text("বন্ধ করুন", color = PrimaryGreen)
                }
            },
            containerColor = White,
            titleContentColor = DarkText,
            textContentColor = DarkText
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .background(White, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            if (viewMode == ViewMode.LIST) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ayah.numberInSurah.toString(), color = GrayText, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("পারা ${ayah.juz} • পৃষ্ঠা ${ayah.page}", color = PrimaryGreen, fontSize = 10.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { /* TODO: Bookmark */ }) {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark", tint = GrayText)
                        }
                        IconButton(onClick = { showTafsirDialog = true }) {
                            Icon(Icons.Outlined.Book, contentDescription = "Tafsir", tint = GrayText)
                        }
                        IconButton(onClick = { /* TODO: Play */ }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = GrayText)
                        }
                        IconButton(onClick = { /* TODO: More */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = GrayText)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (viewMode == ViewMode.READING) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ayah.numberInSurah.toString(), color = GrayText, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("পারা ${ayah.juz} • পৃষ্ঠা ${ayah.page}", color = PrimaryGreen, fontSize = 10.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { /* TODO: Bookmark */ }) {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark", tint = GrayText)
                        }
                        IconButton(onClick = { showTafsirDialog = true }) {
                            Icon(Icons.Outlined.Book, contentDescription = "Tafsir", tint = GrayText)
                        }
                        IconButton(onClick = { /* TODO: Play */ }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = GrayText)
                        }
                        IconButton(onClick = { /* TODO: More */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = GrayText)
                        }
                    }
                }
                CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (ayah.words.isNotEmpty()) {
                            ayah.words.forEach { word ->
                                if (word.charTypeName != "end") {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(word.textUthmani ?: "", fontSize = 32.sp, color = DarkText, fontFamily = com.example.ui.theme.amiriQuranFont)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(word.translation?.text ?: "", fontSize = 12.sp, color = GrayText)
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("\u06DD${ayah.numberInSurah.toArabicNumerals()}", fontSize = 32.sp, color = DarkText, fontFamily = com.example.ui.theme.amiriQuranFont)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("", fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            // Fallback if words not available
                            val words = ayah.arabicText.split(" ")
                            words.forEach { word ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(word, fontSize = 32.sp, color = DarkText, fontFamily = com.example.ui.theme.amiriQuranFont)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("শব্দ", fontSize = 12.sp, color = GrayText)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = ayah.bengaliText,
                    fontSize = 16.sp,
                    color = GrayText,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (viewMode == ViewMode.TAFSIR) {
                Text(
                    text = "${ayah.arabicText} \u06DD${ayah.numberInSurah.toArabicNumerals()}",
                    fontSize = 32.sp,
                    lineHeight = 56.sp,
                    textAlign = TextAlign.Right,
                    color = DarkText,
                    fontFamily = com.example.ui.theme.amiriQuranFont,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = ayah.bengaliText,
                    fontSize = 16.sp,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                if (ayah.tafsirText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val paragraphs = ayah.tafsirText.split(Regex("\\n+"))
                    paragraphs.forEach { paragraph ->
                        if (paragraph.isNotBlank()) {
                            Text(
                                text = paragraph.trim(),
                                fontSize = 15.sp,
                                color = DarkText,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            } else {
                Text(
                    text = "${ayah.arabicText} \u06DD${ayah.numberInSurah.toArabicNumerals()}",
                    fontSize = 32.sp,
                    lineHeight = 56.sp,
                    textAlign = TextAlign.Right,
                    color = DarkText,
                    fontFamily = com.example.ui.theme.amiriQuranFont,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = ayah.bengaliText,
                    fontSize = 16.sp,
                    color = GrayText,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun FloatingAudioPlayer() {
    NavigationBar(
        containerColor = DarkText,
        contentColor = White,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = White)
                Text("হোম", color = White, fontSize = 10.sp)
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(PrimaryGreen, CircleShape)
                    .offset(y = (-20).dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = White, modifier = Modifier.size(32.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = White)
                    Text("সেটিংস", color = White, fontSize = 10.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = White)
                    Text("মেনু", color = White, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun MushafPageView(page: Int, ayahs: List<CombinedAyah>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val combinedText = androidx.compose.ui.text.buildAnnotatedString {
            ayahs.forEach { ayah ->
                append(ayah.arabicText)
                append(" \u06DD${ayah.numberInSurah.toArabicNumerals()} ")
            }
        }

        Text(
            text = combinedText,
            fontSize = 32.sp,
            lineHeight = 56.sp,
            textAlign = TextAlign.Justify,
            color = DarkText,
            fontFamily = com.example.ui.theme.amiriQuranFont,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            style = androidx.compose.ui.text.TextStyle(
                textDirection = androidx.compose.ui.text.style.TextDirection.Rtl
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Page Divider
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            HorizontalDivider(color = Border, modifier = Modifier.fillMaxWidth())
            Box(
                modifier = Modifier
                    .background(White)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "পৃষ্ঠা $page",
                    color = GrayText,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun Int.toArabicNumerals(): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { if (it.isDigit()) arabicDigits[it - '0'] else it }.joinToString("")
}
