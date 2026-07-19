package com.example.ui.screens.mushaf

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.screens.mushaf.components.PageViewer
import com.example.ui.viewmodels.MushafViewerViewModel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.activity.compose.BackHandler
import androidx.compose.ui.window.Dialog
import com.example.utils.DateUtil
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MushafViewerScreen(
    mushafId: String,
    initialPage: Int = 1,
    initialShowIndex: Boolean = false,
    onBack: () -> Unit,
    viewModel: MushafViewerViewModel
) {
    remember(mushafId, initialPage) {
        viewModel.initMushaf(mushafId, initialPage)
        true
    }

    val currentPage by viewModel.currentPageNumber.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val pagePath by viewModel.currentPagePath.collectAsState()
    val isPdf by viewModel.isPdf.collectAsState()
    val pdfPageOffset by viewModel.pdfPageOffset.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val isReady by viewModel.isReady.collectAsState()
    val isDownloaded by viewModel.isDownloaded.collectAsState()
    val currentTheme by viewModel.theme.collectAsState()
    val scrollDirection by viewModel.scrollDirection.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (currentTheme) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemDark
    }
    val isVertical = scrollDirection == "Vertical"
    
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    var showSelectorSheet by remember { mutableStateOf(initialShowIndex) }

    BackHandler(enabled = !showSelectorSheet) {
        showSelectorSheet = true
    }

    val pagerState = rememberPagerState(initialPage = initialPage - 1, pageCount = { totalPages })
    var showOffsetDialog by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        val newPage = pagerState.currentPage + 1
        if (newPage != currentPage) {
            viewModel.jumpToPage(newPage)
            viewModel.prefetchPages(mushafId, newPage)
        }
    }
    
    LaunchedEffect(currentPage) {
        if (pagerState.currentPage + 1 != currentPage) {
            pagerState.scrollToPage(currentPage - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "পৃষ্ঠা $currentPage", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp,
                        color = Color(0xFF10B981)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showSelectorSheet) {
                            showSelectorSheet = false
                            onBack()
                        } else {
                            showSelectorSheet = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = Color(0xFF10B981)
                        )
                    }
                    IconButton(onClick = { 
                        viewModel.setScrollDirection(if (isVertical) "Horizontal" else "Vertical") 
                    }) {
                        Icon(
                            imageVector = if (isVertical) Icons.Default.SwapHoriz else Icons.Default.SwapVert,
                            contentDescription = "Toggle Scroll Direction",
                            tint = Color(0xFF10B981)
                        )
                    }
                    IconButton(onClick = { showJumpDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "নির্দিষ্ট পৃষ্ঠায় যান", tint = Color(0xFF10B981))
                    }
                    if (isPdf) {
                        IconButton(onClick = { showOffsetDialog = true }) {
                            Icon(Icons.Default.Tune, contentDescription = "Page Offset Alignment", tint = Color(0xFF10B981))
                        }
                    }
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Page",
                            tint = if (isBookmarked) Color(0xFFE5A93C) else Color(0xFF10B981)
                        )
                    }
                    IconButton(onClick = { 
                        showSelectorSheet = true 
                    }) {
                        Icon(Icons.Default.List, contentDescription = "Surah / Para Selector", tint = Color(0xFF10B981))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF1A1A1A) else Color.White,
                    titleContentColor = Color(0xFF10B981),
                    navigationIconContentColor = if (isDark) Color.White else Color.Black
                )
            )
        },
        containerColor = if (isDark) Color.Black else Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color.Black else Color.White)
                .padding(paddingValues)
        ) {
            if (!isReady) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF10B981))
                }
            } else if (isVertical) {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val pageNum = page + 1
                    OnDemandPageViewer(
                        mushafId = mushafId,
                        pageNumber = pageNum,
                        pdfPageOffset = pdfPageOffset,
                        isDark = isDark,
                        viewModel = viewModel
                    )
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    // Quran is read right-to-left
                    
                ) { page ->
                    val pageNum = page + 1
                    OnDemandPageViewer(
                        mushafId = mushafId,
                        pageNumber = pageNum,
                        pdfPageOffset = pdfPageOffset,
                        isDark = isDark,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (showSelectorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSelectorSheet = false },
            containerColor = if (isDark) Color(0xFF1C1C1E) else Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(showSelectorSheet) {
                if (showSelectorSheet) {
                    focusRequester.requestFocus()
                }
            }
            BackHandler(enabled = true) {
                showSelectorSheet = false
                onBack()
            }
            var selectedTab by remember { mutableStateOf(0) } // 0 for Surah, 1 for Para
            var searchQuery by remember { mutableStateOf("") }
            
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(focusRequester)
                    .focusable()
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Back && keyEvent.type == KeyEventType.KeyUp) {
                            showSelectorSheet = false
                            onBack()
                            true
                        } else {
                            false
                        }
                    }
            ) {
                Text(
                    text = "দ্রুত নেভিগেশন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF10B981),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Scroll settings row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "পাতা পরিবর্তন পদ্ধতি:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isDark) Color.White else Color.Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { viewModel.setScrollDirection("Horizontal") },
                            shape = RoundedCornerShape(20.dp),
                            color = if (!isVertical) Color(0xFF10B981) else (if (isDark) Color(0xFF2D2D2D) else Color(0xFFF3F4F6)),
                            contentColor = if (!isVertical) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                            modifier = Modifier.testTag("scroll_horizontal_option")
                        ) {
                            Text(
                                text = "ডানে-বামে",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        
                        Surface(
                            onClick = { viewModel.setScrollDirection("Vertical") },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isVertical) Color(0xFF10B981) else (if (isDark) Color(0xFF2D2D2D) else Color(0xFFF3F4F6)),
                            contentColor = if (isVertical) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                            modifier = Modifier.testTag("scroll_vertical_option")
                        ) {
                            Text(
                                text = "উপর-নিচে",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                 TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF10B981)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; searchQuery = "" },
                        text = {
                            Text(
                                text = "সূরা সূচী",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (selectedTab == 0) Color(0xFF10B981) else (if (isDark) Color.LightGray else Color.Gray)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; searchQuery = "" },
                        text = {
                            Text(
                                text = "পারা সূচী",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (selectedTab == 1) Color(0xFF10B981) else (if (isDark) Color.LightGray else Color.Gray)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { 
                        Text(
                            text = if (selectedTab == 0) "সূরা খুঁজুন..." else "পারা খুঁজুন...",
                            color = if (isDark) Color.Gray else Color.LightGray
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.4f),
                        cursorColor = Color(0xFF10B981),
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedPlaceholderColor = if (isDark) Color.Gray else Color.LightGray,
                        unfocusedPlaceholderColor = if (isDark) Color.Gray else Color.LightGray
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
                            val isCurrent = currentPage in startPage..(if (surahNum < 114) com.example.data.QuranData.surahStartPages[surahNum] - 1 else totalPages)
                            
                            val cardBgColor = if (isDark) {
                                if (isCurrent) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF2D2D2D)
                            } else {
                                if (isCurrent) Color(0xFFECFDF5) else Color(0xFFF3F4F6)
                            }
                            val cardBorder = if (isCurrent) {
                                androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF10B981) else Color(0xFFA7F3D0))
                            } else {
                                if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null
                            }
                            
                            Surface(
                                onClick = {
                                    viewModel.jumpToPage(startPage)
                                    showSelectorSheet = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = cardBgColor,
                                border = cardBorder
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
                                                color = if (isDark) Color.White else Color(0xFF1F2937)
                                            )
                                            Text(
                                                text = meaning,
                                                fontSize = 12.sp,
                                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "পৃষ্ঠা $startPage",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                                    )
                                }
                            }
                        }
                    }
                } else if (selectedTab == 1) {
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
                            val isHafezi = (mushafId == "hafizi_15line" || mushafId == "imdadia_hafezi" || mushafId == "custom_pdf")
                            val startPage = if (isHafezi) {
                                com.example.data.HafeziQuranData.getParaStartPage(juzNum, 1)
                            } else {
                                juz.third
                            }
                            val endPage = if (isHafezi) {
                                if (juzNum < 30) com.example.data.HafeziQuranData.getParaStartPage(juzNum + 1, 1) - 1 else totalPages
                            } else {
                                if (juzNum < 30) com.example.data.QuranData.juzList[juzNum].third - 1 else totalPages
                            }
                            val isCurrent = currentPage in startPage..endPage
                            
                            val cardBgColor = if (isDark) {
                                if (isCurrent) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF2D2D2D)
                            } else {
                                if (isCurrent) Color(0xFFECFDF5) else Color(0xFFF3F4F6)
                            }
                            val cardBorder = if (isCurrent) {
                                androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF10B981) else Color(0xFFA7F3D0))
                            } else {
                                if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null
                            }

                            Surface(
                                onClick = {
                                    viewModel.jumpToPage(startPage)
                                    showSelectorSheet = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = cardBgColor,
                                border = cardBorder
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
                                            color = if (isDark) Color.White else Color(0xFF1F2937)
                                        )
                                    }
                                    Text(
                                        text = "পৃষ্ঠা $startPage",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
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
                    text = "সূরা ফাতিহা পৃষ্ঠা সমন্বয়",
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
                        text = "আপনার পিডিএফ কিতাবের কত নম্বর পাতায় সূরা ফাতিহা বা মূল কুরআন শুরু হয়েছে তা সিলেক্ট করুন। এর পর অফসেট স্বয়ংক্রিয়ভাবে সমন্বয় হয়ে যাবে।",
                        fontSize = 13.sp,
                        color = if (isDark) Color.LightGray else Color.DarkGray
                    )
                    
                    val currentFatihahPage = pdfPageOffset + 1
                    
                    Text(
                        text = "সূরা ফাতিহা এর পৃষ্ঠা নম্বর (PDF অনুযায়ী):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color.Black
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                if (currentFatihahPage > 1) {
                                    viewModel.adjustOffset(-1)
                                }
                            },
                            modifier = Modifier.background(if (isDark) Color(0xFF2D2D2D) else Color(0xFFF3F4F6), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease Page", tint = if (isDark) Color.White else Color.Black)
                        }
                        
                        Text(
                            text = "$currentFatihahPage নং পৃষ্ঠা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        IconButton(
                            onClick = { viewModel.adjustOffset(1) },
                            modifier = Modifier.background(if (isDark) Color(0xFF2D2D2D) else Color(0xFFF3F4F6), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase Page", tint = if (isDark) Color.White else Color.Black)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "সহজ নির্বাচন (ক্লিক করুন):",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color.Gray
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3, 4, 5).forEach { pageNum ->
                            val isSelected = currentFatihahPage == pageNum
                            Surface(
                                onClick = {
                                    val newOffset = pageNum - 1
                                    val diff = newOffset - pdfPageOffset
                                    viewModel.adjustOffset(diff)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF10B981) else (if (isDark) Color(0xFF2D2D2D) else Color(0xFFF3F4F6)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pageNum.toString(),
                                        color = if (isSelected) Color.White else (if (isDark) Color.White else Color.Black),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
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

    if (showJumpDialog) {
        Dialog(
            onDismissRequest = { showJumpDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1C1C1E) else Color.White
                ),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0xFFE5E7EB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "নির্দিষ্ট পৃষ্ঠায় যান",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF10B981),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    var selectedParaIndex by remember { mutableStateOf(0) }
                    var pageInput by remember { mutableStateOf("") }

                    // Dynamic list of 30 paras
                    val paras = remember {
                        (1..30).map { paraNum ->
                            val paraName = "${DateUtil.toBengaliNumerals(paraNum)} পারা"
                            Pair(paraNum, paraName)
                        }
                    }

                    // Helper logic for custom page system
                    fun getParaPageCount(para: Int): Int {
                        return when (para) {
                            1 -> 21
                            29 -> 24
                            30 -> 25
                            else -> 20
                        }
                    }

                    fun getParaStartPage(para: Int): Int {
                        var startPage = 1
                        for (i in 1 until para) {
                            startPage += getParaPageCount(i)
                        }
                        return startPage
                    }

                    val selectedParaNum = paras[selectedParaIndex].first
                    val maxPagesInPara = getParaPageCount(selectedParaNum)

                    var expandedPara by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Para Selector (Box with Dropdown)
                        Box(modifier = Modifier.weight(0.65f)) {
                            OutlinedButton(
                                onClick = { expandedPara = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0xFFE5E7EB)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Color.White else Color.Black),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = paras[selectedParaIndex].second,
                                        color = if (isDark) Color.White else Color.Black,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expandedPara,
                                onDismissRequest = { expandedPara = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .heightIn(max = 250.dp)
                                    .background(if (isDark) Color(0xFF1C1C1E) else Color.White)
                            ) {
                                paras.forEachIndexed { idx, item ->
                                    DropdownMenuItem(
                                        text = { Text(item.second, color = if (isDark) Color.White else Color.Black) },
                                        onClick = {
                                            selectedParaIndex = idx
                                            pageInput = "" // Clear page input when changing para
                                            expandedPara = false
                                        }
                                    )
                                }
                            }
                        }

                        // Right: Page input (OutlinedTextField)
                        OutlinedTextField(
                            value = pageInput,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() || it in '০'..'৯' }
                                if (filtered.length <= 2) {
                                    val converted = filtered.map { char ->
                                        if (char in '0'..'9') (char - '0' + '০'.code).toChar() else char
                                    }.joinToString("")
                                    pageInput = converted
                                }
                            },
                            label = { Text("পৃষ্ঠা...", color = if (isDark) Color.LightGray else Color.Gray, fontSize = 12.sp) },
                            placeholder = { Text("১ - ${DateUtil.toBengaliNumerals(maxPagesInPara)}", color = Color.Gray, fontSize = 11.sp) },
                            modifier = Modifier.weight(0.35f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = if (isDark) Color(0xFF2D2D2D) else Color(0xFFE5E7EB),
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            )
                        )
                    }

                    // Real-time page preview math
                    val cleanInput = pageInput.map { char ->
                        if (char in '০'..'৯') (char - '০' + '0'.code).toChar() else char
                    }.joinToString("")
                    val parsedPage = cleanInput.toIntOrNull()
                    if (parsedPage != null && parsedPage in 1..maxPagesInPara) {
                        val prevPagesCount = getParaStartPage(selectedParaNum) - 1
                        val absPage = prevPagesCount + parsedPage
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "হিসাব: ${DateUtil.toBengaliNumerals(prevPagesCount)} + ${DateUtil.toBengaliNumerals(parsedPage)} = ${DateUtil.toBengaliNumerals(absPage)} নম্বর পৃষ্ঠা",
                            color = Color(0xFF10B981),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { showJumpDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("বাদ দিন", color = if (isDark) Color.LightGray else Color.Gray, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val cleanInputVal = pageInput.map { char ->
                                    if (char in '০'..'৯') (char - '০' + '0'.code).toChar() else char
                                }.joinToString("")
                                val targetParaPage = cleanInputVal.toIntOrNull()
                                if (targetParaPage != null && targetParaPage in 1..maxPagesInPara) {
                                    val targetPage = getParaStartPage(selectedParaNum) + targetParaPage - 1
                                    if (targetPage in 1..totalPages) {
                                        showJumpDialog = false
                                        viewModel.jumpToPage(targetPage)
                                    } else {
                                        android.widget.Toast.makeText(context, "সঠিক পৃষ্ঠা নম্বর লিখুন", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val maxPagesBengali = DateUtil.toBengaliNumerals(maxPagesInPara)
                                    android.widget.Toast.makeText(context, "১ থেকে $maxPagesBengali এর মধ্যে পৃষ্ঠা নম্বর লিখুন", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("পৃষ্ঠায় যান", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnDemandPageViewer(
    mushafId: String,
    pageNumber: Int,
    pdfPageOffset: Int,
    isDark: Boolean,
    viewModel: MushafViewerViewModel
) {
    var pagePath by remember(mushafId, pageNumber, pdfPageOffset) { mutableStateOf<String?>(null) }
    var isLoading by remember(mushafId, pageNumber, pdfPageOffset) { mutableStateOf(true) }
    var hasError by remember(mushafId, pageNumber, pdfPageOffset) { mutableStateOf(false) }

    LaunchedEffect(mushafId, pageNumber, pdfPageOffset) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color.Black else Color.White), 
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF10B981))
                Spacer(modifier = Modifier.height(8.dp))
                Text("লোড হচ্ছে...", color = if (isDark) Color.LightGray else Color.Gray, fontSize = 13.sp)
            }
        }
    } else if (hasError) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color.Black else Color.White), 
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("পাতা লোড করা যায়নি", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("ইন্টারনেট সংযোগ চেক করে আবার চেষ্টা করুন", color = if (isDark) Color.LightGray else Color.Gray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
        PageViewer(pagePath = pagePath!!, isDark = isDark)
    }
}
