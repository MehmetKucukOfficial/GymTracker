package com.gymtracker.data.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
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
}
