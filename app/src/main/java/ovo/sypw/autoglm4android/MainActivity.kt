package ovo.sypw.autoglm4android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ovo.sypw.autoglm4android.ui.history.HistoryScreen
import ovo.sypw.autoglm4android.ui.home.HomeScreen
import ovo.sypw.autoglm4android.ui.settings.SettingsScreen
import ovo.sypw.autoglm4android.ui.theme.AutoGLM4AndroidTheme

/**
 * 屏幕路由
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object History : Screen("history")
    data object HistoryDetail : Screen("history/{taskId}") {
        fun createRoute(taskId: String) = "history/$taskId"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AutoGLM4AndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings.route)
                                },
                                onNavigateToHistory = {
                                    navController.navigate(Screen.History.route)
                                }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.History.route) {
                            HistoryScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToDetail = { taskId ->
                                    navController.navigate(Screen.HistoryDetail.createRoute(taskId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
