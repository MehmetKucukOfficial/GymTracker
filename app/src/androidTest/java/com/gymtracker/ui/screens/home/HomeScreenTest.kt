package com.gymtracker.ui.screens.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.R
import com.gymtracker.data.home.HomeDashboardData
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyDashboard_atTwoHundredPercentFontScale_staysUsableAndDoesNotShowFakeZeroes() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 2f)
            ) {
                GymTrackerTheme(darkTheme = false) {
                    HomeContent(
                        uiState = HomeUiState.Content(HomeDashboardData()),
                        greeting = "Günaydın",
                        onNavigate = {},
                        onRetry = {}
                    )
                }
            }
        }

        composeRule.onNodeWithTag("home-layout-compact").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.home_empty_title))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("0 kcal").assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.home_value_no_meal)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun populatedDashboard_inDarkTheme_exposesMergedMetricSemantics() {
        composeRule.setContent {
            GymTrackerTheme(darkTheme = true) {
                HomeContent(
                    uiState = HomeUiState.Content(
                        HomeDashboardData(
                            totalWorkouts = 12,
                            weekWorkouts = 3,
                            latestWeight = 81.5f,
                            todayCalories = 450,
                            todayNutritionEntryCount = 1,
                            lastWorkoutName = "Full Body"
                        )
                    ),
                    greeting = "İyi akşamlar",
                    onNavigate = {},
                    onRetry = {}
                )
            }
        }

        composeRule.onNodeWithTag("home-state-content").assertIsDisplayed()
        val metricDescription = "${text(R.string.home_metric_total_workouts)}: 12. " +
            text(R.string.home_support_all_time)
        composeRule.onNodeWithContentDescription(metricDescription)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun errorState_retryActionIsReachable() {
        val retries = AtomicInteger(0)
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                HomeContent(
                    uiState = HomeUiState.Error,
                    greeting = "Günaydın",
                    onNavigate = {},
                    onRetry = { retries.incrementAndGet() }
                )
            }
        }

        composeRule.onNodeWithTag("home-state-error").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_retry)).performClick()
        assertEquals(1, retries.get())
    }

    private fun text(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
