package com.gymtracker.domain

import com.gymtracker.data.entities.WorkoutSet
import kotlin.math.min

data class MuscleFatigue(
    val muscle: MuscleGroup,
    val fatiguePercent: Float,  // 0.0 - 1.0
    val totalVolume: Float
)

object FatigueCalculator {

    // Her kas için haftalık maksimum işlem hacmi (sets × reps × kg)
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

    fun calculate(sets: List<WorkoutSet>): List<MuscleFatigue> {
        val volumeByMuscle = mutableMapOf<MuscleGroup, Float>()

        sets.forEach { set ->
            val exerciseDef = ExerciseLibrary.exercises.find { it.name == set.exerciseName }
            exerciseDef?.muscleActivations?.forEach { activation ->
                val volume = set.reps * set.weightKg * activation.activationPercent
                volumeByMuscle[activation.muscle] =
                    (volumeByMuscle[activation.muscle] ?: 0f) + volume
            }
        }

        return MuscleGroup.values().map { muscle ->
            val volume = volumeByMuscle[muscle] ?: 0f
            val max = maxWeeklyVolume[muscle] ?: 10000f
            MuscleFatigue(
                muscle = muscle,
                fatiguePercent = min(volume / max, 1.0f),
                totalVolume = volume
            )
        }
    }
}
