package com.gymtracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gymtracker.ui.navigation.NavigationPolicy
import com.gymtracker.ui.navigation.bottomNavItems

enum class GtNavigationType { BOTTOM_BAR, RAIL }

fun navigationTypeForWidth(width: Dp): GtNavigationType =
    if (width >= 600.dp) GtNavigationType.RAIL else GtNavigationType.BOTTOM_BAR

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedTopLevel = NavigationPolicy.topLevelFor(currentRoute)
    val showAllLabels = LocalDensity.current.fontScale < 1.6f

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        bottomNavItems.forEach { item ->
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selectedTopLevel == item.screen) item.selectedIcon else item.icon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = showAllLabels,
                selected = selectedTopLevel == item.screen,
                onClick = { navController.navigateTopLevel(item.screen.route, currentRoute) },
                modifier = Modifier.testTag("bottom-nav-${item.screen.route}"),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun GtNavigationRail(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedTopLevel = NavigationPolicy.topLevelFor(currentRoute)

    NavigationRail(
        modifier = Modifier.testTag("adaptive-navigation-rail"),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        bottomNavItems.forEach { item ->
            val label = stringResource(item.labelRes)
            NavigationRailItem(
                selected = selectedTopLevel == item.screen,
                onClick = { navController.navigateTopLevel(item.screen.route, currentRoute) },
                icon = {
                    Icon(
                        imageVector = if (selectedTopLevel == item.screen) item.selectedIcon else item.icon,
                        contentDescription = null
                    )
                },
                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.testTag("rail-nav-${item.screen.route}"),
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

private fun NavController.navigateTopLevel(route: String, currentRoute: String?) {
    val selectedTopLevel = NavigationPolicy.topLevelFor(currentRoute)
    val target = bottomNavItems.first { it.screen.route == route }.screen
    if (selectedTopLevel != target || currentRoute != route) {
        navigate(route) {
            popUpTo(graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}
