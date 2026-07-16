package com.gymtracker.data.programs

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgramRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: AppDatabase
    private lateinit var repository: ProgramRepository

    @Before
    fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        ProgramSeedInitializer(database, database.programDao()).seedIfNeeded()
        repository = ProgramRepository(database.programDao(), database.workoutDao(), database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun startWorkout_atomicallyCreatesIncompletePlannedSetsAndResumesExisting() = runBlocking {
        val program = repository.programs.first().first { !it.isPaid }
        val plan = repository.observeWorkoutPlans(program.id).first().first()

        val created = repository.startWorkout(program.id, plan.workout.id, startedAt = 1_000L)
        assertTrue(created is ProgramWorkoutStartResult.Created)
        val expectedSetCount = plan.exercises.sumOf { it.targetSets }
        val sets = database.workoutDao().getSetsBySessionSync(created.sessionId)
        assertEquals(expectedSetCount, sets.size)
        assertTrue(sets.all { !it.isCompleted })
        assertTrue(sets.all { it.reps == 0 && it.targetReps != null && it.exerciseId != null })
        assertEquals(
            plan.orderedExercises.map { it.exerciseId },
            sets.distinctBy { it.exerciseId }.map { it.exerciseId }
        )

        val resumed = repository.startWorkout(program.id, plan.workout.id, startedAt = 2_000L)
        assertTrue(resumed is ProgramWorkoutStartResult.ExistingActive)
        assertEquals(created.sessionId, resumed.sessionId)
        assertEquals(expectedSetCount, database.workoutDao().getSetsBySessionSync(created.sessionId).size)
    }

    @Test
    fun startWorkout_rejectsLockedProgramWithoutCreatingSession() = runBlocking {
        val program = repository.programs.first().first { it.isPaid && !it.isPurchased }
        val plan = repository.observeWorkoutPlans(program.id).first().first()

        try {
            repository.startWorkout(program.id, plan.workout.id)
            fail("Expected locked program to be rejected")
        } catch (_: IllegalStateException) {
            // Expected.
        }

        assertEquals(null, database.workoutDao().getActiveSession())
    }
}
