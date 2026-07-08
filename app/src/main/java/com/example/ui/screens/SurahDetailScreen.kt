package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.launch
import com.example.data.model.CombinedAyah
import com.example.ui.state.UiState
import com.example.ui.theme.*
import com.example.ui.viewmodels.SurahDetailViewModel
import com.example.ui.viewmodels.PlaybackMode

enum class ViewMode { LIST, READING, MUSHAF, TAFSIR }

data class ProcessedWord(
    val id: Int,
    val position: Int,
    val charTypeName: String,
    val textUthmani: String,
    val translationText: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    surahNumber: Int,
    isJuz: Boolean = false,
    initialViewMode: String? = null,
    initialAyah: Int = -1,
    viewModel: SurahDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val showTranslation by viewModel.showTranslation.collectAsState()
    val arabicFontSize by viewModel.arabicFontSize.collectAsState()
    val bengaliFontSize by viewModel.bengaliFontSize.collectAsState()
    val arabicFontName by viewModel.arabicFontName.collectAsState()
    
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPlayingAyahNumber by viewModel.currentPlayingAyahNumber.collectAsState()
    val playbackMode by viewModel.playbackMode.collectAsState()

    // Download States
    val isDownloadingOffline by viewModel.isDownloadingOffline.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val downloadError by viewModel.downloadError.collectAsState()
    
    val parsedViewMode = when (initialViewMode) {
        "MUSHAF" -> ViewMode.MUSHAF
        "READING" -> ViewMode.READING
        "TAFSIR" -> ViewMode.TAFSIR
        else -> ViewMode.LIST
    }
    var viewMode by remember { mutableStateOf(parsedViewMode) }
    var showPlayerBottomSheet by remember { mutableStateOf(false) }
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val settingsSheetState = rememberModalBottomSheetState()
    
    val currentPlayingAyah = (uiState as? UiState.Success)?.data?.find { it.numberInSurah == currentPlayingAyahNumber }

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
                    IconButton(onClick = { showSettingsBottomSheet = true }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = GrayText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OffWhite,
                )
            )
        },
        bottomBar = {
            FloatingAudioPlayer(
                isPlaying = isPlaying,
                currentPlayingAyahNum = currentPlayingAyahNumber,
                playbackMode = playbackMode,
                onPlayPauseClick = {
                    val currentAyah = currentPlayingAyah ?: (uiState as? UiState.Success)?.data?.firstOrNull()
                    viewModel.togglePlayPause(currentAyah, surahNumber)
                },
                onPreviousClick = {
                    val currentAyah = currentPlayingAyah ?: (uiState as? UiState.Success)?.data?.firstOrNull()
                    viewModel.playPrevious(currentAyah, surahNumber)
                },
                onNextClick = {
                    val currentAyah = currentPlayingAyah ?: (uiState as? UiState.Success)?.data?.firstOrNull()
                    viewModel.playNext(currentAyah, surahNumber)
                },
                onModeToggleClick = {
                    viewModel.setPlaybackMode(
                        if (playbackMode == PlaybackMode.SURAH) PlaybackMode.AYAH else PlaybackMode.SURAH
                    )
                },
                onPlayerTabClick = {
                    showPlayerBottomSheet = true
                },
                onBackClick = onNavigateBack
            )
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
                    
                    val displayedData = if (initialAyah > 0) {
                        state.data.filter { it.numberInSurah == initialAyah }
                    } else {
                        state.data
                    }
                    
                    LaunchedEffect(displayedData) {
                        if (initialAyah > 0) {
                            val headerCount = if (!isJuz && surahNumber != 1 && surahNumber != 9 && initialAyah <= 0) 2 else 1
                            if (viewMode == ViewMode.MUSHAF) {
                                val targetAyah = displayedData.find { it.numberInSurah == initialAyah }
                                if (targetAyah != null) {
                                    val ayahsByPage = displayedData.groupBy { it.page }
                                    val pagesList = ayahsByPage.keys.toList()
                                    val pageIndex = pagesList.indexOf(targetAyah.page)
                                    if (pageIndex != -1) {
                                        listState.scrollToItem(pageIndex + headerCount)
                                    }
                                }
                            } else {
                                val ayahIndex = displayedData.indexOfFirst { it.numberInSurah == initialAyah }
                                if (ayahIndex != -1) {
                                    listState.scrollToItem(ayahIndex + headerCount)
                                }
                            }
                        }
                    }
                    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            val surahData = com.example.data.QuranData.surahNames.find { it.first == surahNumber }
                            val surahName = surahData?.second?.first ?: "সূরা $surahNumber"
                            val title = if (isJuz) {
                                "পারা $surahNumber"
                            } else if (surahNumber == 2 && initialAyah == 255) {
                                "আয়াতুল কুরসি"
                            } else {
                                surahName
                            }
                            val subtitle = if (isJuz) "" else if (surahNumber == 2 && initialAyah == 255) "সূরা আল-বাকারাহ, আয়াত ২৫৫" else (surahData?.second?.second ?: "")
                            val info1 = if (isJuz) "পারা: $surahNumber" else "সূরা: $surahNumber"
                            val info2 = if (isJuz) "" else com.example.data.QuranData.getSurahType(surahNumber)
                            val info3 = if (surahNumber == 2 && initialAyah == 255) "১টি আয়াত" else "মোট আয়াত: ${displayedData.size}"
                            
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
                                        val ayahIndex = displayedData.indexOfFirst { it.numberInSurah.toString() == searchQuery }
                                        if (ayahIndex != -1) {
                                            val headerCount = if (!isJuz && surahNumber != 1 && surahNumber != 9 && initialAyah <= 0) 2 else 1
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(ayahIndex + headerCount)
                                            }
                                        }
                                    }
                                },
                                onPlayerClick = {
                                    val currentAyah = currentPlayingAyah ?: displayedData.firstOrNull()
                                    viewModel.togglePlayPause(currentAyah, surahNumber)
                                }
                            )
                        }
                        if (!isJuz && surahNumber != 1 && surahNumber != 9 && initialAyah <= 0) {
                            item {
                                BismillahSection(arabicFontName = arabicFontName)
                            }
                        }
                        if (viewMode == ViewMode.MUSHAF) {
                            val ayahsByPage = displayedData.groupBy { it.page }
                            ayahsByPage.forEach { (page, ayahs) ->
                                item {
                                    MushafPageView(
                                        page = page,
                                        ayahs = ayahs,
                                        surahNumber = surahNumber,
                                        onPlayWord = { viewModel.playWord(it) },
                                        onPlayAyah = { viewModel.togglePlayPause(it, surahNumber) },
                                        arabicFontName = arabicFontName,
                                        arabicFontSize = arabicFontSize
                                    )
                                }
                            }
                        } else {
                            items(
                                items = displayedData,
                                key = { it.number }
                            ) { ayah ->
                                val isAyahPlaying = isPlaying && currentPlayingAyahNumber == ayah.numberInSurah
                                AyahCard(
                                    ayah = ayah,
                                    viewMode = viewMode,
                                    surahNumber = surahNumber,
                                    playAudio = { viewModel.togglePlayPause(ayah, surahNumber) },
                                    onPlayWord = { viewModel.playWord(it) },
                                    isPlaying = isAyahPlaying,
                                    showTranslation = showTranslation,
                                    arabicFontSize = arabicFontSize,
                                    bengaliFontSize = bengaliFontSize,
                                    arabicFontName = arabicFontName
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (showPlayerBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPlayerBottomSheet = false },
                sheetState = sheetState,
                containerColor = White
            ) {
                PlayerBottomSheetContent(
                    currentPlayingAyahNum = currentPlayingAyahNumber,
                    playbackMode = playbackMode,
                    onModeChange = { viewModel.setPlaybackMode(it) },
                    onClose = { showPlayerBottomSheet = false },
                    currentAyah = currentPlayingAyah,
                    arabicFontName = arabicFontName
                )
            }
        }

        if (showSettingsBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsBottomSheet = false },
                sheetState = settingsSheetState,
                containerColor = White
            ) {
                ReaderSettingsBottomSheetContent(
                    showTranslation = showTranslation,
                    onShowTranslationToggle = { viewModel.toggleTranslation() },
                    arabicFontSize = arabicFontSize,
                    onArabicFontSizeChange = { viewModel.setArabicFontSize(it) },
                    bengaliFontSize = bengaliFontSize,
                    onBengaliFontSizeChange = { viewModel.setBengaliFontSize(it) },
                    arabicFontName = arabicFontName,
                    onArabicFontNameChange = { viewModel.setArabicFontName(it) },
                    isDownloadingOffline = isDownloadingOffline,
                    downloadProgress = downloadProgress,
                    downloadStatus = downloadStatus,
                    downloadError = downloadError,
                    onDownloadClick = {
                        viewModel.downloadSurahOffline(surahNumber, "এই")
                    },
                    onCancelDownloadClick = { viewModel.cancelOfflineDownload() },
                    onClose = { showSettingsBottomSheet = false }
                )
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
    onSearch: () -> Unit,
    onPlayerClick: () -> Unit
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
                    .clickable { onPlayerClick() }
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
fun BismillahSection(arabicFontName: String = "Amiri Quran") {
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
            fontFamily = com.example.ui.theme.getArabicFont(arabicFontName)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AyahCard(
    ayah: CombinedAyah,
    viewMode: ViewMode,
    surahNumber: Int,
    playAudio: () -> Unit,
    onPlayWord: (String) -> Unit,
    isPlaying: Boolean,
    showTranslation: Boolean,
    arabicFontSize: Float,
    bengaliFontSize: Float,
    arabicFontName: String = "Amiri Quran"
) {
    var showTafsirDialog by remember { mutableStateOf(false) }
    val arabicFont = com.example.ui.theme.getArabicFont(arabicFontName)

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
            .background(if (isPlaying) PrimaryGreen.copy(alpha = 0.05f) else White, RoundedCornerShape(20.dp))
            .border(if (isPlaying) 1.dp else 0.dp, if (isPlaying) PrimaryGreen else White, RoundedCornerShape(20.dp))
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
                        IconButton(onClick = playAudio) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                                contentDescription = "Play", 
                                tint = if (isPlaying) PrimaryGreen else GrayText
                            )
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
                        IconButton(onClick = playAudio) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                                contentDescription = "Play", 
                                tint = if (isPlaying) PrimaryGreen else GrayText
                            )
                        }
                        IconButton(onClick = { /* TODO: More */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = GrayText)
                        }
                    }
                }
                CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                    if (ayah.words.isNotEmpty()) {
                        val processedWords = remember(ayah.words) {
                            val list = mutableListOf<ProcessedWord>()
                            ayah.words.forEach { word ->
                                val text = word.textUthmani ?: ""
                                val isPause = word.charTypeName == "pause" || 
                                              word.charTypeName == "stop" ||
                                              text.trim() in listOf("ۖ", "ۗ", "ۚ", "ۛ", "ۜ", "ۘ", "ۙ", "ج")
                                
                                if (isPause) {
                                    val lastWordIndex = list.indexOfLast { it.charTypeName == "word" }
                                    if (lastWordIndex != -1) {
                                        val lastWord = list[lastWordIndex]
                                        list[lastWordIndex] = lastWord.copy(
                                            textUthmani = lastWord.textUthmani + " " + text
                                        )
                                    } else {
                                        list.add(ProcessedWord(word.id, word.position, "word", text, word.translation?.text))
                                    }
                                } else {
                                    list.add(ProcessedWord(word.id, word.position, word.charTypeName, text, word.translation?.text))
                                }
                            }
                            list
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            processedWords.forEach { word ->
                                if (word.charTypeName != "end") {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            val url = String.format(java.util.Locale.US, "https://verses.quran.com/wbw/%03d_%03d_%03d.mp3", surahNumber, ayah.numberInSurah, word.position)
                                            onPlayWord(url)
                                        }.padding(4.dp)
                                    ) {
                                        Text(word.textUthmani, fontSize = arabicFontSize.sp, color = DarkText, fontFamily = arabicFont)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(word.translationText ?: "", fontSize = 12.sp, color = GrayText)
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AyahCircle(
                                            number = ayah.numberInSurah,
                                            fontSize = arabicFontSize.toFloat(),
                                            color = PrimaryGreen
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // Fallback to beautiful cohesive AyahInlineText instead of splitting by space and showing "শব্দ" placeholders
                        AyahInlineText(
                            arabicText = ayah.arabicText,
                            ayahNumber = ayah.numberInSurah,
                            fontSize = arabicFontSize.toFloat(),
                            fontFamily = arabicFont,
                            color = DarkText,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (showTranslation) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = ayah.bengaliText,
                        fontSize = bengaliFontSize.sp,
                        color = GrayText,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (viewMode == ViewMode.TAFSIR) {
                AyahInlineText(
                    arabicText = ayah.arabicText,
                    ayahNumber = ayah.numberInSurah,
                    fontSize = arabicFontSize.toFloat(),
                    fontFamily = arabicFont,
                    color = DarkText,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showTranslation) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = ayah.bengaliText,
                        fontSize = bengaliFontSize.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                AyahInlineText(
                    arabicText = ayah.arabicText,
                    ayahNumber = ayah.numberInSurah,
                    fontSize = arabicFontSize.toFloat(),
                    fontFamily = arabicFont,
                    color = DarkText,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showTranslation) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = ayah.bengaliText,
                        fontSize = bengaliFontSize.sp,
                        color = GrayText,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingAudioPlayer(
    isPlaying: Boolean,
    currentPlayingAyahNum: Int?,
    playbackMode: PlaybackMode,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onModeToggleClick: () -> Unit,
    onPlayerTabClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GrayText,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 2. Center Pill controller
            Row(
                modifier = Modifier
                    .background(OffWhite, RoundedCornerShape(30.dp))
                    .border(1.dp, GrayText.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                    .clickable { onPlayerTabClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mode toggle
                IconButton(
                    onClick = onModeToggleClick, 
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (playbackMode == PlaybackMode.SURAH) Icons.Default.Repeat else Icons.Default.RepeatOne,
                        contentDescription = "Mode",
                        tint = if (playbackMode == PlaybackMode.SURAH) PrimaryGreen else GrayText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = onPreviousClick, 
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = GrayText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Play/Pause
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(PrimaryGreen, CircleShape)
                        .clickable { onPlayPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = onNextClick, 
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = GrayText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Symmetrical spacer to balance the back button
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun PlayerBottomSheetContent(
    currentPlayingAyahNum: Int?,
    playbackMode: PlaybackMode,
    onModeChange: (PlaybackMode) -> Unit,
    onClose: () -> Unit,
    currentAyah: CombinedAyah?,
    arabicFontName: String = "Amiri Quran"
) {
    val arabicFont = com.example.ui.theme.getArabicFont(arabicFontName)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "কুরআন প্লেয়ার",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = GrayText)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (currentAyah != null) {
            Text(
                text = currentAyah.arabicText,
                fontSize = 24.sp,
                fontFamily = arabicFont,
                textAlign = TextAlign.Center,
                color = DarkText,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentAyah.bengaliText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = GrayText,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        } else {
            Text(
                text = "কোনো আয়াত বাজানো হচ্ছে না",
                fontSize = 16.sp,
                color = GrayText
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mode Toggles (Single Ayah vs Continuous Surah)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OffWhite, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (playbackMode == PlaybackMode.AYAH) PrimaryGreen else Color.Transparent)
                    .clickable { onModeChange(PlaybackMode.AYAH) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "সিঙ্গেল আয়াত",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (playbackMode == PlaybackMode.AYAH) White else DarkText
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (playbackMode == PlaybackMode.SURAH) PrimaryGreen else Color.Transparent)
                    .clickable { onModeChange(PlaybackMode.SURAH) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "সূরা কন্টিনিউয়াস",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (playbackMode == PlaybackMode.SURAH) White else DarkText
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ReaderSettingsBottomSheetContent(
    showTranslation: Boolean,
    onShowTranslationToggle: (Boolean) -> Unit,
    arabicFontSize: Float,
    onArabicFontSizeChange: (Float) -> Unit,
    bengaliFontSize: Float,
    onBengaliFontSizeChange: (Float) -> Unit,
    arabicFontName: String = "Amiri Quran",
    onArabicFontNameChange: (String) -> Unit = {},
    isDownloadingOffline: Boolean,
    downloadProgress: Int,
    downloadStatus: String?,
    downloadError: String?,
    onDownloadClick: () -> Unit,
    onCancelDownloadClick: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "রিডার সেটিংস",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = GrayText)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Translation Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "বাংলা অনুবাদ প্রদর্শন",
                fontSize = 16.sp,
                color = DarkText
            )
            Switch(
                checked = showTranslation,
                onCheckedChange = onShowTranslationToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = PrimaryGreen,
                    uncheckedThumbColor = GrayText,
                    uncheckedTrackColor = OffWhite
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Arabic Font Size
        Text(
            text = "আরবি হরফের আকার: ${arabicFontSize.toInt()} sp",
            fontSize = 14.sp,
            color = GrayText
        )
        Slider(
            value = arabicFontSize,
            onValueChange = onArabicFontSizeChange,
            valueRange = 24f..48f,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryGreen,
                activeTrackColor = PrimaryGreen,
                inactiveTrackColor = OffWhite
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bengali Font Size
        Text(
            text = "বাংলা হরফের আকার: ${bengaliFontSize.toInt()} sp",
            fontSize = 14.sp,
            color = GrayText
        )
        Slider(
            value = bengaliFontSize,
            onValueChange = onBengaliFontSizeChange,
            valueRange = 12f..28f,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryGreen,
                activeTrackColor = PrimaryGreen,
                inactiveTrackColor = OffWhite
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Arabic Font Style Selection
        Text(
            text = "আরবি ফন্ট নির্বাচন করুন",
            fontSize = 14.sp,
            color = GrayText
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(com.example.ui.theme.arabicFontsList) { font ->
                val isSelected = font == arabicFontName
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) PrimaryGreen else OffWhite,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) PrimaryGreen else Border,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onArabicFontNameChange(font) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = font,
                            color = if (isSelected) White else DarkText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الحمد لله",
                            color = if (isSelected) White else GrayText,
                            fontSize = 18.sp,
                            fontFamily = com.example.ui.theme.getArabicFont(font)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Border)
        Spacer(modifier = Modifier.height(16.dp))

        // Offline Download Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "অফলাইন ডাউনলোড",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (isDownloadingOffline) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF06B6D4).copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = downloadStatus ?: "অডিও ও ডাটা ফাইল ডাউনলোড হচ্ছে...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ডাউনলোড অগ্রগতি:",
                        fontSize = 11.sp,
                        color = GrayText
                    )
                    Text(
                        text = "$downloadProgress%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF06B6D4)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF06B6D4),
                    trackColor = Border
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCancelDownloadClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ডাউনলোড বাতিল করুন", fontSize = 12.sp)
                }
            }
        } else {
            downloadStatus?.let { status ->
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            downloadError?.let { err ->
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onDownloadClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("সম্পূর্ণ সুরার অফলাইন অডিও ও ডাটা নামান", fontSize = 12.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MushafPageView(
    page: Int,
    ayahs: List<CombinedAyah>,
    surahNumber: Int,
    onPlayWord: (String) -> Unit,
    onPlayAyah: (CombinedAyah) -> Unit,
    arabicFontName: String = "Amiri Quran",
    arabicFontSize: Float = 28f
) {
    val arabicFont = com.example.ui.theme.getArabicFont(arabicFontName)
    
    // Build a single, unified annotated string for all ayahs of this page to ensure continuous flow
    val annotatedString = remember(ayahs, surahNumber) {
        buildAnnotatedString {
            ayahs.forEachIndexed { index, ayah ->
                val ayahStart = length
                
                if (ayah.words.isNotEmpty()) {
                    val processedWords = mutableListOf<ProcessedWord>()
                    ayah.words.forEach { word ->
                        val text = word.textUthmani ?: ""
                        val isPause = word.charTypeName == "pause" || 
                                      word.charTypeName == "stop" ||
                                      text.trim() in listOf("ۖ", "ۗ", "ۚ", "ۛ", "ۜ", "ۘ", "ۙ", "ج")
                        
                        if (isPause) {
                            val lastWordIndex = processedWords.indexOfLast { it.charTypeName == "word" }
                            if (lastWordIndex != -1) {
                                val lastWord = processedWords[lastWordIndex]
                                processedWords[lastWordIndex] = lastWord.copy(
                                    textUthmani = lastWord.textUthmani + text
                                )
                            } else {
                                processedWords.add(ProcessedWord(word.id, word.position, "word", text, word.translation?.text))
                            }
                        } else {
                            processedWords.add(ProcessedWord(word.id, word.position, word.charTypeName, text, word.translation?.text))
                        }
                    }
                    
                    processedWords.forEachIndexed { wIndex, word ->
                        if (word.charTypeName != "end") {
                            val wordStart = length
                            append(word.textUthmani)
                            val wordEnd = length
                            
                            val url = String.format(java.util.Locale.US, "https://verses.quran.com/wbw/%03d_%03d_%03d.mp3", surahNumber, ayah.numberInSurah, word.position)
                            addStringAnnotation(
                                tag = "word_url",
                                annotation = url,
                                start = wordStart,
                                end = wordEnd
                            )
                            
                            if (wIndex < processedWords.lastIndex) {
                                append(" ")
                            }
                        }
                    }
                } else {
                    // Fallback to full Arabic text if words are empty
                    append(ayah.arabicText)
                }
                
                append(" ")
                
                // Add the inline Ayah Circle
                val circleId = "circle_${ayah.numberInSurah}"
                appendInlineContent(circleId, "\uFFFC")
                
                // Add a space between ayahs
                if (index < ayahs.lastIndex) {
                    append("  ")
                }
                
                val ayahEnd = length
                // Add base ayah_index annotation covering the entire ayah text, circle, and spacing
                addStringAnnotation(
                    tag = "ayah_index",
                    annotation = index.toString(),
                    start = ayahStart,
                    end = ayahEnd
                )
            }
        }
    }
    
    val inlineContent = remember(ayahs) {
        ayahs.associate { ayah ->
            val circleId = "circle_${ayah.numberInSurah}"
            circleId to InlineTextContent(
                Placeholder(
                    width = (arabicFontSize * 1.35f).sp,
                    height = (arabicFontSize * 1.35f).sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                AyahCircle(
                    number = ayah.numberInSurah,
                    fontSize = arabicFontSize,
                    color = PrimaryGreen,
                    modifier = Modifier.clickable { onPlayAyah(ayah) }
                )
            }
        }
    }
    
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            Text(
                text = annotatedString,
                inlineContent = inlineContent,
                fontSize = arabicFontSize.sp,
                lineHeight = (arabicFontSize * 1.65f).sp,
                fontFamily = arabicFont,
                color = DarkText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .pointerInput(annotatedString) {
                        detectTapGestures { offset ->
                            val currentLayoutResult = layoutResult ?: return@detectTapGestures
                            val charIndex = currentLayoutResult.getOffsetForPosition(offset)
                            
                            // Check if word_url annotation exists at click offset
                            val wordUrlAnnotations = annotatedString.getStringAnnotations(
                                tag = "word_url",
                                start = charIndex,
                                end = charIndex
                            )
                            
                            if (wordUrlAnnotations.isNotEmpty()) {
                                onPlayWord(wordUrlAnnotations.first().item)
                            } else {
                                // Fallback: check if ayah_index annotation exists
                                val ayahAnnotations = annotatedString.getStringAnnotations(
                                    tag = "ayah_index",
                                    start = charIndex,
                                    end = charIndex
                                )
                                if (ayahAnnotations.isNotEmpty()) {
                                    val idx = ayahAnnotations.first().item.toIntOrNull()
                                    if (idx != null && idx in ayahs.indices) {
                                        onPlayAyah(ayahs[idx])
                                    }
                                }
                            }
                        }
                    },
                onTextLayout = { layoutResult = it }
            )
        }
        
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

@Composable
fun AyahCircle(
    number: Int,
    fontSize: Float,
    color: Color = PrimaryGreen,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    // Size is proportional to font size to ensure it scales perfectly and stays compact
    val boxSize = (fontSize * 1.35f).dp
    val textFontSize = (fontSize * 0.55f).sp
    
    Box(
        modifier = modifier
            .size(boxSize)
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val radius = size.minDimension / 2f
            
            // Outer dynamic circle
            drawCircle(
                color = color,
                radius = radius - 1.dp.toPx(),
                style = Stroke(width = 1.5.dp.toPx())
            )
            
            // Inner decorative dynamic solid line
            val innerRadius = radius - 3.dp.toPx()
            if (innerRadius > 0) {
                drawCircle(
                    color = color.copy(alpha = 0.35f),
                    radius = innerRadius,
                    style = Stroke(
                        width = 0.8.dp.toPx()
                    )
                )
            }
        }
        Text(
            text = number.toArabicNumerals(),
            color = DarkText,
            fontSize = textFontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = amiriFont,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 1.dp)
        )
    }
}

@Composable
fun AyahInlineText(
    arabicText: String,
    ayahNumber: Int,
    fontSize: Float,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    color: Color,
    modifier: Modifier = Modifier
) {
    val inlineContentId = "ayah_circle_${ayahNumber}"
    val annotatedString = buildAnnotatedString {
        append(arabicText)
        append(" ")
        appendInlineContent(inlineContentId, "\uFFFC")
    }
    
    val inlineContent = mapOf(
        inlineContentId to InlineTextContent(
            Placeholder(
                width = (fontSize * 1.35f).sp,
                height = (fontSize * 1.35f).sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            AyahCircle(
                number = ayahNumber,
                fontSize = fontSize,
                color = PrimaryGreen
            )
        }
    )
    
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = annotatedString,
            inlineContent = inlineContent,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.6f).sp,
            fontFamily = fontFamily,
            color = color,
            textAlign = TextAlign.Right,
            modifier = modifier
        )
    }
}
