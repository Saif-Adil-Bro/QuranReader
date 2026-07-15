package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.QuranIndexComponent
import com.example.ui.viewmodels.QuranListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranListScreen(
    viewModel: QuranListViewModel,
    mode: String = "normal",
    onSurahClick: (Int) -> Unit,
    onJuzClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        containerColor = Color(0xFFFCFAF2), // Premium Warm background
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (mode == "reading") "প্যারাগ্রাফ পঠন" else "সূরা তালিকা",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E4534)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF2E4534))
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF2E4534))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFCFAF2),
                    titleContentColor = Color(0xFF2E4534),
                    navigationIconContentColor = Color(0xFF2E4534),
                    actionIconContentColor = Color(0xFF2E4534)
                )
            )
        }
    ) { padding ->
        QuranIndexComponent(
            modifier = Modifier.padding(padding),
            uiState = uiState,
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onSurahClick = onSurahClick,
            onJuzClick = onJuzClick,
            onRetryClick = { viewModel.loadSurahs() }
        )
    }
}

