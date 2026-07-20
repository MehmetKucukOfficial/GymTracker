package com.gymtracker.ui.navigation

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymtracker.MainActivity
import com.gymtracker.R
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun shell_showsExactlyTheFivePrimaryTargets() {
        listOf("home", "workouts", "programs", "nutrition", "progress").forEach { route ->
            composeRule.onNodeWithTag("bottom-nav-$route").assertIsDisplayed()
        }
        composeRule.onNodeWithTag("bottom-nav-progress/measurements").assertDoesNotExist()
    }

    @Test
    fun expandedWidth_usesNavigationRailAndKeepsTopLevelSelection() {
        composeRule.activityRule.scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("adaptive-navigation-rail")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("adaptive-navigation-rail").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-nav-home").assertDoesNotExist()
        composeRule.onNodeWithTag("rail-nav-programs").performClick().assertIsSelected()

        composeRule.activityRule.scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    @Test
    fun progressChild_keepsProgressSelectedAndTopAppBarBackReturnsToHub() {
        composeRule.onNodeWithTag("bottom-nav-progress").performClick()
        composeRule.onNodeWithText(text(R.string.progress_hub_subtitle))
            .assertIsDisplayed()

        composeRule.onNodeWithText(text(R.string.measurements_title)).performClick()
        composeRule.onNodeWithTag("bottom-nav-progress").assertIsSelected()
        composeRule.onNodeWithTag("gt-top-app-bar").assertIsDisplayed()

        composeRule.onNodeWithTag("gt-top-app-bar-back").performClick()
        composeRule.onNodeWithText(text(R.string.progress_hub_subtitle))
            .assertIsDisplayed()
    }

    @Test
    fun progressChild_systemBackStillReturnsToHub() {
        composeRule.onNodeWithTag("bottom-nav-progress").performClick()
        composeRule.onNodeWithTag("progress-destination-progress/photos").performClick()
        composeRule.onNodeWithTag("gt-top-app-bar-back").assertIsDisplayed()

        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithText(text(R.string.progress_hub_subtitle)).assertIsDisplayed()
    }

    @Test
    fun selectedTopLevelDestination_survivesActivityRecreation() {
        composeRule.onNodeWithTag("bottom-nav-programs").performClick()
        composeRule.onNodeWithTag("bottom-nav-programs").assertIsSelected()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithTag("bottom-nav-programs").assertIsSelected()
        composeRule.onNodeWithText(text(R.string.programs_subtitle))
            .assertIsDisplayed()
    }

    @Test
    fun workoutDetail_usesIdRouteAndSurvivesActivityRecreation() {
        composeRule.onNodeWithTag("bottom-nav-workouts").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithContentDescription(text(R.string.workout_new_content_description))
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("workout-active-card").fetchSemanticsNodes().isNotEmpty()
        }
        val canCreateWorkout =
            composeRule.onAllNodesWithContentDescription(text(R.string.workout_new_content_description))
                .fetchSemanticsNodes().isNotEmpty()
        if (canCreateWorkout) {
            composeRule.onNodeWithContentDescription(text(R.string.workout_new_content_description)).performClick()
            composeRule.onNodeWithText(text(R.string.workout_name_label)).performTextInput("Route Test Workout")
            composeRule.onNodeWithText(text(R.string.workout_start)).performClick()
        } else {
            composeRule.onNodeWithTag("workout-active-card").performClick()
        }

        composeRule.onNodeWithTag("workout-detail-active").assertIsDisplayed()
        composeRule.onNodeWithTag("gt-top-app-bar-back").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-nav-workouts").assertDoesNotExist()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithTag("workout-detail-active").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-nav-workouts").assertDoesNotExist()
    }

    @Test
    fun programDetail_usesIdRouteAndSurvivesActivityRecreation() {
        composeRule.onNodeWithTag("bottom-nav-programs").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Full Body 3 Gün")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Full Body 3 Gün").performClick()

        composeRule.onNodeWithText(text(R.string.program_plan_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("gt-top-app-bar-back").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-nav-programs").assertDoesNotExist()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText(text(R.string.program_plan_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-nav-programs").assertDoesNotExist()
    }

    private fun text(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
