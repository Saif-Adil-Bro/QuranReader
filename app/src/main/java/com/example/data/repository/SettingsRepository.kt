package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Repository for handling app-wide settings like translation toggles.
 */
class SettingsRepository(val context: Context) {

    private val SHOW_TRANSLATION_KEY = booleanPreferencesKey("show_translation")
    private val LAST_READ_SURAH_KEY = intPreferencesKey("last_read_surah")
    private val LAST_READ_PAGE_KEY = intPreferencesKey("last_read_page")
    
    // Reading Mode Settings
    private val ARABIC_FONT_SIZE_KEY = floatPreferencesKey("arabic_font_size")
    private val BENGALI_FONT_SIZE_KEY = floatPreferencesKey("bengali_font_size")
    private val THEME_KEY = stringPreferencesKey("theme")
    private val AUTO_SCROLL_SPEED_KEY = floatPreferencesKey("auto_scroll_speed")
    private val ARABIC_FONT_NAME_KEY = stringPreferencesKey("arabic_font_name")
    
    // Hafezi Mode Settings
    private val REPEAT_COUNT_KEY = intPreferencesKey("repeat_count")

    val showTranslationFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[SHOW_TRANSLATION_KEY] ?: true }

    val lastReadSurahFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[LAST_READ_SURAH_KEY] ?: 1 }

    val lastReadPageFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[LAST_READ_PAGE_KEY] ?: 1 }

    val arabicFontSizeFlow: Flow<Float> = context.dataStore.data
        .map { preferences -> preferences[ARABIC_FONT_SIZE_KEY] ?: 24f }

    val bengaliFontSizeFlow: Flow<Float> = context.dataStore.data
        .map { preferences -> preferences[BENGALI_FONT_SIZE_KEY] ?: 16f }

    val arabicFontNameFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[ARABIC_FONT_NAME_KEY] ?: "Amiri Quran" }

    val themeFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[THEME_KEY] ?: "Light" }

    val autoScrollSpeedFlow: Flow<Float> = context.dataStore.data
        .map { preferences -> preferences[AUTO_SCROLL_SPEED_KEY] ?: 1f }

    val repeatCountFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[REPEAT_COUNT_KEY] ?: 1 }

    suspend fun setShowTranslation(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_TRANSLATION_KEY] = show }
    }

    suspend fun setLastReadSurah(surahNumber: Int) {
        context.dataStore.edit { preferences -> preferences[LAST_READ_SURAH_KEY] = surahNumber }
    }

    suspend fun setLastReadPage(pageNumber: Int) {
        context.dataStore.edit { preferences -> preferences[LAST_READ_PAGE_KEY] = pageNumber }
    }

    suspend fun setArabicFontSize(size: Float) {
        context.dataStore.edit { preferences -> preferences[ARABIC_FONT_SIZE_KEY] = size }
    }

    suspend fun setBengaliFontSize(size: Float) {
        context.dataStore.edit { preferences -> preferences[BENGALI_FONT_SIZE_KEY] = size }
    }

    suspend fun setArabicFontName(fontName: String) {
        context.dataStore.edit { preferences -> preferences[ARABIC_FONT_NAME_KEY] = fontName }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences -> preferences[THEME_KEY] = theme }
    }

    suspend fun setAutoScrollSpeed(speed: Float) {
        context.dataStore.edit { preferences -> preferences[AUTO_SCROLL_SPEED_KEY] = speed }
    }

    suspend fun setRepeatCount(count: Int) {
        context.dataStore.edit { preferences -> preferences[REPEAT_COUNT_KEY] = count }
    }
}
