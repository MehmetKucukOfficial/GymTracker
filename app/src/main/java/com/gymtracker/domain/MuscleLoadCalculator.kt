package com.gymtracker.domain

import com.gymtracker.data.workout.CompletedSetForLoad
import kotlin.math.min

enum class MuscleLoadLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class MuscleLoad(
    val muscle: MuscleGroup,
    val loadScore: Float,
    val weightedVolume: Float,
    val level: MuscleLoadLevel
)

data class MuscleLoadSummary(
    val muscles: List<MuscleLoad>,
    val completedSetCount: Int,
    val unknownExerciseCount: Int,
    val bodyweightCalibrationKg: Float = MuscleLoadCalculator.DEFAULT_BODYWEIGHT_EQUIVALENT_KG,
    val usesPersonalBodyweight: Boolean = false
)

object MuscleLoadCalculator {

    const val DEFAULT_BODYWEIGHT_EQUIVALENT_KG = 20f
    private const val WINDOW_MILLIS = 7L * 24L * 60L * 60L * 1_000L

    private val maxWeeklyVolume = mapOf(
        MuscleGroup.UPPER_BACK to 12000f,
        MuscleGroup.LATS to 12000f,
        MuscleGroup.TRAPS to 8000f,
        MuscleGroup.FRONT_DELTS to 6000f,
        MuscleGroup.SIDE_DELTS to 5000f,
        MuscleGroup.REAR_DELTS to 5000f,
        MuscleGroup.FOREARMS to 6000f,
        MuscleGroup.ABS to 10000f,
        MuscleGroup.OBLIQUES to 8000f,
        MuscleGroup.LOWER_BACK to 10000f,
        MuscleGroup.GLUTES to 15000f,
        MuscleGroup.QUADS to 20000f,
        MuscleGroup.HAMSTRINGS to 15000f,
        MuscleGroup.CALVES to 12000f,
        MuscleGroup.HIP_FLEXORS to 5000f,
        MuscleGroup.NECK to 3000f,
        MuscleGroup.SERRATUS to 3000f,
        MuscleGroup.ADDUCTORS to 6000f,
        MuscleGroup.GLUTE_MEDIUS to 5000f,
        MuscleGroup.CHEST_UPPER to 6000f,
        MuscleGroup.CHEST_MID to 8000f,
        MuscleGroup.CHEST_LOWER to 4000f,
        MuscleGroup.BICEPS_LONG_HEAD to 4000f,
        MuscleGroup.BICEPS_SHORT_HEAD to 4000f,
        MuscleGroup.BRACHIALIS to 3000f,
        MuscleGroup.TRICEPS_LATERAL to 4000f,
        MuscleGroup.TRICEPS_LONG to 5000f,
        MuscleGroup.TRICEPS_MEDIAL to 3000f,
        MuscleGroup.TERES to 3000f,
        MuscleGroup.INTERNAL_OBLIQUES to 5000f,
        MuscleGroup.TRANSVERSE_ABDOMINIS to 5000f,
        MuscleGroup.RHOMBOIDS to 5000f
    )

    fun calculate(
        sets: List<CompletedSetForLoad>,
        nowMillis: Long,
        personalBodyweightKg: Float? = null
    ): MuscleLoadSummary {
        val volumeByMuscle = mutableMapOf<MuscleGroup, Float>()
        var unknownExerciseCount = 0
        val validPersonalBodyweight = personalBodyweightKg
            ?.takeIf { it.isFinite() && it in 20f..400f }
        val bodyweightEquivalent = validPersonalBodyweight ?: DEFAULT_BODYWEIGHT_EQUIVALENT_KG

        sets.forEach { set ->
            val definition = ExerciseLibrary.exercises.find { it.name == set.exerciseName }
            if (definition == null) {
                unknownExerciseCount++
                return@forEach
            }
            if (set.reps <= 0 || !set.weightKg.isFinite() || set.weightKg < 0f) return@forEach
            val effectiveWeight = set.weightKg.takeIf { it > 0f } ?: bodyweightEquivalent
            val ageMillis = (nowMillis - set.completedAt).coerceIn(0L, WINDOW_MILLIS)
            val recencyWeight = 1f - (ageMillis.toFloat() / WINDOW_MILLIS.toFloat() * 0.5f)
            definition.muscleActivations.forEach { activation ->
                val weightedVolume = set.reps * effectiveWeight * activation.activationPercent * recencyWeight
                volumeByMuscle[activation.muscle] =
                    (volumeByMuscle[activation.muscle] ?: 0f) + weightedVolume
            }
        }

        val loads = MuscleGroup.values().map { muscle ->
            val volume = volumeByMuscle[muscle] ?: 0f
            val score = min(volume / (maxWeeklyVolume[muscle] ?: 10000f), 1f)
            MuscleLoad(
                muscle = muscle,
                loadScore = score,
                weightedVolume = volume,
                level = when {
                    score < 0.33f -> MuscleLoadLevel.LOW
                    score < 0.67f -> MuscleLoadLevel.MEDIUM
                    else -> MuscleLoadLevel.HIGH
                }
            )
        }
        return MuscleLoadSummary(
            muscles = loads,
            completedSetCount = sets.size,
            unknownExerciseCount = unknownExerciseCount,
            bodyweightCalibrationKg = bodyweightEquivalent,
            usesPersonalBodyweight = validPersonalBodyweight != null
        )
    }
}
