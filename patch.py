with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

target = """        composable("search") {
            val viewModel: SearchViewModel = viewModel(factory = viewModelFactory)
            SearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSurah = { surahNumber ->
                    navController.navigate("detail/$surahNumber")
                }
            )
        }"""

replacement = """        composable("search") {
            val viewModel: SearchViewModel = viewModel(factory = viewModelFactory)
            SearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSurah = { surahNumber ->
                    navController.navigate("detail/$surahNumber")
                },
                onNavigateToAyah = { surahNumber, ayahNumber ->
                    navController.navigate("detail/$surahNumber?viewMode=LIST&initialAyah=$ayahNumber")
                }
            )
        }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
