package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.example.ui.viewmodels.ReadingModeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingModeScreen(
    surahNumber: Int,
    viewModel: ReadingModeViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val arabicFontSize by viewModel.arabicFontSize.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val tanzilTextStyle by viewModel.tanzilTextStyle.collectAsState()
    val showWaqfSigns by viewModel.showWaqfSigns.collectAsState()

    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(surahNumber, tanzilTextStyle) {
        viewModel.loadSurah(surahNumber)
    }

    // Determine theme colors
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
                    val firstAyah = (uiState as? UiState.Success)?.data?.firstOrNull()
                    val surahData = firstAyah?.let { com.example.data.QuranData.surahNames.find { s -> s.first == it.surahNumber } }
                    val surahNameBangla = surahData?.second?.second ?: "সূরা $surahNumber"
                    Text(surahNameBangla, fontWeight = FontWeight.Bold, fontSize = 18.sp) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Reading Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = topBarContentColor,
                    navigationIconContentColor = topBarContentColor,
                    actionIconContentColor = topBarContentColor
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding)
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
                            onClick = { viewModel.loadSurah(surahNumber) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    val firstAyah = state.data.firstOrNull()
                    val surahData = firstAyah?.let { com.example.data.QuranData.surahNames.find { s -> s.first == it.surahNumber } }
                    val surahNameArabic = surahData?.second?.first ?: "سورة $surahNumber"
                    val juzNum = firstAyah?.juz ?: 1
                    val juzName = "পারা ${juzNum.toBengaliNumerals()}"

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            TanzilMushafFrame(
                                titleRight = surahNameArabic,
                                titleLeft = juzName,
                                pageNumber = if (state.data.isNotEmpty()) state.data.first().page.toBengaliNumerals() else "",
                                theme = theme
                            ) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    val arabicFont = getArabicFont("") // Amiri Quran by default
                                    val annotatedString = remember(state.data, showWaqfSigns) {
                                        buildAnnotatedString {
                                            state.data.forEachIndexed { index, ayah ->
                                                val textToDisplay = if (showWaqfSigns) ayah.arabicText else ayah.arabicText.removeWaqfSigns()
                                                append(textToDisplay)
                                                
                                                val numInSurahStr = ayah.numberInSurah.toArabicNumerals()
                                                append(" ﴿$numInSurahStr﴾")
                                                
                                                if (index < state.data.lastIndex) {
                                                    append("   ")
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
                }
            }
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = containerColor
            ) {
                ReadingSettingsContent(
                    arabicFontSize = arabicFontSize,
                    onArabicFontSizeChange = { viewModel.setArabicFontSize(it) },
                    theme = theme,
                    onThemeChange = { viewModel.setTheme(it) },
                    tanzilTextStyle = tanzilTextStyle,
                    onTanzilTextStyleChange = { viewModel.setTanzilTextStyle(it) },
                    showWaqfSigns = showWaqfSigns,
                    onShowWaqfSignsToggle = { viewModel.setShowWaqfSigns(it) },
                    topBarContentColor = topBarContentColor,
                    containerColor = containerColor
                )
            }
        }
    }
}

@Composable
fun ReadingSettingsContent(
    arabicFontSize: Float,
    onArabicFontSizeChange: (Float) -> Unit,
    theme: String,
    onThemeChange: (String) -> Unit,
    tanzilTextStyle: String,
    onTanzilTextStyleChange: (String) -> Unit,
    showWaqfSigns: Boolean = true,
    onShowWaqfSignsToggle: (Boolean) -> Unit = {},
    topBarContentColor: Color,
    containerColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "পঠন সেটিংস",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = topBarContentColor,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Arabic font size
        Text(
            text = "আরবি ফ্রন্ট সাইজ: ${arabicFontSize.toInt().toBengaliNumerals()}sp", 
            style = MaterialTheme.typography.bodyMedium,
            color = topBarContentColor
        )
        Slider(
            value = arabicFontSize,
            onValueChange = onArabicFontSizeChange,
            valueRange = 18f..40f,
            steps = 22,
            colors = SliderDefaults.colors(
                thumbColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631),
                activeTrackColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631)
            )
        )

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
                onCheckedChange = onShowWaqfSignsToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = containerColor.copy(alpha = 0.5f)
                )
            )
        }

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
                        .clickable { onThemeChange(tKey) }
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
        
        Spacer(modifier = Modifier.height(20.dp))

        // Tanzil Quran Script selector
        Text(
            text = "কুরআন স্ক্রিপ্ট স্টাইল", 
            style = MaterialTheme.typography.bodyMedium, 
            color = topBarContentColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val scripts = listOf(
                Pair("quran-uthmani", "উসমানী"),
                Pair("quran-simple", "সহজ"),
                Pair("quran-simple-clean", "ক্লিন"),
                Pair("quran-simple-plain", "প্লেইন")
            )
            scripts.forEach { (styleId, styleName) ->
                val isSel = styleId == tanzilTextStyle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) (if (theme == "Dark") Color(0xFF6B5843) else Color(0xFF1E5631)) else containerColor.copy(alpha = 0.5f),
                    contentColor = if (isSel) Color.White else topBarContentColor,
                    border = BorderStroke(1.dp, topBarContentColor.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTanzilTextStyleChange(styleId) }
                ) {
                    Text(
                        text = styleName,
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

@Composable
fun ThemeOption(name: String, currentTheme: String, onClick: () -> Unit) {
    val isSelected = name == currentTheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
