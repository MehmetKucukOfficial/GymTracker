package com.gymtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.gymtracker.R

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Workout : Screen("workouts")
    object WorkoutDetail : Screen("workouts/{sessionId}") {
        const val ARG_SESSION_ID = "sessionId"

        fun createRoute(sessionId: Long): String {
            require(sessionId > 0) { "sessionId must be positive" }
            return "workouts/$sessionId"
        }
    }
    object Programs : Screen("programs")
    object ProgramDetail : Screen("programs/{programId}") {
        const val ARG_PROGRAM_ID = "programId"

        fun createRoute(programId: Long): String {
            require(programId > 0) { "programId must be positive" }
            return "programs/$programId"
        }
    }
    object Nutrition : Screen("nutrition")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
    object Measurements : Screen("progress/measurements")
    object ProgressPhotos : Screen("progress/photos")
    object Fatigue : Screen("progress/load")
}

data class BottomNavItem(
    val screen: Screen,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, R.string.nav_home, Icons.Default.Home),
    BottomNavItem(Screen.Workout, R.string.nav_workout, Icons.Default.FitnessCenter),
    BottomNavItem(Screen.Programs, R.string.nav_programs, Icons.AutoMirrored.Filled.List),
    BottomNavItem(Screen.Nutrition, R.string.nav_nutrition, Icons.Default.RestaurantMenu),
    BottomNavItem(Screen.Progress, R.string.nav_progress, Icons.AutoMirrored.Filled.ShowChart)
)

object NavigationPolicy {
    fun topLevelFor(route: String?): Screen? {
        val normalized = route?.substringBefore('?') ?: return null
        return when {
            normalized == Screen.Home.route -> Screen.Home
            normalized == Screen.Workout.route || normalized == Screen.WorkoutDetail.route ||
                normalized.startsWith("${Screen.Workout.route}/") -> Screen.Workout
            normalized == Screen.Programs.route || normalized == Screen.ProgramDetail.route ||
                normalized.startsWith("${Screen.Programs.route}/") -> Screen.Programs
            normalized == Screen.Nutrition.route -> Screen.Nutrition
            normalized == Screen.Progress.route || normalized.startsWith("${Screen.Progress.route}/") ->
                Screen.Progress
            else -> null
        }
    }

    fun shouldShowBottomBar(route: String?): Boolean {
        val normalized = route?.substringBefore('?') ?: return true
        return normalized != Screen.Settings.route &&
            normalized != Screen.WorkoutDetail.route &&
            normalized != Screen.ProgramDetail.route &&
            !normalized.startsWith("${Screen.Workout.route}/") &&
            !normalized.startsWith("${Screen.Programs.route}/")
    }
}
