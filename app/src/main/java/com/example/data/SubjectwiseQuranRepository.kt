package com.example.data

import android.content.Context
import com.example.data.local.offline.OfflineQuranDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object SubjectwiseQuranRepository {

    fun Int.toBanglaDigits(): String {
        val englishDigits = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val banglaDigits = listOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return this.toString().map { char ->
            val index = englishDigits.indexOf(char)
            if (index != -1) banglaDigits[index] else char
        }.joinToString("")
    }

    val surahBanglaNames = mapOf(
        1 to "সূরা আল-ফাতিহা", 2 to "সূরা আল-বাকারা", 3 to "সূরা আল-ইমরান", 4 to "সূরা আন-নিসা", 5 to "সূরা আল-মায়িদাহ",
        6 to "সূরা আল-আন'আম", 7 to "সূরা আল-আ'রাফ", 8 to "সূরা আল-আনফাল", 9 to "সূরা আত-তাওবাহ", 10 to "সূরা ইউনুস",
        11 to "সূরা হূদ", 12 to "সূরা ইউসুফ", 13 to "সূরা আর্-রা'দ", 14 to "সূরা ইব্রাহীম", 15 to "সূরা আল-হিজর",
        16 to "সূরা আন-নাহল", 17 to "সূরা আল-ইসরা", 18 to "সূরা আল-কাহফ", 19 to "সূরা মারইয়াম", 20 to "সূরা ত্বে-হা",
        21 to "সূরা আল-আনবিয়া", 22 to "সূরা আল-হাজ্জ", 23 to "সূরা আল-মুমিনুন", 24 to "সূরা আন-নূর", 25 to "সূরা আল-ফুরকান",
        26 to "সূরা আশ-শু'আরা", 27 to "সূরা আন-নামল", 28 to "সূরা আল-কাসাস", 29 to "সূরা আল-আনকাবুত", 30 to "সূরা আর-রূম",
        31 to "সূরা লুকমান", 32 to "সূরা আস-সাজদাহ", 33 to "সূরা আল-আহযাব", 34 to "সূরা সাবা", 35 to "সূরা ফাতির",
        36 to "সূরা ইয়া-সীন", 37 to "সূরা আস-সাফফাত", 38 to "সূরা স্বাদ", 39 to "সূরা আজ-যুমার", 40 to "সূরা গাফির",
        41 to "সূরা ফুসসিলাত", 42 to "সূরা আশ-শুরা", 43 to "সূরা আজ-যুখরুফ", 44 to "সূরা অদ-দুখান", 45 to "সূরা আল-জাসিয়াহ",
        46 to "সূরা আল-আহকাফ", 47 to "সূরা মুহাম্মদ", 48 to "সূরা আল-ফাতহ", 49 to "সূরা আল-হুজুরাত", 50 to "সূরা ক্বাফ",
        51 to "সূরা আয-যারিয়াত", 52 to "সূরা আত-তূর", 53 to "সূরা আন-নাজম", 54 to "সূরা আল-কামার", 55 to "সূরা আর-রহমান",
        56 to "সূরা আল-ওয়াকি'আহ", 57 to "সূরা আল-হাদীদ", 58 to "সূরা আল-মুজাদালাহ", 59 to "সূরা আল-হাশর", 60 to "সূরা আল-মুমতাহিনাহ",
        61 to "সূরা আস-সফ", 62 to "সূরা আল-জুমু'আহ", 63 to "সূরা আল-মুনাফিকুন", 64 to "সূরা আত-তাগাবুন", 65 to "সূরা আত-তালাক",
        66 to "সূরা আত-তাহরীম", 67 to "সূরা আল-মুলক", 68 to "সূরা আল-কলম", 69 to "সূরা আল-হাক্কাহ", 70 to "সূরা আল-মা'আরিজ",
        71 to "সূরা নূহ", 72 to "সূরা আল-জিন", 73 to "সূরা আল-মুযযামমিল", 74 to "সূরা আল-মুদ্দাসসির", 75 to "সূরা আল-কিয়ামাহ",
        76 to "সূরা আল-ইনসান", 77 to "সূরা আল-মুরসালাت", 78 to "সূরা আন-নাবা", 79 to "সূরা আন-নাযি'আত", 80 to "সূরা আবাসা",
        81 to "সূরা আত-তাকভীর", 82 to "সূরা আল-ইনফিতার", 83 to "সূরা আল-মুতাফফিফীন", 84 to "সূরা আল-ইনশিকাক", 85 to "সূরা আল-বুরুজ",
        86 to "সূরা আত-ত্বারিক", 87 to "সূরা আল-আ'লা", 88 to "সূরা আল-গাশিয়াহ", 89 to "সূরা আল-ফজর", 90 to "সূরা আল-বালাদ",
        91 to "সূরা আশ-শামস", 92 to "সূরা আল-লাইল", 93 to "সূরা আদ-দুহা", 94 to "সূরা আশ-শারহ", 95 to "সূরা আত-তীন",
        96 to "সূরা আল-আলাক", 97 to "সূরা আল-কদর", 98 to "সূরা আল-বায়্যিনাহ", 99 to "সূরা যিলযাল", 100 to "সূরা আল-আদিয়াত",
        101 to "সূরা আল-কারিয়াহ", 102 to "সূরা আত-তাকাসুর", 103 to "সূরা আল-আসর", 104 to "সূরা আল-হুমাযাহ", 105 to "সূরা আল-ফীল",
        106 to "সূরা কুরাইশ", 107 to "সূরা আল-মাউন", 108 to "সূরা আল-কাউসার", 109 to "সূরা আল-কাফিরুন", 110 to "সূরা আন-নাসর",
        111 to "সূরা আল-লাহাব", 112 to "সূরা আল-ইখলাস", 113 to "সূরা আল-ফালাক", 114 to "সূরা আন-নাস"
    )

    private var cachedCategories: List<SubjectwiseCategory>? = null

    suspend fun getCategories(context: Context): List<SubjectwiseCategory> = withContext(Dispatchers.IO) {
        cachedCategories?.let {
            if (it.isNotEmpty()) return@withContext it
        }

        try {
            val jsonString = context.assets.open("subjectwise_topics.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val dao = OfflineQuranDatabase.getDatabase(context).offlineQuranDao()

            val categoryList = mutableListOf<SubjectwiseCategory>()
            var globalVerseCounter = 1

            for (i in 0 until jsonArray.length()) {
                val catObj = jsonArray.getJSONObject(i)
                val categoryId = catObj.getInt("category_id")
                val categoryNameBn = catObj.getString("category_name_bn")
                val icon = catObj.optString("icon", "")
                val topicsArray = catObj.getJSONArray("topics")

                val topicList = mutableListOf<SubjectwiseTopic>()

                for (t in 0 until topicsArray.length()) {
                    val topicObj = topicsArray.getJSONObject(t)
                    val topicId = topicObj.getInt("topic_id")
                    val titleBn = topicObj.getString("title_bn")
                    val versesArray = topicObj.getJSONArray("verses")

                    val verseList = mutableListOf<SubjectwiseVerse>()

                    for (v in 0 until versesArray.length()) {
                        val verseObj = versesArray.getJSONObject(v)
                        val surahNumber = verseObj.getInt("surah")
                        val ayahNumber = verseObj.getInt("ayah")

                        // Query Room offline DB
                        val ayahEntity = dao.getAyahBySurahAndNumber(surahNumber, ayahNumber)

                        if (ayahEntity != null) {
                            val surahName = surahBanglaNames[surahNumber] ?: "সূরা $surahNumber"
                            val verseNoBangla = ayahNumber.toBanglaDigits()

                            verseList.add(
                                SubjectwiseVerse(
                                    id = globalVerseCounter++,
                                    surahNumber = surahNumber,
                                    ayahNumber = ayahNumber,
                                    surahName = surahName,
                                    verseNo = verseNoBangla,
                                    arabicText = ayahEntity.arabicText,
                                    banglaTranslation = ayahEntity.bengaliText,
                                    lesson = ""
                                )
                            )
                        }
                    }

                    if (verseList.isNotEmpty()) {
                        topicList.add(
                            SubjectwiseTopic(
                                topicId = topicId,
                                titleBn = titleBn,
                                verses = verseList
                            )
                        )
                    }
                }

                if (topicList.isNotEmpty()) {
                    categoryList.add(
                        SubjectwiseCategory(
                            categoryId = categoryId,
                            categoryNameBn = categoryNameBn,
                            icon = icon,
                            topics = topicList
                        )
                    )
                }
            }

            cachedCategories = categoryList
            return@withContext categoryList
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
}
