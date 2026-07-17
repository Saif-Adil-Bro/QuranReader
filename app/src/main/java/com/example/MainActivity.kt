package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.compose.runtime.LaunchedEffect
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

import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import com.example.ui.components.BottomNavBar

import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodels.HomeViewModel
import com.example.ui.screens.FloatingPlayerShortcut
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val appContainer = remember { (application as QuranApplication).container }
      val themeState by appContainer.settingsRepository.themeFlow.collectAsState(initial = "Light")
      val keepScreenOn by appContainer.settingsRepository.keepScreenOnFlow.collectAsState(initial = false)
      
      LaunchedEffect(keepScreenOn) {
          if (keepScreenOn) {
              window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
          } else {
              window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
          }
      }
      val isSystemDark = isSystemInDarkTheme()
      val darkTheme = when (themeState) {
          "Dark" -> true
          "Light" -> false
          else -> isSystemDark
      }
      MyApplicationTheme(darkTheme = darkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val viewModelFactory = AppViewModelFactory(
            quranRepository = appContainer.quranRepository,
            settingsRepository = appContainer.settingsRepository,
            audioRepository = appContainer.audioRepository,
            aiRepository = appContainer.aiRepository,
            bookmarkDao = appContainer.bookmarkDao,
            memorizedPageDao = appContainer.memorizedPageDao,
            mushafRepository = appContainer.mushafRepository
          )
          
          val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
          val navController = rememberNavController()
          val navBackStackEntry by navController.currentBackStackEntryAsState()
          val currentRoute = navBackStackEntry?.destination?.route
          val visibleEntries by navController.visibleEntries.collectAsState()
          val isSplashVisible = visibleEntries.any { it.destination.route == "splash" }
          
          Scaffold(
            bottomBar = {
              BottomNavBar(
                navController = navController,
                currentRoute = currentRoute,
                isSplashVisible = isSplashVisible
              )
            }
          ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
              AppNavGraph(
                navController = navController,
                viewModelFactory = viewModelFactory,
                homeViewModel = homeViewModel,
                modifier = Modifier.fillMaxSize()
              )
              
              if (currentRoute != "splash" && currentRoute != "recitation/player") {
                FloatingPlayerShortcut(
                  viewModel = homeViewModel,
                  onClick = { navController.navigate("recitation/player") },
                  modifier = Modifier.align(Alignment.BottomEnd)
                )
              }
            }
          }
        }
      }
    }
  }
}
