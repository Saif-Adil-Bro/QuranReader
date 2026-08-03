import sys

file_path = "app/src/main/java/com/example/ui/screens/NotificationScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

import1 = "import androidx.compose.foundation.lazy.itemsIndexed\n"
import2 = "import androidx.compose.foundation.lazy.itemsIndexed\nimport com.example.utils.DateUtil\n"

content = content.replace(import1, import2)

var_decls = """    var readIds by remember { mutableStateOf(setOf<String>()) }
    var hiddenIds by remember { mutableStateOf(setOf<String>()) }
    var forceUpdate by remember { mutableIntStateOf(0) }"""

var_decls_new = """    var readIds by remember { mutableStateOf(setOf<String>()) }
    var hiddenIds by remember { mutableStateOf(setOf<String>()) }
    var forceUpdate by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableStateOf("All") } // "All" or "Unread"
    var expandedGroupAuthor by remember { mutableStateOf<String?>(null) }"""

content = content.replace(var_decls, var_decls_new)

old_notifs = """    val notificationPosts = remember(blogPosts, hiddenIds) {
        blogPosts.filter { (it.category == "নোটিফিকেশন" || it.category == "নোটিশ") && !hiddenIds.contains(it.id.ifBlank { (it.title + it.content).hashCode().toString() }) }
    }"""

new_notifs = """    val notificationPosts = remember(blogPosts, hiddenIds) {
        blogPosts.filter { (it.category == "নোটিফিকেশন" || it.category == "নোটিশ") && !hiddenIds.contains(it.id.ifBlank { (it.title + it.content).hashCode().toString() }) }
    }

    val filteredNotificationPosts = remember(notificationPosts, selectedTab, readIds) {
        if (selectedTab == "Unread") {
            notificationPosts.filter { !readIds.contains(it.id.ifBlank { (it.title + it.content).hashCode().toString() }) }
        } else {
            notificationPosts
        }
    }
    
    // Grouping: Group consecutive unread notifications from the same author if there are more than 1
    val displayItems = remember(filteredNotificationPosts, readIds, expandedGroupAuthor) {
        val result = mutableListOf<Any>()
        var i = 0
        while (i < filteredNotificationPosts.size) {
            val currentPost = filteredNotificationPosts[i]
            val currentAuthor = currentPost.author
            val currentId = currentPost.id.ifBlank { (currentPost.title + currentPost.content).hashCode().toString() }
            val isCurrentRead = readIds.contains(currentId)
            
            // Check if we can group
            if (!isCurrentRead) {
                var j = i + 1
                while (j < filteredNotificationPosts.size) {
                    val nextPost = filteredNotificationPosts[j]
                    val nextId = nextPost.id.ifBlank { (nextPost.title + nextPost.content).hashCode().toString() }
                    if (nextPost.author == currentAuthor && !readIds.contains(nextId)) {
                        j++
                    } else {
                        break
                    }
                }
                
                val groupSize = j - i
                if (groupSize > 1 && expandedGroupAuthor != currentAuthor) {
                    result.add(NotificationGroupHeader(author = currentAuthor, count = groupSize, posts = filteredNotificationPosts.subList(i, j)))
                    i = j
                    continue
                }
            }
            
            result.add(currentPost)
            i++
        }
        result
    }
"""

content = content.replace(old_notifs, new_notifs)

empty_check_old = "else if (notificationPosts.isEmpty()) {"
empty_check_new = "else if (displayItems.isEmpty()) {"
content = content.replace(empty_check_old, empty_check_new)

list_old = """                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = notificationPosts,
                        key = { index, post -> if (post.id.isNotEmpty()) post.id else "${post.title}_$index" }
                    ) { _, post ->
                        val nId = post.id.ifBlank { (post.title + post.content).hashCode().toString() }
                        val isRead = readIds.contains(nId)
                        
                        NotificationCard(
                            post = post,
                            isRead = isRead,"""

list_new = """                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == "All",
                            onClick = { selectedTab = "All" },
                            label = { Text("সবগুলো", fontWeight = if (selectedTab == "All") FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryGreen
                            )
                        )
                        FilterChip(
                            selected = selectedTab == "Unread",
                            onClick = { selectedTab = "Unread" },
                            label = { Text("আনরিড", fontWeight = if (selectedTab == "Unread") FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryGreen
                            )
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = displayItems,
                            key = { index, item -> 
                                if (item is BlogPost) {
                                    if (item.id.isNotEmpty()) item.id else "${item.title}_$index"
                                } else {
                                    "group_${(item as NotificationGroupHeader).author}_$index"
                                }
                            }
                        ) { _, item ->
                            if (item is NotificationGroupHeader) {
                                NotificationGroupCard(
                                    header = item,
                                    onClick = { expandedGroupAuthor = if (expandedGroupAuthor == item.author) null else item.author }
                                )
                            } else if (item is BlogPost) {
                                val post = item
                                val nId = post.id.ifBlank { (post.title + post.content).hashCode().toString() }
                                val isRead = readIds.contains(nId)
                                
                                NotificationCard(
                                    post = post,
                                    isRead = isRead,"""

content = content.replace(list_old, list_new)

end_braces_old = """                            onDeleteClick = {
                                NotificationStateHelper.hideNotification(context, nId)
                                forceUpdate++
                            }
                        )
                    }
                }
            }"""

end_braces_new = """                            onDeleteClick = {
                                NotificationStateHelper.hideNotification(context, nId)
                                forceUpdate++
                            }
                        )
                    }
                        }
                    }
                }
            }"""

content = content.replace(end_braces_old, end_braces_new)

helpers_new = """
data class NotificationGroupHeader(
    val author: String,
    val count: Int,
    val posts: List<BlogPost>
)

@Composable
fun NotificationGroupCard(
    header: NotificationGroupHeader,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${header.author} এবং আরও ${DateUtil.toBengaliNumerals(header.count - 1)}টি নতুন নোটিফিকেশন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "বিস্তারিত দেখতে ট্যাপ করুন",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
"""

with open(file_path, "w") as f:
    f.write(content + helpers_new)

