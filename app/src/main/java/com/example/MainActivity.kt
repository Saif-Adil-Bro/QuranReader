package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.navigation.AppNavGraph
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.AppViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val appContainer = (application as QuranApplication).container
          val viewModelFactory = AppViewModelFactory(
            quranRepository = appContainer.quranRepository,
            settingsRepository = appContainer.settingsRepository,
            audioRepository = appContainer.audioRepository,
            aiRepository = appContainer.aiRepository,
            bookmarkDao = appContainer.bookmarkDao,
            memorizedPageDao = appContainer.memorizedPageDao
          )
          val navController = rememberNavController()

          AppNavGraph(
            navController = navController,
            viewModelFactory = viewModelFactory
          )
        }
      }
    }
  }
}
