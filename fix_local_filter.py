import sys

file_path = "app/src/main/java/com/example/ui/screens/NotificationScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Fix LaunchedEffect
old_launched = """.filter { !hiddenIds.contains(it.id) }"""
new_launched = ""

content = content.replace(old_launched, new_launched)

# Fix remember block
old_remember = """    val notificationPosts = remember(blogPosts, hiddenIds, localNotifications) {
        val firestoreNotifs = blogPosts.filter { (it.category == "নোটিফিকেশন" || it.category == "নোটিশ") && !hiddenIds.contains(it.id.ifBlank { (it.title + it.content).hashCode().toString() }) }
        (firestoreNotifs + localNotifications).sortedByDescending { it.timestamp }
    }"""

new_remember = """    val notificationPosts = remember(blogPosts, hiddenIds, localNotifications) {
        val firestoreNotifs = blogPosts.filter { (it.category == "নোটিফিকেশন" || it.category == "নোটিশ") && !hiddenIds.contains(it.id.ifBlank { (it.title + it.content).hashCode().toString() }) }
        val localNotifs = localNotifications.filter { !hiddenIds.contains(it.id) }
        (firestoreNotifs + localNotifs).sortedByDescending { it.timestamp }
    }"""

content = content.replace(old_remember, new_remember)

with open(file_path, "w") as f:
    f.write(content)
