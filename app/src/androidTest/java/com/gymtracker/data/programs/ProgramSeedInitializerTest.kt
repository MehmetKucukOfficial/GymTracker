package com.gymtracker.data.programs

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDifficulty
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgramSeedInitializerTest {

    private lateinit var database: AppDatabase
    private lateinit var initializer: ProgramSeedInitializer

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        initializer = ProgramSeedInitializer(database, database.programDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedIfNeeded_isIdempotentAcrossRepeatedAndConcurrentCalls() = runBlocking {
        initializer.seedIfNeeded()
        initializer.seedIfNeeded()
        coroutineScope {
            List(10) { async { initializer.seedIfNeeded() } }.awaitAll()
        }

        val dao = database.programDao()
        assertEquals(5, dao.getProgramCount())
        assertEquals(23, dao.getProgramWorkoutCount())
        assertEquals(108, dao.getProgramDayExerciseCount())
        assertNotNull(dao.getSeedMetadata("default-programs-v1"))
    }

    @Test
    fun seedIfNeeded_doesNotDuplicateExistingLegacyPrograms() = runBlocking {
        val dao = database.programDao()
        dao.insertProgram(
            Program(
                name = "Existing custom program",
                description = "Legacy data",
                durationWeeks = 4,
                daysPerWeek = 3,
                difficulty = ProgramDifficulty.BEGINNER
            )
        )

        initializer.seedIfNeeded()

        assertEquals(1, dao.getProgramCount())
        assertEquals(0, dao.getProgramWorkoutCount())
        assertEquals(0, dao.getProgramDayExerciseCount())
        assertNotNull(dao.getSeedMetadata("default-programs-v1"))
    }
}
