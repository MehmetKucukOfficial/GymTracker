package com.gymtracker.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.MealType
import com.gymtracker.data.measurements.MeasurementChartPeriod
import com.gymtracker.data.measurements.MeasurementRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MutationContractTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun nutritionUpdateAndUndoPreserveIdentityAndSnapshot() = runBlocking {
        val dao = database.nutritionDao()
        val id = dao.insertEntry(
            FoodEntry(
                date = 1_000,
                foodItemId = 4,
                foodName = "Historical snapshot",
                amountGrams = 100f,
                calories = 200f,
                protein = 10f,
                carbs = 30f,
                fat = 4f,
                mealType = MealType.LUNCH
            )
        )
        val original = dao.getEntriesForDay(0, 2_000).first().single()
        dao.updateEntry(original.copy(amountGrams = 150f, calories = 300f))
        val edited = dao.getEntriesForDay(0, 2_000).first().single()
        dao.deleteEntry(edited)
        dao.insertEntry(edited)
        val restored = dao.getEntriesForDay(0, 2_000).first().single()

        assertEquals(id, restored.id)
        assertEquals("Historical snapshot", restored.foodName)
        assertEquals(150f, restored.amountGrams)
        assertEquals(300f, restored.calories)
    }

    @Test
    fun measurementRepositoryUsesPeriodQueryAndUndoPreservesIdentity() = runBlocking {
        val clock = Clock.fixed(
            Instant.parse("2026-07-15T12:00:00Z"),
            ZoneId.of("Europe/Istanbul")
        )
        val repository = MeasurementRepository(database.measurementDao(), clock)
        repository.save(
            BodyMeasurement(
                id = 7,
                date = Instant.parse("2026-07-10T10:00:00Z").toEpochMilli(),
                weightKg = 81f
            )
        )
        repository.save(
            BodyMeasurement(
                id = 8,
                date = Instant.parse("2026-06-01T10:00:00Z").toEpochMilli(),
                weightKg = 83f
            )
        )

        val recent = repository.observeChart(MeasurementChartPeriod.SEVEN_DAYS).first()
        assertEquals(listOf(7L), recent.map(BodyMeasurement::id))

        val deleted = recent.single()
        repository.delete(deleted)
        repository.save(deleted)
        assertEquals(7L, repository.observeChart(MeasurementChartPeriod.SEVEN_DAYS).first().single().id)
    }

    @Test
    fun latestValidWeightFlowSelectsNewestUsableCalibration() = runBlocking {
        val dao = database.measurementDao()
        dao.insert(BodyMeasurement(id = 1, date = 1_000, weightKg = 80f))
        dao.insert(BodyMeasurement(id = 2, date = 2_000, weightKg = null))

        assertEquals(80f, dao.observeLatestWithWeight().first()?.weightKg)

        dao.insert(BodyMeasurement(id = 3, date = 3_000, weightKg = 82.5f))
        assertEquals(82.5f, dao.observeLatestWithWeight().first()?.weightKg)
    }
}
