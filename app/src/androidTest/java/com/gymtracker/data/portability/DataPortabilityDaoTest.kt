package com.gymtracker.data.portability

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.NutritionGoal
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDayExercise
import com.gymtracker.data.entities.ProgramDifficulty
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.entities.SeedMetadata
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataPortabilityDaoTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun snapshotReadsEveryTableInsideOneTransactionInStableOrder() = runTest {
        database.workoutDao().insertSession(WorkoutSession(id = 20, name = "Later"))
        database.workoutDao().insertSession(WorkoutSession(id = 10, name = "Earlier"))
        database.workoutDao().insertSet(
            WorkoutSet(
                id = 30,
                sessionId = 10,
                exerciseName = "Squat",
                setNumber = 1,
                reps = 5,
                weightKg = 100f
            )
        )
        database.measurementDao().insert(BodyMeasurement(id = 40, weightKg = 80f))
        database.nutritionDao().insertFoodItem(
            FoodItem(
                id = 50,
                name = "Rice",
                caloriesPer100g = 130f,
                proteinPer100g = 2f,
                carbsPer100g = 28f,
                fatPer100g = 0f
            )
        )
        database.nutritionDao().insertEntry(
            FoodEntry(
                id = 60,
                date = 1,
                foodItemId = 50,
                foodName = "Rice",
                amountGrams = 100f,
                calories = 130f,
                protein = 2f,
                carbs = 28f,
                fat = 0f
            )
        )
        database.nutritionDao().upsertGoal(NutritionGoal())
        database.progressPhotoDao().insert(
            ProgressPhoto(id = 70, photoUri = "file:///managed.jpg", storageVersion = 2)
        )
        database.programDao().insertProgram(
            Program(
                id = 80,
                name = "Plan",
                description = "Description",
                durationWeeks = 4,
                daysPerWeek = 3,
                difficulty = ProgramDifficulty.BEGINNER
            )
        )
        database.programDao().insertWorkout(
            ProgramWorkout(
                id = 90,
                programId = 80,
                weekNumber = 1,
                dayNumber = 1,
                dayName = "Day"
            )
        )
        database.programDao().insertDayExercises(
            listOf(
                ProgramDayExercise(
                    id = 100,
                    programWorkoutId = 90,
                    exerciseId = "squat",
                    exerciseName = "Squat",
                    position = 0,
                    targetSets = 3,
                    targetReps = "5",
                    restSeconds = 120
                )
            )
        )
        database.programDao().insertSeedMetadata(SeedMetadata("programs", 1))

        val dao = database.dataPortabilityDao()
        val snapshot = database.withTransaction {
            DatabaseSnapshot(
                workoutSessions = dao.workoutSessions(),
                workoutSets = dao.workoutSets(),
                bodyMeasurements = dao.bodyMeasurements(),
                foodItems = dao.foodItems(),
                foodEntries = dao.foodEntries(),
                progressPhotos = dao.progressPhotos(),
                programs = dao.programs(),
                programWorkouts = dao.programWorkouts(),
                programDayExercises = dao.programDayExercises(),
                nutritionGoals = dao.nutritionGoals(),
                seedMetadata = dao.seedMetadata()
            )
        }

        assertEquals(listOf(10L, 20L), snapshot.workoutSessions.map { it.id })
        assertEquals(listOf(30L), snapshot.workoutSets.map { it.id })
        assertEquals(listOf(40L), snapshot.bodyMeasurements.map { it.id })
        assertEquals(listOf(50L), snapshot.foodItems.map { it.id })
        assertEquals(listOf(60L), snapshot.foodEntries.map { it.id })
        assertEquals(listOf(70L), snapshot.progressPhotos.map { it.id })
        assertEquals(listOf(80L), snapshot.programs.map { it.id })
        assertEquals(listOf(90L), snapshot.programWorkouts.map { it.id })
        assertEquals(listOf(100L), snapshot.programDayExercises.map { it.id })
        assertEquals(listOf(1), snapshot.nutritionGoals.map { it.id })
        assertEquals(listOf("programs"), snapshot.seedMetadata.map { it.seedKey })
    }

    @Test
    fun replaceAll_rollsBackDeletesWhenIncomingSnapshotViolatesForeignKeys() = runTest {
        val original = WorkoutSession(id = 10, name = "Current data")
        database.workoutDao().insertSession(original)
        val dao = database.dataPortabilityDao()
        val current = database.withTransaction { dao.readSnapshot() }
        val invalid = current.copy(
            workoutSessions = emptyList(),
            workoutSets = listOf(
                WorkoutSet(
                    id = 30,
                    sessionId = 999,
                    exerciseName = "Missing session",
                    setNumber = 1,
                    reps = 5,
                    weightKg = 100f
                )
            )
        )

        val failure = runCatching { dao.replaceAll(invalid) }.exceptionOrNull()

        assertEquals(true, failure != null)
        assertEquals(listOf(original), dao.workoutSessions())
        assertEquals(emptyList<WorkoutSet>(), dao.workoutSets())
    }
}
