package com.example.data.repository

import android.content.Context
import com.example.data.api.QuranApi
import com.example.data.api.QuranComApi
import com.example.data.model.CombinedAyah
import com.example.data.model.Surah
import com.example.data.model.QuranComTafsirResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first
import java.io.File

/**
 * Repository to manage data operations for the Quran Reader app
 */
class QuranRepository(
    private val api: QuranApi,
    private val quranComApi: QuranComApi,
    private val settingsRepository: SettingsRepository,
    val context: Context
) {
    fun isTafsirDownloaded(tafsirId: String): Boolean {
        val dir = File(context.filesDir, "tafsir_cache/$tafsirId")
        if (!dir.exists()) return false
        return (dir.listFiles()?.size ?: 0) >= 114
    }

    suspend fun downloadTafsir(tafsirId: String, onProgress: (Float) -> Unit) {
        val dir = File(context.filesDir, "tafsir_cache/$tafsirId")
        dir.mkdirs()
        for (i in 1..114) {
            val file = File(dir, "$i.json")
            if (!file.exists() || file.length() == 0L) {
                try {
                    val response = quranComApi.getSurahTafsirs(i, tafsirId)
                    file.writeText(Gson().toJson(response))
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw Exception("Failed to download Surah $i for tafsir $tafsirId: ${e.message}")
                }
            }
            onProgress(i / 114f)
        }
    }

    private suspend fun fetchSingleSurahTafsir(surahNumber: Int, tafsirId: String): QuranComTafsirResponse? {
        val file = File(context.filesDir, "tafsir_cache/$tafsirId/$surahNumber.json")
        if (file.exists() && file.length() > 0) {
            try {
                return Gson().fromJson(file.readText(), QuranComTafsirResponse::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return try {
            quranComApi.getSurahTafsirs(surahNumber, tafsirId)
        } catch (e: Exception) {
            null
        }
    }


    private suspend fun getCombinedSurahTafsirs(surahNumber: Int, tafsirIdsStr: String): QuranComTafsirResponse? = coroutineScope {
        try {
            val ids = tafsirIdsStr.split(",")
            val deferreds = ids.map { id ->
                async { fetchSingleSurahTafsir(surahNumber, id.trim()) }
            }
            val responses = deferreds.awaitAll()
            val allTafsirs = responses.flatMap { it?.tafsirs ?: emptyList() }
            QuranComTafsirResponse(tafsirs = allTafsirs)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getCombinedPageTafsirs(pageNumber: Int, tafsirIdsStr: String): QuranComTafsirResponse? = coroutineScope {
        try {
            val ids = tafsirIdsStr.split(",")
            val deferreds = ids.map { id ->
                async { quranComApi.getPageTafsirs(pageNumber, id.trim()) }
            }
            val responses = deferreds.awaitAll()
            val allTafsirs = responses.flatMap { it?.tafsirs ?: emptyList() }
            QuranComTafsirResponse(tafsirs = allTafsirs)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getCombinedJuzTafsirs(juzNumber: Int, tafsirIdsStr: String): QuranComTafsirResponse? = coroutineScope {
        try {
            val ids = tafsirIdsStr.split(",")
            val deferreds = ids.map { id ->
                async { quranComApi.getJuzTafsirs(juzNumber, id.trim()) }
            }
            val responses = deferreds.awaitAll()
            val allTafsirs = responses.flatMap { it?.tafsirs ?: emptyList() }
            QuranComTafsirResponse(tafsirs = allTafsirs)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun buildCombinedTafsirText(tafsirs: List<com.example.data.model.QuranComTafsirItem>?, verseKey: String): String? {
        if (tafsirs.isNullOrEmpty()) return null
        val verseTafsirs = tafsirs.filter { it.verseKey == verseKey }
        if (verseTafsirs.isEmpty()) return null
        
        if (verseTafsirs.size == 1) {
            return verseTafsirs.first().text
        }
        
        val availableTafsirs = getAvailableTafsirs("bn")
        return verseTafsirs.joinToString("<br><br>") { item ->
            val tafsirInfo = availableTafsirs.find { it.id == item.resourceId }
            val name = tafsirInfo?.name ?: "Tafsir ${item.resourceId}"
            val lang = tafsirInfo?.languageName?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: ""
            "<b>$name ($lang)</b><br>${item.text}"
        }
    }


    private val BISMILLAH_PREFIXES = listOf(
        "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ ",
        "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
        "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ ",
        "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ",
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ ",
        "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ",
        "بسم الله الرحمن الرحيم ",
        "بسم الله الرحمن الرحيم"
    )

    // In-memory caching structures to optimize loading times and prevent repeated network calls
    private var cachedSurahs: List<Surah>? = null
    private val cachedSurahDetails = java.util.concurrent.ConcurrentHashMap<String, List<CombinedAyah>>()
    private val cachedPageDetails = java.util.concurrent.ConcurrentHashMap<String, List<CombinedAyah>>()
    private val cachedJuzDetails = java.util.concurrent.ConcurrentHashMap<String, List<CombinedAyah>>()

    private val muqattaatMap = mapOf(
        "الم" to "الٓمٓ",
        "المص" to "الٓمٓصٓ",
        "الر" to "الٓر",
        "المر" to "الٓمٓر",
        "كهيعص" to "كٓهٰیٰعٓصٓ",
        "طه" to "طٰهٰ",
        "طسم" to "طسٓمٓ",
        "طس" to "طسٓ",
        "يس" to "يٰسٓ",
        "ص" to "صٓ",
        "حم" to "حٰمٓ",
        "عسق" to "عٓسٓقٓ",
        "ق" to "قٓ",
        "ন" to "نٓ",
        "ن" to "نٓ"
    )

    private fun formatHurufeMuqattaat(text: String): String {
        val diacriticsRegex = Regex("[\\u064B-\\u065F\\u0670\\u06E1\\u06E2\\u06D6-\\u06DC]")
        val words = text.split(" ")
        val formattedWords = words.map { word ->
            val cleanWord = word.replace(diacriticsRegex, "").trim()
            muqattaatMap[cleanWord] ?: word
        }
        return formattedWords.joinToString(" ")
    }

    private fun processArabicText(ayah: com.example.data.model.Ayah, defaultSurahNumber: Int = -1): String {
        val surahNumber = ayah.surah?.number ?: defaultSurahNumber
        var text = ayah.text
        if (ayah.numberInSurah == 1 && surahNumber != 1 && surahNumber != 9) {
            for (prefix in BISMILLAH_PREFIXES) {
                if (text.startsWith(prefix)) {
                    text = text.removePrefix(prefix).trimStart()
                    break
                }
            }
        }
        return text
    }

    private fun cleanCombinedAyahList(list: List<CombinedAyah>): List<CombinedAyah> {
        val tajweedRegex = Regex("[\u06E2\u06E5\u06E6]")
        return list.map { ayah ->
            val sNum = ayah.surahNumber
            val rawWords = ayah.words
            var cleanedArabicText = ayah.arabicText
            if (ayah.numberInSurah == 1 && sNum != 1 && sNum != 9) {
                for (prefix in BISMILLAH_PREFIXES) {
                    if (cleanedArabicText.startsWith(prefix)) {
                        cleanedArabicText = cleanedArabicText.removePrefix(prefix).trimStart()
                        break
                    }
                }
            }
            
            // Format Hurufe Muqatta'at in full text
            cleanedArabicText = formatHurufeMuqattaat(cleanedArabicText)
            
            // Remove specific Tajweed marks
            cleanedArabicText = cleanedArabicText.replace(tajweedRegex, "")
            
            // Format Hurufe Muqatta'at in individual word-by-word text and remove Tajweed marks
            val formattedWords = rawWords.map { word ->
                val text = word.textUthmani
                if (text != null) {
                    word.copy(textUthmani = formatHurufeMuqattaat(text).replace(tajweedRegex, ""))
                } else {
                    word
                }
            }
            
            ayah.copy(words = formattedWords, arabicText = cleanedArabicText)
        }
    }

    private val surahCacheFile by lazy {
        File(context.filesDir, "quran_text_cache/surahs.json")
    }

    private fun getSurahDetailsCacheFile(surahNumber: Int, tafsirIds: String, arabicEdition: String = "quran-uthmani"): File {
        return if (arabicEdition == "quran-uthmani") {
            File(context.filesDir, "quran_text_cache/surah_details_${surahNumber}_$tafsirIds.json")
        } else {
            File(context.filesDir, "quran_text_cache/surah_details_${surahNumber}_${arabicEdition}_$tafsirIds.json")
        }
    }

    private fun getPageDetailsCacheFile(pageNumber: Int, tafsirIds: String): File {
        return File(context.filesDir, "quran_text_cache/page_details_${pageNumber}_$tafsirIds.json")
    }

    private fun getJuzDetailsCacheFile(juzNumber: Int, tafsirIds: String): File {
        return File(context.filesDir, "quran_text_cache/juz_details_${juzNumber}_$tafsirIds.json")
    }

    fun getDownloadedSurahsCount(): Int {
        var count = 0
        for (i in 1..114) {
            if (isSurahDownloaded(i)) {
                count++
            }
        }
        return count
    }

    fun isSurahDownloaded(surahNumber: Int): Boolean {
        val cacheDir = File(context.filesDir, "quran_text_cache")
        if (!cacheDir.exists()) return false
        val cacheFiles = cacheDir.listFiles { _, name ->
            name.startsWith("surah_details_${surahNumber}_") && name.endsWith(".json")
        }
        if (cacheFiles.isNullOrEmpty()) return false
        
        for (cacheFile in cacheFiles) {
            if (cacheFile.length() == 0L) continue
            try {
                val json = cacheFile.readText()
                val type = object : com.google.gson.reflect.TypeToken<List<com.example.data.model.CombinedAyah>>() {}.type
                val list = com.google.gson.Gson().fromJson<List<com.example.data.model.CombinedAyah>>(json, type)
                if (!list.isNullOrEmpty()) {
                    val hasWords = list.any { it.words.isNotEmpty() }
                    val hasBengaliWords = hasWords && list.any { ayah ->
                        ayah.words.any { word ->
                            word.translation?.text?.any { it in 'ঀ'..'৿' } == true
                        }
                    }
                    if (hasBengaliWords) return true
                }
            } catch (e: Exception) {
            }
        }
        return false
    }

    fun isAllSurahsDownloaded(): Boolean {
        return getDownloadedSurahsCount() == 114
    }

    fun deleteDownloadedSurahs() {
        val dir = File(context.filesDir, "quran_text_cache")
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        cachedSurahs = null
        cachedSurahDetails.clear()
        cachedPageDetails.clear()
        cachedJuzDetails.clear()
    }

    /**
     * Fetches the list of all Surahs
     */
    suspend fun getSurahs(): List<Surah> {
        cachedSurahs?.let { return it }
        return withContext(Dispatchers.IO) {
            // First, try loading from cache
            if (surahCacheFile.exists() && surahCacheFile.length() > 0) {
                try {
                    val json = surahCacheFile.readText()
                    val type = object : TypeToken<List<Surah>>() {}.type
                    val list = Gson().fromJson<List<Surah>>(json, type)
                    if (!list.isNullOrEmpty()) {
                        cachedSurahs = list
                        return@withContext list
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Fetch from network
            val response = api.getSurahs()
            if (response.code == 200) {
                val data = response.data
                cachedSurahs = data
                try {
                    surahCacheFile.parentFile?.mkdirs()
                    surahCacheFile.writeText(Gson().toJson(data))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                data
            } else {
                throw Exception("Failed to load Surahs: ${response.status}")
            }
        }
    }

    /**
     * Fetches a specific Surah with both Arabic text and Bengali translation,
     * and combines them into a list of CombinedAyah for easy UI consumption.
     */
    suspend fun getSurahDetailsCombined(surahNumber: Int, arabicEdition: String = "quran-uthmani"): List<CombinedAyah> {
        val tafsirIdsSet = settingsRepository.selectedTafsirIdsFlow.first()
        val tafsirIdsStr = tafsirIdsSet.joinToString(",")
        val audioEdition = settingsRepository.selectedQariIdFlow.first()
        val cacheKey = "${surahNumber}_${arabicEdition}_${tafsirIdsStr}_${audioEdition}"
        val inMemory = cachedSurahDetails[cacheKey]
        if (inMemory != null) {
            val hasWords = inMemory.any { it.words.isNotEmpty() }
            val hasTajweed = inMemory.any { !it.textUthmaniTajweed.isNullOrEmpty() }
            val hasBengaliWords = hasWords && inMemory.any { ayah ->
                ayah.words.any { word ->
                    word.translation?.text?.any { it in '\u0980'..'\u09FF' } == true
                }
            }
            if (hasBengaliWords && hasTajweed) {
                return inMemory
            }
        }
        return withContext(Dispatchers.IO) {
            val cacheFile = getSurahDetailsCacheFile(surahNumber, tafsirIdsStr, "${arabicEdition}_${audioEdition}")
            var cachedList: List<CombinedAyah>? = null
            if (cacheFile.exists() && cacheFile.length() > 0) {
                try {
                    val json = cacheFile.readText()
                    val type = object : TypeToken<List<CombinedAyah>>() {}.type
                    val list = Gson().fromJson<List<CombinedAyah>>(json, type)
                    if (!list.isNullOrEmpty()) {
                        val cleanedList = cleanCombinedAyahList(list)
                        cachedList = cleanedList
                        
                        // If it has words for some ayahs and has Bengali translations, return it immediately
                        val hasWords = list.any { it.words.isNotEmpty() }
                        val hasTajweed = list.any { !it.textUthmaniTajweed.isNullOrEmpty() }
                        val hasBengaliWords = hasWords && list.any { ayah ->
                            ayah.words.any { word ->
                                word.translation?.text?.any { it in '\u0980'..'\u09FF' } == true
                            }
                        }
                        if (hasBengaliWords && hasTajweed) {
                            cachedSurahDetails[cacheKey] = cleanedList
                            return@withContext cleanedList
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val result = withTimeoutOrNull(5000) {
                    val response = if (arabicEdition == "quran-uthmani") {
                        api.getSurahWithEditions(surahNumber, "quran-uthmani,bn.bengali,$audioEdition")
                    } else {
                        api.getSurahWithEditions(surahNumber, "$arabicEdition,bn.bengali,$audioEdition")
                    }
                    
                    val quranComResponse = try {
                        quranComApi.getSurahVerses(surahNumber)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }

                    val quranComTafsirResponse = getCombinedSurahTafsirs(surahNumber, tafsirIdsStr)

                    if (response.code == 200 && response.data.size >= 2) {
                        // Determine which edition is which based on language code and format
                        val arabicEditionObj = response.data.find { it.edition.identifier == arabicEdition }
                            ?: response.data.find { it.edition.language == "ar" && it.edition.format == "text" }
                        val bengaliEdition = response.data.find { it.edition.identifier == "bn.bengali" }
                        val audioEdition = response.data.find { it.edition.identifier == "ar.alafasy" }

                        if (arabicEditionObj == null || bengaliEdition == null) {
                            throw Exception("Missing Arabic or Bengali editions in the response.")
                        }

                        val arabicAyahs = arabicEditionObj.ayahs
                        val bengaliAyahs = bengaliEdition.ayahs
                        val audioAyahs = audioEdition?.ayahs

                        // Combine them
                        val combined = arabicAyahs.mapIndexed { index, arabicAyah ->
                            val quranComVerse = quranComResponse?.verses?.find { it.verseNumber == arabicAyah.numberInSurah }
                            val verseKey = "$surahNumber:${arabicAyah.numberInSurah}"
                            val tafsir = buildCombinedTafsirText(quranComTafsirResponse?.tafsirs, verseKey)
                            val cachedWords = cachedList?.getOrNull(index)?.words ?: emptyList()
                            CombinedAyah(
                                number = arabicAyah.number,
                                numberInSurah = arabicAyah.numberInSurah,
                                page = arabicAyah.page,
                                juz = arabicAyah.juz,
                                surahNumber = surahNumber,
                                arabicText = processArabicText(arabicAyah, surahNumber),
                                bengaliText = bengaliAyahs.getOrNull(index)?.text ?: "Translation not available",
                                tafsirText = tafsir,
                                audioUrl = audioAyahs?.getOrNull(index)?.audio,
                                words = if (quranComVerse != null && quranComVerse.words.isNotEmpty()) quranComVerse.words else cachedWords,
                                textUthmaniTajweed = quranComVerse?.textUthmaniTajweed ?: cachedList?.getOrNull(index)?.textUthmaniTajweed
                            )
                        }
                        val cleaned = cleanCombinedAyahList(combined)
                        cachedSurahDetails[cacheKey] = cleaned
                        
                        try {
                            cacheFile.parentFile?.mkdirs()
                            cacheFile.writeText(Gson().toJson(cleaned))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        cleaned
                    } else {
                        null
                    }
                }

                if (result != null) {
                    result
                } else {
                    cachedList?.let { return@withContext it }
                    throw Exception("Failed to load Surah details: Timeout or invalid response.")
                }
            } catch (e: Exception) {
                cachedList?.let { return@withContext it }
                throw e
            }
        }
    }

    /**
     * Fetches a specific page of the Quran
     */
    suspend fun getPageCombined(pageNumber: Int): List<CombinedAyah> {
        val tafsirIdsSet = settingsRepository.selectedTafsirIdsFlow.first()
        val tafsirIdsStr = tafsirIdsSet.joinToString(",")
        val audioEdition = settingsRepository.selectedQariIdFlow.first()
        val cacheKey = "${pageNumber}_${tafsirIdsStr}"
        val inMemory = cachedPageDetails[cacheKey]
        if (inMemory != null) {
            val hasMissingWords = inMemory.any { it.words.isEmpty() }
            val hasMissingTajweed = inMemory.any { it.textUthmaniTajweed.isNullOrEmpty() }
            if (!hasMissingWords && !hasMissingTajweed) {
                return inMemory
            }
        }
        return withContext(Dispatchers.IO) {
            val cacheFile = getPageDetailsCacheFile(pageNumber, "${tafsirIdsStr}_${audioEdition}")
            var cachedList: List<CombinedAyah>? = null
            if (cacheFile.exists() && cacheFile.length() > 0) {
                try {
                    val json = cacheFile.readText()
                    val type = object : TypeToken<List<CombinedAyah>>() {}.type
                    val list = Gson().fromJson<List<CombinedAyah>>(json, type)
                    if (!list.isNullOrEmpty()) {
                        val cleanedList = cleanCombinedAyahList(list)
                        cachedList = cleanedList
                        val hasMissingWords = list.any { it.words.isEmpty() }
                        val hasMissingTajweed = list.any { it.textUthmaniTajweed.isNullOrEmpty() }
                        
                        if (!hasMissingWords && !hasMissingTajweed) {
                            cachedPageDetails[cacheKey] = cleanedList
                            return@withContext cleanedList
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val result = withTimeoutOrNull(5000) {
                    val arabicResponse = api.getPageArabic(pageNumber)
                    val audioResponse = api.getPageEdition(pageNumber, audioEdition)
                    
                    val quranComResponse = try {
                        quranComApi.getPageVerses(pageNumber)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }

                    val quranComTafsirResponse = getCombinedPageTafsirs(pageNumber, tafsirIdsStr)

                    if (arabicResponse.code == 200 && audioResponse.code == 200) {
                        val arabicAyahs = arabicResponse.data.ayahs
                        val audioAyahs = audioResponse.data.ayahs
                        
                        val combined = arabicAyahs.mapIndexed { index, arabicAyah ->
                            val quranComVerse = quranComResponse?.verses?.find { it.id == arabicAyah.number }
                            val verseKey = "${arabicAyah.surah?.number ?: 0}:${arabicAyah.numberInSurah}"
                            val tafsir = buildCombinedTafsirText(quranComTafsirResponse?.tafsirs, verseKey)
                            CombinedAyah(
                                number = arabicAyah.number,
                                numberInSurah = arabicAyah.numberInSurah,
                                page = arabicAyah.page,
                                juz = arabicAyah.juz,
                                surahNumber = arabicAyah.surah?.number ?: 0,
                                arabicText = processArabicText(arabicAyah),
                                bengaliText = "", // Hafezi mode doesn't need translation
                                tafsirText = tafsir,
                                audioUrl = audioAyahs.getOrNull(index)?.audio,
                                words = quranComVerse?.words ?: emptyList(),
                                textUthmaniTajweed = quranComVerse?.textUthmaniTajweed
                            )
                        }
                        val cleaned = cleanCombinedAyahList(combined)
                        cachedPageDetails[cacheKey] = cleaned
                        
                        try {
                            cacheFile.parentFile?.mkdirs()
                            cacheFile.writeText(Gson().toJson(cleaned))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        cleaned
                    } else {
                        null
                    }
                }

                if (result != null) {
                    result
                } else {
                    cachedList?.let { return@withContext it }
                    throw Exception("Failed to load Page details: Timeout or invalid response.")
                }
            } catch (e: retrofit2.HttpException) {
                cachedList?.let { return@withContext it }
                val errorBody = e.response()?.errorBody()?.string()
                throw Exception("HTTP Error ${e.code()}: $errorBody")
            } catch (e: Exception) {
                cachedList?.let { return@withContext it }
                throw Exception(e.toString())
            }
        }
    }

    /**
     * Fetches a specific Juz of the Quran
     */
    suspend fun getJuzCombined(juzNumber: Int): List<CombinedAyah> {
        val tafsirIdsSet = settingsRepository.selectedTafsirIdsFlow.first()
        val tafsirIdsStr = tafsirIdsSet.joinToString(",")
        val audioEdition = settingsRepository.selectedQariIdFlow.first()
        val cacheKey = "${juzNumber}_${tafsirIdsStr}"
        val inMemory = cachedJuzDetails[cacheKey]
        if (inMemory != null) {
            val hasWords = inMemory.any { it.words.isNotEmpty() }
            val hasTajweed = inMemory.any { !it.textUthmaniTajweed.isNullOrEmpty() }
            val hasBengaliWords = hasWords && inMemory.any { ayah ->
                ayah.words.any { word ->
                    word.translation?.text?.any { it in '\u0980'..'\u09FF' } == true
                }
            }
            if (hasBengaliWords && hasTajweed) {
                return inMemory
            }
        }
        return withContext(Dispatchers.IO) {
            val cacheFile = getJuzDetailsCacheFile(juzNumber, "${tafsirIdsStr}_${audioEdition}")
            var cachedList: List<CombinedAyah>? = null
            if (cacheFile.exists() && cacheFile.length() > 0) {
                try {
                    val json = cacheFile.readText()
                    val type = object : TypeToken<List<CombinedAyah>>() {}.type
                    val list = Gson().fromJson<List<CombinedAyah>>(json, type)
                    if (!list.isNullOrEmpty()) {
                        val cleanedList = cleanCombinedAyahList(list)
                        cachedList = cleanedList
                        
                        // If it has words for some ayahs and contains Bengali, return it immediately
                        val hasWords = list.any { it.words.isNotEmpty() }
                        val hasTajweed = list.any { !it.textUthmaniTajweed.isNullOrEmpty() }
                        val hasBengaliWords = hasWords && list.any { ayah ->
                            ayah.words.any { word ->
                                word.translation?.text?.any { it in '\u0980'..'\u09FF' } == true
                            }
                        }
                        if (hasBengaliWords && hasTajweed) {
                            cachedJuzDetails[cacheKey] = cleanedList
                            return@withContext cleanedList
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val result = withTimeoutOrNull(5000) {
                    val arabicResponse = api.getJuzArabic(juzNumber)
                    val bengaliResponse = api.getJuzBengali(juzNumber)
                    val audioResponse = api.getJuzEdition(juzNumber, audioEdition)
                    
                    val quranComResponse = try {
                        quranComApi.getJuzVerses(juzNumber)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }

                    val quranComTafsirResponse = getCombinedJuzTafsirs(juzNumber, tafsirIdsStr)

                    if (arabicResponse.code == 200 && bengaliResponse.code == 200) {
                        val arabicAyahs = arabicResponse.data.ayahs
                        val bengaliAyahs = bengaliResponse.data.ayahs
                        val audioAyahs = audioResponse.data.ayahs
                        
                        val combined = arabicAyahs.mapIndexed { index, arabicAyah ->
                            val quranComVerse = quranComResponse?.verses?.find { it.id == arabicAyah.number }
                            val verseKey = "${arabicAyah.surah?.number ?: 0}:${arabicAyah.numberInSurah}"
                            val tafsir = buildCombinedTafsirText(quranComTafsirResponse?.tafsirs, verseKey)
                            val cachedWords = cachedList?.getOrNull(index)?.words ?: emptyList()
                            CombinedAyah(
                                number = arabicAyah.number,
                                numberInSurah = arabicAyah.numberInSurah,
                                page = arabicAyah.page,
                                juz = arabicAyah.juz,
                                surahNumber = arabicAyah.surah?.number ?: 0,
                                arabicText = processArabicText(arabicAyah),
                                bengaliText = bengaliAyahs.getOrNull(index)?.text ?: "Translation not available",
                                tafsirText = tafsir,
                                audioUrl = audioAyahs.getOrNull(index)?.audio,
                                words = if (quranComVerse != null && quranComVerse.words.isNotEmpty()) quranComVerse.words else cachedWords,
                                textUthmaniTajweed = quranComVerse?.textUthmaniTajweed ?: cachedList?.getOrNull(index)?.textUthmaniTajweed
                            )
                        }
                        val cleaned = cleanCombinedAyahList(combined)
                        cachedJuzDetails[cacheKey] = cleaned
                        
                        try {
                            cacheFile.parentFile?.mkdirs()
                            cacheFile.writeText(Gson().toJson(cleaned))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        cleaned
                    } else {
                        null
                    }
                }

                if (result != null) {
                    result
                } else {
                    cachedList?.let { return@withContext it }
                    throw Exception("Failed to load Juz details: Timeout or invalid response.")
                }
            } catch (e: Exception) {
                cachedList?.let { return@withContext it }
                throw e
            }
        }
    }

    /**
     * Searches the Quran by a keyword
     */
    suspend fun searchQuran(keyword: String, edition: String = "bn.bengali"): com.example.data.model.SearchResponse {
        return withContext(Dispatchers.IO) {
            val response = api.searchQuranWithEdition(keyword, edition)
            if (response.code == 200) {
                response.data
            } else {
                throw Exception("Search failed: ${response.status}")
            }
        }
    }

    private var cachedTafsirs: List<com.example.data.model.TafsirResourceDto>? = null

    suspend fun getAvailableTafsirs(language: String = "bn"): List<com.example.data.model.TafsirResourceDto> {
        if (cachedTafsirs != null) return cachedTafsirs!!
        return try {
            val response = quranComApi.getAvailableTafsirs(language)
            cachedTafsirs = response.tafsirs
            cachedTafsirs!!
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
