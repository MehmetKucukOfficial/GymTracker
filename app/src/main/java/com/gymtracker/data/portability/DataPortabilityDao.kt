package com.gymtracker.data.portability

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutSessions(items: List<WorkoutSession>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutSets(items: List<WorkoutSet>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBodyMeasurements(items: List<BodyMeasurement>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFoodItems(items: List<FoodItem>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFoodEntries(items: List<FoodEntry>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProgressPhotos(items: List<ProgressPhoto>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPrograms(items: List<Program>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProgramWorkouts(items: List<ProgramWorkout>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProgramDayExercises(items: List<ProgramDayExercise>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNutritionGoals(items: List<NutritionGoal>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSeedMetadata(items: List<SeedMetadata>)

    @Query("DELETE FROM workout_sets")
    suspend fun deleteWorkoutSets()

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteWorkoutSessions()

    @Query("DELETE FROM body_measurements")
    suspend fun deleteBodyMeasurements()

    @Query("DELETE FROM food_entries")
    suspend fun deleteFoodEntries()

    @Query("DELETE FROM food_items")
    suspend fun deleteFoodItems()

    @Query("DELETE FROM progress_photos")
    suspend fun deleteProgressPhotos()

    @Query("DELETE FROM program_day_exercises")
    suspend fun deleteProgramDayExercises()

    @Query("DELETE FROM program_workouts")
    suspend fun deleteProgramWorkouts()

    @Query("DELETE FROM programs")
    suspend fun deletePrograms()

    @Query("DELETE FROM nutrition_goals")
    suspend fun deleteNutritionGoals()

    @Query("DELETE FROM seed_metadata")
    suspend fun deleteSeedMetadata()

    @Transaction
    suspend fun replaceAll(snapshot: DatabaseSnapshot) {
        deleteWorkoutSets()
        deleteWorkoutSessions()
        deleteBodyMeasurements()
        deleteFoodEntries()
        deleteFoodItems()
        deleteProgressPhotos()
        deleteProgramDayExercises()
        deleteProgramWorkouts()
        deletePrograms()
        deleteNutritionGoals()
        deleteSeedMetadata()

        insertWorkoutSessions(snapshot.workoutSessions)
        insertWorkoutSets(snapshot.workoutSets)
        insertBodyMeasurements(snapshot.bodyMeasurements)
        insertFoodItems(snapshot.foodItems)
        insertFoodEntries(snapshot.foodEntries)
        insertProgressPhotos(snapshot.progressPhotos)
        insertPrograms(snapshot.programs)
        insertProgramWorkouts(snapshot.programWorkouts)
        insertProgramDayExercises(snapshot.programDayExercises)
        insertNutritionGoals(snapshot.nutritionGoals)
        insertSeedMetadata(snapshot.seedMetadata)
    }
}

suspend fun DataPortabilityDao.readSnapshot(): DatabaseSnapshot = DatabaseSnapshot(
    workoutSessions = workoutSessions(),
    workoutSets = workoutSets(),
    bodyMeasurements = bodyMeasurements(),
    foodItems = foodItems(),
    foodEntries = foodEntries(),
    progressPhotos = progressPhotos(),
    programs = programs(),
    programWorkouts = programWorkouts(),
    programDayExercises = programDayExercises(),
    nutritionGoals = nutritionGoals(),
    seedMetadata = seedMetadata()
)

fun DatabaseSnapshot.totalRecords(): Int =
    workoutSessions.size +
        workoutSets.size +
        bodyMeasurements.size +
        foodItems.size +
        foodEntries.size +
        progressPhotos.size +
        programs.size +
        programWorkouts.size +
        programDayExercises.size +
        nutritionGoals.size +
        seedMetadata.size

fun emptyDatabaseSnapshot(): DatabaseSnapshot = DatabaseSnapshot(
    workoutSessions = emptyList(),
    workoutSets = emptyList(),
    bodyMeasurements = emptyList(),
    foodItems = emptyList(),
    foodEntries = emptyList(),
    progressPhotos = emptyList(),
    programs = emptyList(),
    programWorkouts = emptyList(),
    programDayExercises = emptyList(),
    nutritionGoals = emptyList(),
    seedMetadata = emptyList()
)
