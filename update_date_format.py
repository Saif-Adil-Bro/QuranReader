with open("app/src/main/java/com/example/ui/screens/PostsScreen.kt", "r") as f:
    content = f.read()

helper_func = """
fun formatPostDate(timestamp: Long): String {
    if (timestamp <= 0L) return "সম্প্রতি"
    val diffMillis = System.currentTimeMillis() - timestamp
    if (diffMillis < 0) return "সম্প্রতি"
    val seconds = diffMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    fun String.toBanglaDigits(): String {
        val banglaDigits = mapOf(
            '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
            '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
        )
        return this.map { banglaDigits[it] ?: it }.joinToString("")
    }

    return when {
        minutes < 1 -> "এখনই"
        minutes < 60 -> "${minutes.toString().toBanglaDigits()} মিনিট আগে"
        hours < 24 -> "${hours.toString().toBanglaDigits()} ঘণ্টা আগে"
        days < 7 -> "${days.toString().toBanglaDigits()} দিন আগে"
        else -> {
            try {
                val sdf = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("bn", "BD"))
                sdf.format(java.util.Date(timestamp))
            } catch (e: Exception) {
                "সম্প্রতি"
            }
        }
    }
}
"""

# Append helper function at the end or before AddPostDialog
if "fun formatPostDate" not in content:
    content = content.replace("fun BlogPostCard(", helper_func + "\nfun BlogPostCard(", 1)

# Replace post.readTime in BlogPostCard and BlogPostDetailScreen
content = content.replace("text = post.readTime,", "text = formatPostDate(post.timestamp),")

with open("app/src/main/java/com/example/ui/screens/PostsScreen.kt", "w") as f:
    f.write(content)

print("Updated PostsScreen with formatPostDate!")
