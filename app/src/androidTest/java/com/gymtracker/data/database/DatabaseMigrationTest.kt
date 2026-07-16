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
    fun migrate3To6_preservesRowsAndMarksLegacyData() {
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
            6,
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

    private companion object {
        const val TEST_DATABASE = "migration-test"
    }
}
