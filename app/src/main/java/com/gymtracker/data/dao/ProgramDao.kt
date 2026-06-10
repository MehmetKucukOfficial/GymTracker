package com.gymtracker.data.dao

import androidx.room.*
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Query("SELECT * FROM programs ORDER BY isPaid ASC, name ASC")
    fun getAll(): Flow<List<Program>>

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getById(id: Long): Program?

    @Query("SELECT * FROM program_workouts WHERE programId = :programId ORDER BY weekNumber, dayNumber")
    fun getWorkoutsForProgram(programId: Long): Flow<List<ProgramWorkout>>

    @Insert
    suspend fun insertProgram(program: Program): Long

    @Insert
    suspend fun insertWorkouts(workouts: List<ProgramWorkout>)

    @Update
    suspend fun updateProgram(program: Program)

    @Delete
    suspend fun deleteProgram(program: Program)
}
