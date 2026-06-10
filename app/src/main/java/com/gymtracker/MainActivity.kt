package com.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymtracker.ui.components.BottomNavigationBar
import com.gymtracker.ui.navigation.Screen
import com.gymtracker.ui.screens.fatigue.FatigueScreen
import com.gymtracker.ui.screens.home.HomeScreen
import com.gymtracker.ui.screens.measurements.MeasurementsScreen
import com.gymtracker.ui.screens.nutrition.NutritionScreen
import com.gymtracker.ui.screens.progress.ProgressScreen
import com.gymtracker.ui.screens.programs.ProgramsScreen
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
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
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
            WorkoutScreen()
        }
        composable(
            Screen.WorkoutDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            WorkoutScreen()
        }
        composable(Screen.Measurements.route) {
            MeasurementsScreen()
        }
        composable(Screen.Nutrition.route) {
            NutritionScreen()
        }
        composable(Screen.Progress.route) {
            ProgressScreen()
        }
        composable(Screen.Programs.route) {
            ProgramsScreen()
        }
        composable(Screen.Fatigue.route) {
            FatigueScreen()
        }
    }
}
