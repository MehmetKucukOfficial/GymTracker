package com.gymtracker.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymtracker.MainActivity
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
    fun progressChild_keepsProgressSelectedAndSystemBackReturnsToHub() {
        composeRule.onNodeWithTag("bottom-nav-progress").performClick()
        composeRule.onNodeWithText("Ölçümlerini, fotoğraflarını ve kas yükünü tek yerde izle.")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Vücut ölçümleri").performClick()
        composeRule.onNodeWithTag("bottom-nav-progress").assertIsSelected()

        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.onNodeWithText("Ölçümlerini, fotoğraflarını ve kas yükünü tek yerde izle.")
            .assertIsDisplayed()
    }

    @Test
    fun selectedTopLevelDestination_survivesActivityRecreation() {
        composeRule.onNodeWithTag("bottom-nav-programs").performClick()
        composeRule.onNodeWithTag("bottom-nav-programs").assertIsSelected()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithTag("bottom-nav-programs").assertIsSelected()
        composeRule.onNodeWithText("Antrenman programları").assertIsDisplayed()
    }

    @Test
    fun workoutDetail_usesIdRouteAndSurvivesActivityRecreation() {
        composeRule.onNodeWithTag("bottom-nav-workouts").performClick()
        composeRule.onNodeWithContentDescription("Yeni Antrenman").performClick()
        composeRule.onNodeWithText("Antrenman Adı").performTextInput("Route Test Workout")
        composeRule.onNodeWithText("Başlat").performClick()

        composeRule.onNodeWithText("Route Test Workout").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-nav-workouts").assertDoesNotExist()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("Route Test Workout").assertIsDisplayed()
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

        composeRule.onNodeWithText("Haftalık Antrenman Planı").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-nav-programs").assertDoesNotExist()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("Haftalık Antrenman Planı").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-nav-programs").assertDoesNotExist()
    }
}
