package com.gymtracker.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationContractTest {

    @Test
    fun bottomNavigation_containsFiveProductTargetsInDecisionOrder() {
        assertEquals(
            listOf(
                Screen.Home,
                Screen.Workout,
                Screen.Programs,
                Screen.Nutrition,
                Screen.Progress
            ),
            bottomNavItems.map { it.screen }
        )
    }

    @Test
    fun nestedRoutes_resolveToTheirOwningTopLevelTarget() {
        assertEquals(Screen.Workout, NavigationPolicy.topLevelFor(Screen.WorkoutDetail.route))
        assertEquals(Screen.Workout, NavigationPolicy.topLevelFor("workouts/42"))
        assertEquals(Screen.Programs, NavigationPolicy.topLevelFor("programs/5"))
        assertEquals(Screen.Progress, NavigationPolicy.topLevelFor(Screen.Measurements.route))
        assertEquals(Screen.Progress, NavigationPolicy.topLevelFor(Screen.ProgressPhotos.route))
        assertEquals(Screen.Progress, NavigationPolicy.topLevelFor(Screen.Fatigue.route))
    }

    @Test
    fun detailRoutes_hideBottomBarWhileProgressChildrenKeepIt() {
        assertFalse(NavigationPolicy.shouldShowBottomBar(Screen.WorkoutDetail.route))
        assertFalse(NavigationPolicy.shouldShowBottomBar("workouts/42"))
        assertFalse(NavigationPolicy.shouldShowBottomBar(Screen.ProgramDetail.route))
        assertTrue(NavigationPolicy.shouldShowBottomBar(Screen.ProgressPhotos.route))
    }

    @Test
    fun detailRoutes_requirePositiveDatabaseIds() {
        assertEquals("workouts/42", Screen.WorkoutDetail.createRoute(42))
        assertEquals("programs/5", Screen.ProgramDetail.createRoute(5))
        assertThrows(IllegalArgumentException::class.java) {
            Screen.WorkoutDetail.createRoute(0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Screen.ProgramDetail.createRoute(-1)
        }
    }
}
