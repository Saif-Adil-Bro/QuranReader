package com.example.ui.screens.mushaf

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.mushaf.components.PageViewer
import com.example.ui.viewmodels.MushafViewerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MushafViewerScreen(
    mushafId: String,
    initialPage: Int = 1,
    onBack: () -> Unit,
    viewModel: MushafViewerViewModel
) {
    LaunchedEffect(mushafId, initialPage) {
        viewModel.initMushaf(mushafId, initialPage)
    }

    val currentPage by viewModel.currentPageNumber.collectAsState()
    val pagePath by viewModel.currentPagePath.collectAsState()
    val isPdf by viewModel.isPdf.collectAsState()
    val pdfPageOffset by viewModel.pdfPageOffset.collectAsState()
    
    val pagerState = rememberPagerState(initialPage = initialPage - 1, pageCount = { 604 })
    var showSelectorSheet by remember { mutableStateOf(false) }
    var showOffsetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        val newPage = pagerState.currentPage + 1
        if (newPage != currentPage) {
            viewModel.jumpToPage(newPage)
        }
    }
    
    LaunchedEffect(currentPage) {
        if (pagerState.currentPage + 1 != currentPage) {
            pagerState.animateScrollToPage(currentPage - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("পৃষ্ঠা $currentPage", fontWeight = FontWeight.Bold, fontSize = 18.sp) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isPdf) {
                        IconButton(onClick = { showOffsetDialog = true }) {
                            Icon(Icons.Default.Tune, contentDescription = "Page Offset Alignment", tint = Color(0xFF10B981))
                        }
                    }
                    IconButton(onClick = { showSelectorSheet = true }) {
                        Icon(Icons.Default.List, contentDescription = "Surah / Para Selector", tint = Color(0xFF10B981))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF10B981),
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true // Quran is read right-to-left
            ) { page ->
                val pageNum = page + 1
                OnDemandPageViewer(
                    mushafId = mushafId,
                    pageNumber = pageNum,
                    viewModel = viewModel
                )
            }
        }
    }

    if (showSelectorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSelectorSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            var selectedTab by remember { mutableStateOf(0) } // 0 for Surah, 1 for Para
            var searchQuery by remember { mutableStateOf("") }
            
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "দ্রুত নেভিগেশন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF10B981),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF10B981)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; searchQuery = "" },
                        text = { Text("সূরা সূচী", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; searchQuery = "" },
                        text = { Text("পারা সূচী", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (selectedTab == 0) "সূরা খুঁজুন..." else "পারা খুঁজুন...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        cursorColor = Color(0xFF10B981)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    val filteredSurahs = remember(searchQuery) {
                        com.example.data.QuranData.surahNames.filter { surah ->
                            val num = surah.first.toString()
                            val name = surah.second.first
                            val meaning = surah.second.second
                            num.contains(searchQuery) || name.contains(searchQuery) || meaning.contains(searchQuery)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredSurahs) { surah ->
                            val surahNum = surah.first
                            val name = surah.second.first
                            val meaning = surah.second.second
                            val startPage = com.example.data.QuranData.surahStartPages[surahNum - 1]
                            val isCurrent = currentPage in startPage..(if (surahNum < 114) com.example.data.QuranData.surahStartPages[surahNum] - 1 else 604)
                            
                            Surface(
                                onClick = {
                                    viewModel.jumpToPage(startPage)
                                    showSelectorSheet = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) Color(0xFFECFDF5) else Color(0xFFF3F4F6),
                                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFF10B981), shape = CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = surahNum.toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF1F2937)
                                            )
                                            Text(
                                                text = meaning,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "পৃষ্ঠা $startPage",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF059669)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val filteredParas = remember(searchQuery) {
                        com.example.data.QuranData.juzList.filter { juz ->
                            val num = juz.first.toString()
                            val name = juz.second
                            num.contains(searchQuery) || name.contains(searchQuery)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredParas) { juz ->
                            val juzNum = juz.first
                            val name = juz.second
                            val startPage = juz.third
                            val isCurrent = currentPage in startPage..(if (juzNum < 30) com.example.data.QuranData.juzList[juzNum].third - 1 else 604)
                            
                            Surface(
                                onClick = {
                                    viewModel.jumpToPage(startPage)
                                    showSelectorSheet = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) Color(0xFFECFDF5) else Color(0xFFF3F4F6),
                                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFF059669), shape = CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = juzNum.toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF1F2937)
                                        )
                                    }
                                    Text(
                                        text = "পৃষ্ঠা $startPage",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF059669)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOffsetDialog) {
        AlertDialog(
            onDismissRequest = { showOffsetDialog = false },
            title = {
                Text(
                    text = "পৃষ্ঠা সামঞ্জস্য (Page Offset Alignment)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF10B981)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "যদি সূচী অনুযায়ী সূরার পাতাটি ঠিকভাবে না আসে, তাহলে অফসেট পরিবর্তন করুন। বর্তমান অফসেট পরিবর্তন করলে পাতার সংখ্যা ডানে বা বামে সরবে।",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.adjustOffset(-1) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease Offset")
                        }
                        Text(
                            text = "অফসেট: $pdfPageOffset",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { viewModel.adjustOffset(1) }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase Offset")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOffsetDialog = false }) {
                    Text("ঠিক আছে", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun OnDemandPageViewer(
    mushafId: String,
    pageNumber: Int,
    viewModel: MushafViewerViewModel
) {
    var pagePath by remember(mushafId, pageNumber) { mutableStateOf<String?>(null) }
    var isLoading by remember(mushafId, pageNumber) { mutableStateOf(true) }
    var hasError by remember(mushafId, pageNumber) { mutableStateOf(false) }

    LaunchedEffect(mushafId, pageNumber) {
        isLoading = true
        hasError = false
        val path = viewModel.getPagePath(mushafId, pageNumber)
        if (path != null) {
            pagePath = path
            isLoading = false
        } else {
            val success = viewModel.downloadPageOnDemand(mushafId, pageNumber)
            if (success) {
                pagePath = viewModel.getPagePath(mushafId, pageNumber)
                isLoading = false
            } else {
                isLoading = false
                hasError = true
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF10B981))
                Spacer(modifier = Modifier.height(8.dp))
                Text("পাতা ডাউনলোড হচ্ছে...", color = Color.Gray, fontSize = 13.sp)
            }
        }
    } else if (hasError) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("পাতা লোড করা যায়নি", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("ইন্টারনেট সংযোগ চেক করে আবার চেষ্টা করুন", color = Color.Gray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        isLoading = true
                        hasError = false
                        pagePath = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("আবার চেষ্টা করুন", color = Color.White)
                }
            }
        }
    } else if (pagePath != null) {
        PageViewer(pagePath = pagePath!!)
    }
}
