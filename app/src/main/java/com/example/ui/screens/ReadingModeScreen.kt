package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CombinedAyah
import com.example.ui.state.UiState
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

    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(surahNumber, tanzilTextStyle) {
        viewModel.loadSurah(surahNumber)
    }

    // Determine colors based on theme
    val (backgroundColor, textColor) = when (theme) {
        "Dark" -> Pair(Color(0xFF121212), Color(0xFFE0E0E0))
        "Sepia" -> Pair(Color(0xFFFBF0D9), Color(0xFF5D4037))
        else -> Pair(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Surah $surahNumber") },
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
                    containerColor = backgroundColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor,
                    actionIconContentColor = textColor
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadSurah(surahNumber) }) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            // Render all ayahs continuously like a paragraph for reading mode
                            val combinedText = state.data.joinToString(" ") { "${it.arabicText} ﴿${it.numberInSurah}﴾" }
                            Text(
                                text = combinedText,
                                fontSize = arabicFontSize.sp,
                                lineHeight = (arabicFontSize * 1.5).sp,
                                textAlign = TextAlign.Justify,
                                color = textColor,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                ReadingSettingsContent(
                    arabicFontSize = arabicFontSize,
                    onArabicFontSizeChange = { viewModel.setArabicFontSize(it) },
                    theme = theme,
                    onThemeChange = { viewModel.setTheme(it) },
                    tanzilTextStyle = tanzilTextStyle,
                    onTanzilTextStyleChange = { viewModel.setTanzilTextStyle(it) }
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
    onTanzilTextStyleChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Reading Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(text = "Arabic Font Size: ${arabicFontSize.toInt()}sp", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = arabicFontSize,
            onValueChange = onArabicFontSizeChange,
            valueRange = 18f..40f,
            steps = 22
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Theme", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ThemeOption(name = "Light", currentTheme = theme, onClick = { onThemeChange("Light") })
            ThemeOption(name = "Dark", currentTheme = theme, onClick = { onThemeChange("Dark") })
            ThemeOption(name = "Sepia", currentTheme = theme, onClick = { onThemeChange("Sepia") })
        }
        
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Tanzil Script Style", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val scripts = listOf(
                Pair("quran-uthmani", "Uthmani"),
                Pair("quran-simple", "Simple"),
                Pair("quran-simple-clean", "Clean"),
                Pair("quran-simple-plain", "Plain")
            )
            scripts.forEach { (styleId, styleName) ->
                val isSelected = styleId == tanzilTextStyle
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTanzilTextStyleChange(styleId) }
                ) {
                    Text(
                        text = styleName,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
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
