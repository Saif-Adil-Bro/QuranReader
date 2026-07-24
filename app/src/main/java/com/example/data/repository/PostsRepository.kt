package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.BlogPost
import com.example.data.model.ShortPost
import com.example.utils.PostNotificationHelper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostsRepository(private val context: Context) {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val prefs: SharedPreferences = context.getSharedPreferences("posts_prefs", Context.MODE_PRIVATE)

    private val _blogPosts = MutableStateFlow<List<BlogPost>>(emptyList())
    val blogPosts: StateFlow<List<BlogPost>> = _blogPosts

    private val _shortPosts = MutableStateFlow<List<ShortPost>>(emptyList())
    val shortPosts: StateFlow<List<ShortPost>> = _shortPosts

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var postsFromBlog = listOf<BlogPost>()
    private var postsFromArticles = listOf<BlogPost>()

    init {
        // Load initial state (empty, relying on Firestore and user-added posts)
        _blogPosts.value = emptyList()
        _shortPosts.value = emptyList()

        // Ensure Anonymous Auth for Firestore Security Rules
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                auth.signInAnonymously().addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        task.exception?.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listenToFirestore()
    }

    private fun extractTimestamp(doc: DocumentSnapshot): Long {
        return try {
            val raw = doc.get("timestamp") ?: doc.get("createdAt") ?: doc.get("date")
            when (raw) {
                is com.google.firebase.Timestamp -> raw.toDate().time
                is Number -> raw.toLong()
                is String -> raw.toLongOrNull() ?: System.currentTimeMillis()
                else -> System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    @Synchronized
    private fun updateMergedBlogPosts() {
        val allRemote = (postsFromBlog + postsFromArticles).distinctBy { it.id }
        val existingBlogPosts = _blogPosts.value
        val remainingLocals = existingBlogPosts
            .filter { it.id.startsWith("local_") }
            .filter { local -> allRemote.none { remote -> remote.title.trim() == local.title.trim() && remote.content.trim() == local.content.trim() } }

        val combined = if (allRemote.isNotEmpty()) {
            (allRemote + remainingLocals)
                .distinctBy { if (it.id.isNotEmpty()) it.id else "${it.title.trim()}_${it.content.trim()}" }
                .sortedByDescending { it.timestamp }
        } else if (remainingLocals.isNotEmpty()) {
            remainingLocals.sortedByDescending { it.timestamp }
        } else {
            existingBlogPosts
        }

        _blogPosts.value = combined
    }

    private val knownPostIds = mutableSetOf<String>()
    private var isInitialBlogLoad = true
    private var isInitialArticlesLoad = true
    private var isInitialShortLoad = true

    private fun parseBlogDocs(docs: List<DocumentSnapshot>): List<BlogPost> {
        return docs.mapNotNull { doc ->
            try {
                val title = doc.getString("title") ?: doc.getString("name") ?: doc.getString("heading") ?: doc.getString("subject") ?: doc.getString("topic") ?: ""
                val content = doc.getString("content") ?: doc.getString("text") ?: doc.getString("body") ?: doc.getString("description") ?: doc.getString("details") ?: doc.getString("desc") ?: doc.getString("message") ?: doc.getString("post") ?: ""
                if (title.isBlank() && content.isBlank()) null
                else {
                    BlogPost(
                        id = doc.id,
                        title = title.ifBlank { "ইসলামিক পোস্ট" },
                        content = content,
                        author = doc.getString("author") ?: doc.getString("writer") ?: doc.getString("publisher") ?: "ইসলামিক এডমিন",
                        category = doc.getString("category") ?: doc.getString("cat") ?: doc.getString("tag") ?: "সাধারণ",
                        imageUrl = doc.getString("imageUrl") ?: doc.getString("image") ?: doc.getString("img") ?: doc.getString("photo") ?: "",
                        readTime = doc.getString("readTime") ?: "২ মিনিট",
                        timestamp = extractTimestamp(doc)
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun listenToFirestore() {
        try {
            val parseDocs = { docs: List<DocumentSnapshot> -> parseBlogDocs(docs) }

            // Listen to blog_posts
            firestore.collection("blog_posts")
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error == null && snapshot != null) {
                        if (isInitialBlogLoad) {
                            isInitialBlogLoad = false
                            snapshot.documents.forEach { knownPostIds.add(it.id) }
                        } else {
                            for (dc in snapshot.documentChanges) {
                                if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    val doc = dc.document
                                    if (!knownPostIds.contains(doc.id)) {
                                        knownPostIds.add(doc.id)
                                        val title = doc.getString("title") ?: doc.getString("name") ?: ""
                                        val content = doc.getString("content") ?: doc.getString("text") ?: doc.getString("body") ?: ""
                                        if (title.isNotBlank() || content.isNotBlank()) {
                                            val blogPost = BlogPost(
                                                id = doc.id,
                                                title = title.ifBlank { "নতুন ইসলামিক পোস্ট" },
                                                content = content,
                                                author = doc.getString("author") ?: "ইসলামিক এডমিন",
                                                category = doc.getString("category") ?: "সাধারণ",
                                                imageUrl = doc.getString("imageUrl") ?: doc.getString("image") ?: "",
                                                readTime = doc.getString("readTime") ?: "২ মিনিট",
                                                timestamp = extractTimestamp(doc)
                                            )
                                            try {
                                                PostNotificationHelper.showBlogPostNotification(context, blogPost)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        postsFromBlog = parseDocs(snapshot.documents)
                        updateMergedBlogPosts()
                    }
                }

            // Listen to articles (as seen in Firestore console)
            firestore.collection("articles")
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error == null && snapshot != null) {
                        if (isInitialArticlesLoad) {
                            isInitialArticlesLoad = false
                            snapshot.documents.forEach { knownPostIds.add(it.id) }
                        } else {
                            for (dc in snapshot.documentChanges) {
                                if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    val doc = dc.document
                                    if (!knownPostIds.contains(doc.id)) {
                                        knownPostIds.add(doc.id)
                                        val title = doc.getString("title") ?: doc.getString("name") ?: ""
                                        val content = doc.getString("content") ?: doc.getString("text") ?: doc.getString("body") ?: ""
                                        if (title.isNotBlank() || content.isNotBlank()) {
                                            val blogPost = BlogPost(
                                                id = doc.id,
                                                title = title.ifBlank { "নতুন নিবন্ধ" },
                                                content = content,
                                                author = doc.getString("author") ?: "ইসলামিক এডমিন",
                                                category = doc.getString("category") ?: "সাধারণ",
                                                imageUrl = doc.getString("imageUrl") ?: doc.getString("image") ?: "",
                                                readTime = doc.getString("readTime") ?: "২ মিনিট",
                                                timestamp = extractTimestamp(doc)
                                            )
                                            try {
                                                PostNotificationHelper.showBlogPostNotification(context, blogPost)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        postsFromArticles = parseDocs(snapshot.documents)
                        updateMergedBlogPosts()
                    }
                }

            // Listen to short_posts
            firestore.collection("short_posts")
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error == null && snapshot != null) {
                        if (isInitialShortLoad) {
                            isInitialShortLoad = false
                            snapshot.documents.forEach { knownPostIds.add(it.id) }
                        } else {
                            for (dc in snapshot.documentChanges) {
                                if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    val doc = dc.document
                                    if (!knownPostIds.contains(doc.id)) {
                                        knownPostIds.add(doc.id)
                                        val text = doc.getString("text") ?: doc.getString("content") ?: doc.getString("title") ?: ""
                                        if (text.isNotBlank()) {
                                            val shortPost = ShortPost(
                                                id = doc.id,
                                                text = text,
                                                reference = doc.getString("reference") ?: doc.getString("ref") ?: "",
                                                category = doc.getString("category") ?: "দৈনিক নসীহত",
                                                author = doc.getString("author") ?: "ইসলামিক স্কলার",
                                                timestamp = extractTimestamp(doc)
                                            )
                                            try {
                                                PostNotificationHelper.showPhotoCardNotification(context, shortPost)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        val remotePosts = snapshot.documents.mapNotNull { doc ->
                            try {
                                val text = doc.getString("text") ?: doc.getString("content") ?: doc.getString("title") ?: ""
                                if (text.isBlank()) null
                                else {
                                    ShortPost(
                                        id = doc.id,
                                        text = text,
                                        reference = doc.getString("reference") ?: doc.getString("ref") ?: "",
                                        category = doc.getString("category") ?: "দৈনিক নসীহত",
                                        author = doc.getString("author") ?: "ইসলামিক স্কলার",
                                        timestamp = extractTimestamp(doc)
                                    )
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        val remoteDeduplicated = remotePosts.distinctBy { it.id }
                        val remainingLocals = _shortPosts.value
                            .filter { it.id.startsWith("local_") }
                            .filter { local -> remoteDeduplicated.none { remote -> remote.text.trim() == local.text.trim() && remote.reference.trim() == local.reference.trim() } }

                        val combined = (remoteDeduplicated + remainingLocals)
                            .distinctBy { "${it.text.trim()}_${it.reference.trim()}" }
                            .sortedByDescending { it.timestamp }
                        _shortPosts.value = combined
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            _isLoading.value = false
        }
    }

    fun addBlogPost(title: String, content: String, category: String, author: String = "ইসলামিক এডমিন", onSuccess: () -> Unit, onError: (String) -> Unit) {
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
        )

        try {
            // Save ONLY to blog_posts to avoid duplicated entries in both collections
            firestore.collection("blog_posts")
                .add(newPost)
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addShortPost(text: String, reference: String, category: String, author: String = "ইসলামিক স্কলার", onSuccess: () -> Unit, onError: (String) -> Unit) {
        val now = System.currentTimeMillis()
        val localShort = ShortPost(
            id = "local_$now",
            text = text,
            reference = reference,
            category = category,
            author = author,
            timestamp = now
        )

        // Optimistic UI update
        _shortPosts.value = listOf(localShort) + _shortPosts.value.filter { it.id != localShort.id }

        // Notify caller immediately for responsive UX
        onSuccess()

        val newShort = hashMapOf<String, Any>(
            "text" to text,
            "reference" to reference,
            "category" to category,
            "author" to author,
            "timestamp" to now,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        try {
            firestore.collection("short_posts")
                .add(newShort)
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refresh(onComplete: (() -> Unit)? = null) {
        var pendingCount = 3
        fun checkDone() {
            pendingCount--
            if (pendingCount <= 0) {
                _isLoading.value = false
                onComplete?.invoke()
            }
        }

        try {
            _isLoading.value = true
            firestore.collection("blog_posts").get().addOnCompleteListener { task ->
                try {
                    if (task.isSuccessful && task.result != null) {
                        val docs = task.result.documents
                        docs.forEach { knownPostIds.add(it.id) }
                        postsFromBlog = parseBlogDocs(docs)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                updateMergedBlogPosts()
                checkDone()
            }

            firestore.collection("articles").get().addOnCompleteListener { task ->
                try {
                    if (task.isSuccessful && task.result != null) {
                        val docs = task.result.documents
                        docs.forEach { knownPostIds.add(it.id) }
                        postsFromArticles = parseBlogDocs(docs)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                updateMergedBlogPosts()
                checkDone()
            }

            firestore.collection("short_posts").get().addOnCompleteListener { task ->
                try {
                    if (task.isSuccessful && task.result != null) {
                        val docs = task.result.documents
                        docs.forEach { knownPostIds.add(it.id) }
                        val remotePosts = docs.mapNotNull { doc ->
                            try {
                                val text = doc.getString("text") ?: doc.getString("content") ?: doc.getString("title") ?: ""
                                if (text.isBlank()) null
                                else {
                                    ShortPost(
                                        id = doc.id,
                                        text = text,
                                        reference = doc.getString("reference") ?: doc.getString("ref") ?: "",
                                        category = doc.getString("category") ?: "দৈনিক নসীহত",
                                        author = doc.getString("author") ?: "ইসলামিক স্কলার",
                                        timestamp = extractTimestamp(doc)
                                    )
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        val remainingLocals = _shortPosts.value
                            .filter { it.id.startsWith("local_") }
                            .filter { local -> remotePosts.none { remote -> remote.text.trim() == local.text.trim() } }

                        _shortPosts.value = (remotePosts + remainingLocals)
                            .distinctBy { if (it.id.isNotEmpty()) it.id else it.text.trim() }
                            .sortedByDescending { it.timestamp }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                checkDone()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isLoading.value = false
            onComplete?.invoke()
        }
    }

    private fun getInitialBlogPosts(): List<BlogPost> {
        return emptyList()
    }

    private fun getInitialShortPosts(): List<ShortPost> {
        return emptyList()
    }
}
