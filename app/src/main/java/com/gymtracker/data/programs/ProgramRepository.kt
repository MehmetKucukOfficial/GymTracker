package com.gymtracker.data.programs

import androidx.room.withTransaction
import com.gymtracker.data.dao.ProgramDao
import com.gymtracker.data.dao.WorkoutDao
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.data.entities.WorkoutStatus
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ProgramWorkoutStartResult {
    val sessionId: Long

    data class Created(override val sessionId: Long) : ProgramWorkoutStartResult
    data class ExistingActive(override val sessionId: Long) : ProgramWorkoutStartResult
}

@Singleton
class ProgramRepository @Inject constructor(
    private val dao: ProgramDao,
    private val workoutDao: WorkoutDao,
    private val database: AppDatabase
) {
    val programs = dao.getAll()

    fun observeProgram(programId: Long) = dao.observeById(programId)

    fun observeWorkoutPlans(programId: Long) = dao.observeWorkoutPlans(programId)

    suspend fun purchase(program: Program) {
        dao.updateProgram(program.copy(isPurchased = true))
    }

    suspend fun startWorkout(
        programId: Long,
        programWorkoutId: Long,
        startedAt: Long = System.currentTimeMillis()
    ): ProgramWorkoutStartResult = database.withTransaction {
        val program = requireNotNull(dao.getById(programId)) { "Program was not found" }
        check(!program.isPaid || program.isPurchased) { "Program is locked" }
        val day = requireNotNull(dao.getWorkout(programId, programWorkoutId)) {
            "Program workout was not found"
        }
        val exercises = dao.getDayExercises(day.id)
        check(exercises.isNotEmpty()) { "Program workout has no readable exercises" }
        check(
            exercises.all {
                it.targetSets in 1..20 && it.targetReps.isNotBlank() && it.restSeconds in 0..3_600
            }
        ) { "Program workout contains invalid exercise targets" }

        val active = workoutDao.getActiveSession()
        if (active != null) return@withTransaction ProgramWorkoutStartResult.ExistingActive(active.id)

        val sessionId = workoutDao.insertSession(
            WorkoutSession(
                name = "${program.name} • ${day.dayName}",
                date = startedAt,
                status = WorkoutStatus.ACTIVE
            )
        )
        workoutDao.insertSets(
            exercises.flatMap { exercise ->
                (1..exercise.targetSets).map { setNumber ->
                    WorkoutSet(
                        sessionId = sessionId,
                        exerciseName = exercise.exerciseName,
                        setNumber = setNumber,
                        reps = 0,
                        weightKg = 0f,
                        isCompleted = false,
                        exerciseId = exercise.exerciseId,
                        targetReps = exercise.targetReps,
                        exerciseOrder = exercise.position
                    )
                }
            }
        )
        ProgramWorkoutStartResult.Created(sessionId)
    }
}
