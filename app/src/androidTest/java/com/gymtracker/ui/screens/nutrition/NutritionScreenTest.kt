package com.gymtracker.ui.screens.nutrition

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
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.MealType
import com.gymtracker.data.entities.NutritionGoal
import com.gymtracker.ui.theme.GymTrackerTheme
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NutritionScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun overGoalAtTwoHundredPercentFontScaleUsesExplicitWarningState() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                GymTrackerTheme(darkTheme = true) {
                    NutritionContent(
                        daily = DailyNutrition(
                            entries = listOf(
                                FoodEntry(
                                    id = 1,
                                    date = 1_000,
                                    foodItemId = 1,
                                    foodName = "Test öğünü",
                                    amountGrams = 100f,
                                    calories = 2_500f,
                                    protein = 190f,
                                    carbs = 250f,
                                    fat = 90f,
                                    mealType = MealType.DINNER
                                )
                            ),
                            goal = NutritionGoal(dailyCalories = 2_000),
                            totalCalories = 2_500f,
                            totalProtein = 190f,
                            totalCarbs = 250f,
                            totalFat = 90f
                        ),
                        selectedDate = LocalDate.of(2026, 7, 15),
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
            }
        }

        composeRule.onNodeWithTag("nutrition-content").assertIsDisplayed()
        composeRule.onNodeWithTag("nutrition-calories-over").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.nutrition_calories_over, 500)).assertIsDisplayed()
    }

    @Test
    fun emptyDayPrimaryActionIsReachable() {
        val clicks = AtomicInteger(0)
        composeRule.setContent {
            GymTrackerTheme(darkTheme = false) {
                NutritionContent(
                    daily = DailyNutrition(),
                    selectedDate = LocalDate.of(2026, 7, 15),
                    canGoNext = false,
                    onPreviousDay = {},
                    onNextDay = {},
                    onToday = {},
                    onAddEntry = { clicks.incrementAndGet() },
                    onGoalSettings = {},
                    onAddCustomFood = {},
                    onDeleteEntry = {}
                )
            }
        }

        composeRule.onNodeWithTag("nutrition-content")
            .performScrollToNode(hasText(text(R.string.nutrition_empty_title)))
        composeRule.onNodeWithText(text(R.string.nutrition_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.nutrition_add_entry)).performClick()
        assertEquals(1, clicks.get())
    }

    private fun text(id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)
}
