package com.gymtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Workout : Screen("workout")
    object WorkoutDetail : Screen("workout/{sessionId}") {
        fun createRoute(sessionId: Long) = "workout/$sessionId"
    }
    object Measurements : Screen("measurements")
    object Nutrition : Screen("nutrition")
    object Progress : Screen("progress")
    object Programs : Screen("programs")
    object ProgramDetail : Screen("programs/{programId}") {
        fun createRoute(programId: Long) = "programs/$programId"
    }
    object Fatigue : Screen("fatigue")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Ana Sayfa", Icons.Default.Home),
    BottomNavItem(Screen.Workout, "Antrenman", Icons.Default.FitnessCenter),
    BottomNavItem(Screen.Nutrition, "Beslenme", Icons.Default.RestaurantMenu),
    BottomNavItem(Screen.Measurements, "Ölçümler", Icons.Default.Straighten),
    BottomNavItem(Screen.Progress, "Fotoğraflar", Icons.Default.PhotoLibrary),
    BottomNavItem(Screen.Programs, "Programlar", Icons.Default.List),
    BottomNavItem(Screen.Fatigue, "Kas Haritası", Icons.Default.Accessibility)
)
