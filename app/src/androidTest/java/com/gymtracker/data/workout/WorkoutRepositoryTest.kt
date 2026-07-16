package com.gymtracker.data.workout

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.data.entities.WorkoutStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: AppDatabase
    private lateinit var repository: WorkoutRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WorkoutRepository(database.workoutDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun concurrentStarts_resumeTheSingleActiveSession() = runBlocking {
        val ids = coroutineScope {
            List(8) { index ->
                async {
                    repository.startOrResumeSession(
                        name = "Session $index",
                        startedAt = 1_000L + index
                    )
                }
            }.awaitAll()
        }

        assertEquals(1, ids.distinct().size)
        assertEquals(ids.singleDistinct(), repository.activeSession.first()?.id)
        assertEquals(1, repository.sessions.first().count { it.status == WorkoutStatus.ACTIVE })
    }

    @Test
    fun completedSession_isReadOnlyAndCannotBeCompletedTwice() = runBlocking {
        val sessionId = repository.startOrResumeSession("Read only test", startedAt = 1_000L)
        val setId = repository.addSet(
            WorkoutSet(
                sessionId = sessionId,
                exerciseName = "Bench Press",
                setNumber = 1,
                reps = 8,
                weightKg = 60f
            )
        )

        repository.finishSession(
            sessionId = sessionId,
            durationMinutes = 45,
            notes = "Done",
            completedAt = 5_000L
        )

        val completed = repository.observeSession(sessionId).first()
        assertNotNull(completed)
        assertEquals(WorkoutStatus.COMPLETED, completed?.status)
        assertEquals(5_000L, completed?.completedAt)
        assertEquals(null, repository.activeSession.first())

        assertIllegalState {
            repository.addSet(
                WorkoutSet(
                    sessionId = sessionId,
                    exerciseName = "Squat",
                    setNumber = 2,
                    reps = 5,
                    weightKg = 100f
                )
            )
        }
        assertIllegalState {
            repository.deleteSet(
                WorkoutSet(
                    id = setId,
                    sessionId = sessionId,
                    exerciseName = "Bench Press",
                    setNumber = 1,
                    reps = 8,
                    weightKg = 60f
                )
            )
        }
        assertIllegalState {
            repository.finishSession(sessionId, 50, "Again", completedAt = 6_000L)
        }
        assertTrue(repository.observeSets(sessionId).first().isNotEmpty())
    }

    @Test
    fun plannedSet_mustBeCompletedBeforeSessionCanFinish() = runBlocking {
        val sessionId = repository.startOrResumeSession("Planned", startedAt = 1_000L)
        val setId = database.workoutDao().insertSetForActiveSession(
            WorkoutSet(
                sessionId = sessionId,
                exerciseName = "Squat",
                setNumber = 1,
                reps = 0,
                weightKg = 0f,
                isCompleted = false,
                exerciseId = "exercise.squat",
                targetReps = "8-10"
            )
        )

        assertIllegalState {
            repository.finishSession(sessionId, 45, "Too early", completedAt = 2_000L)
        }

        repository.completePlannedSet(setId, reps = 9, weightKg = 80f)
        val completedSet = repository.observeSets(sessionId).first().single()
        assertTrue(completedSet.isCompleted)
        assertEquals(9, completedSet.reps)
        assertEquals(80f, completedSet.weightKg)

        repository.finishSession(sessionId, 45, "Done", completedAt = 3_000L)
        assertEquals(WorkoutStatus.COMPLETED, repository.observeSession(sessionId).first()?.status)
    }

    private suspend fun assertIllegalState(block: suspend () -> Unit) {
        try {
            block()
            fail("Expected IllegalStateException")
        } catch (_: IllegalStateException) {
            // Expected: completed workouts are immutable.
        }
    }

    private fun List<Long>.singleDistinct(): Long = distinct().single()
}
