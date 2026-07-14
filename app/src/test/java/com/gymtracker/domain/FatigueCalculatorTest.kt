package com.gymtracker.domain

import com.gymtracker.data.entities.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FatigueCalculatorTest {

    @Test
    fun calculate_distributesKnownExerciseVolumeByActivation() {
        val result = FatigueCalculator.calculate(
            listOf(
                WorkoutSet(
                    sessionId = 1L,
                    exerciseName = "Bench Press",
                    setNumber = 1,
                    reps = 10,
                    weightKg = 100f
                )
            )
        ).associateBy { it.muscle }

        assertEquals(330f, result.getValue(MuscleGroup.CHEST_MID).totalVolume, 0.001f)
        assertEquals(330f / 8000f, result.getValue(MuscleGroup.CHEST_MID).fatiguePercent, 0.001f)
        assertEquals(150f, result.getValue(MuscleGroup.FRONT_DELTS).totalVolume, 0.001f)
    }

    @Test
    fun calculate_capsFatigueAndIgnoresUnknownExercises() {
        val result = FatigueCalculator.calculate(
            listOf(
                WorkoutSet(sessionId = 1L, exerciseName = "Bench Press", setNumber = 1, reps = 100, weightKg = 1_000f),
                WorkoutSet(sessionId = 1L, exerciseName = "Unknown", setNumber = 2, reps = 100, weightKg = 1_000f)
            )
        ).associateBy { it.muscle }

        assertEquals(1f, result.getValue(MuscleGroup.CHEST_MID).fatiguePercent, 0.001f)
        assertTrue(result.getValue(MuscleGroup.CALVES).totalVolume == 0f)
    }
}
