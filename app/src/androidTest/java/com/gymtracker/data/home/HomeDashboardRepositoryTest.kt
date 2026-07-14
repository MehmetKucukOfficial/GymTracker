package com.gymtracker.data.home

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.MealType
import com.gymtracker.data.entities.WorkoutSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeDashboardRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: AppDatabase
    private lateinit var repository: HomeDashboardRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HomeDashboardRepository(
            database.workoutDao(),
            database.measurementDao(),
            database.nutritionDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun dashboard_reactsWhenEachSourceChanges() = runBlocking {
        val dayStart = 1_000_000L
        val nextDayStart = dayStart + 86_400_000L
        val emissions = Channel<HomeDashboardData>(Channel.UNLIMITED)
        val collection = launch {
            repository.observeDashboard(
                weekStartMillis = dayStart - 6 * 86_400_000L,
                dayStartMillis = dayStart,
                nextDayStartMillis = nextDayStart
            ).collect(emissions::send)
        }

        assertEquals(HomeDashboardData(), emissions.receiveMatching { it.totalWorkouts == 0 })

        database.workoutDao().insertSession(
            WorkoutSession(name = "Reactive workout", date = dayStart + 1_000)
        )
        val workoutUpdate = emissions.receiveMatching { it.totalWorkouts == 1 }
        assertEquals(1, workoutUpdate.weekWorkouts)
        assertEquals("Reactive workout", workoutUpdate.lastWorkoutName)

        database.measurementDao().insert(
            BodyMeasurement(date = dayStart + 2_000, weightKg = 81.5f)
        )
        assertEquals(81.5f, emissions.receiveMatching { it.latestWeight != null }.latestWeight)

        database.nutritionDao().insertEntry(
            FoodEntry(
                date = dayStart + 3_000,
                foodItemId = 0,
                foodName = "Test meal",
                amountGrams = 100f,
                calories = 450f,
                protein = 30f,
                carbs = 40f,
                fat = 15f,
                mealType = MealType.LUNCH
            )
        )
        val nutritionUpdate = emissions.receiveMatching { it.todayCalories == 450 }
        assertEquals(450, nutritionUpdate.todayCalories)
        assertEquals(1, nutritionUpdate.todayNutritionEntryCount)

        collection.cancel()
        emissions.close()
        Unit
    }

    @Suppress("UNREACHABLE_CODE")
    private suspend fun Channel<HomeDashboardData>.receiveMatching(
        predicate: (HomeDashboardData) -> Boolean
    ): HomeDashboardData = withTimeout(5_000) {
        while (true) {
            val value = receive()
            if (predicate(value)) return@withTimeout value
        }
        error("unreachable")
    }
}
