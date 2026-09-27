package com.example.pathbuilder.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pathbuilder.data.GameSettings
import com.example.pathbuilder.data.GameStats
import com.example.pathbuilder.data.SettingsRepository
import com.example.pathbuilder.ui.screens.AboutScreen
import com.example.pathbuilder.ui.screens.GameResult
import com.example.pathbuilder.ui.screens.GameScreen
import com.example.pathbuilder.ui.screens.GameViewModel
import com.example.pathbuilder.ui.screens.HomeScreen
import com.example.pathbuilder.ui.screens.HowToPlayScreen
import com.example.pathbuilder.ui.screens.ResultScreen
import com.example.pathbuilder.ui.screens.SettingsScreen
import com.example.pathbuilder.ui.screens.SplashScreen

private object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val HOW_TO_PLAY = "how_to_play"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val RESULT = "result/{success}/{moves}/{time}/{remaining}/{path}"

    fun result(r: GameResult) = "result/${r.success}/${r.moves}/${r.timeMillis}/${r.tilesRemaining}/${r.pathLength}"
}

@Composable
fun PathBuilderNavHost(
    settings: GameSettings,
    repository: SettingsRepository,
    navController: NavHostController = rememberNavController(),
) {
    val stats by repository.stats.collectAsStateWithLifecycle(GameStats())

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(240)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(240)) },
        popExitTransition = { fadeOut(tween(180)) },
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                stats = stats,
                onPlay = { navController.navigate(Routes.GAME) { launchSingleTop = true } },
                onHowToPlay = { navController.navigate(Routes.HOW_TO_PLAY) { launchSingleTop = true } },
                onSettings = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                onAbout = { navController.navigate(Routes.ABOUT) { launchSingleTop = true } },
            )
        }
        composable(Routes.GAME) {
            val vm: GameViewModel = viewModel(factory = GameViewModel.factory(repository))
            GameScreen(
                viewModel = vm,
                settings = settings,
                onExit = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onFinished = { result ->
                    navController.navigate(Routes.result(result)) {
                        popUpTo(Routes.GAME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            Routes.RESULT,
            arguments = listOf(
                navArgument("success") { type = NavType.BoolType },
                navArgument("moves") { type = NavType.IntType },
                navArgument("time") { type = NavType.LongType },
                navArgument("remaining") { type = NavType.IntType },
                navArgument("path") { type = NavType.IntType },
            ),
        ) { entry ->
            val args = requireNotNull(entry.arguments)
            val result = GameResult(
                success = args.getBoolean("success"),
                moves = args.getInt("moves"),
                timeMillis = args.getLong("time"),
                tilesRemaining = args.getInt("remaining"),
                pathLength = args.getInt("path"),
            )
            ResultScreen(
                result = result,
                onPlayAgain = { navController.navigate(Routes.GAME) { popUpTo(Routes.HOME) } },
                onHome = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
        composable(Routes.HOW_TO_PLAY) { HowToPlayScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.SETTINGS) {
            SettingsScreen(settings = settings, repository = repository, onBack = { navController.popBackStack() })
        }
        composable(Routes.ABOUT) { AboutScreen(onBack = { navController.popBackStack() }) }
    }
}
