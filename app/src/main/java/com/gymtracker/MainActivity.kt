package com.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.gymtracker.ui.components.BottomNavigationBar
import com.gymtracker.ui.components.GtNavigationRail
import com.gymtracker.ui.components.navigationTypeForWidth
import com.gymtracker.ui.components.GtNavigationType
import com.gymtracker.ui.navigation.NavigationPolicy
import com.gymtracker.ui.navigation.DeepLinkContract
import com.gymtracker.ui.navigation.PositiveLongNavType
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
fun MainNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        when (navigationTypeForWidth(maxWidth)) {
            GtNavigationType.BOTTOM_BAR -> Scaffold(
                bottomBar = {
                    if (NavigationPolicy.shouldShowBottomBar(currentRoute)) {
                        BottomNavigationBar(navController)
                    }
                }
            ) { innerPadding ->
                AppNavHost(navController, Modifier.padding(innerPadding))
            }
            GtNavigationType.RAIL -> Row(Modifier.fillMaxSize()) {
                if (NavigationPolicy.shouldShowBottomBar(currentRoute)) {
                    GtNavigationRail(navController)
                }
                AppNavHost(navController, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Home.route,
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinkContract.patternFor(Screen.Home) })
        ) {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable(
            route = Screen.Workout.route,
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinkContract.patternFor(Screen.Workout) })
        ) {
            WorkoutScreen(
                onOpenSession = { sessionId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(sessionId))
                }
            )
        }
        composable(
            Screen.WorkoutDetail.route,
            arguments = listOf(
                navArgument(Screen.WorkoutDetail.ARG_SESSION_ID) { type = PositiveLongNavType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = DeepLinkContract.patternFor(Screen.WorkoutDetail) }
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
        composable(
            route = Screen.Nutrition.route,
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinkContract.patternFor(Screen.Nutrition) })
        ) {
            NutritionScreen()
        }
        composable(
            route = Screen.Progress.route,
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinkContract.patternFor(Screen.Progress) })
        ) {
            ProgressHubScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable(
            route = Screen.Programs.route,
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinkContract.patternFor(Screen.Programs) })
        ) {
            ProgramsScreen(
                onProgramSelected = { programId ->
                    navController.navigate(Screen.ProgramDetail.createRoute(programId))
                }
            )
        }
        composable(
            Screen.ProgramDetail.route,
            arguments = listOf(
                navArgument(Screen.ProgramDetail.ARG_PROGRAM_ID) { type = PositiveLongNavType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = DeepLinkContract.patternFor(Screen.ProgramDetail) }
            )
        ) {
            ProgramDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenSession = { sessionId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(sessionId))
                }
            )
        }
        composable(
            route = Screen.Measurements.route,
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinkContract.patternFor(Screen.Measurements) })
        ) {
            MeasurementsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.ProgressPhotos.route,
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinkContract.patternFor(Screen.ProgressPhotos) })
        ) {
            ProgressScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.Fatigue.route,
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinkContract.patternFor(Screen.Fatigue) })
        ) {
            FatigueScreen(onBack = { navController.popBackStack() })
        }
    }
}
