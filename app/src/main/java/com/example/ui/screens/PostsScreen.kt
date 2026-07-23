package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.security.MessageDigest
import com.example.data.model.BlogPost
import com.example.data.model.ShortPost
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodels.PostsViewModel
import com.example.utils.PostShareUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsScreen(
    viewModel: PostsViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val blogPosts by viewModel.filteredBlogPosts.collectAsState()
    val shortPosts by viewModel.filteredShortPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedBlogPostForReader by remember { mutableStateOf<BlogPost?>(null) }
    var selectedShortPostForCard by remember { mutableStateOf<ShortPost?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAddPostDialog by remember { mutableStateOf(false) }
    var adminClickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val categories = listOf("সকল", "কুরআন ও জীবন", "নফল ইবাদত", "দৈনিক নসীহত", "মাসনুন জিকির", "আত্মশুদ্ধি", "সাধারণ")

    if (selectedBlogPostForReader != null) {
        BackHandler {
            selectedBlogPostForReader = null
        }
        BlogPostDetailScreen(
            post = selectedBlogPostForReader!!,
            onBackClick = { selectedBlogPostForReader = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "ইসলামিক পোস্ট ও ফটো কার্ড",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        val now = System.currentTimeMillis()
                                        if (now - lastClickTime < 1000) {
                                            adminClickCount++
                                        } else {
                                            adminClickCount = 1
                                        }
                                        lastClickTime = now

                                        if (adminClickCount >= 5) {
                                            adminClickCount = 0
                                            showPasswordDialog = true
                                        }
                                    }
                                )
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("কীওয়ার্ড বা বিষয় দিয়ে খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                // Category Chips Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedCategory.value = category },
                            label = { Text(category, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PrimaryGreen
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ইসলামিক ব্লগ (${blogPosts.size})", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ফটো কার্ড ও নসীহত (${shortPosts.size})", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                } else {
                    if (selectedTabIndex == 0) {
                        // Blog List
                        if (blogPosts.isEmpty()) {
                            EmptyStateView("কোন ইসলামিক ব্লগ পোস্ট পাওয়া যায়নি")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(blogPosts, key = { it.id.ifEmpty { it.title } }) { post ->
                                    BlogPostCard(
                                        post = post,
                                        onClick = { selectedBlogPostForReader = post }
                                    )
                                }
                            }
                        }
                    } else {
                        // Short Posts List
                        if (shortPosts.isEmpty()) {
                            EmptyStateView("কোন সংক্ষিপ্ত নসীহত পাওয়া যায়নি")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(shortPosts, key = { it.id.ifEmpty { it.text } }) { post ->
                                    ShortPostCard(
                                        post = post,
                                        onCopyClick = { PostShareUtil.copyToClipboard(context, post) },
                                        onTextShareClick = { PostShareUtil.shareAsText(context, post) },
                                        onPhotoCardClick = { selectedShortPostForCard = post }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Photo Card Generator BottomSheet / Dialog
        if (selectedShortPostForCard != null) {
            PhotoCardCustomizerDialog(
                post = selectedShortPostForCard!!,
                onDismiss = { selectedShortPostForCard = null }
            )
        }

        // Admin Secret Password Dialog
        if (showPasswordDialog) {
            AdminPasswordDialog(
                onSuccess = {
                    showPasswordDialog = false
                    showAddPostDialog = true
                },
                onDismiss = { showPasswordDialog = false }
            )
        }

        // Admin Secret Add Post Dialog
        if (showAddPostDialog) {
            AddPostDialog(
                viewModel = viewModel,
                onDismiss = { showAddPostDialog = false }
            )
        }
    }
}

@Composable
fun BlogPostCard(
    post: BlogPost,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PrimaryGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = post.category,
                        color = PrimaryGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.readTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.author,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "সম্পূর্ণ পড়ুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShortPostCard(
    post: ShortPost,
    onCopyClick: () -> Unit,
    onTextShareClick: () -> Unit,
    onPhotoCardClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF0EA5E9).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = post.category,
                        color = Color(0xFF0284C7),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (post.reference.isNotEmpty()) {
                    Text(
                        text = post.reference,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onCopyClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onTextShareClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share Text", modifier = Modifier.size(18.dp))
                    }
                }

                Button(
                    onClick = onPhotoCardClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ফটো কার্ড শেয়ার", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogPostDetailScreen(
    post: BlogPost,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var textSizeSp by remember { mutableFloatStateOf(16f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ব্লগ পোস্ট",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পিছনে যান")
                    }
                },
                actions = {
                    IconButton(onClick = { if (textSizeSp > 12f) textSizeSp -= 2f }) {
                        Text("A-", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGreen)
                    }
                    IconButton(onClick = { if (textSizeSp < 28f) textSizeSp += 2f }) {
                        Text("A+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryGreen)
                    }
                    IconButton(
                        onClick = {
                            val shareText = "✨ ${post.title} ✨\n\n${post.content}\n\n— ${post.author}\n\n📱 ❝কুরআন রিডার❞ অ্যাপ থেকে"
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "ব্লগ পোস্ট শেয়ার করুন"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "শেয়ার", tint = PrimaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Category Badge & Read Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PrimaryGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = post.category,
                        color = PrimaryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.readTime,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Author Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = post.author,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "প্রকাশিত • ইসলামিক নসীহত ও ব্লগ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Post Title
            Text(
                text = post.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(16.dp))

            // Body Content
            Text(
                text = post.content,
                fontSize = textSizeSp.sp,
                lineHeight = (textSizeSp * 1.6f).sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCardCustomizerDialog(
    post: ShortPost,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTheme by remember { mutableStateOf(PostShareUtil.CardTheme.EMERALD) }
    var bgImageUrl by remember { mutableStateOf("") }
    var overlayAlpha by remember { mutableFloatStateOf(0.70f) }
    var textAlignName by remember { mutableStateOf("CENTER") } // "LEFT", "CENTER", "RIGHT"
    var fontName by remember { mutableStateOf("SolaimanLipi") } // "SolaimanLipi", "Hind Siliguri", "Shorif Shishir Unicode", "Default"
    var fontSizeSp by remember { mutableFloatStateOf(44f) }
    var customCategory by remember { mutableStateOf(post.category) }
    var customText by remember { mutableStateOf(post.text) }
    var customRef by remember { mutableStateOf(post.reference) }
    var showLogo by remember { mutableStateOf(true) }
    var showWatermark by remember { mutableStateOf(true) }

    var cardBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGeneratingPreview by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }

    // Sample background image presets
    val presetBgUrls = remember {
        listOf(
            "https://images.unsplash.com/photo-1542816417-0983cbe33577?w=800&q=80" to "মসজিদ ১",
            "https://images.unsplash.com/photo-1564769625905-50e93615e769?w=800&q=80" to "মসজিদ ২",
            "https://images.unsplash.com/photo-1519817650390-64a93db51149?w=800&q=80" to "তারা ও আকাশ",
            "https://images.unsplash.com/photo-1509021436468-d51030005963?w=800&q=80" to "জ্যামিতিক প্যাটার্ন",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=80" to "প্রকৃতি"
        )
    }

    LaunchedEffect(
        selectedTheme, bgImageUrl, overlayAlpha, textAlignName, fontName, fontSizeSp, customCategory, customText, customRef, showLogo, showWatermark, post
    ) {
        isGeneratingPreview = true
        cardBitmap = PostShareUtil.generateCardBitmap(
            context = context,
            post = post,
            theme = selectedTheme,
            bgImageUrl = bgImageUrl.ifBlank { null },
            overlayAlpha = overlayAlpha,
            textAlignName = textAlignName,
            fontName = fontName,
            fontSizeSp = fontSizeSp,
            customCategory = customCategory,
            customText = customText,
            customRef = customRef,
            showLogo = showLogo,
            showWatermark = showWatermark
        )
        isGeneratingPreview = false
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "ফটো কার্ড মেকার ও কাস্টমাইজ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "বন্ধ করুন")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                if (!isSharing) {
                                    isSharing = true
                                    coroutineScope.launch {
                                        PostShareUtil.shareAsImage(
                                            context = context,
                                            post = post,
                                            theme = selectedTheme,
                                            bgImageUrl = bgImageUrl.ifBlank { null },
                                            overlayAlpha = overlayAlpha,
                                            textAlignName = textAlignName,
                                            fontName = fontName,
                                            fontSizeSp = fontSizeSp,
                                            customCategory = customCategory,
                                            customText = customText,
                                            customRef = customRef,
                                            showLogo = showLogo,
                                            showWatermark = showWatermark
                                        )
                                        isSharing = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isSharing
                        ) {
                            if (isSharing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("শেয়ার করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Live Preview Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cardBitmap != null) {
                            Image(
                                bitmap = cardBitmap!!.asImageBitmap(),
                                contentDescription = "Card Preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        }

                        if (isGeneratingPreview) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryGreen)
                            }
                        }
                    }
                }

                // Controls Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Theme Color Presets
                        Column {
                            Text(
                                text = "🎨 ব্যাকগ্রাউন্ড থিম",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(PostShareUtil.CardTheme.entries.toTypedArray()) { theme ->
                                    val isSelected = selectedTheme == theme
                                    val themeBg = Color(android.graphics.Color.parseColor(theme.bgColors.first))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(themeBg)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) PrimaryGreen else Color.Gray.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedTheme = theme }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = theme.title,
                                            fontSize = 12.sp,
                                            color = Color(android.graphics.Color.parseColor(theme.textColor)),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // 2. Background Image URL & Presets
                        Column {
                            Text(
                                text = "🖼️ ব্যাকগ্রাউন্ড পিকচার (URL ও প্রিসেট)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Presets
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = bgImageUrl.isBlank(),
                                        onClick = { bgImageUrl = "" },
                                        label = { Text("কোনো ছবি নয়") }
                                    )
                                }
                                items(presetBgUrls) { (url, label) ->
                                    FilterChip(
                                        selected = bgImageUrl == url,
                                        onClick = { bgImageUrl = url },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = bgImageUrl,
                                onValueChange = { bgImageUrl = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("ছবির URL লিংক লিখুন (https://...)", fontSize = 13.sp) },
                                singleLine = true,
                                trailingIcon = {
                                    if (bgImageUrl.isNotEmpty()) {
                                        IconButton(onClick = { bgImageUrl = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (bgImageUrl.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "কালার ওভারলে অপাসিটি:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Slider(
                                        value = overlayAlpha,
                                        onValueChange = { overlayAlpha = it },
                                        valueRange = 0.15f..0.95f,
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(thumbColor = PrimaryGreen, activeTrackColor = PrimaryGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${(overlayAlpha * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // 3. Bangla Fonts Selection
                        Column {
                            Text(
                                text = "🔤 বাংলা ফন্ট সিলেক্ট করুন",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val fontsList = listOf(
                                "SolaimanLipi" to "সোলাইমান লিপি",
                                "Hind Siliguri" to "হিন্দ শিলিগুড়ি",
                                "Shorif Shishir Unicode" to "শরীফ শিশির",
                                "Default" to "ডিফল্ট ফন্ট"
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(fontsList) { (key, label) ->
                                    FilterChip(
                                        selected = fontName == key,
                                        onClick = { fontName = key },
                                        label = { Text(label, fontWeight = if (fontName == key) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryGreen,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // 4. Text Alignment Options
                        Column {
                            Text(
                                text = "📐 টেক্সট এলাইনমেন্ট (পজিশন)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = textAlignName == "LEFT",
                                    onClick = { textAlignName = "LEFT" },
                                    label = {
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.FormatAlignLeft,
                                                contentDescription = "বাম",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = textAlignName == "CENTER",
                                    onClick = { textAlignName = "CENTER" },
                                    label = {
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.FormatAlignCenter,
                                                contentDescription = "মাঝখানে",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = textAlignName == "RIGHT",
                                    onClick = { textAlignName = "RIGHT" },
                                    label = {
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.FormatAlignRight,
                                                contentDescription = "ডান",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // 5. Logo & Credit Watermark Toggles
                        Column {
                            Text(
                                text = "🏷️ লোগো ও ক্রেডিট ওয়াটারমার্ক অপশন",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = showLogo,
                                    onClick = { showLogo = !showLogo },
                                    label = { Text(if (showLogo) "লোগো চালু (Top-Left)" else "লোগো বন্ধ") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = showWatermark,
                                    onClick = { showWatermark = !showWatermark },
                                    label = { Text(if (showWatermark) "ক্রেডিট চালু (Bottom)" else "ক্রেডিট বন্ধ") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // 6. Font Size Adjustment
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔠 ফন্ট সাইজ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${fontSizeSp.toInt()} sp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = fontSizeSp,
                                onValueChange = { fontSizeSp = it },
                                valueRange = 32f..58f,
                                colors = SliderDefaults.colors(thumbColor = PrimaryGreen, activeTrackColor = PrimaryGreen)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // 7. Title, Text & Reference Customization
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "✏️ টাইটেল ও পোস্টের লেখা কাস্টমাইজ করুন",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedTextField(
                                value = customCategory,
                                onValueChange = { customCategory = it },
                                label = { Text("কার্ডের টাইটেল / ক্যাটাগরি (ঐচ্ছিক)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = customText,
                                onValueChange = { customText = it },
                                label = { Text("কার্ডের মূল বাণী/নসীহত") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = customRef,
                                onValueChange = { customRef = it },
                                label = { Text("সূত্র / রেফারেন্স (ঐচ্ছিক)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AddPostDialog(
    viewModel: PostsViewModel,
    onDismiss: () -> Unit
) {
    var isBlogType by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("সাধারণ") }
    var reference by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("ইসলামিক এডমিন") }
    var errorMessage by remember { mutableStateOf("") }

    val quickCategories = listOf("কুরআন ও জীবন", "নফল ইবাদত", "দৈনিক নসীহত", "মাসনুন জিকির", "আত্মশুদ্ধি", "সাধারণ")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Icon & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PostAdd,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "নতুন পোস্ট তৈরি করুন",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ইসলামিক কনটেন্ট ও নসীহত শেয়ার করুন",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Segmented Button Toggle for Post Type
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            // Blog Post Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isBlogType) PrimaryGreen else Color.Transparent)
                                    .clickable { isBlogType = true }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Article,
                                        contentDescription = null,
                                        tint = if (isBlogType) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "ব্লগ পোস্ট",
                                        fontSize = 13.sp,
                                        fontWeight = if (isBlogType) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isBlogType) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Photo Card / Short Post Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (!isBlogType) PrimaryGreen else Color.Transparent)
                                    .clickable { isBlogType = false }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FormatQuote,
                                        contentDescription = null,
                                        tint = if (!isBlogType) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "নসীহত/কার্ড",
                                        fontSize = 13.sp,
                                        fontWeight = if (!isBlogType) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isBlogType) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (isBlogType) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("ব্লগ শিরোনাম") },
                            placeholder = { Text("যেমন: ফজরের নামাজের গুরুত্ব ও ফজিলত") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Title,
                                    contentDescription = null,
                                    tint = PrimaryGreen
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        label = { Text(if (isBlogType) "বিস্তারিত ব্লগ কনটেন্ট" else "সংক্ষিপ্ত নসীহত বা আয়াত/হাদিস") },
                        placeholder = { Text(if (isBlogType) "এখানে বিস্তারিত বক্তব্য লিখুন..." else "এখানে নসীহত বা বাণীটি লিখুন...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick Category Selection Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "ক্যাটাগরি বাছাই করুন:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(quickCategories) { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen.copy(alpha = 0.2f),
                                        selectedLabelColor = PrimaryGreen
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("ক্যাটাগরি নাম (কাস্টম)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (!isBlogType) {
                        OutlinedTextField(
                            value = reference,
                            onValueChange = { reference = it },
                            label = { Text("সূত্র / রেফারেন্স") },
                            placeholder = { Text("যেমন: সহীহ বুখারী, হাদিস ১২৩৪") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = PrimaryGreen
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("লেখকের নাম") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMessage.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("বাতিল", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (isBlogType) {
                                if (title.isBlank() || contentText.isBlank()) {
                                    errorMessage = "শিরোনাম ও বিস্তারিত লেখা আবশ্যক"
                                    return@Button
                                }
                                viewModel.addBlogPost(
                                    title = title,
                                    content = contentText,
                                    category = category,
                                    author = author,
                                    onSuccess = onDismiss,
                                    onError = { errorMessage = it }
                                )
                            } else {
                                if (contentText.isBlank()) {
                                    errorMessage = "নসীহত ও লেখা আবশ্যক"
                                    return@Button
                                }
                                viewModel.addShortPost(
                                    text = contentText,
                                    reference = reference,
                                    category = category,
                                    author = author,
                                    onSuccess = onDismiss,
                                    onError = { errorMessage = it }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("পাবলিশ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AdminPasswordDialog(
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
                Text(
                    text = "অ্যাডমিন পাসওয়ার্ড",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "নতুন পোস্ট যুক্ত করতে অ্যাডমিন পাসওয়ার্ড প্রদান করুন:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = false
                    },
                    label = { Text("পাসওয়ার্ড") },
                    singleLine = true,
                    isError = passwordError,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                if (passwordError) {
                    Text(
                        text = "ভুল পাসওয়ার্ড! আবার চেষ্টা করুন।",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val inputHash = try {
                        val md = MessageDigest.getInstance("SHA-256")
                        val digest = md.digest(password.trim().toByteArray())
                        digest.joinToString("") { "%02x".format(it) }
                    } catch (e: Exception) { "" }

                    // SHA-256 hash of "admin@#$%"
                    val targetHash = "2525164f23b2c17435fce1cbe4a3df578c734b46513b2e53526fa94ff1aef3f6"

                    if (inputHash == targetHash || password.trim() == "admin@#$%") {
                        onSuccess()
                    } else {
                        passwordError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("যাচাই করুন", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
