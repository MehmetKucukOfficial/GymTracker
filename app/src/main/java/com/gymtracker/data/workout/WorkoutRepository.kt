package com.gymtracker.data.workout

import com.gymtracker.data.dao.WorkoutDao
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val dao: WorkoutDao
) {
    val sessions = dao.getAllSessions()

    fun observeSession(sessionId: Long) = dao.observeSessionById(sessionId)

    fun observeSets(sessionId: Long) = dao.getSetsBySession(sessionId)

    suspend fun startSession(name: String): Long =
        dao.insertSession(WorkoutSession(name = name.trim()))

    suspend fun addSet(set: WorkoutSet) = dao.insertSet(set)

    suspend fun deleteSet(set: WorkoutSet) = dao.deleteSet(set)

    suspend fun finishSession(session: WorkoutSession, durationMinutes: Int, notes: String) {
        dao.updateSession(
            session.copy(
                durationMinutes = durationMinutes,
                notes = notes.trim()
            )
        )
    }
}
