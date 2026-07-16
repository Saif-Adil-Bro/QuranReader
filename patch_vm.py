import sys

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "r") as f:
    content = f.read()

enums = """
enum class GamePhase { SETUP, LOADING, PLAYING, RESULT }
enum class GameSource { ENTIRE_QURAN, SPECIFIC_SURAH }
enum class GameType { ARABIC_TO_BENGALI, BENGALI_TO_ARABIC }

data class WordGameConfig(
    val source: GameSource = GameSource.ENTIRE_QURAN,
    val selectedSurah: Int = 1,
    val type: GameType = GameType.ARABIC_TO_BENGALI,
    val totalQuestions: Int = 10
)
"""

if "enum class GamePhase" not in content:
    content = content.replace("data class WordQuestion(", enums + "\ndata class WordQuestion(")

old_game_state = """    // 3. Word Game State
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
    }"""

new_game_state = """    // 3. Word Game State
    private val _gamePhase = MutableStateFlow(GamePhase.SETUP)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _gameConfig = MutableStateFlow(WordGameConfig())
    val gameConfig: StateFlow<WordGameConfig> = _gameConfig.asStateFlow()

    private val _dynamicQuestions = MutableStateFlow<List<WordQuestion>>(emptyList())
    val dynamicQuestions: StateFlow<List<WordQuestion>> = _dynamicQuestions.asStateFlow()

    private val _gameScore = MutableStateFlow(0)
    val gameScore: StateFlow<Int> = _gameScore.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _lastAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val lastAnswerCorrect: StateFlow<Boolean?> = _lastAnswerCorrect.asStateFlow()

    fun updateGameConfig(config: WordGameConfig) {
        _gameConfig.value = config
    }
    
    fun setGamePhase(phase: GamePhase) {
        _gamePhase.value = phase
    }

    fun startDynamicGame() {
        _gamePhase.value = GamePhase.LOADING
        viewModelScope.launch {
            try {
                val config = _gameConfig.value
                val surahToFetch = if (config.source == GameSource.ENTIRE_QURAN) {
                    (1..114).random()
                } else {
                    config.selectedSurah
                }
                
                val ayahs = quranRepository.getSurahDetailsCombined(surahToFetch)
                val allWords = mutableListOf<com.example.data.model.QuranComWord>()
                for (ayah in ayahs) {
                    allWords.addAll(ayah.words.filter { it.translation?.text != null && it.textUthmani != null && it.translation.text.isNotBlank() && it.textUthmani.isNotBlank() })
                }
                
                // If not enough words, fallback to Surah Al-Baqarah
                val finalWords = if (allWords.size < config.totalQuestions) {
                    val fallbackAyahs = quranRepository.getSurahDetailsCombined(2)
                    allWords.clear()
                    for (ayah in fallbackAyahs) {
                        allWords.addAll(ayah.words.filter { it.translation?.text != null && it.textUthmani != null && it.translation.text.isNotBlank() && it.textUthmani.isNotBlank() })
                    }
                    allWords
                } else {
                    allWords
                }
                
                val selectedWords = finalWords.shuffled().take(config.totalQuestions)
                
                val generatedQuestions = selectedWords.map { word ->
                    val isArabicToBengali = config.type == GameType.ARABIC_TO_BENGALI
                    val questionText = if (isArabicToBengali) "${word.textUthmani}" else "${word.translation?.text}"
                    val correctAns = if (isArabicToBengali) "${word.translation?.text}" else "${word.textUthmani}"
                    
                    // Pick 3 random wrong answers
                    val wrongWords = finalWords.filter { it.id != word.id }.shuffled().take(3)
                    val wrongAns = wrongWords.map { if (isArabicToBengali) "${it.translation?.text}" else "${it.textUthmani}" }.toMutableList()
                    
                    // Ensure unique options
                    var options = (wrongAns + correctAns).distinct()
                    while(options.size < 4 && finalWords.size > 4) {
                       val extraWord = finalWords.random()
                       val extraOpt = if (isArabicToBengali) "${extraWord.translation?.text}" else "${extraWord.textUthmani}"
                       if (!options.contains(extraOpt)) {
                           options = options + extraOpt
                       }
                    }
                    
                    WordQuestion(questionText, options.shuffled(), correctAns)
                }
                
                _dynamicQuestions.value = generatedQuestions
                _gameScore.value = 0
                _currentQuestionIndex.value = 0
                _lastAnswerCorrect.value = null
                _gamePhase.value = GamePhase.PLAYING
                
            } catch (e: Exception) {
                e.printStackTrace()
                _gamePhase.value = GamePhase.SETUP // go back on error
            }
        }
    }

    fun submitAnswer(selectedAnswer: String) {
        val currentQ = _dynamicQuestions.value[_currentQuestionIndex.value]
        val isCorrect = selectedAnswer == currentQ.correctAnswer
        _lastAnswerCorrect.value = isCorrect
        if (isCorrect) {
            _gameScore.value += 1
        }
    }

    fun nextQuestion() {
        _lastAnswerCorrect.value = null
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < _dynamicQuestions.value.size) {
            _currentQuestionIndex.value = nextIdx
        } else {
            _gamePhase.value = GamePhase.RESULT
        }
    }

    fun resetGame() {
        _gamePhase.value = GamePhase.SETUP
        _gameScore.value = 0
        _currentQuestionIndex.value = 0
        _lastAnswerCorrect.value = null
        _dynamicQuestions.value = emptyList()
    }"""

content = content.replace(old_game_state, new_game_state)

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w") as f:
    f.write(content)
