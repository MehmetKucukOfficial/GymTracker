package com.gymtracker.data.nutrition

import com.gymtracker.data.entities.NutritionGoal
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
}
