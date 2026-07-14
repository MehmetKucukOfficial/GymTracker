package com.gymtracker.data.workout

import com.gymtracker.data.dao.WorkoutDao
import com.gymtracker.data.entities.WorkoutSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val dao: WorkoutDao
) {
    val sessions = dao.getAllSessions()
    val completedSessions = dao.getCompletedSessions()
    val activeSession = dao.observeActiveSession()

    fun observeSession(sessionId: Long) = dao.observeSessionById(sessionId)

    fun observeSets(sessionId: Long) = dao.getSetsBySession(sessionId)

    suspend fun startOrResumeSession(
        name: String,
        startedAt: Long = System.currentTimeMillis()
    ): Long {
        val normalizedName = name.trim()
        require(normalizedName.isNotEmpty()) { "Workout name cannot be blank" }
        return dao.startOrResumeSession(normalizedName, startedAt)
    }

    suspend fun addSet(set: WorkoutSet): Long {
        require(set.exerciseName.isNotBlank()) { "Exercise name cannot be blank" }
        require(set.setNumber > 0) { "Set number must be positive" }
        require(set.reps > 0) { "Repetitions must be positive" }
        require(set.weightKg >= 0f) { "Weight cannot be negative" }
        return dao.insertSetForActiveSession(set)
    }

    suspend fun deleteSet(set: WorkoutSet) {
        check(dao.deleteSetFromActiveSession(set.id, set.sessionId) == 1) {
            "Sets can only be deleted from an active workout session"
        }
    }

    suspend fun finishSession(
        sessionId: Long,
        durationMinutes: Int,
        notes: String,
        completedAt: Long = System.currentTimeMillis()
    ) {
        require(durationMinutes >= 0) { "Duration cannot be negative" }
        check(
            dao.completeActiveSession(
                sessionId = sessionId,
                completedAt = completedAt,
                durationMinutes = durationMinutes,
                notes = notes.trim()
            ) == 1
        ) { "Only an active workout session can be completed" }
    }
}
