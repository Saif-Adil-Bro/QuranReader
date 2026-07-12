package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CombinedAyah
import com.example.data.model.removeWaqfSigns
import com.example.ui.state.UiState
import com.example.ui.theme.getArabicFont
import com.example.ui.viewmodels.HafeziModeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HafeziModeScreen(
    viewModel: HafeziModeViewModel,
    initialPage: Int,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val isPageMemorized by viewModel.isPageMemorized.collectAsState()
    val currentPlayingAyahNumber by viewModel.currentPlayingAyahNumber.collectAsState()
    val repeatCount by viewModel.repeatCount.collectAsState()
    
    val currentPage by viewModel.currentPage.collectAsState()
    val arabicFontSize by viewModel.arabicFontSize.collectAsState()
    val arabicFontName by viewModel.arabicFontName.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val showWaqfSigns by viewModel.showWaqfSigns.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(initialPage) {
        if (currentPage == 1 && initialPage != 1) {
            viewModel.loadPage(initialPage)
        } else if (uiState is UiState.Loading) {
            viewModel.loadPage(currentPage)
        }
    }

    // Dynamic coloring based on user-selected reading theme
    val backgroundColor = when (theme) {
        "Dark" -> Color(0xFF121212)
        "Sepia" -> Color(0xFFFBF0D9)
        else -> Color(0xFFF8F6F0)
    }
    val containerColor = when (theme) {
        "Dark" -> Color(0xFF1E1E1E)
        "Sepia" -> Color(0xFFF1E4C3)
        else -> Color(0xFFEFECE4)
    }
    val topBarContentColor = when (theme) {
        "Dark" -> Color(0xFFE0E0E0)
        "Sepia" -> Color(0xFF5D4037)
        else -> Color(0xFF333333)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "পৃষ্ঠা ${currentPage.toBengaliNumerals()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFFE5A93C) else topBarContentColor
                        )
                    }
                    IconButton(onClick = { viewModel.toggleMemorized() }) {
                        Icon(
                            if (isPageMemorized) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = "Mark Memorized",
                            tint = if (isPageMemorized) Color(0xFF1E5631) else topBarContentColor
                        )
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = topBarContentColor,
                    navigationIconContentColor = topBarContentColor,
                    actionIconContentColor = topBarContentColor
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = containerColor,
                contentColor = topBarContentColor
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.previousPage() }, 
                        enabled = currentPage > 1,
                        colors = ButtonDefaults.textButtonColors(contentColor = topBarContentColor)
                    ) {
                        Text("পূর্ববর্তী", fontWeight = FontWeight.Bold)
                    }
                    
                    FloatingActionButton(
                        onClick = {
                            if (isPlaying) viewModel.pauseAudio()
                            else viewModel.playAudio()
                        },
                        containerColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(2.dp)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }
                    
                    TextButton(
                        onClick = { viewModel.nextPage() }, 
                        enabled = currentPage < 604,
                        colors = ButtonDefaults.textButtonColors(contentColor = topBarContentColor)
                    ) {
                        Text("পরবর্তী", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631)
                    )
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadPage(currentPage) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    HafeziPageContent(
                        ayahs = state.data,
                        playingAyahNumber = currentPlayingAyahNumber,
                        arabicFontSize = arabicFontSize,
                        arabicFontName = arabicFontName,
                        theme = theme,
                        currentPage = currentPage,
                        showWaqfSigns = showWaqfSigns
                    )
                }
            }
        }
        
        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = containerColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "পঠন ও অডিও সেটিংস",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = topBarContentColor,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Repeat count
                    Text(
                        text = "আয়াত পুনরাবৃত্তি: ${repeatCount.toBengaliNumerals()} বার", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = topBarContentColor
                    )
                    Slider(
                        value = repeatCount.toFloat(),
                        onValueChange = { viewModel.setRepeatCount(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631),
                            activeTrackColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Font Size Slider
                    Text(
                        text = "আরবি ফ্রন্ট সাইজ: ${arabicFontSize.toInt().toBengaliNumerals()}sp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = topBarContentColor
                    )
                    Slider(
                        value = arabicFontSize,
                        onValueChange = { viewModel.setArabicFontSize(it) },
                        valueRange = 18f..40f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631),
                            activeTrackColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Theme selector
                    Text(
                        text = "থিম", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = topBarContentColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Light" to "লাইট", "Sepia" to "সেপিয়া", "Dark" to "ডার্ক").forEach { (tKey, tName) ->
                            val isSel = tKey == theme
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) (if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631)) else containerColor.copy(alpha = 0.5f),
                                contentColor = if (isSel) Color.White else topBarContentColor,
                                border = BorderStroke(1.dp, topBarContentColor.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setTheme(tKey) }
                            ) {
                                Text(
                                    text = tName,
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Waqf signs toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "থামার চিহ্ন প্রদর্শন (ম, জ, ছলে, ইত্যাদি)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = topBarContentColor
                        )
                        Switch(
                            checked = showWaqfSigns,
                            onCheckedChange = { viewModel.setShowWaqfSigns(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = containerColor.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Font style selector
                    Text(
                        text = "ফন্ট স্টাইল", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = topBarContentColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val fonts = listOf("Amiri Quran" to "উসমানী", "Amiri" to "আমিরী", "Scheherazade New" to "শাহরাজাদ")
                        fonts.forEach { (fKey, fName) ->
                            val isSel = fKey == arabicFontName
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) (if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631)) else containerColor.copy(alpha = 0.5f),
                                contentColor = if (isSel) Color.White else topBarContentColor,
                                border = BorderStroke(1.dp, topBarContentColor.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setArabicFontName(fKey) }
                            ) {
                                Text(
                                    text = fName,
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun HafeziPageContent(
    ayahs: List<CombinedAyah>, 
    playingAyahNumber: Int?,
    arabicFontSize: Float,
    arabicFontName: String,
    theme: String,
    currentPage: Int,
    showWaqfSigns: Boolean = true
) {
    val arabicFont = getArabicFont(arabicFontName)
    val firstAyah = ayahs.firstOrNull()
    
    // Resolve Page Headers
    val surahData = firstAyah?.let { com.example.data.QuranData.surahNames.find { s -> s.first == it.surahNumber } }
    val surahNameArabic = surahData?.second?.first ?: "" // e.g. سورة الفاتحة
    val juzNum = firstAyah?.juz ?: 1
    val juzName = "পারা ${juzNum.toBengaliNumerals()}"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TanzilMushafFrame(
            titleRight = surahNameArabic,
            titleLeft = juzName,
            pageNumber = currentPage.toBengaliNumerals(),
            theme = theme
        ) {
            // Flow the text in Right-to-Left (RTL) mode, completely avoiding bracket layout bugs
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val annotatedString = remember(ayahs, playingAyahNumber, theme, showWaqfSigns) {
                    buildAnnotatedString {
                        ayahs.forEachIndexed { index, ayah ->
                            val start = length
                            val textToDisplay = if (showWaqfSigns) ayah.arabicText else ayah.arabicText.removeWaqfSigns()
                            append(textToDisplay)
                            
                            // Beautiful unicode ornament brackets for verse numbers
                            val numInSurahStr = ayah.numberInSurah.toArabicNumerals()
                            append(" ﴿$numInSurahStr﴾")
                            
                            val end = length
                            
                            // Highlight currently active/playing verse beautifully in green/accent shade
                            if (ayah.number == playingAyahNumber) {
                                addStyle(
                                    style = SpanStyle(
                                        background = if (theme == "Dark") Color(0xFF1E3524) else Color(0xFFE8F5E9),
                                        color = if (theme == "Dark") Color(0xFF81C784) else Color(0xFF1B5E20),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    start = start,
                                    end = end
                                )
                            }
                            
                            if (index < ayahs.lastIndex) {
                                append("   ") // Visual negative space between consecutive verses
                            }
                        }
                    }
                }

                Text(
                    text = annotatedString,
                    fontSize = arabicFontSize.sp,
                    lineHeight = (arabicFontSize * 1.65f).sp,
                    fontFamily = arabicFont,
                    color = when (theme) {
                        "Dark" -> Color(0xFFE0E0E0)
                        "Sepia" -> Color(0xFF4E342E)
                        else -> Color(0xFF1A1A1A)
                    },
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TanzilMushafFrame(
    titleRight: String = "",
    titleLeft: String = "",
    pageNumber: String = "",
    theme: String = "Light",
    content: @Composable BoxScope.() -> Unit
) {
    // Elegant, authentic Mushaf border styling mimicking Tanzil.net
    val backgroundColor = when (theme) {
        "Dark" -> Color(0xFF1E1E1E)
        "Sepia" -> Color(0xFFF7EED6)
        else -> Color(0xFFFDFBF7)
    }
    val borderColor = when (theme) {
        "Dark" -> Color(0xFF5A493B)
        "Sepia" -> Color(0xFF8B6C4F)
        else -> Color(0xFF8B7355) // Antique gold border
    }
    val innerBorderColor = when (theme) {
        "Dark" -> Color(0xFF3E3127)
        "Sepia" -> Color(0xFFB59372)
        else -> Color(0xFFC5A059) // Thin golden guide lines
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(3.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        // Inner thin guiding border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, innerBorderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            // Draw traditional corner accents at the four inner corners
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val thickness = 1.dp.toPx()
                val offset = 1.dp.toPx()
                val length = 6.dp.toPx()
                
                // Top-Left cornerTicks
                drawLine(innerBorderColor, androidx.compose.ui.geometry.Offset(offset, offset), androidx.compose.ui.geometry.Offset(offset + length, offset), thickness)
                drawLine(innerBorderColor, androidx.compose.ui.geometry.Offset(offset, offset), androidx.compose.ui.geometry.Offset(offset, offset + length), thickness)
                
                // Top-Right cornerTicks
                drawLine(innerBorderColor, androidx.compose.ui.geometry.Offset(w - offset, offset), androidx.compose.ui.geometry.Offset(w - offset - length, offset), thickness)
                drawLine(innerBorderColor, androidx.compose.ui.geometry.Offset(w - offset, offset), androidx.compose.ui.geometry.Offset(w - offset, offset + length), thickness)
                
                // Bottom-Left cornerTicks
                drawLine(innerBorderColor, androidx.compose.ui.geometry.Offset(offset, h - offset), androidx.compose.ui.geometry.Offset(offset + length, h - offset), thickness)
                drawLine(innerBorderColor, androidx.compose.ui.geometry.Offset(offset, h - offset), androidx.compose.ui.geometry.Offset(offset, h - offset - length), thickness)
                
                // Bottom-Right cornerTicks
                drawLine(innerBorderColor, androidx.compose.ui.geometry.Offset(w - offset, h - offset), androidx.compose.ui.geometry.Offset(w - offset - length, h - offset), thickness)
                drawLine(innerBorderColor, androidx.compose.ui.geometry.Offset(w - offset, h - offset), androidx.compose.ui.geometry.Offset(w - offset, h - offset - length), thickness)
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                // Render Page Headers if they are provided
                if (titleRight.isNotEmpty() || titleLeft.isNotEmpty() || pageNumber.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = titleLeft,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = borderColor.copy(alpha = 0.85f)
                        )
                        if (pageNumber.isNotEmpty()) {
                            Text(
                                text = "— পৃষ্ঠা $pageNumber —",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = borderColor.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = titleRight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = borderColor.copy(alpha = 0.85f)
                        )
                    }
                    HorizontalDivider(
                        color = innerBorderColor.copy(alpha = 0.35f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Main Content inside the frame
                Box(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }
        }
    }
}
