package com.example.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the base response from Al-Quran API
 */
data class ApiResponse<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: T
)

/**
 * Represents a Surah item in the list
 */
data class Surah(
    @SerializedName("number") val number: Int,
    @SerializedName("name") val name: String,
    @SerializedName("englishName") val englishName: String,
    @SerializedName("englishNameTranslation") val englishNameTranslation: String,
    @SerializedName("numberOfAyahs") val numberOfAyahs: Int,
    @SerializedName("revelationType") val revelationType: String
)

/**
 * Represents a detailed Surah response with its ayahs and edition info
 */
data class SurahDetail(
    @SerializedName("number") val number: Int,
    @SerializedName("name") val name: String,
    @SerializedName("englishName") val englishName: String,
    @SerializedName("englishNameTranslation") val englishNameTranslation: String,
    @SerializedName("revelationType") val revelationType: String,
    @SerializedName("numberOfAyahs") val numberOfAyahs: Int,
    @SerializedName("ayahs") val ayahs: List<Ayah>,
    @SerializedName("edition") val edition: Edition
)

/**
 * Represents an Ayah (verse) in a Surah
 */
data class Ayah(
    @SerializedName("number") val number: Int,
    @SerializedName("text") val text: String,
    @SerializedName("surah") val surah: Surah? = null,
    @SerializedName("numberInSurah") val numberInSurah: Int,
    @SerializedName("juz") val juz: Int,
    @SerializedName("manzil") val manzil: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("ruku") val ruku: Int,
    @SerializedName("hizbQuarter") val hizbQuarter: Int,
    @SerializedName("audio") val audio: String? = null,
    @SerializedName("audioSecondary") val audioSecondary: List<String>? = null
)

/**
 * Represents the edition of the Quran text (e.g., Arabic Uthmani or Bengali translation)
 */
data class Edition(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("language") val language: String,
    @SerializedName("name") val name: String,
    @SerializedName("englishName") val englishName: String,
    @SerializedName("format") val format: String,
    @SerializedName("type") val type: String
)

/**
 * A combined UI model for displaying an Ayah with its translation side-by-side
 */
data class JuzResponse(
    @SerializedName("number") val number: Int,
    @SerializedName("ayahs") val ayahs: List<Ayah>,
    @SerializedName("edition") val edition: Edition
)

data class PageResponse(
    @SerializedName("number") val number: Int,
    @SerializedName("ayahs") val ayahs: List<Ayah>,
    @SerializedName("edition") val edition: Edition
)

/**
 * A combined UI model for displaying an Ayah with its translation side-by-side
 */
data class CombinedAyah(
    val number: Int, // Overall Ayah number in the Quran
    val numberInSurah: Int, // Ayah number within the Surah
    val page: Int, // Page number of the Ayah
    val juz: Int = 0, // Juz number of the Ayah
    val arabicText: String,
    val bengaliText: String,
    val tafsirText: String? = null,
    val audioUrl: String? = null,
    val words: List<QuranComWord> = emptyList()
)

/**
 * Models for Search
 */
data class SearchResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("matches") val matches: List<SearchMatch>
)

data class SearchMatch(
    @SerializedName("number") val number: Int,
    @SerializedName("text") val text: String,
    @SerializedName("edition") val edition: Edition,
    @SerializedName("surah") val surah: Surah,
    @SerializedName("numberInSurah") val numberInSurah: Int
)

/**
 * Models for Quran.com API v4 (Word by Word)
 */
data class QuranComResponse(
    @SerializedName("verses") val verses: List<QuranComVerse>
)

data class QuranComVerse(
    @SerializedName("id") val id: Int,
    @SerializedName("verse_key") val verseKey: String,
    @SerializedName("verse_number") val verseNumber: Int,
    @SerializedName("words") val words: List<QuranComWord> = emptyList(),
    @SerializedName("translations") val translations: List<QuranComTranslation>? = null,
    @SerializedName("tafsirs") val tafsirs: List<QuranComTafsir>? = null
)

data class QuranComTafsir(
    @SerializedName("resource_id") val resourceId: Int,
    @SerializedName("text") val text: String
)

data class QuranComWord(
    @SerializedName("id") val id: Int,
    @SerializedName("position") val position: Int,
    @SerializedName("char_type_name") val charTypeName: String,
    @SerializedName("text_uthmani") val textUthmani: String? = null,
    @SerializedName("translation") val translation: QuranComWordTranslation? = null
)

data class QuranComWordTranslation(
    @SerializedName("text") val text: String?
)

data class QuranComTranslation(
    @SerializedName("id") val id: Int,
    @SerializedName("resource_id") val resourceId: Int,
    @SerializedName("text") val text: String
)
