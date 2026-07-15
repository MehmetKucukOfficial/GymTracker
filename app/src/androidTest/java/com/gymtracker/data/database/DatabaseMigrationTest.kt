package com.gymtracker.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate3To7_preservesRowsAndMarksLegacyData() {
        migrationHelper.createDatabase(TEST_DATABASE, 3).apply {
            execSQL(
                """
                INSERT INTO workout_sessions
                    (id, name, date, durationMinutes, notes)
                VALUES
                    (1, 'Baseline session', 1000, 45, 'must survive')
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO progress_photos
                    (id, date, photoUri, weightKg, notes, angle)
                VALUES
                    (1, 1000, 'content://legacy/photo', 80.5, 'legacy', 'FRONT')
                """.trimIndent()
            )
            close()
        }

        migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE,
            7,
            true,
            *DatabaseMigrations.ALL
        ).close()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = androidx.room.Room.databaseBuilder(context, AppDatabase::class.java, TEST_DATABASE)
            .addMigrations(*DatabaseMigrations.ALL)
            .build()
        migrationHelper.closeWhenFinished(database)

        val session = runBlocking { database.workoutDao().getSessionById(1) }

        assertEquals("Baseline session", session?.name)
        assertEquals(45, session?.durationMinutes)
        assertEquals("must survive", session?.notes)
        assertEquals(com.gymtracker.data.entities.WorkoutStatus.COMPLETED, session?.status)
        assertEquals(2_701_000L, session?.completedAt)
        assertEquals(null, runBlocking { database.programDao().getSeedMetadata("default-programs-v1") })
        val photo = runBlocking { database.progressPhotoDao().getAllSync().single() }
        assertEquals("content://legacy/photo", photo.photoUri)
        assertEquals("image/*", photo.mimeType)
        assertEquals(0L, photo.sizeBytes)
        assertEquals(0, photo.storageVersion)
    }

    @Test
    fun migrate6To7_normalizesProgramExercisesAndPreservesLegacyPayload() {
        val legacyJson = """[{"name":"Bench Press","sets":3,"reps":"8-10","rest":120}]"""
        migrationHelper.createDatabase(TEST_DATABASE, 6).apply {
            execSQL(
                """
                INSERT INTO programs
                    (id, name, description, durationWeeks, daysPerWeek, difficulty,
                     isPaid, price, isPurchased, coverImageUrl, creatorName)
                VALUES (1, 'Legacy plan', 'Preserve me', 8, 3, 'BEGINNER', 0, 0, 0, '', 'Coach')
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO program_workouts
                    (id, programId, weekNumber, dayNumber, dayName, exercises)
                VALUES (10, 1, 1, 1, 'Day A', ?)
                """.trimIndent(),
                arrayOf(legacyJson)
            )
            execSQL(
                """
                INSERT INTO program_workouts
                    (id, programId, weekNumber, dayNumber, dayName, exercises)
                VALUES (11, 1, 1, 2, 'Corrupt day', 'not-json')
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO workout_sessions
                    (id, name, date, durationMinutes, notes, status, completedAt)
                VALUES (20, 'Existing workout', 1000, 0, '', 'ACTIVE', NULL)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO workout_sets
                    (id, sessionId, exerciseName, setNumber, reps, weightKg, isCompleted, notes)
                VALUES (30, 20, 'Bench Press', 1, 8, 60, 1, '')
                """.trimIndent()
            )
            close()
        }

        migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE,
            7,
            true,
            *DatabaseMigrations.ALL
        ).close()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = androidx.room.Room.databaseBuilder(context, AppDatabase::class.java, TEST_DATABASE)
            .addMigrations(*DatabaseMigrations.ALL)
            .build()
        migrationHelper.closeWhenFinished(database)

        val exercise = runBlocking { database.programDao().getDayExercises(10).single() }
        assertEquals("exercise.bench-press", exercise.exerciseId)
        assertEquals("Bench Press", exercise.exerciseName)
        assertEquals(3, exercise.targetSets)
        assertEquals("8-10", exercise.targetReps)
        assertEquals(120, exercise.restSeconds)
        val legacyWorkout = runBlocking { database.programDao().getWorkout(1, 10) }
        assertEquals(legacyJson, legacyWorkout?.exercises)
        assertEquals(emptyList<com.gymtracker.data.entities.ProgramDayExercise>(), runBlocking {
            database.programDao().getDayExercises(11)
        })
        assertEquals("not-json", runBlocking { database.programDao().getWorkout(1, 11) }?.exercises)
        val existingSet = runBlocking { database.workoutDao().getSetsBySessionSync(20).single() }
        assertEquals(null, existingSet.exerciseId)
        assertEquals(null, existingSet.targetReps)
        assertEquals(0, existingSet.exerciseOrder)
    }

    private companion object {
        const val TEST_DATABASE = "migration-test"
    }
}
