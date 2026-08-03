with open("app/src/main/java/com/example/data/repository/PostsRepository.kt", "r") as f:
    content = f.read()

target = """    fun addBlogPost(title: String, content: String, category: String, author: String = "ইসলামিক এডমিন", onSuccess: () -> Unit, onError: (String) -> Unit) {
        val now = System.currentTimeMillis()
        val localPost = BlogPost(
            id = "local_$now",
            title = title,
            content = content,
            category = category,
            author = author,
            timestamp = now
        )

        // Optimistic UI update
        _blogPosts.value = listOf(localPost) + _blogPosts.value.filter { it.id != localPost.id }

        // Notify caller immediately for responsive UX
        onSuccess()

        val newPost = hashMapOf<String, Any>(
            "title" to title,
            "content" to content,
            "category" to category,
            "author" to author,
            "imageUrl" to "",
            "readTime" to "${(content.length / 300).coerceAtLeast(1)} মিনিট",
            "timestamp" to now,
            "createdAt" to com.google.firebase.Timestamp.now()
        )"""

replacement = """    fun addBlogPost(title: String, content: String, category: String, author: String = "ইসলামিক এডমিন", imageUrl: String = "", onSuccess: () -> Unit, onError: (String) -> Unit) {
        val now = System.currentTimeMillis()
        val localPost = BlogPost(
            id = "local_$now",
            title = title,
            content = content,
            category = category,
            author = author,
            imageUrl = imageUrl,
            timestamp = now
        )

        // Optimistic UI update
        _blogPosts.value = listOf(localPost) + _blogPosts.value.filter { it.id != localPost.id }

        // Notify caller immediately for responsive UX
        onSuccess()

        val newPost = hashMapOf<String, Any>(
            "title" to title,
            "content" to content,
            "category" to category,
            "author" to author,
            "imageUrl" to imageUrl,
            "readTime" to "${(content.length / 300).coerceAtLeast(1)} মিনিট",
            "timestamp" to now,
            "createdAt" to com.google.firebase.Timestamp.now()
        )"""

if target in content:
    with open("app/src/main/java/com/example/data/repository/PostsRepository.kt", "w") as f:
        f.write(content.replace(target, replacement))
    print("Success")
else:
    print("Target not found")
