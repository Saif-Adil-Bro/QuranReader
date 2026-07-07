package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.SearchScreen
import com.example.ui.viewmodels.SearchViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HafeziModeScreen
import com.example.ui.screens.QuranListScreen
import com.example.ui.screens.ReadingModeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SurahDetailScreen
import com.example.ui.viewmodels.AppViewModelFactory
import com.example.ui.viewmodels.HafeziModeViewModel
import com.example.ui.viewmodels.HomeViewModel
import com.example.ui.viewmodels.QuranListViewModel
import com.example.ui.viewmodels.ReadingModeViewModel
import com.example.ui.viewmodels.SettingsViewModel
import com.example.ui.viewmodels.SurahDetailViewModel
import kotlinx.coroutines.delay

import com.example.ui.screens.mushaf.MushafTabScreen
import com.example.ui.screens.mushaf.MushafViewerScreen
import com.example.ui.viewmodels.MushafSelectionViewModel
import com.example.ui.viewmodels.MushafViewerViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModelFactory: AppViewModelFactory,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "splash", modifier = modifier) {
        
        composable("splash") {
            SplashScreen {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }

        composable("mushaf") {
            val viewModel: MushafSelectionViewModel = viewModel(factory = viewModelFactory)
            MushafTabScreen(
                onMushafSelected = { mushaf ->
                    navController.navigate("mushaf/viewer/${mushaf.id}")
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "mushaf/viewer/{mushafId}?page={page}",
            arguments = listOf(
                navArgument("mushafId") { type = NavType.StringType },
                navArgument("page") { type = NavType.IntType; defaultValue = 1 }
            )
        ) { backStackEntry ->
            val mushafId = backStackEntry.arguments?.getString("mushafId") ?: "madani"
            val page = backStackEntry.arguments?.getInt("page") ?: 1
            val viewModel: MushafViewerViewModel = viewModel(factory = viewModelFactory)
            MushafViewerScreen(
                mushafId = mushafId,
                initialPage = page,
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable("home") {
            val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSurah = { surahNumber -> navController.navigate("detail/$surahNumber") },
                onNavigateToJuz = { juzNumber -> navController.navigate("juz/$juzNumber") },
                onNavigateToNormalMode = { navController.navigate("list/normal") },
                onNavigateToReadingMode = { navController.navigate("list/reading") },
                onNavigateToHafeziMode = { page -> navController.navigate("hafezi/$page") },
                onNavigateToSearch = { navController.navigate("search") },
                onSettingsClick = { navController.navigate("settings") },
                onNavigateToMushaf = { navController.navigate("mushaf") },
                onNavigateToMushafPage = { mushafId, page -> navController.navigate("mushaf/viewer/$mushafId?page=$page") },
                onNavigateToSurahWithAyah = { surahNumber, viewMode, initialAyah ->
                    navController.navigate("detail/$surahNumber?viewMode=$viewMode&initialAyah=$initialAyah")
                }
            )
        }

        composable(
            route = "list/{mode}",
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "normal"
            val viewModel: QuranListViewModel = viewModel(factory = viewModelFactory)
            QuranListScreen(
                viewModel = viewModel,
                onSurahClick = { surahNumber ->
                    if (mode == "normal") {
                        navController.navigate("detail/$surahNumber")
                    } else {
                        navController.navigate("reading/$surahNumber")
                    }
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable(
            route = "detail/{surahNumber}?viewMode={viewMode}&initialAyah={initialAyah}",
            arguments = listOf(
                navArgument("surahNumber") { type = NavType.IntType },
                navArgument("viewMode") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("initialAyah") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
            val viewMode = backStackEntry.arguments?.getString("viewMode")
            val initialAyah = backStackEntry.arguments?.getInt("initialAyah") ?: -1
            val viewModel: SurahDetailViewModel = viewModel(factory = viewModelFactory)
            SurahDetailScreen(
                surahNumber = surahNumber,
                isJuz = false,
                initialViewMode = viewMode,
                initialAyah = initialAyah,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "juz/{juzNumber}",
            arguments = listOf(navArgument("juzNumber") { type = NavType.IntType })
        ) { backStackEntry ->
            val juzNumber = backStackEntry.arguments?.getInt("juzNumber") ?: 1
            val viewModel: SurahDetailViewModel = viewModel(factory = viewModelFactory)
            SurahDetailScreen(
                surahNumber = juzNumber,
                isJuz = true,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "reading/{surahNumber}",
            arguments = listOf(navArgument("surahNumber") { type = NavType.IntType })
        ) { backStackEntry ->
            val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
            val viewModel: ReadingModeViewModel = viewModel(factory = viewModelFactory)
            ReadingModeScreen(
                surahNumber = surahNumber,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "hafezi/{page}",
            arguments = listOf(navArgument("page") { type = NavType.IntType })
        ) { backStackEntry ->
            val page = backStackEntry.arguments?.getInt("page") ?: 1
            val viewModel: HafeziModeViewModel = viewModel(factory = viewModelFactory)
            HafeziModeScreen(
                viewModel = viewModel,
                initialPage = page,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("search") {
            val viewModel: SearchViewModel = viewModel(factory = viewModelFactory)
            SearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSurah = { surahNumber ->
                    navController.navigate("detail/$surahNumber")
                }
            )
        }
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500) // 1.5 second delay
        onSplashComplete()
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Quran Reader",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
