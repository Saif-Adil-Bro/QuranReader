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
import androidx.compose.ui.unit.dp
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
import com.example.ui.screens.TajweedIndexScreen
import com.example.ui.screens.TajweedModeScreen
import com.example.ui.screens.RecitationPlayerScreen
import com.example.ui.screens.RecitationIndexScreen
import com.example.ui.viewmodels.AppViewModelFactory
import com.example.ui.viewmodels.HafeziModeViewModel
import com.example.ui.viewmodels.TajweedModeViewModel
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
import com.example.ui.viewmodels.SplashViewModel
import com.example.ui.viewmodels.SplashLoadingState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModelFactory: AppViewModelFactory,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "splash", modifier = modifier) {
        
        composable("splash") {
            val splashViewModel: SplashViewModel = viewModel(factory = viewModelFactory)
            SplashScreen(
                viewModel = splashViewModel,
                onSplashComplete = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("mushaf") {
            val viewModel: MushafSelectionViewModel = viewModel(factory = viewModelFactory)
            MushafTabScreen(
                onMushafSelected = { mushaf ->
                    navController.navigate("mushaf/viewer/${mushaf.id}")
                },
                onLastReadSelected = { mushafId, page ->
                    navController.navigate("mushaf/viewer/$mushafId?page=$page")
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "mushaf/viewer/{mushafId}?page={page}&showIndex={showIndex}",
            arguments = listOf(
                navArgument("mushafId") { type = NavType.StringType },
                navArgument("page") { type = NavType.IntType; defaultValue = 1 },
                navArgument("showIndex") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val mushafId = backStackEntry.arguments?.getString("mushafId") ?: "madani"
            val page = backStackEntry.arguments?.getInt("page") ?: 1
            val showIndex = backStackEntry.arguments?.getBoolean("showIndex") ?: false
            val viewModel: MushafViewerViewModel = viewModel(factory = viewModelFactory)
            MushafViewerScreen(
                mushafId = mushafId,
                initialPage = page,
                initialShowIndex = showIndex,
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
                onNavigateToReadingMode = { surahNumber -> navController.navigate("reading/$surahNumber") },
                onNavigateToHafeziMode = { page -> navController.navigate("hafezi/$page") },
                onNavigateToSearch = { navController.navigate("search") },
                onSettingsClick = { navController.navigate("settings") },
                onNavigateToMushaf = { navController.navigate("mushaf") },
                onNavigateToMushafPage = { mushafId, page, showIndex ->
                    navController.navigate("mushaf/viewer/$mushafId?page=$page&showIndex=$showIndex")
                },
                onNavigateToSurahWithAyah = { surahNumber, viewMode, initialAyah ->
                    navController.navigate("detail/$surahNumber?viewMode=$viewMode&initialAyah=$initialAyah")
                },
                onNavigateToTajweedIndex = {
                    navController.navigate("tajweed/index")
                },
                onNavigateToTajweedMode = { page ->
                    navController.navigate("tajweed/$page")
                },
                onNavigateToPlayer = {
                    if (viewModel.currentPlayingSurah.value != null) {
                        navController.navigate("recitation/player")
                    } else {
                        navController.navigate("recitation/index")
                    }
                }
            )
        }

        composable("recitation/index") { backStackEntry ->
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry("home")
            }
            val homeViewModel: HomeViewModel = viewModel(homeBackStackEntry, factory = viewModelFactory)
            RecitationIndexScreen(
                viewModel = homeViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToPlayer = { navController.navigate("recitation/player") }
            )
        }

        composable("recitation/player") { backStackEntry ->
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry("home")
            }
            val homeViewModel: HomeViewModel = viewModel(homeBackStackEntry, factory = viewModelFactory)
            RecitationPlayerScreen(
                viewModel = homeViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("tajweed/index") {
            TajweedIndexScreen(
                onBackClick = { navController.popBackStack() },
                onSurahClick = { surahId ->
                    val startPage = com.example.data.QuranData.surahStartPages[surahId - 1]
                    navController.navigate("tajweed/$startPage")
                },
                onJuzClick = { juzId ->
                    val startPage = if (juzId == 1) 1 else (juzId - 1) * 20 + 2
                    navController.navigate("tajweed/$startPage")
                },
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        composable(
            route = "tajweed/{page}",
            arguments = listOf(navArgument("page") { type = NavType.IntType })
        ) { backStackEntry ->
            val page = backStackEntry.arguments?.getInt("page") ?: 1
            val viewModel: TajweedModeViewModel = viewModel(factory = viewModelFactory)
            TajweedModeScreen(
                viewModel = viewModel,
                initialPage = page,
                onNavigateBack = { navController.popBackStack() }
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
                mode = mode,
                onSurahClick = { surahNumber ->
                    if (mode == "normal") {
                        navController.navigate("detail/$surahNumber")
                    } else {
                        navController.navigate("reading/$surahNumber")
                    }
                },
                onJuzClick = { juzNumber ->
                    if (mode == "normal") {
                        navController.navigate("juz/$juzNumber")
                    } else {
                        val startSurah = when (juzNumber) {
                            1 -> 1
                            2 -> 2
                            3 -> 2
                            4 -> 3
                            5 -> 4
                            6 -> 4
                            7 -> 5
                            8 -> 6
                            9 -> 7
                            10 -> 8
                            11 -> 9
                            12 -> 11
                            13 -> 12
                            14 -> 15
                            15 -> 17
                            16 -> 18
                            17 -> 21
                            18 -> 23
                            19 -> 25
                            20 -> 27
                            21 -> 29
                            22 -> 33
                            23 -> 36
                            24 -> 39
                            25 -> 41
                            26 -> 46
                            27 -> 51
                            28 -> 58
                            29 -> 67
                            30 -> 78
                            else -> 1
                        }
                        navController.navigate("reading/$startSurah")
                    }
                },
                onSettingsClick = {
                    navController.navigate("settings")
                },
                onBackClick = {
                    navController.popBackStack()
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
                onNavigateBack = { 
                    if (!navController.popBackStack("list/reading", false)) {
                        navController.navigate("list/reading") {
                            popUpTo("home") { saveState = true }
                        }
                    }
                }
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSurah = { surahNumber ->
                    navController.navigate("detail/$surahNumber")
                },
                onNavigateToPage = { page ->
                    navController.navigate("hafezi/$page")
                },
                onNavigateToJuz = { juzNumber ->
                    navController.navigate("juz/$juzNumber")
                },
                onNavigateToAyah = { surahNumber, ayahNumber ->
                    navController.navigate("detail/$surahNumber?viewMode=LIST&initialAyah=$ayahNumber")
                }
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
fun SplashScreen(
    viewModel: SplashViewModel,
    onSplashComplete: () -> Unit
) {
    val loadingState by viewModel.loadingState.collectAsState()
    
    LaunchedEffect(loadingState) {
        if (loadingState is SplashLoadingState.Complete) {
            onSplashComplete()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051210)), // Deep elegant dark background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant pulsing logo or icon
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .size(96.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Quran Reader",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "কুরআন রিডার",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Dynamic loading animation (Phase 2: Loading State)
            AnimatedVisibility(
                visible = loadingState is SplashLoadingState.Loading,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF10B981),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "কুরআন ডাটা লোড করা হচ্ছে...",
                        fontSize = 14.sp,
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}
