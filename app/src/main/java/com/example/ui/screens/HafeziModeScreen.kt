package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Circle
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
import com.example.ui.viewmodels.HafeziModeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(initialPage) {
        if (currentPage == 1 && initialPage != 1) {
            viewModel.loadPage(initialPage)
        } else if (uiState is UiState.Loading) {
            viewModel.loadPage(currentPage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Page $currentPage") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleMemorized() }) {
                        Icon(
                            if (isPageMemorized) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = "Mark Memorized",
                            tint = if (isPageMemorized) Color(0xFF4CAF50) else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { viewModel.previousPage() }, enabled = currentPage > 1) {
                        Text("Prev Page")
                    }
                    
                    FloatingActionButton(
                        onClick = {
                            if (isPlaying) viewModel.pauseAudio()
                            else viewModel.playAudio()
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }
                    
                    Button(onClick = { viewModel.nextPage() }, enabled = currentPage < 604) {
                        Text("Next Page")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFFF8E1)) // Warm paper-like background
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
                        Button(onClick = { viewModel.loadPage(currentPage) }) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    HafeziPageContent(
                        ayahs = state.data,
                        playingAyahNumber = currentPlayingAyahNumber
                    )
                }
            }
        }
        
        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Hafezi Audio Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(text = "Repeat Audio Page: $repeatCount times", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = repeatCount.toFloat(),
                        onValueChange = { viewModel.setRepeatCount(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun HafeziPageContent(ayahs: List<CombinedAyah>, playingAyahNumber: Int?) {
    // In a real Hafezi mushaf, the layout is strictly 15 lines.
    // For simplicity in Compose without complex custom layout measurement, 
    // we flow the text inline and highlight the currently playing ayah.
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // A simple text rendering per ayah. 
        // For a more seamless block, we can join them, but highlighting individual ayahs
        // is easier this way.
        ayahs.forEach { ayah ->
            val isPlaying = ayah.number == playingAyahNumber
            
            Text(
                text = "${ayah.arabicText} ﴿${ayah.numberInSurah}﴾",
                fontSize = 24.sp,
                lineHeight = 38.sp,
                textAlign = TextAlign.Justify,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
            )
        }
    }
}
