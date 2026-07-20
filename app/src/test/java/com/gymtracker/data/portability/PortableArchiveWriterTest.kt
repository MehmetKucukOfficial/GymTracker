package com.gymtracker.data.portability

import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.entities.FoodEntry
import com.gymtracker.data.entities.FoodItem
import com.gymtracker.data.entities.MealType
import com.gymtracker.data.entities.NutritionGoal
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDayExercise
import com.gymtracker.data.entities.ProgramDifficulty
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.entities.SeedMetadata
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.data.entities.WorkoutStatus
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class PortableArchiveWriterTest {
    @Test
    fun write_createsVersionedDataAndManagedMediaWithoutLeakingDevicePaths() {
        val mainBytes = "main-photo".encodeToByteArray()
        val thumbnailBytes = "thumbnail".encodeToByteArray()
        val snapshot = sampleSnapshot()
        val writer = PortableArchiveWriter { uri ->
            when (uri) {
                MANAGED_MAIN_URI -> ByteArrayInputStream(mainBytes)
                MANAGED_THUMB_URI -> ByteArrayInputStream(thumbnailBytes)
                else -> null
            }
        }
        val archive = PortableArchive(
            exportedAtEpochMillis = 1_721_493_600_000,
            appVersionName = "1.0",
            settings = PortableSettings("DARK"),
            data = snapshot.toPortableData()
        )

        val output = ByteArrayOutputStream()
        val summary = writer.write(archive, snapshot, output)
        val entries = unzip(output.toByteArray())
        val decoded = Json.decodeFromString<PortableArchive>(
            requireNotNull(entries[PORTABLE_DATA_ENTRY]).decodeToString()
        )

        assertEquals(PORTABLE_ARCHIVE_FORMAT, decoded.format)
        assertEquals(PORTABLE_ARCHIVE_VERSION, decoded.formatVersion)
        assertEquals(PORTABLE_DATABASE_SCHEMA_VERSION, decoded.databaseSchemaVersion)
        assertEquals("DARK", decoded.settings.themeMode)
        assertEquals(1, summary.includedManagedPhotos)
        assertEquals(1, summary.metadataOnlyLegacyPhotos)
        assertEquals(2, summary.mediaEntries)
        assertArrayEquals(mainBytes, entries[photoMainAssetPath(10)])
        assertArrayEquals(thumbnailBytes, entries[photoThumbnailAssetPath(10)])
        assertNull(decoded.data.progressPhotos.single { it.id == 11L }.mainAssetPath)
        assertFalse(requireNotNull(entries[PORTABLE_DATA_ENTRY]).decodeToString().contains("file:///"))
    }

    @Test
    fun write_failsTheArchiveWhenManagedMediaIsMissing() {
        val snapshot = sampleSnapshot()
        val writer = PortableArchiveWriter { null }
        val archive = PortableArchive(
            exportedAtEpochMillis = 1,
            appVersionName = "1.0",
            settings = PortableSettings("SYSTEM"),
            data = snapshot.toPortableData()
        )

        val error = assertThrows(MissingManagedPhotoException::class.java) {
            writer.write(archive, snapshot, ByteArrayOutputStream())
        }

        assertEquals(10L, error.photoId)
        assertEquals(photoMainAssetPath(10), error.assetPath)
    }

    private fun sampleSnapshot(): DatabaseSnapshot = DatabaseSnapshot(
        workoutSessions = listOf(
            WorkoutSession(
                id = 1,
                name = "Strength",
                date = 100,
                durationMinutes = 45,
                notes = "Complete",
                status = WorkoutStatus.COMPLETED,
                completedAt = 200
            )
        ),
        workoutSets = listOf(
            WorkoutSet(
                id = 2,
                sessionId = 1,
                exerciseName = "Squat",
                setNumber = 1,
                reps = 5,
                weightKg = 100f,
                exerciseId = "squat",
                targetReps = "5",
                exerciseOrder = 0
            )
        ),
        bodyMeasurements = listOf(BodyMeasurement(id = 3, date = 300, weightKg = 80f)),
        foodItems = listOf(
            FoodItem(
                id = 4,
                name = "Rice",
                caloriesPer100g = 130f,
                proteinPer100g = 2.7f,
                carbsPer100g = 28f,
                fatPer100g = 0.3f
            )
        ),
        foodEntries = listOf(
            FoodEntry(
                id = 5,
                date = 400,
                foodItemId = 4,
                foodName = "Rice",
                amountGrams = 100f,
                calories = 130f,
                protein = 2.7f,
                carbs = 28f,
                fat = 0.3f,
                mealType = MealType.LUNCH
            )
        ),
        progressPhotos = listOf(
            ProgressPhoto(
                id = 10,
                date = 500,
                photoUri = MANAGED_MAIN_URI,
                thumbnailUri = MANAGED_THUMB_URI,
                storageVersion = 2,
                mimeType = "image/jpeg",
                sizeBytes = 10,
                widthPx = 100,
                heightPx = 200,
                angle = PhotoAngle.FRONT
            ),
            ProgressPhoto(
                id = 11,
                date = 600,
                photoUri = "content://legacy/photo",
                storageVersion = 0,
                angle = PhotoAngle.BACK
            )
        ),
        programs = listOf(
            Program(
                id = 6,
                name = "Plan",
                description = "Description",
                durationWeeks = 4,
                daysPerWeek = 3,
                difficulty = ProgramDifficulty.BEGINNER
            )
        ),
        programWorkouts = listOf(
            ProgramWorkout(
                id = 7,
                programId = 6,
                weekNumber = 1,
                dayNumber = 1,
                dayName = "Day"
            )
        ),
        programDayExercises = listOf(
            ProgramDayExercise(
                id = 8,
                programWorkoutId = 7,
                exerciseId = "squat",
                exerciseName = "Squat",
                position = 0,
                targetSets = 3,
                targetReps = "5",
                restSeconds = 120
            )
        ),
        nutritionGoals = listOf(NutritionGoal()),
        seedMetadata = listOf(SeedMetadata("programs", 1))
    )

    private fun unzip(bytes: ByteArray): Map<String, ByteArray> {
        val entries = linkedMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes()
                zip.closeEntry()
            }
        }
        return entries
    }

    private companion object {
        const val MANAGED_MAIN_URI = "file:///managed/main.jpg"
        const val MANAGED_THUMB_URI = "file:///managed/thumb.jpg"
    }
}
