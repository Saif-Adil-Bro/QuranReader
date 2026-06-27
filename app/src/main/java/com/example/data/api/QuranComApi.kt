package com.example.data.api

import com.example.data.model.QuranComResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuranComApi {
    @GET("verses/by_chapter/{chapter_number}")
    suspend fun getSurahVerses(
        @Path("chapter_number") chapterNumber: Int,
        @Query("words") words: Boolean = true,
        @Query("word_fields") wordFields: String = "text_uthmani,location",
        @Query("translations") translations: Int = 161,
        @Query("language") language: String = "bn",
        @Query("word_translation_language") wordTranslationLanguage: String = "bn",
        @Query("per_page") perPage: Int = 1000
    ): QuranComResponse

    @GET("verses/by_juz/{juz_number}")
    suspend fun getJuzVerses(
        @Path("juz_number") juzNumber: Int,
        @Query("words") words: Boolean = true,
        @Query("word_fields") wordFields: String = "text_uthmani,location",
        @Query("translations") translations: Int = 161,
        @Query("language") language: String = "bn",
        @Query("word_translation_language") wordTranslationLanguage: String = "bn",
        @Query("per_page") perPage: Int = 1000
    ): QuranComResponse

    @GET("verses/by_page/{page_number}")
    suspend fun getPageVerses(
        @Path("page_number") pageNumber: Int,
        @Query("words") words: Boolean = true,
        @Query("word_fields") wordFields: String = "text_uthmani,location",
        @Query("translations") translations: Int = 161,
        @Query("language") language: String = "bn",
        @Query("word_translation_language") wordTranslationLanguage: String = "bn",
        @Query("per_page") perPage: Int = 1000
    ): QuranComResponse
}
