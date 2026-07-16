import sys

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# We need to add GamePhase, GameSource, GameType, WordGameConfig imports if they are not there, but since they are in SettingsViewModel, we can import them from there
# Actually we defined them in the SettingsViewModel.kt file (outside the class or inside the package). So we just need to import them.
# Let's import them if needed.

imports = """import com.example.ui.viewmodels.GamePhase
import com.example.ui.viewmodels.GameSource
import com.example.ui.viewmodels.GameType
import com.example.ui.viewmodels.WordGameConfig
"""
if "import com.example.ui.viewmodels.GamePhase" not in content:
    content = content.replace("import com.example.ui.viewmodels.SettingsViewModel", imports + "import com.example.ui.viewmodels.SettingsViewModel")


old_game_ui = """@Composable
fun GameDialogContent(viewModel: SettingsViewModel) {
    val score by viewModel.gameScore.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val lastCorrect by viewModel.lastAnswerCorrect.collectAsState()
    
    val question = viewModel.questions[currentIndex]
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("স্কোর: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryGreen)
            Text("প্রশ্ন: ${currentIndex + 1}/${viewModel.questions.size}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Question Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("নিচের শব্দটির সঠিক অর্থ নির্বাচন করুন:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(question.question, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Options List
        question.options.forEach { opt ->
            val isCorrectOpt = opt == question.correctAnswer
            val borderCol = when {
                lastCorrect != null && isCorrectOpt -> Color(0xFF10B981)
                lastCorrect == false && !isCorrectOpt -> Border
                else -> Border
            }
            val bgCol = when {
                lastCorrect != null && isCorrectOpt -> Color(0xFFD1FAF5)
                else -> Color.White
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable(enabled = lastCorrect == null) { viewModel.submitAnswer(opt) },
                colors = CardDefaults.cardColors(containerColor = bgCol),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(opt, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (lastCorrect != null && isCorrectOpt) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen)
                    }
                }
            }
        }
        
        if (lastCorrect != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.nextQuestion() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("পরবর্তী প্রশ্ন", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}"""

new_game_ui = """@Composable
fun GameDialogContent(viewModel: SettingsViewModel) {
    val phase by viewModel.gamePhase.collectAsState()
    
    when (phase) {
        GamePhase.SETUP -> GameSetupScreen(viewModel)
        GamePhase.LOADING -> GameLoadingScreen()
        GamePhase.PLAYING -> GamePlayingScreen(viewModel)
        GamePhase.RESULT -> GameResultScreen(viewModel)
    }
}

@Composable
fun GameSetupScreen(viewModel: SettingsViewModel) {
    val config by viewModel.gameConfig.collectAsState()
    val surahs = com.example.data.surahInfoList
    var isSurahDropdownExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Text("১. কিসের উপর গেম খেলতে চান?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f).clickable { viewModel.updateGameConfig(config.copy(source = GameSource.ENTIRE_QURAN)) },
                colors = CardDefaults.cardColors(containerColor = if (config.source == GameSource.ENTIRE_QURAN) Color(0xFFFDE6B0) else Color.White),
                border = BorderStroke(1.dp, if (config.source == GameSource.ENTIRE_QURAN) PrimaryGreen else Border)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = if (config.source == GameSource.ENTIRE_QURAN) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("সম্পূর্ণ কুরআন", fontSize = 13.sp, fontWeight = if (config.source == GameSource.ENTIRE_QURAN) FontWeight.Bold else FontWeight.Medium, color = if (config.source == GameSource.ENTIRE_QURAN) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
                }
            }
            Card(
                modifier = Modifier.weight(1f).clickable { viewModel.updateGameConfig(config.copy(source = GameSource.SPECIFIC_SURAH)) },
                colors = CardDefaults.cardColors(containerColor = if (config.source == GameSource.SPECIFIC_SURAH) Color(0xFFFDE6B0) else Color.White),
                border = BorderStroke(1.dp, if (config.source == GameSource.SPECIFIC_SURAH) PrimaryGreen else Border)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = if (config.source == GameSource.SPECIFIC_SURAH) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("নির্দিষ্ট সূরা", fontSize = 13.sp, fontWeight = if (config.source == GameSource.SPECIFIC_SURAH) FontWeight.Bold else FontWeight.Medium, color = if (config.source == GameSource.SPECIFIC_SURAH) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        if (config.source == GameSource.SPECIFIC_SURAH) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().clickable { isSurahDropdownExpanded = true },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Border)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val selectedName = surahs.find { it.first == config.selectedSurah }?.second?.arabicName ?: "সূরা নির্বাচন করুন"
                    Text("নির্বাচিত সূরা: $selectedName", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            androidx.compose.material3.DropdownMenu(
                expanded = isSurahDropdownExpanded,
                onDismissRequest = { isSurahDropdownExpanded = false }
            ) {
                surahs.forEach { surahInfo ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("${surahInfo.first}. ${surahInfo.second.arabicName}") },
                        onClick = { 
                            viewModel.updateGameConfig(config.copy(selectedSurah = surahInfo.first))
                            isSurahDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("২. গেমের ধরণ নির্ধারণ করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f).clickable { viewModel.updateGameConfig(config.copy(type = GameType.ARABIC_TO_BENGALI)) },
                colors = CardDefaults.cardColors(containerColor = if (config.type == GameType.ARABIC_TO_BENGALI) Color(0xFFD1FAF5) else Color.White),
                border = BorderStroke(1.dp, if (config.type == GameType.ARABIC_TO_BENGALI) PrimaryGreen else Border)
            ) {
                Text("আরবি -> বাংলা", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = if (config.type == GameType.ARABIC_TO_BENGALI) FontWeight.Bold else FontWeight.Medium, color = if (config.type == GameType.ARABIC_TO_BENGALI) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
            }
            Card(
                modifier = Modifier.weight(1f).clickable { viewModel.updateGameConfig(config.copy(type = GameType.BENGALI_TO_ARABIC)) },
                colors = CardDefaults.cardColors(containerColor = if (config.type == GameType.BENGALI_TO_ARABIC) Color(0xFFD1FAF5) else Color.White),
                border = BorderStroke(1.dp, if (config.type == GameType.BENGALI_TO_ARABIC) PrimaryGreen else Border)
            ) {
                Text("বাংলা -> আরবি", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = if (config.type == GameType.BENGALI_TO_ARABIC) FontWeight.Bold else FontWeight.Medium, color = if (config.type == GameType.BENGALI_TO_ARABIC) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("৩. মোট কতটি প্রশ্ন?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 20, 30).forEach { count ->
                Card(
                    modifier = Modifier.weight(1f).clickable { viewModel.updateGameConfig(config.copy(totalQuestions = count)) },
                    colors = CardDefaults.cardColors(containerColor = if (config.totalQuestions == count) Color(0xFFFDE6B0) else Color.White),
                    border = BorderStroke(1.dp, if (config.totalQuestions == count) PrimaryGreen else Border)
                ) {
                    Text("$count টি", modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = if (config.totalQuestions == count) FontWeight.Bold else FontWeight.Medium, color = if (config.totalQuestions == count) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.startDynamicGame() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)) // Orange like in image
        ) {
            Text("গেম শুরু করুন", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun GameLoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PrimaryGreen)
            Spacer(modifier = Modifier.height(16.dp))
            Text("কুরআন থেকে শব্দ সংগ্রহ করা হচ্ছে...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GamePlayingScreen(viewModel: SettingsViewModel) {
    val score by viewModel.gameScore.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val lastCorrect by viewModel.lastAnswerCorrect.collectAsState()
    val questions by viewModel.dynamicQuestions.collectAsState()
    
    if (questions.isEmpty()) return
    
    val question = questions[currentIndex]
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("স্কোর: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryGreen)
            Text("প্রশ্ন: ${currentIndex + 1}/${questions.size}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Question Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("নিচের শব্দটির সঠিক অর্থ নির্বাচন করুন:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(question.question, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Options List
        question.options.forEach { opt ->
            val isCorrectOpt = opt == question.correctAnswer
            val isSelected = lastCorrect != null // once answered, show correct/wrong
            
            val borderCol = when {
                isSelected && isCorrectOpt -> Color(0xFF10B981)
                lastCorrect == false && !isCorrectOpt -> Border
                else -> Border
            }
            val bgCol = when {
                isSelected && isCorrectOpt -> Color(0xFFD1FAF5)
                else -> Color.White
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable(enabled = lastCorrect == null) { viewModel.submitAnswer(opt) },
                colors = CardDefaults.cardColors(containerColor = bgCol),
                border = BorderStroke(1.dp, borderCol)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(opt, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (isSelected && isCorrectOpt) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen)
                    }
                }
            }
        }
        
        if (lastCorrect != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.nextQuestion() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(if (currentIndex == questions.size - 1) "ফলাফল দেখুন" else "পরবর্তী প্রশ্ন", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun GameResultScreen(viewModel: SettingsViewModel) {
    val score by viewModel.gameScore.collectAsState()
    val total = viewModel.dynamicQuestions.value.size
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val comment = when {
        score == total -> "মাশাআল্লাহ! অসাধারণ!"
        score >= total * 0.8 -> "আলহামদুলিল্লাহ! খুব ভালো!"
        score >= total * 0.5 -> "ভালো চেষ্টা, আরো চর্চা করুন!"
        else -> "ইনশাআল্লাহ! পরবর্তীতে আরো ভালো হবে।"
    }
    
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("কুরআন শব্দ গেইম", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PrimaryGreen)
                Text("quranbn.com", fontSize = 12.sp, color = PrimaryGreen.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha=0.2f))
                ) {
                   Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                       Text("আপনার স্কোর", fontSize = 16.sp, color = PrimaryGreen)
                       Spacer(modifier = Modifier.height(8.dp))
                       Row(verticalAlignment = Alignment.Bottom) {
                           Text("$score", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                           Text("/$total", fontSize = 24.sp, color = PrimaryGreen, modifier = Modifier.padding(bottom = 6.dp))
                       }
                       Spacer(modifier = Modifier.height(16.dp))
                       Text(comment, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryGreen)
                   }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* Share link */ },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                ) {
                    Text("আপনিও খেলুন: quranbn.com/game", fontSize = 12.sp, color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { 
                android.widget.Toast.makeText(context, "ফলাফল কার্ড ডাউনলোড শুরু হয়েছে", android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ফলাফল কার্ড ডাউনলোড করুন", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.resetGame() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
            border = BorderStroke(1.dp, PrimaryGreen)
        ) {
            Text("পুনরায় খেলুন", fontWeight = FontWeight.Bold)
        }
    }
}"""

content = content.replace(old_game_ui, new_game_ui)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
