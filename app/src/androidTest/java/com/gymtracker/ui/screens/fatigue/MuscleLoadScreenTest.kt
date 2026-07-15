package com.gymtracker.ui.screens.fatigue

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.R
import com.gymtracker.domain.MuscleLoad
import com.gymtracker.domain.MuscleLoadLevel
import com.gymtracker.domain.MuscleLoadSummary
import com.gymtracker.domain.MuscleGroup
import com.gymtracker.ui.theme.GymTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MuscleLoadScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyStateAtTwoHundredPercentFontScaleIsReachable() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                GymTrackerTheme(darkTheme = true) {
                    MuscleLoadContent(
                        uiState = MuscleLoadUiState.Content(
                            MuscleLoadSummary(emptyList(), completedSetCount = 0, unknownExerciseCount = 0)
                        ),
                        onRetry = {}
                    )
                }
            }
        }

        composeRule.onNodeWithTag("muscle-load-content")
            .performScrollToNode(hasText(text(R.string.muscle_load_empty_title)))
        composeRule.onNodeWithText(text(R.string.muscle_load_empty_title)).assertIsDisplayed()
    }

    @Test
    fun contentExposesMethodologyAndColorIndependentLoadList() {
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                MuscleLoadContent(
                    uiState = MuscleLoadUiState.Content(
                        MuscleLoadSummary(
                            muscles = listOf(
                                MuscleLoad(
                                    muscle = MuscleGroup.CHEST_MID,
                                    loadScore = 0.45f,
                                    weightedVolume = 3_600f,
                                    level = MuscleLoadLevel.MEDIUM
                                )
                            ),
                            completedSetCount = 1,
                            unknownExerciseCount = 0,
                            bodyweightCalibrationKg = 82.5f,
                            usesPersonalBodyweight = true
                        )
                    ),
                    onRetry = {}
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.muscle_load_completed_sets, 1)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.muscle_load_personal_calibration, 82.5f)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(text(R.string.muscle_load_methodology_action)).performClick()
        composeRule.onNodeWithText(text(R.string.muscle_load_methodology_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_ok)).performClick()
        composeRule.onNodeWithTag("muscle-load-content")
            .performScrollToNode(hasText(text(R.string.muscle_chest_mid)))
        composeRule.onNodeWithText(text(R.string.muscle_chest_mid)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.muscle_load_score, 0.45f)).assertIsDisplayed()
    }

    private fun text(id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)
}
