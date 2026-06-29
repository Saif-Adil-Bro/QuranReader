package com.example.data.repository

import com.example.data.api.QuranApi
import com.example.data.api.QuranComApi
import com.example.data.model.CombinedAyah
import com.example.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository to manage data operations for the Quran Reader app
 */
class QuranRepository(private val api: QuranApi, private val quranComApi: QuranComApi) {

    private val BISMILLAH_PREFIX = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ "

    private fun processArabicText(ayah: com.example.data.model.Ayah, defaultSurahNumber: Int = -1): String {
        val surahNumber = ayah.surah?.number ?: defaultSurahNumber
        var text = ayah.text
        if (ayah.numberInSurah == 1 && surahNumber != 1 && surahNumber != 9 && text.startsWith(BISMILLAH_PREFIX)) {
            text = text.removePrefix(BISMILLAH_PREFIX)
        }
        return text
    }

    /**
     * Fetches the list of all Surahs
     */
    suspend fun getSurahs(): List<Surah> {
        return withContext(Dispatchers.IO) {
            val response = api.getSurahs()
            if (response.code == 200) {
                response.data
            } else {
                throw Exception("Failed to load Surahs: ${response.status}")
            }
        }
    }

    /**
     * Fetches a specific Surah with both Arabic text and Bengali translation,
     * and combines them into a list of CombinedAyah for easy UI consumption.
     */
    suspend fun getSurahDetailsCombined(surahNumber: Int): List<CombinedAyah> {
        return withContext(Dispatchers.IO) {
            val response = api.getSurahWithTranslation(surahNumber)
            val quranComResponse = try {
                quranComApi.getSurahVerses(surahNumber)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (response.code == 200 && response.data.size >= 2) {
                // Determine which edition is which based on language code and format
                val arabicEdition = response.data.find { it.edition.identifier == "quran-uthmani" }
                val bengaliEdition = response.data.find { it.edition.identifier == "bn.bengali" }
                val audioEdition = response.data.find { it.edition.identifier == "ar.alafasy" }

                if (arabicEdition == null || bengaliEdition == null) {
                    throw Exception("Missing Arabic or Bengali editions in the response.")
                }

                val arabicAyahs = arabicEdition.ayahs
                val bengaliAyahs = bengaliEdition.ayahs
                val audioAyahs = audioEdition?.ayahs

                // Combine them
                arabicAyahs.mapIndexed { index, arabicAyah ->
                    val quranComVerse = quranComResponse?.verses?.find { it.verseNumber == arabicAyah.numberInSurah }
                    val tafsir = quranComVerse?.tafsirs?.firstOrNull()?.text?.let { android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_COMPACT).toString() }
                    CombinedAyah(
                        number = arabicAyah.number,
                        numberInSurah = arabicAyah.numberInSurah,
                        page = arabicAyah.page,
                        juz = arabicAyah.juz,
                        arabicText = processArabicText(arabicAyah, surahNumber),
                        bengaliText = bengaliAyahs.getOrNull(index)?.text ?: "Translation not available",
                        tafsirText = tafsir,
                        audioUrl = audioAyahs?.getOrNull(index)?.audio,
                        words = quranComVerse?.words ?: emptyList()
                    )
                }
            } else {
                throw Exception("Failed to load Surah details: Invalid response structure.")
            }
        }
    }

    /**
     * Fetches a specific page of the Quran
     */
    suspend fun getPageCombined(pageNumber: Int): List<CombinedAyah> {
        return withContext(Dispatchers.IO) {
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
                    
                    arabicAyahs.mapIndexed { index, arabicAyah ->
                        val quranComVerse = quranComResponse?.verses?.find { it.id == arabicAyah.number }
                        val tafsir = quranComVerse?.tafsirs?.firstOrNull()?.text?.let { android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_COMPACT).toString() }
                        CombinedAyah(
                            number = arabicAyah.number,
                            numberInSurah = arabicAyah.numberInSurah,
                            page = arabicAyah.page,
                            juz = arabicAyah.juz,
                            arabicText = processArabicText(arabicAyah),
                            bengaliText = "", // Hafezi mode doesn't need translation
                            tafsirText = tafsir,
                            audioUrl = audioAyahs.getOrNull(index)?.audio,
                            words = quranComVerse?.words ?: emptyList()
                        )
                    }
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
        return withContext(Dispatchers.IO) {
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
                    
                    arabicAyahs.mapIndexed { index, arabicAyah ->
                        val quranComVerse = quranComResponse?.verses?.find { it.id == arabicAyah.number }
                        val tafsir = quranComVerse?.tafsirs?.firstOrNull()?.text?.let { android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_COMPACT).toString() }
                        CombinedAyah(
                            number = arabicAyah.number,
                            numberInSurah = arabicAyah.numberInSurah,
                            page = arabicAyah.page,
                            juz = arabicAyah.juz,
                            arabicText = processArabicText(arabicAyah),
                            bengaliText = bengaliAyahs.getOrNull(index)?.text ?: "Translation not available",
                            tafsirText = tafsir,
                            audioUrl = audioAyahs.getOrNull(index)?.audio,
                            words = quranComVerse?.words ?: emptyList()
                        )
                    }
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
    suspend fun searchQuran(keyword: String): com.example.data.model.SearchResponse {
        return withContext(Dispatchers.IO) {
            val response = api.searchQuran(keyword)
            if (response.code == 200) {
                response.data
            } else {
                throw Exception("Search failed: ${response.status}")
            }
        }
    }
}
