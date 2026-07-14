package com.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymtracker.ui.components.BottomNavigationBar
import com.gymtracker.ui.navigation.NavigationPolicy
import com.gymtracker.ui.navigation.Screen
import com.gymtracker.ui.screens.fatigue.FatigueScreen
import com.gymtracker.ui.screens.home.HomeScreen
import com.gymtracker.ui.screens.measurements.MeasurementsScreen
import com.gymtracker.ui.screens.nutrition.NutritionScreen
import com.gymtracker.ui.screens.progress.ProgressScreen
import com.gymtracker.ui.screens.progress.ProgressHubScreen
import com.gymtracker.ui.screens.programs.ProgramDetailScreen
import com.gymtracker.ui.screens.programs.ProgramsScreen
import com.gymtracker.ui.screens.workout.WorkoutDetailScreen
import com.gymtracker.ui.screens.workout.WorkoutScreen
import com.gymtracker.ui.theme.GymTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymTrackerTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    Scaffold(
        bottomBar = {
            if (NavigationPolicy.shouldShowBottomBar(currentRoute)) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        AppNavHost(navController, Modifier.padding(innerPadding))
    }
}

@Composable
private fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable(Screen.Workout.route) {
            WorkoutScreen(
                onOpenSession = { sessionId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(sessionId))
                }
            )
        }
        composable(
            Screen.WorkoutDetail.route,
            arguments = listOf(
                navArgument(Screen.WorkoutDetail.ARG_SESSION_ID) { type = NavType.LongType }
            )
        ) {
            WorkoutDetailScreen(
                onBack = { navController.popBackStack() },
                onFinished = {
                    if (!navController.popBackStack(Screen.Workout.route, inclusive = false)) {
                        navController.navigate(Screen.Workout.route) { launchSingleTop = true }
                    }
                }
            )
        }
        composable(Screen.Nutrition.route) {
            NutritionScreen()
        }
        composable(Screen.Progress.route) {
            ProgressHubScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable(Screen.Programs.route) {
            ProgramsScreen(
                onProgramSelected = { programId ->
                    navController.navigate(Screen.ProgramDetail.createRoute(programId))
                }
            )
        }
        composable(
            Screen.ProgramDetail.route,
            arguments = listOf(
                navArgument(Screen.ProgramDetail.ARG_PROGRAM_ID) { type = NavType.LongType }
            )
        ) {
            ProgramDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Measurements.route) {
            MeasurementsScreen()
        }
        composable(Screen.ProgressPhotos.route) {
            ProgressScreen()
        }
        composable(Screen.Fatigue.route) {
            FatigueScreen()
        }
    }
}
