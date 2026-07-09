package com.example.ui.screens.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadStatus
import com.example.data.model.MushafStyle
import com.example.ui.screens.mushaf.components.MushafCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafSelectionScreen(
    mushafs: List<MushafStyle>,
    downloadStatus: Map<String, DownloadStatus>,
    onSelectMushaf: (MushafStyle) -> Unit,
    onDownload: (MushafStyle) -> Unit,
    onPause: (String) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("মুসহাফ লাইব্রেরি", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = Color(0xFF10B981)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (mushafs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF10B981))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mushafs) { mushaf ->
                        MushafCard(
                            mushaf = mushaf,
                            downloadStatus = downloadStatus[mushaf.id],
                            onSelect = { onSelectMushaf(mushaf) },
                            onDownload = { onDownload(mushaf) },
                            onPause = { onPause(mushaf.id) },
                            onCancel = { onCancel(mushaf.id) },
                            onDelete = { onDelete(mushaf.id) }
                        )
                    }
                }
            }
        }
    }
}
