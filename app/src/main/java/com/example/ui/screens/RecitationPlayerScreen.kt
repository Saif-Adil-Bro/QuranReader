package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuranData
import com.example.ui.theme.*
import com.example.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecitationPlayerScreen(
    viewModel: HomeViewModel,
    onBackClick: () -> Unit
) {
    val currentPlayingSurah by viewModel.currentPlayingSurah.collectAsState()
    val currentPlayingAyahIndex by viewModel.currentPlayingAyahIndex.collectAsState()
    val currentPlayingAyahs by viewModel.currentPlayingAyahs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedQariId by viewModel.selectedQariId.collectAsState()
    val isRepeatAyahEnabled by viewModel.isRepeatAyahEnabled.collectAsState()
    val isRepeatSurahEnabled by viewModel.isRepeatSurahEnabled.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()

    var showQariSelectorDialog by remember { mutableStateOf(false) }

    val totalAyahs = currentPlayingAyahs.size
    val progress = if (totalAyahs > 0) {
        (currentPlayingAyahIndex + 1).toFloat() / totalAyahs.toFloat()
    } else {
        0.0f
    }

    val surahName = currentPlayingSurah?.let { surahNum ->
        QuranData.surahNames.find { it.first == surahNum }?.second?.first ?: "সূরা"
    } ?: "সূরা সিলেক্ট করুন"

    val englishName = currentPlayingSurah?.let { surahNum ->
        QuranData.surahNames.find { it.first == surahNum }?.second?.second ?: ""
    } ?: ""

    val qariName = when (selectedQariId) {
        "ar.alafasy" -> "মিশারি রশিদ আলাফাসি"
        "ar.abdulbasit" -> "আব্দুল বাসিদ"
        "ar.minshawi" -> "মুহাম্মাদ আল-মিনশাবি"
        "ar.hudhaify" -> "আলী আল-হুদাইফি"
        else -> "মিশারি রশিদ আলাফাসি"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "অডিও প্লেয়ার",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryGreen
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showQariSelectorDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Qari Selection",
                            tint = PrimaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = DarkText
                ),
                modifier = Modifier.shadow(1.dp)
            )
        },
        bottomBar = {
            if (currentPlayingSurah != null) {
                // CARD 3: Media Controller Card (Progress bar & rearranged controls)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                        .shadow(3.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Progress Bar with Timers
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(100.dp)),
                                color = PrimaryGreen,
                                trackColor = Border
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "আয়াত ${(currentPlayingAyahIndex + 1).toBengaliNumerals()}",
                                    fontSize = 11.sp,
                                    color = GrayText
                                )
                                Text(
                                    text = "মোট ${totalAyahs.toBengaliNumerals()} আয়াত",
                                    fontSize = 11.sp,
                                    color = GrayText
                                )
                            }
                        }

                        // Playback Controls Row (Loop1, Prev, Play/Pause, Next, Loop2)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ayah Repeat Toggle (Left side loop)
                            IconButton(
                                onClick = { viewModel.toggleRepeatAyah() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isRepeatAyahEnabled) PrimaryGreen else BackgroundGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RepeatOne,
                                    contentDescription = "Ayah Repeat",
                                    tint = if (isRepeatAyahEnabled) White else PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Prev Button
                            IconButton(
                                onClick = { viewModel.previousAyah() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(BackgroundGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Play/Pause Button
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(PrimaryGreen)
                                    .clickable {
                                        if (isPlaying) {
                                            viewModel.pauseSurahAudio()
                                        } else {
                                            viewModel.resumeSurahAudio()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Next Button
                            IconButton(
                                onClick = { viewModel.nextAyah() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(BackgroundGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Surah Repeat Toggle (Right side loop)
                            IconButton(
                                onClick = { viewModel.toggleRepeatSurah() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isRepeatSurahEnabled) PrimaryGreen else BackgroundGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = "Surah Repeat",
                                    tint = if (isRepeatSurahEnabled) White else PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF6F8F6)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentPlayingSurah == null) {
                // Empty state when no surah is selected or active
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = PrimaryGreen.copy(alpha = 0.3f),
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "কোনো সূরা চালু নেই",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "সূরা তালিকা থেকে একটি সূরা নির্বাচন করে প্লে করুন।",
                            fontSize = 13.sp,
                            color = GrayText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("সূরা তালিকা দেখুন", color = White)
                        }
                    }
                }
            } else {
                // CARD 1: Surah & Reciter Details Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "সূরা $surahName",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ক্বারী: $qariName",
                            fontSize = 13.sp,
                            color = GrayText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "আয়াত: ${(currentPlayingAyahIndex + 1).toBengaliNumerals()} / ${totalAyahs.toBengaliNumerals()}",
                            fontSize = 11.sp,
                            color = GrayText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // CARD 2: Currently Playing Ayah Content View with Arabic and Bengali translation (Internally Scrollable & Large)
                val currentAyahObj = currentPlayingAyahs.getOrNull(currentPlayingAyahIndex)
                if (currentAyahObj != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = currentAyahObj.arabicText,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    lineHeight = 38.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = Border.copy(alpha = 0.4f), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = currentAyahObj.bengaliText,
                                    fontSize = 14.sp,
                                    fontFamily = com.example.ui.theme.solaimanLipiFont,
                                    color = DarkText,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (showQariSelectorDialog) {
        QariSelectorDialog(
            selectedQariId = selectedQariId,
            onDismiss = { showQariSelectorDialog = false },
            onSelectQari = { qariId ->
                viewModel.setSelectedQariId(qariId)
                showQariSelectorDialog = false
            }
        )
    }
}
