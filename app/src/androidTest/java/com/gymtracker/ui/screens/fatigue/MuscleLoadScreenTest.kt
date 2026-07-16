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
            .performScrollToNode(hasText("Henüz kas yükü yok"))
        composeRule.onNodeWithText("Henüz kas yükü yok").assertIsDisplayed()
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
                            unknownExerciseCount = 0
                        )
                    ),
                    onRetry = {}
                )
            }
        }

        composeRule.onNodeWithText("1 tamamlanmış set").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Kas yükü yöntemi hakkında bilgi").performClick()
        composeRule.onNodeWithText("Kas yükü nasıl hesaplanır?").assertIsDisplayed()
        composeRule.onNodeWithText("Tamam").performClick()
        composeRule.onNodeWithTag("muscle-load-content")
            .performScrollToNode(hasText("Orta Göğüs"))
        composeRule.onNodeWithText("Orta Göğüs").assertIsDisplayed()
        composeRule.onNodeWithText("Skor 0.45").assertIsDisplayed()
    }
}
