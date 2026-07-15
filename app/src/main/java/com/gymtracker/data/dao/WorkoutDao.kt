package com.gymtracker.data.dao

import androidx.room.*
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.data.entities.WorkoutStatus
import com.gymtracker.data.workout.CompletedSetForLoad
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY completedAt DESC, date DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' ORDER BY date DESC LIMIT 1")
    fun observeActiveSession(): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' ORDER BY date DESC LIMIT 1")
    suspend fun getActiveSession(): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun observeSessionById(id: Long): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY exerciseName, setNumber")
    fun getSetsBySession(sessionId: Long): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY exerciseName, setNumber")
    suspend fun getSetsBySessionSync(sessionId: Long): List<WorkoutSet>

    @Query("SELECT * FROM workout_sets WHERE date(sessionId) >= :since ORDER BY sessionId DESC")
    suspend fun getSetsSince(since: Long): List<WorkoutSet>

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_sessions s ON ws.sessionId = s.id
        WHERE s.date >= :since
    """)
    suspend fun getSetsFromSessionsSince(since: Long): List<WorkoutSet>

    @Query(
        """
        SELECT ws.exerciseName,
               ws.reps,
               ws.weightKg,
               COALESCE(s.completedAt, s.date) AS completedAt
        FROM workout_sets ws
        INNER JOIN workout_sessions s ON ws.sessionId = s.id
        WHERE s.status = 'COMPLETED'
          AND ws.isCompleted = 1
          AND COALESCE(s.completedAt, s.date) >= :since
        ORDER BY completedAt DESC, ws.id DESC
        """
    )
    fun observeCompletedSetsForLoadSince(since: Long): Flow<List<CompletedSetForLoad>>

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Transaction
    suspend fun startOrResumeSession(name: String, startedAt: Long): Long {
        return getActiveSession()?.id ?: insertSession(
            WorkoutSession(
                name = name,
                date = startedAt,
                status = WorkoutStatus.ACTIVE
            )
        )
    }

    @Insert
    suspend fun insertSet(set: WorkoutSet): Long

    @Transaction
    suspend fun insertSetForActiveSession(set: WorkoutSet): Long {
        val session = getSessionById(set.sessionId)
        check(session?.status == WorkoutStatus.ACTIVE) {
            "Sets can only be added to an active workout session"
        }
        return insertSet(set)
    }

    @Insert
    suspend fun insertSets(sets: List<WorkoutSet>)

    @Update
    suspend fun updateSet(set: WorkoutSet)

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Delete
    suspend fun deleteSet(set: WorkoutSet)

    @Query(
        """
        DELETE FROM workout_sets
        WHERE id = :setId
          AND sessionId = :sessionId
          AND EXISTS (
              SELECT 1 FROM workout_sessions
              WHERE id = :sessionId AND status = 'ACTIVE'
          )
        """
    )
    suspend fun deleteSetFromActiveSession(setId: Long, sessionId: Long): Int

    @Query(
        """
        UPDATE workout_sessions
        SET status = 'COMPLETED',
            completedAt = :completedAt,
            durationMinutes = :durationMinutes,
            notes = :notes
        WHERE id = :sessionId AND status = 'ACTIVE'
        """
    )
    suspend fun completeActiveSession(
        sessionId: Long,
        completedAt: Long,
        durationMinutes: Int,
        notes: String
    ): Int

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("SELECT COUNT(*) FROM workout_sessions")
    suspend fun getSessionCount(): Int

    @Query("SELECT * FROM workout_sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSessionsInRange(startDate: Long, endDate: Long): Flow<List<WorkoutSession>>
}
