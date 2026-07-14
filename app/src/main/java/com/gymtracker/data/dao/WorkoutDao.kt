package com.gymtracker.data.dao

import androidx.room.*
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

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

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertSet(set: WorkoutSet): Long

    @Insert
    suspend fun insertSets(sets: List<WorkoutSet>)

    @Update
    suspend fun updateSet(set: WorkoutSet)

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Delete
    suspend fun deleteSet(set: WorkoutSet)

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("SELECT COUNT(*) FROM workout_sessions")
    suspend fun getSessionCount(): Int

    @Query("SELECT * FROM workout_sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSessionsInRange(startDate: Long, endDate: Long): Flow<List<WorkoutSession>>
}
