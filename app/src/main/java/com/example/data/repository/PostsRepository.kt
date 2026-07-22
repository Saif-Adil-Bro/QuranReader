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

    private var isFirstBlogSync = true
    private var isFirstShortSync = true

    init {
        // Load initial offline fallbacks
        _blogPosts.value = getInitialBlogPosts()
        _shortPosts.value = getInitialShortPosts()

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

    private fun listenToFirestore() {
        try {
            val handleBlogDocs: (List<DocumentSnapshot>) -> Unit = { docs ->
                val posts = docs.mapNotNull { doc ->
                    try {
                        val title = doc.getString("title") ?: doc.getString("name") ?: ""
                        val content = doc.getString("content") ?: doc.getString("text") ?: doc.getString("body") ?: ""
                        if (title.isBlank() && content.isBlank()) null
                        else {
                            BlogPost(
                                id = doc.id,
                                title = title.ifBlank { "ইসলামিক পোস্ট" },
                                content = content,
                                author = doc.getString("author") ?: "ইসলামিক এডমিন",
                                category = doc.getString("category") ?: "সাধারণ",
                                imageUrl = doc.getString("imageUrl") ?: doc.getString("image") ?: "",
                                readTime = doc.getString("readTime") ?: "২ মিনিট",
                                timestamp = extractTimestamp(doc)
                            )
                        }
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.timestamp }

                if (posts.isNotEmpty()) {
                    _blogPosts.value = posts
                }
            }

            // Listen to blog_posts
            firestore.collection("blog_posts")
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        handleBlogDocs(snapshot.documents)
                    }
                }

            // Listen to articles (as seen in Firestore console)
            firestore.collection("articles")
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        handleBlogDocs(snapshot.documents)
                    }
                }

            // Listen to short_posts
            firestore.collection("short_posts")
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val posts = snapshot.documents.mapNotNull { doc ->
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
                        }.sortedByDescending { it.timestamp }

                        if (posts.isNotEmpty()) {
                            _shortPosts.value = posts
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            _isLoading.value = false
        }
    }

    fun addBlogPost(title: String, content: String, category: String, author: String = "ইসলামিক এডমিন", onSuccess: () -> Unit, onError: (String) -> Unit) {
        val newPost = hashMapOf(
            "title" to title,
            "content" to content,
            "category" to category,
            "author" to author,
            "imageUrl" to "",
            "readTime" to "${(content.length / 300).coerceAtLeast(1)} মিনিট",
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("blog_posts")
            .add(newPost)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                // Fallback: Add locally if network/firestore rules error
                val localPost = BlogPost(
                    id = "local_${System.currentTimeMillis()}",
                    title = title,
                    content = content,
                    category = category,
                    author = author,
                    timestamp = System.currentTimeMillis()
                )
                _blogPosts.value = listOf(localPost) + _blogPosts.value
                onSuccess()
            }
    }

    fun addShortPost(text: String, reference: String, category: String, author: String = "ইসলামিক স্কলার", onSuccess: () -> Unit, onError: (String) -> Unit) {
        val newShort = hashMapOf(
            "text" to text,
            "reference" to reference,
            "category" to category,
            "author" to author,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("short_posts")
            .add(newShort)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                // Fallback: Add locally
                val localShort = ShortPost(
                    id = "local_${System.currentTimeMillis()}",
                    text = text,
                    reference = reference,
                    category = category,
                    author = author,
                    timestamp = System.currentTimeMillis()
                )
                _shortPosts.value = listOf(localShort) + _shortPosts.value
                onSuccess()
            }
    }

    private fun getInitialBlogPosts(): List<BlogPost> {
        return listOf(
            BlogPost(
                id = "default_1",
                title = "কুরআন তিলাওয়াত ও হৃদয় প্রশান্তির ইসলামিক তাৎপর্য",
                content = """
                    আল-কুরআন মহান আল্লাহর কালাম, যা মানবজাতির জন্য হিদায়াত ও রহমত হিসেবে নাজিল হয়েছে। দৈনিক জীবনের ব্যস্ততা ও মানসিক ক্লান্তির মাঝে তিলাওয়াত মানুষের অন্তরে এক অনাবিল প্রশান্তি এনে দেয়।

                    আল্লাহ তাআলা পবিত্র কুরআনে এরশাদ করেছেন:
                    "যারা ঈমান এনেছে এবং আল্লাহর স্মরণে যাদের অন্তর প্রশান্ত হয়; জেনে রাখ, আল্লাহর স্মরণেই কেবল অন্তরসমূহ প্রশান্ত হয়।" (সূরা আর-রাদ, আয়াত ২৮)

                    প্রতিদিন নিয়ম করে অন্তত কয়েক আয়াত অর্থসহ তিলাওয়াত করার মাধ্যমে আমরা আমাদের জীবনকে সুন্নাহ ও হিদায়াতের আলোয় আলোকিত করতে পারি।
                """.trimIndent(),
                author = "মুফতি আব্দুর রহমান",
                category = "কুরআন ও জীবন",
                readTime = "৩ মিনিট",
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            BlogPost(
                id = "default_2",
                title = "তাহাজ্জুদ সালাতের ফজিলত ও দোয়ার কবুলিয়াত",
                content = """
                    রাতের শেষ তৃতীয়াংশে যখন পৃথিবীর মানুষ ঘুমে মগ্ন থাকে, তখন মহান আল্লাহ তায়ালা প্রথম আসমানে নেমে আসেন এবং বান্দার ডাক শোনেন।

                    রাসূলুল্লাহ (সাল্লাল্লাহু আলাইহি ওয়া সাল্লাম) বলেছেন:
                    "আল্লাহ তাআলা প্রতি রাতে শেষ তৃতীয়াংশে প্রথম আসমানে অবতীর্ণ হয়ে আহ্বান করেন: কে আছ আমাকে ডাকবে, আমি তার ডাকে সাড়া দেব? কে আছ আমার কাছে চাইবে, আমি তাকে দান করব? কে আছ আমার কাছে ক্ষমা চাইবে, আমি তাকে ক্ষমা করব?" (সহীহ বুখারী)

                    তাহাজ্জুদের দুই রাকাত সালাত জীবনের বড় বড় গুনাহ মাফের উপায় এবং কঠিন বিপদ থেকে রক্ষার মহৌষধ।
                """.trimIndent(),
                author = "হাফেজ মাওলানা জাবের",
                category = "নফল ইবাদত",
                readTime = "৪ মিনিট",
                timestamp = System.currentTimeMillis() - 172800000L
            )
        )
    }

    private fun getInitialShortPosts(): List<ShortPost> {
        return listOf(
            ShortPost(
                id = "short_1",
                text = "উত্তম নৈতিকতা ও হাসিমুখে কথা বলাও এক ধরণের সদকা। কারো সাথে সাক্ষাৎ হলে হাসিমুখে সালাম দিন।",
                reference = "সহীহ জামে আস-সগীর, হাদিস: ৩৮৬১",
                category = "দৈনিক নীতি কথা",
                author = "ইসলামিক পয়েন্ট"
            ),
            ShortPost(
                id = "short_2",
                text = "যে ব্যক্তি সকালে ও সন্ধ্যায় তিনবার 'রদিতু বিল্লাহি রব্বান ওয়াবিল ইসলামি দ্বীনান ওয়াবি মুহাম্মাদিন নাবিয়্যান' পড়বে, আল্লাহ তায়ালা কিয়ামতের দিন তাকে সন্তুষ্ট করার দায়িত্ব নেবেন।",
                reference = "সূনান আবু দাউদ, হাদিস: ৫০৭২",
                category = "মাসনুন জিকির",
                author = "মাসনুন আমল"
            ),
            ShortPost(
                id = "short_3",
                text = "সবচেয়ে বুদ্ধিমান সেই ব্যক্তি যে নিজের নফসকে নিয়ন্ত্রণে রাখে এবং মৃত্যুর পরবর্তী জীবনের জন্য আমল করে।",
                reference = "সূনান তিরমিজি, হাদিস: ২৪৫৯",
                category = "আত্মশুদ্ধি",
                author = "নসীহত"
            )
        )
    }
}
