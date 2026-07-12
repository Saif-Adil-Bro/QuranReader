package com.example.data.repository

import android.content.Context
import com.example.data.api.QuranApi
import com.example.data.api.QuranComApi
import com.example.data.model.CombinedAyah
import com.example.data.model.Surah
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository to manage data operations for the Quran Reader app
 */
class QuranRepository(
    private val api: QuranApi,
    private val quranComApi: QuranComApi,
    val context: Context
) {

    private val BISMILLAH_PREFIX = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ "

    // In-memory caching structures to optimize loading times and prevent repeated network calls
    private var cachedSurahs: List<Surah>? = null
    private val cachedSurahDetails = java.util.concurrent.ConcurrentHashMap<String, List<CombinedAyah>>()
    private val cachedPageDetails = java.util.concurrent.ConcurrentHashMap<Int, List<CombinedAyah>>()
    private val cachedJuzDetails = java.util.concurrent.ConcurrentHashMap<Int, List<CombinedAyah>>()

    private fun processArabicText(ayah: com.example.data.model.Ayah, defaultSurahNumber: Int = -1): String {
        val surahNumber = ayah.surah?.number ?: defaultSurahNumber
        var text = ayah.text
        if (ayah.numberInSurah == 1 && surahNumber != 1 && surahNumber != 9 && text.startsWith(BISMILLAH_PREFIX)) {
            text = text.removePrefix(BISMILLAH_PREFIX)
        }
        return text
    }

    private val surahCacheFile by lazy {
        File(context.filesDir, "quran_text_cache/surahs.json")
    }

    private fun getSurahDetailsCacheFile(surahNumber: Int, arabicEdition: String = "quran-uthmani"): File {
        return if (arabicEdition == "quran-uthmani") {
            File(context.filesDir, "quran_text_cache/surah_details_$surahNumber.json")
        } else {
            File(context.filesDir, "quran_text_cache/surah_details_${surahNumber}_$arabicEdition.json")
        }
    }

    private fun getPageDetailsCacheFile(pageNumber: Int): File {
        return File(context.filesDir, "quran_text_cache/page_details_$pageNumber.json")
    }

    private fun getJuzDetailsCacheFile(juzNumber: Int): File {
        return File(context.filesDir, "quran_text_cache/juz_details_$juzNumber.json")
    }

    fun getDownloadedSurahsCount(): Int {
        var count = 0
        for (i in 1..114) {
            if (getSurahDetailsCacheFile(i).exists()) {
                count++
            }
        }
        return count
    }

    fun isSurahDownloaded(surahNumber: Int): Boolean {
        return getSurahDetailsCacheFile(surahNumber).exists()
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
        val cacheKey = "${surahNumber}_$arabicEdition"
        cachedSurahDetails[cacheKey]?.let { return it }
        return withContext(Dispatchers.IO) {
            val cacheFile = getSurahDetailsCacheFile(surahNumber, arabicEdition)
            if (cacheFile.exists() && cacheFile.length() > 0) {
                try {
                    val json = cacheFile.readText()
                    val type = object : TypeToken<List<CombinedAyah>>() {}.type
                    val list = Gson().fromJson<List<CombinedAyah>>(json, type)
                    
                    // Invalidate cache if it contains ayahs with missing words (to recover from past bugs/timeouts)
                    val hasMissingWords = list.any { it.words.isEmpty() }
                    
                    if (!list.isNullOrEmpty() && !hasMissingWords) {
                        cachedSurahDetails[cacheKey] = list
                        return@withContext list
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val response = if (arabicEdition == "quran-uthmani") {
                api.getSurahWithTranslation(surahNumber)
            } else {
                api.getSurahWithEditions(surahNumber, "$arabicEdition,bn.bengali,ar.alafasy")
            }
            
            val quranComResponse = try {
                quranComApi.getSurahVerses(surahNumber)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

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
                    val tafsir = quranComVerse?.tafsirs?.firstOrNull()?.text?.let { android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_COMPACT).toString() }
                    CombinedAyah(
                        number = arabicAyah.number,
                        numberInSurah = arabicAyah.numberInSurah,
                        page = arabicAyah.page,
                        juz = arabicAyah.juz,
                        surahNumber = arabicAyah.surah?.number ?: surahNumber,
                        arabicText = processArabicText(arabicAyah, surahNumber),
                        bengaliText = bengaliAyahs.getOrNull(index)?.text ?: "Translation not available",
                        tafsirText = tafsir,
                        audioUrl = audioAyahs?.getOrNull(index)?.audio,
                        words = quranComVerse?.words ?: emptyList()
                    )
                }
                cachedSurahDetails[cacheKey] = combined
                
                try {
                    cacheFile.parentFile?.mkdirs()
                    cacheFile.writeText(Gson().toJson(combined))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                combined
            } else {
                throw Exception("Failed to load Surah details: Invalid response structure.")
            }
        }
    }

    /**
     * Fetches a specific page of the Quran
     */
    suspend fun getPageCombined(pageNumber: Int): List<CombinedAyah> {
        cachedPageDetails[pageNumber]?.let { return it }
        return withContext(Dispatchers.IO) {
            val cacheFile = getPageDetailsCacheFile(pageNumber)
            if (cacheFile.exists() && cacheFile.length() > 0) {
                try {
                    val json = cacheFile.readText()
                    val type = object : TypeToken<List<CombinedAyah>>() {}.type
                    val list = Gson().fromJson<List<CombinedAyah>>(json, type)
                    
                    val hasMissingWords = list.any { it.words.isEmpty() }
                    
                    if (!list.isNullOrEmpty() && !hasMissingWords) {
                        cachedPageDetails[pageNumber] = list
                        return@withContext list
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val arabicResponse = api.getPageArabic(pageNumber)
                val audioResponse = api.getPageAudio(pageNumber)
                
                val quranComResponse = try {
                    quranComApi.getPageVerses(pageNumber)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                if (arabicResponse.code == 200 && audioResponse.code == 200) {
                    val arabicAyahs = arabicResponse.data.ayahs
                    val audioAyahs = audioResponse.data.ayahs
                    
                    val combined = arabicAyahs.mapIndexed { index, arabicAyah ->
                        val quranComVerse = quranComResponse?.verses?.find { it.id == arabicAyah.number }
                        val tafsir = quranComVerse?.tafsirs?.firstOrNull()?.text?.let { android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_COMPACT).toString() }
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
                            words = quranComVerse?.words ?: emptyList()
                        )
                    }
                    cachedPageDetails[pageNumber] = combined
                    
                    try {
                        cacheFile.parentFile?.mkdirs()
                        cacheFile.writeText(Gson().toJson(combined))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    combined
                } else {
                    throw Exception("Failed to load Page details: Invalid response.")
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                throw Exception("HTTP Error ${e.code()}: $errorBody")
            } catch (e: Exception) {
                throw Exception(e.toString())
            }
        }
    }

    /**
     * Fetches a specific Juz of the Quran
     */
    suspend fun getJuzCombined(juzNumber: Int): List<CombinedAyah> {
        cachedJuzDetails[juzNumber]?.let { return it }
        return withContext(Dispatchers.IO) {
            val cacheFile = getJuzDetailsCacheFile(juzNumber)
            if (cacheFile.exists() && cacheFile.length() > 0) {
                try {
                    val json = cacheFile.readText()
                    val type = object : TypeToken<List<CombinedAyah>>() {}.type
                    val list = Gson().fromJson<List<CombinedAyah>>(json, type)
                    
                    val hasMissingWords = list.any { it.words.isEmpty() }
                    
                    if (!list.isNullOrEmpty() && !hasMissingWords) {
                        cachedJuzDetails[juzNumber] = list
                        return@withContext list
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val arabicResponse = api.getJuzArabic(juzNumber)
                val bengaliResponse = api.getJuzBengali(juzNumber)
                val audioResponse = api.getJuzAudio(juzNumber)
                
                val quranComResponse = try {
                    quranComApi.getJuzVerses(juzNumber)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                if (arabicResponse.code == 200 && bengaliResponse.code == 200) {
                    val arabicAyahs = arabicResponse.data.ayahs
                    val bengaliAyahs = bengaliResponse.data.ayahs
                    val audioAyahs = audioResponse.data.ayahs
                    
                    val combined = arabicAyahs.mapIndexed { index, arabicAyah ->
                        val quranComVerse = quranComResponse?.verses?.find { it.id == arabicAyah.number }
                        val tafsir = quranComVerse?.tafsirs?.firstOrNull()?.text?.let { android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_COMPACT).toString() }
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
                            words = quranComVerse?.words ?: emptyList()
                        )
                    }
                    cachedJuzDetails[juzNumber] = combined
                    
                    try {
                        cacheFile.parentFile?.mkdirs()
                        cacheFile.writeText(Gson().toJson(combined))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    combined
                } else {
                    throw Exception("Failed to load Juz details: Invalid response structure.")
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                throw Exception("HTTP Error ${e.code()}: $errorBody")
            } catch (e: Exception) {
                throw Exception(e.toString())
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
}
