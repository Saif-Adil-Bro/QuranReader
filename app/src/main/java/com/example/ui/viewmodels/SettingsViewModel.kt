package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserNote(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

data class WordQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val bookmarkDao: BookmarkDao
) : ViewModel() {

    val showTranslation: StateFlow<Boolean> = repository.showTranslationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun toggleTranslation(show: Boolean) {
        viewModelScope.launch {
            repository.setShowTranslation(show)
        }
    }

    // 1. Bookmarks flow
    val bookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmark(bookmark)
        }
    }

    // 2. Profile & Notes Storage
    private val sharedPrefs by lazy {
        repository.context.getSharedPreferences("quran_menu_prefs", Context.MODE_PRIVATE)
    }

    // Profile State
    private val _username = MutableStateFlow("দ্বীনদার বান্দা")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _readingTimeMinutes = MutableStateFlow(60) // Default 1 hour reading
    val readingTimeMinutes: StateFlow<Int> = _readingTimeMinutes.asStateFlow()

    fun updateUsername(newName: String) {
        _username.value = newName
        sharedPrefs.edit().putString("username", newName).apply()
    }

    fun addReadingTime(mins: Int) {
        _readingTimeMinutes.value += mins
        sharedPrefs.edit().putInt("reading_time", _readingTimeMinutes.value).apply()
    }

    // Notes State
    private val _notes = MutableStateFlow<List<UserNote>>(emptyList())
    val notes: StateFlow<List<UserNote>> = _notes.asStateFlow()

    fun addNote(title: String, content: String) {
        val newNote = UserNote(
            id = System.currentTimeMillis().toString(),
            title = title,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        val updated = _notes.value + newNote
        _notes.value = updated
        saveNotesToPrefs(updated)
    }

    fun deleteNote(noteId: String) {
        val updated = _notes.value.filterNot { it.id == noteId }
        _notes.value = updated
        saveNotesToPrefs(updated)
    }

    private fun loadNotesFromPrefs() {
        val notesSet = sharedPrefs.getStringSet("user_notes_set", emptySet()) ?: emptySet()
        val loaded = notesSet.mapNotNull { noteStr ->
            val parts = noteStr.split("|||")
            if (parts.size >= 4) {
                UserNote(
                    id = parts[0],
                    title = parts[1],
                    content = parts[2],
                    timestamp = parts[3].toLongOrNull() ?: System.currentTimeMillis()
                )
            } else null
        }.sortedByDescending { it.timestamp }
        _notes.value = loaded
    }

    private fun saveNotesToPrefs(notesList: List<UserNote>) {
        val notesSet = notesList.map { "${it.id}|||${it.title}|||${it.content}|||${it.timestamp}" }.toSet()
        sharedPrefs.edit().putStringSet("user_notes_set", notesSet).apply()
    }

    // 3. Word Game State
    val questions = listOf(
        WordQuestion("আলামীন (العالمين)", listOf("সৃষ্টিজগৎ", "মানুষ", "ফেরেশতা", "নক্ষত্র"), "সৃষ্টিজগৎ"),
        WordQuestion("রাহমান (الرحمن)", listOf("পরম দয়ালু", "বিচারক", "স্রষ্টা", "মালিক"), "পরম দয়ালু"),
        WordQuestion("মা’বুদ (المعبود)", listOf("উপাস্য", "বন্ধু", "সাহায্যকারী", "শাসক"), "উপাস্য"),
        WordQuestion("ইয়াকীন (اليقين)", listOf("নিশ্চয়তা/বিশ্বাস", "সন্দেহ", "ভয়", "আশা"), "নিশ্চয়তা/বিশ্বাস"),
        WordQuestion("হুদা (هدى)", listOf("পথপ্রদর্শন", "জ্ঞান", "আলো", "পরিত্রাণ"), "পথপ্রদর্শন"),
        WordQuestion("সওম (الصوم)", listOf("রোজা/বিরত থাকা", "নামাজ", "হজ", "দান"), "রোজা/বিরত থাকা")
    )

    private val _gameScore = MutableStateFlow(0)
    val gameScore: StateFlow<Int> = _gameScore.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _lastAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val lastAnswerCorrect: StateFlow<Boolean?> = _lastAnswerCorrect.asStateFlow()

    fun submitAnswer(selectedAnswer: String) {
        val currentQ = questions[_currentQuestionIndex.value]
        val isCorrect = selectedAnswer == currentQ.correctAnswer
        _lastAnswerCorrect.value = isCorrect
        if (isCorrect) {
            _gameScore.value += 10
        }
    }

    fun nextQuestion() {
        _lastAnswerCorrect.value = null
        _currentQuestionIndex.value = (_currentQuestionIndex.value + 1) % questions.size
    }

    fun resetGame() {
        _gameScore.value = 0
        _currentQuestionIndex.value = 0
        _lastAnswerCorrect.value = null
    }

    // 4. Quran Planner State
    private val _plannerTarget = MutableStateFlow("১ পৃষ্ঠা")
    val plannerTarget: StateFlow<String> = _plannerTarget.asStateFlow()

    private val _plannerProgress = MutableStateFlow(setOf<String>())
    val plannerProgress: StateFlow<Set<String>> = _plannerProgress.asStateFlow()

    fun updatePlannerTarget(target: String) {
        _plannerTarget.value = target
        sharedPrefs.edit().putString("planner_target", target).apply()
    }

    fun togglePlannerDay(day: String) {
        val current = _plannerProgress.value.toMutableSet()
        if (current.contains(day)) {
            current.remove(day)
        } else {
            current.add(day)
        }
        _plannerProgress.value = current
        sharedPrefs.edit().putStringSet("planner_progress", current).apply()
    }

    // 5. Quran Hifz State
    private val _hifzProgress = MutableStateFlow<Map<String, String>>(emptyMap())
    val hifzProgress: StateFlow<Map<String, String>> = _hifzProgress.asStateFlow()

    fun updateHifzProgress(surahName: String, status: String) {
        val current = _hifzProgress.value.toMutableMap()
        current[surahName] = status
        _hifzProgress.value = current
        val serializedMap = current.entries.joinToString(";") { "${it.key}:${it.value}" }
        sharedPrefs.edit().putString("hifz_progress_map", serializedMap).apply()
    }

    init {
        _username.value = sharedPrefs.getString("username", "দ্বীনদার বান্দা") ?: "দ্বীনদার বান্দা"
        _readingTimeMinutes.value = sharedPrefs.getInt("reading_time", 60)
        _plannerTarget.value = sharedPrefs.getString("planner_target", "১ পৃষ্ঠা") ?: "১ পৃষ্ঠা"
        _plannerProgress.value = sharedPrefs.getStringSet("planner_progress", emptySet()) ?: emptySet()
        
        val hifzStr = sharedPrefs.getString("hifz_progress_map", "") ?: ""
        if (hifzStr.isNotEmpty()) {
            val hifzMap = hifzStr.split(";").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) parts[0] to parts[1] else null
            }.toMap()
            _hifzProgress.value = hifzMap
        }

        loadNotesFromPrefs()
    }
}
