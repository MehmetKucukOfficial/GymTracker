package com.gymtracker.data.nutrition

import com.gymtracker.data.entities.NutritionGoal
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.MealType
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionRulesTest {

    @Test
    fun dayRangeUsesCalendarBoundaryAcrossDst() {
        val zone = ZoneId.of("Europe/Berlin")
        val spring = nutritionDayRange(LocalDate.of(2024, 3, 31), zone)
        val autumn = nutritionDayRange(LocalDate.of(2024, 10, 27), zone)

        assertEquals(23 * 60 * 60 * 1_000L, spring.endExclusiveMillis - spring.startInclusiveMillis)
        assertEquals(25 * 60 * 60 * 1_000L, autumn.endExclusiveMillis - autumn.startInclusiveMillis)
    }

    @Test
    fun invalidGoalCannotCreateInfiniteProgress() {
        assertFalse(isValidNutritionGoal(NutritionGoal(dailyCalories = 0)))
        assertEquals(0f, nutritionProgress(400f, 0))
        assertEquals(1.25f, nutritionProgress(2_500f, 2_000), 0.001f)
        assertTrue(isValidNutritionGoal(NutritionGoal()))
    }

    @Test
    fun editingAmountPreservesFoodSnapshotAndScalesMacros() {
        val original = FoodEntry(
            id = 9,
            date = 1_000,
            foodItemId = 3,
            foodName = "Snapshot meal",
            amountGrams = 100f,
            calories = 250f,
            protein = 20f,
            carbs = 30f,
            fat = 8f,
            mealType = MealType.LUNCH
        )

        val updated = requireNotNull(updateFoodEntrySnapshot(original, 150f))

        assertEquals(9L, updated.id)
        assertEquals("Snapshot meal", updated.foodName)
        assertEquals(375f, updated.calories)
        assertEquals(30f, updated.protein)
        assertEquals(45f, updated.carbs)
        assertEquals(12f, updated.fat)
        assertEquals(null, updateFoodEntrySnapshot(original, 0f))
    }
}
