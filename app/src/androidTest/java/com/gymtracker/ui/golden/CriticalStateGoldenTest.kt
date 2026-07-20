package com.gymtracker.ui.golden

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.R
import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.MealType
import com.gymtracker.data.entities.NutritionGoal
import com.gymtracker.data.measurements.MeasurementChartPeriod
import com.gymtracker.data.measurements.WeightTrend
import com.gymtracker.ui.components.GtTopAppBar
import com.gymtracker.ui.screens.measurements.MeasurementsContent
import com.gymtracker.ui.screens.measurements.MeasurementsUiState
import com.gymtracker.ui.screens.nutrition.DailyNutrition
import com.gymtracker.ui.screens.nutrition.NutritionContent
import com.gymtracker.ui.theme.GymTrackerTheme
import java.time.LocalDate
import java.util.Locale
import java.util.TimeZone
import org.junit.Rule
import org.junit.Test

class CriticalStateGoldenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun darkTurkishLargeFontNutritionOverGoalMatchesGolden() {
        render(locale = Locale("tr", "TR"), darkTheme = true, fontScale = 2f) {
            NutritionContent(
                daily = DailyNutrition(
                    entries = listOf(
                        FoodEntry(
                            id = 1,
                            date = 1_752_537_600_000,
                            foodItemId = 1,
                            foodName = "Izgara tavuk ve bulgur",
                            amountGrams = 240f,
                            calories = 2_450f,
                            protein = 185f,
                            carbs = 260f,
                            fat = 82f,
                            mealType = MealType.DINNER
                        )
                    ),
                    goal = NutritionGoal(
                        dailyCalories = 2_000,
                        dailyProteinG = 160,
                        dailyCarbsG = 220,
                        dailyFatG = 70
                    ),
                    totalCalories = 2_450f,
                    totalProtein = 185f,
                    totalCarbs = 260f,
                    totalFat = 82f
                ),
                selectedDate = LocalDate.of(2025, 7, 15),
                canGoNext = false,
                onPreviousDay = {},
                onNextDay = {},
                onToday = {},
                onAddEntry = {},
                onGoalSettings = {},
                onAddCustomFood = {},
                onDeleteEntry = {}
            )
        }

        composeRule.onNodeWithTag(GOLDEN_ROOT)
            .assertMatchesGolden("nutrition_over_goal_dark_tr_font200")
    }

    @Test
    fun lightEnglishMeasurementsChartAndTopBarMatchGolden() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        render(locale = Locale.US, darkTheme = false, fontScale = 1f) {
            Column {
                GtTopAppBar(
                    title = stringResource(R.string.measurements_title),
                    supportingText = stringResource(R.string.measurements_subtitle),
                    onBack = {}
                )
                MeasurementsContent(
                    uiState = MeasurementsUiState(
                        latest = measurement(4, 1_752_537_600_000, 78.4f, 82f),
                        history = listOf(
                            measurement(3, 1_751_932_800_000, 79.1f, 83f),
                            measurement(2, 1_751_328_000_000, 79.8f, 84f),
                            measurement(1, 1_750_723_200_000, 80.5f, 85f)
                        ),
                        weightTrend = WeightTrend(78.4f, -0.7f),
                        chartPeriod = MeasurementChartPeriod.THIRTY_DAYS,
                        chartMeasurements = listOf(
                            measurement(1, 1_750_723_200_000, 80.5f, 85f),
                            measurement(2, 1_751_328_000_000, 79.8f, 84f),
                            measurement(3, 1_751_932_800_000, 79.1f, 83f),
                            measurement(4, 1_752_537_600_000, 78.4f, 82f)
                        )
                    ),
                    onAddMeasurement = {},
                    onDeleteMeasurement = {},
                    showHeader = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        composeRule.onNodeWithTag(GOLDEN_ROOT)
            .assertMatchesGolden("measurements_chart_topbar_light_en")
    }

    private fun render(
        locale: Locale,
        darkTheme: Boolean,
        fontScale: Float,
        content: @Composable () -> Unit
    ) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(locale))
            this.fontScale = fontScale
        }
        val localizedContext = base.createConfigurationContext(configuration)
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides configuration,
                LocalDensity provides Density(density = 1f, fontScale = fontScale)
            ) {
                GymTrackerTheme(darkTheme = darkTheme) {
                    Surface {
                        Box(
                            Modifier
                                .requiredSize(width = 360.dp, height = 800.dp)
                                .testTag(GOLDEN_ROOT)
                        ) {
                            content()
                        }
                    }
                }
            }
        }
    }

    private fun measurement(
        id: Long,
        date: Long,
        weight: Float,
        waist: Float
    ) = BodyMeasurement(
        id = id,
        date = date,
        weightKg = weight,
        waistCm = waist
    )

    private companion object {
        const val GOLDEN_ROOT = "critical-state-golden-root"
    }
}
