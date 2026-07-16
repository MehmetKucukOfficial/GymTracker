package com.gymtracker.data.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.data.entities.WorkoutStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: WorkoutDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.workoutDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getSetsFromSessionsSince_returnsOnlySetsFromRecentSessions() = runBlocking {
        val cutoff = 1_000L
        val oldSessionId = dao.insertSession(WorkoutSession(name = "Old", date = cutoff - 1))
        val recentSessionId = dao.insertSession(WorkoutSession(name = "Recent", date = cutoff))
        dao.insertSet(WorkoutSet(sessionId = oldSessionId, exerciseName = "Bench Press", setNumber = 1, reps = 8, weightKg = 60f))
        dao.insertSet(WorkoutSet(sessionId = recentSessionId, exerciseName = "Squat", setNumber = 1, reps = 5, weightKg = 100f))

        val sets = dao.getSetsFromSessionsSince(cutoff)

        assertEquals(listOf("Squat"), sets.map { it.exerciseName })
    }

    @Test
    fun deleteSession_cascadesToItsSets() = runBlocking {
        val session = WorkoutSession(name = "To delete", date = 1_000L)
        val sessionId = dao.insertSession(session)
        dao.insertSet(WorkoutSet(sessionId = sessionId, exerciseName = "Bench Press", setNumber = 1, reps = 8, weightKg = 60f))

        dao.deleteSession(session.copy(id = sessionId))

        assertEquals(emptyList<WorkoutSet>(), dao.getSetsBySessionSync(sessionId))
    }

    @Test
    fun observeCompletedSetsForLoadSince_excludesActiveIncompleteAndOldSets() = runBlocking {
        val cutoff = 1_000L
        val activeId = dao.insertSession(WorkoutSession(name = "Active", date = cutoff + 1))
        val completedId = dao.insertSession(
            WorkoutSession(
                name = "Completed",
                date = cutoff - 100,
                status = WorkoutStatus.COMPLETED,
                completedAt = cutoff + 2
            )
        )
        val oldCompletedId = dao.insertSession(
            WorkoutSession(
                name = "Old",
                date = cutoff - 2,
                status = WorkoutStatus.COMPLETED,
                completedAt = cutoff - 1
            )
        )
        dao.insertSet(WorkoutSet(sessionId = activeId, exerciseName = "Active set", setNumber = 1, reps = 8, weightKg = 50f))
        dao.insertSet(WorkoutSet(sessionId = completedId, exerciseName = "Bench Press", setNumber = 1, reps = 8, weightKg = 60f))
        dao.insertSet(WorkoutSet(sessionId = completedId, exerciseName = "Incomplete", setNumber = 2, reps = 8, weightKg = 60f, isCompleted = false))
        dao.insertSet(WorkoutSet(sessionId = oldCompletedId, exerciseName = "Old set", setNumber = 1, reps = 8, weightKg = 60f))

        val sets = dao.observeCompletedSetsForLoadSince(cutoff).first()

        assertEquals(listOf("Bench Press"), sets.map { it.exerciseName })
        assertEquals(cutoff + 2, sets.single().completedAt)
    }
}
