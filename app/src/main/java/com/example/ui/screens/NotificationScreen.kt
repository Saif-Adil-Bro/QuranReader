package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlogPost
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodels.PostsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: PostsViewModel,
    onBackClick: () -> Unit,
    onNavigateToDua: ((Int?) -> Unit)? = null,
    onNavigateToPlanner: (() -> Unit)? = null
) {
    val blogPosts by viewModel.rawBlogPosts.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val pendingPost by viewModel.pendingBlogPost.collectAsState(initial = null)

    var selectedPostForReader by remember { mutableStateOf<BlogPost?>(null) }
    var showAdminPasswordDialog by remember { mutableStateOf(false) }
    var showAddNotificationDialog by remember { mutableStateOf(false) }
    var isAdminUnlocked by remember { mutableStateOf(false) }

    var adminClickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    // Handle incoming pending notification post
    LaunchedEffect(pendingPost) {
        val currentPending = pendingPost
        if (currentPending != null && (currentPending.category == "নোটিফিকেশন" || currentPending.category == "নোটিশ")) {
            selectedPostForReader = currentPending
            viewModel.setPendingBlogPost(null)
        }
    }

    val notificationPosts = remember(blogPosts) {
        blogPosts.filter { it.category == "নোটিফিকেশন" || it.category == "নোটিশ" }
    }

    if (selectedPostForReader != null) {
        BlogPostDetailScreen(
            post = selectedPostForReader!!,
            onBackClick = { selectedPostForReader = null },
            onNavigateToDua = {
                val post = selectedPostForReader
                selectedPostForReader = null
                onNavigateToDua?.invoke(null)
            },
            onNavigateToPlanner = {
                selectedPostForReader = null
                onNavigateToPlanner?.invoke()
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.clickable {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime < 1500) {
                                adminClickCount++
                            } else {
                                adminClickCount = 1
                            }
                            lastClickTime = now
                            if (adminClickCount >= 3) {
                                adminClickCount = 0
                                showAdminPasswordDialog = true
                            }
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "নোটিফিকেশন সেন্টার",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "পিছনে যান"
                        )
                    }
                },
                actions = {
                    if (isAdminUnlocked) {
                        IconButton(onClick = { showAddNotificationDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "নতুন নোটিফিকেশন যোগ করুন",
                                tint = PrimaryGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading && blogPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else if (notificationPosts.isEmpty()) {
                EmptyStateView("কোন নোটিফিকেশন পাওয়া যায়নি")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = notificationPosts,
                        key = { index, post -> if (post.id.isNotEmpty()) post.id else "${post.title}_$index" }
                    ) { _, post ->
                        NotificationCard(
                            post = post,
                            onClick = { selectedPostForReader = post }
                        )
                    }
                }
            }
        }
    }

    if (showAdminPasswordDialog) {
        AdminPasswordDialog(
            onSuccess = {
                showAdminPasswordDialog = false
                isAdminUnlocked = true
                showAddNotificationDialog = true
            },
            onDismiss = { showAdminPasswordDialog = false }
        )
    }

    if (showAddNotificationDialog) {
        AddPostDialog(
            viewModel = viewModel,
            defaultCategory = "নোটিফিকেশন",
            onDismiss = { showAddNotificationDialog = false }
        )
    }
}
