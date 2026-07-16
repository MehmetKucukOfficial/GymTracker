package com.gymtracker.data.dao

import androidx.room.*
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDayExercise
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.data.entities.SeedMetadata
import com.gymtracker.data.programs.ProgramWorkoutPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Query("SELECT * FROM programs ORDER BY isPaid ASC, name ASC")
    fun getAll(): Flow<List<Program>>

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getById(id: Long): Program?

    @Query("SELECT * FROM programs WHERE id = :id")
    fun observeById(id: Long): Flow<Program?>

    @Transaction
    @Query("SELECT * FROM program_workouts WHERE programId = :programId ORDER BY weekNumber, dayNumber")
    fun observeWorkoutPlans(programId: Long): Flow<List<ProgramWorkoutPlan>>

    @Query("SELECT * FROM program_workouts WHERE id = :workoutId AND programId = :programId")
    suspend fun getWorkout(programId: Long, workoutId: Long): ProgramWorkout?

    @Query("SELECT * FROM program_day_exercises WHERE programWorkoutId = :workoutId ORDER BY position")
    suspend fun getDayExercises(workoutId: Long): List<ProgramDayExercise>

    @Insert
    suspend fun insertProgram(program: Program): Long

    @Insert
    suspend fun insertWorkout(workout: ProgramWorkout): Long

    @Insert
    suspend fun insertDayExercises(exercises: List<ProgramDayExercise>)

    @Query("SELECT COUNT(*) FROM programs")
    suspend fun getProgramCount(): Int

    @Query("SELECT COUNT(*) FROM program_workouts")
    suspend fun getProgramWorkoutCount(): Int

    @Query("SELECT COUNT(*) FROM program_day_exercises")
    suspend fun getProgramDayExerciseCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeedMetadata(metadata: SeedMetadata): Long

    @Query("SELECT * FROM seed_metadata WHERE seedKey = :seedKey")
    suspend fun getSeedMetadata(seedKey: String): SeedMetadata?

    @Update
    suspend fun updateProgram(program: Program)

    @Delete
    suspend fun deleteProgram(program: Program)
}
