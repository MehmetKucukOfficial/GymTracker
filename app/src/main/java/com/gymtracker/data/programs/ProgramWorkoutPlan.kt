package com.gymtracker.data.programs

import androidx.room.Embedded
import androidx.room.Relation
import com.gymtracker.data.entities.ProgramDayExercise
import com.gymtracker.data.entities.ProgramWorkout

data class ProgramWorkoutPlan(
    @Embedded val workout: ProgramWorkout,
    @Relation(
        parentColumn = "id",
        entityColumn = "programWorkoutId"
    )
    val exercises: List<ProgramDayExercise>
)

val ProgramWorkoutPlan.orderedExercises: List<ProgramDayExercise>
    get() = exercises.sortedBy { it.position }
