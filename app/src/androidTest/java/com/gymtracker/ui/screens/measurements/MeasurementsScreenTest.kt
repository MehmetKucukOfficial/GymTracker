package com.gymtracker.ui.screens.measurements

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.R
import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.measurements.WeightTrend
import com.gymtracker.data.measurements.MeasurementChartPeriod
import com.gymtracker.ui.theme.GymTrackerTheme
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeasurementsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun latestAndHistoryAtTwoHundredPercentFontScaleStayDistinct() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                GymTrackerTheme(darkTheme = true) {
                    MeasurementsContent(
                        uiState = MeasurementsUiState(
                            latest = BodyMeasurement(id = 2, date = 2_000, weightKg = 81.5f),
                            history = listOf(
                                BodyMeasurement(id = 1, date = 1_000, weightKg = 82f, waistCm = 84f)
                            ),
                            weightTrend = WeightTrend(81.5f, -0.5f)
                        ),
                        onAddMeasurement = {},
                        onDeleteMeasurement = {}
                    )
                }
            }
        }

        composeRule.onNodeWithTag("measurements-latest").assertIsDisplayed()
        composeRule.onNodeWithText("81.5 kg").assertIsDisplayed()
        val historyTitle = text(R.string.measurements_history)
        composeRule.onNodeWithTag("measurements-content")
            .performScrollToNode(hasText(historyTitle))
        composeRule.onNodeWithText(historyTitle).assertIsDisplayed()
        val waistValue = text(R.string.measurements_cm_value, 84f)
        composeRule.onNodeWithTag("measurements-content")
            .performScrollToNode(hasText(waistValue, substring = true))
        composeRule.onNodeWithText(waistValue, substring = true)
            .fetchSemanticsNode()
    }

    @Test
    fun emptyStatePrimaryActionIsReachable() {
        val clicks = AtomicInteger(0)
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                MeasurementsContent(
                    uiState = MeasurementsUiState(),
                    onAddMeasurement = { clicks.incrementAndGet() },
                    onDeleteMeasurement = {}
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.measurements_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.measurements_first_action)).performClick()
        assertEquals(1, clicks.get())
    }

    @Test
    fun realChartIsAccessibleAndPeriodSelectionIsReachable() {
        val selections = AtomicInteger(0)
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                MeasurementsContent(
                    uiState = MeasurementsUiState(
                        latest = BodyMeasurement(id = 3, date = 3_000, weightKg = 80f),
                        history = listOf(BodyMeasurement(id = 2, date = 2_000, weightKg = 81f)),
                        chartPeriod = MeasurementChartPeriod.THIRTY_DAYS,
                        chartMeasurements = listOf(
                            BodyMeasurement(id = 1, date = 1_000, weightKg = 82f),
                            BodyMeasurement(id = 2, date = 2_000, weightKg = 81f),
                            BodyMeasurement(id = 3, date = 3_000, weightKg = 80f)
                        )
                    ),
                    onAddMeasurement = {},
                    onDeleteMeasurement = {},
                    onSelectChartPeriod = { selections.incrementAndGet() }
                )
            }
        }

        composeRule.onNodeWithTag("measurements-weight-chart").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.measurements_period_7)).performClick()
        assertEquals(1, selections.get())
    }

    private fun text(id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)
}
