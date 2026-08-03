import sys

file_path = "app/src/main/java/com/example/ui/screens/NotificationScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add mutableStateOf for localNotifications
old_var_decls = """    var readIds by remember { mutableStateOf(setOf<String>()) }
    var hiddenIds by remember { mutableStateOf(setOf<String>()) }
    var forceUpdate by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableStateOf("All") } // "All" or "Unread"
    var expandedGroupAuthor by remember { mutableStateOf<String?>(null) }"""

new_var_decls = """    var readIds by remember { mutableStateOf(setOf<String>()) }
    var hiddenIds by remember { mutableStateOf(setOf<String>()) }
    var forceUpdate by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableStateOf("All") } // "All" or "Unread"
    var expandedGroupAuthor by remember { mutableStateOf<String?>(null) }
    var localNotifications by remember { mutableStateOf(emptyList<BlogPost>()) }

    LaunchedEffect(Unit) {
        val db = com.example.data.local.NotificationDatabase.getDatabase(context)
        db.localNotificationDao().getAllNotifications().collect { entities ->
            localNotifications = entities.map { entity ->
                BlogPost(
                    id = "local_${entity.id}",
                    title = entity.title,
                    content = entity.content,
                    category = entity.category,
                    author = entity.author,
                    timestamp = entity.timestamp
                )
            }.filter { !hiddenIds.contains(it.id) }
        }
    }"""

content = content.replace(old_var_decls, new_var_decls)

# Update the definition of notificationPosts
old_notifs = """    val notificationPosts = remember(blogPosts, hiddenIds) {
        blogPosts.filter { (it.category == "নোটিফিকেশন" || it.category == "নোটিশ") && !hiddenIds.contains(it.id.ifBlank { (it.title + it.content).hashCode().toString() }) }
    }"""

new_notifs = """    val notificationPosts = remember(blogPosts, hiddenIds, localNotifications) {
        val firestoreNotifs = blogPosts.filter { (it.category == "নোটিফিকেশন" || it.category == "নোটিশ") && !hiddenIds.contains(it.id.ifBlank { (it.title + it.content).hashCode().toString() }) }
        (firestoreNotifs + localNotifications).sortedByDescending { it.timestamp }
    }"""

content = content.replace(old_notifs, new_notifs)

with open(file_path, "w") as f:
    f.write(content)
