package com.gymtracker.domain

import com.gymtracker.data.workout.CompletedSetForLoad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuscleLoadCalculatorTest {

    @Test
    fun calculate_distributesKnownExerciseAndAppliesTimeWeight() {
        val now = 8L * DAY
        val summary = MuscleLoadCalculator.calculate(
            listOf(
                CompletedSetForLoad("Bench Press", reps = 10, weightKg = 100f, completedAt = now),
                CompletedSetForLoad("Bench Press", reps = 10, weightKg = 100f, completedAt = now - 7L * DAY)
            ),
            nowMillis = now
        )
        val result = summary.muscles.associateBy(MuscleLoad::muscle)

        assertEquals(495f, result.getValue(MuscleGroup.CHEST_MID).weightedVolume, 0.001f)
        assertEquals(495f / 8000f, result.getValue(MuscleGroup.CHEST_MID).loadScore, 0.001f)
        assertEquals(2, summary.completedSetCount)
    }

    @Test
    fun calculate_usesBodyweightFallbackAndReportsUnknownExercise() {
        val summary = MuscleLoadCalculator.calculate(
            listOf(
                CompletedSetForLoad("Pull-up", reps = 10, weightKg = 0f, completedAt = DAY),
                CompletedSetForLoad("Unknown", reps = 10, weightKg = 100f, completedAt = DAY)
            ),
            nowMillis = DAY
        )

        assertTrue(summary.muscles.first { it.muscle == MuscleGroup.LATS }.weightedVolume > 0f)
        assertEquals(1, summary.unknownExerciseCount)
        assertEquals(20f, summary.bodyweightCalibrationKg, 0.001f)
        assertEquals(false, summary.usesPersonalBodyweight)
    }

    @Test
    fun calculate_usesPersonalBodyweightForZeroWeightSets() {
        val summary = MuscleLoadCalculator.calculate(
            sets = listOf(
                CompletedSetForLoad("Pull-up", reps = 10, weightKg = 0f, completedAt = DAY)
            ),
            nowMillis = DAY,
            personalBodyweightKg = 80f
        )

        val lats = summary.muscles.first { it.muscle == MuscleGroup.LATS }
        assertEquals(480f, lats.weightedVolume, 0.001f)
        assertEquals(80f, summary.bodyweightCalibrationKg, 0.001f)
        assertTrue(summary.usesPersonalBodyweight)
    }

    @Test
    fun calculate_rejectsInvalidPersonalBodyweightAndKeepsSafeFallback() {
        val summary = MuscleLoadCalculator.calculate(
            sets = listOf(
                CompletedSetForLoad("Pull-up", reps = 10, weightKg = 0f, completedAt = DAY)
            ),
            nowMillis = DAY,
            personalBodyweightKg = Float.NaN
        )

        assertEquals(20f, summary.bodyweightCalibrationKg, 0.001f)
        assertEquals(false, summary.usesPersonalBodyweight)
    }

    @Test
    fun calculate_capsScoreAndAssignsHighLevel() {
        val summary = MuscleLoadCalculator.calculate(
            listOf(CompletedSetForLoad("Bench Press", 100, 1_000f, DAY)),
            nowMillis = DAY
        )
        val chest = summary.muscles.first { it.muscle == MuscleGroup.CHEST_MID }

        assertEquals(1f, chest.loadScore, 0.001f)
        assertEquals(MuscleLoadLevel.HIGH, chest.level)
    }

    private companion object {
        const val DAY = 24L * 60L * 60L * 1_000L
    }
}
