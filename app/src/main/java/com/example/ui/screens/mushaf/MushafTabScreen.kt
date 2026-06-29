package com.example.ui.screens.mushaf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.model.MushafStyle
import com.example.ui.viewmodels.MushafSelectionViewModel

@Composable
fun MushafTabScreen(
    onMushafSelected: (MushafStyle) -> Unit,
    viewModel: MushafSelectionViewModel
) {
    val mushafs by viewModel.mushafs.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()

    MushafSelectionScreen(
        mushafs = mushafs,
        downloadStatus = downloadStatus,
        onSelectMushaf = onMushafSelected,
        onDownload = { viewModel.downloadMushaf(it) },
        onPause = { viewModel.pauseDownload(it) },
        onCancel = { viewModel.cancelDownload(it) },
        onDelete = { viewModel.deleteMushaf(it) }
    )
}
