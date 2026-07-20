package com.gymtracker.data.portability

import androidx.room.Dao
import androidx.room.Query
import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.NutritionGoal
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDayExercise
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.entities.SeedMetadata
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet

@Dao
interface DataPortabilityDao {
    @Query("SELECT * FROM workout_sessions ORDER BY id")
    suspend fun workoutSessions(): List<WorkoutSession>

    @Query("SELECT * FROM workout_sets ORDER BY id")
    suspend fun workoutSets(): List<WorkoutSet>

    @Query("SELECT * FROM body_measurements ORDER BY id")
    suspend fun bodyMeasurements(): List<BodyMeasurement>

    @Query("SELECT * FROM food_items ORDER BY id")
    suspend fun foodItems(): List<FoodItem>

    @Query("SELECT * FROM food_entries ORDER BY id")
    suspend fun foodEntries(): List<FoodEntry>

    @Query("SELECT * FROM progress_photos ORDER BY id")
    suspend fun progressPhotos(): List<ProgressPhoto>

    @Query("SELECT * FROM programs ORDER BY id")
    suspend fun programs(): List<Program>

    @Query("SELECT * FROM program_workouts ORDER BY id")
    suspend fun programWorkouts(): List<ProgramWorkout>

    @Query("SELECT * FROM program_day_exercises ORDER BY id")
    suspend fun programDayExercises(): List<ProgramDayExercise>

    @Query("SELECT * FROM nutrition_goals ORDER BY id")
    suspend fun nutritionGoals(): List<NutritionGoal>

    @Query("SELECT * FROM seed_metadata ORDER BY seedKey")
    suspend fun seedMetadata(): List<SeedMetadata>
}
