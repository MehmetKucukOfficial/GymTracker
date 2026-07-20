package com.gymtracker.data.portability

import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PortableArchiveReaderTest {
    @Test
    fun stage_validatesStrictManifestAndExtractsDeclaredMedia() {
        val photo = PortableProgressPhoto(
            id = 7,
            date = 10,
            weightKg = null,
            notes = "",
            angle = "FRONT",
            mimeType = "image/jpeg",
            sizeBytes = 5,
            storageVersion = 2,
            widthPx = 10,
            heightPx = 20,
            mediaState = PortablePhotoMediaState.INCLUDED,
            mainAssetPath = photoMainAssetPath(7),
            thumbnailAssetPath = null
        )
        val archive = archive(emptyData().copy(progressPhotos = listOf(photo)))
        val root = Files.createTempDirectory("portable-reader-").toFile()
        val zip = File(root, "backup.zip")
        val staging = File(root, "staging")
        val media = "photo".encodeToByteArray()
        writeZip(zip, archive, mapOf(photoMainAssetPath(7) to media))

        try {
            PortableArchiveReader().stage(zip, staging).use { result ->
                assertEquals(PORTABLE_ARCHIVE_VERSION, result.archive.formatVersion)
                assertArrayEquals(media, result.mediaFiles.getValue(photoMainAssetPath(7)).readBytes())
            }
            assertEquals(false, staging.exists())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun stage_rejectsUnexpectedOrUnsafeEntries() {
        val archive = archive()
        val root = Files.createTempDirectory("portable-reader-").toFile()
        val zip = File(root, "backup.zip")
        writeZip(zip, archive, mapOf("../escape" to byteArrayOf(1)))

        try {
            assertThrows(CorruptPortableArchiveException::class.java) {
                PortableArchiveReader().stage(zip, File(root, "staging"))
            }
            assertEquals(false, File(root.parentFile, "escape").exists())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun stage_rejectsMediaWhoseDeclaredCrcDoesNotMatchExtractedBytes() {
        val photo = PortableProgressPhoto(
            id = 7,
            date = 10,
            weightKg = null,
            notes = "",
            angle = "FRONT",
            mimeType = "image/jpeg",
            sizeBytes = 5,
            storageVersion = 2,
            widthPx = 10,
            heightPx = 20,
            mediaState = PortablePhotoMediaState.INCLUDED,
            mainAssetPath = photoMainAssetPath(7),
            thumbnailAssetPath = null
        )
        val archive = archive(emptyData().copy(progressPhotos = listOf(photo)))
        val root = Files.createTempDirectory("portable-reader-").toFile()
        val zip = File(root, "backup.zip")
        writeZip(zip, archive, mapOf(photoMainAssetPath(7) to "photo".encodeToByteArray()))
        val bytes = zip.readBytes()
        val mediaCentralDirectory = bytes.lastIndexOfSignature(
            byteArrayOf(0x50, 0x4b, 0x01, 0x02)
        )
        repeat(4) { offset -> bytes[mediaCentralDirectory + 16 + offset] = 0 }
        zip.writeBytes(bytes)

        try {
            assertThrows(CorruptPortableArchiveException::class.java) {
                PortableArchiveReader().stage(zip, File(root, "staging"))
            }
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun validation_rejectsBrokenRelationshipsBeforeDatabaseMutation() {
        val broken = archive(
            emptyData().copy(
                workoutSets = listOf(
                    PortableWorkoutSet(
                        id = 2,
                        sessionId = 99,
                        exerciseName = "Squat",
                        setNumber = 1,
                        reps = 5,
                        weightKg = 100f,
                        isCompleted = true,
                        notes = "",
                        exerciseId = "squat",
                        targetReps = null,
                        exerciseOrder = 0
                    )
                )
            )
        )

        assertThrows(InvalidPortableArchiveException::class.java) {
            broken.validateForRestore()
        }
    }

    @Test
    fun validation_rejectsMoreThanOneActiveWorkout() {
        val active = PortableWorkoutSession(
            id = 1,
            name = "Active",
            date = 10,
            durationMinutes = 0,
            notes = "",
            status = "ACTIVE",
            completedAt = null
        )
        val archive = archive(
            emptyData().copy(
                workoutSessions = listOf(active, active.copy(id = 2))
            )
        )

        assertThrows(InvalidPortableArchiveException::class.java) {
            archive.validateForRestore()
        }
    }

    @Test
    fun databaseMapping_usesPublishedUrisAndKeepsLegacyMetadataExplicit() {
        val managed = PortableProgressPhoto(
            id = 7,
            date = 10,
            weightKg = null,
            notes = "",
            angle = "FRONT",
            mimeType = "image/jpeg",
            sizeBytes = 5,
            storageVersion = 2,
            widthPx = 10,
            heightPx = 20,
            mediaState = PortablePhotoMediaState.INCLUDED,
            mainAssetPath = photoMainAssetPath(7),
            thumbnailAssetPath = null
        )
        val legacy = managed.copy(
            id = 8,
            storageVersion = 0,
            mediaState = PortablePhotoMediaState.LEGACY_METADATA_ONLY,
            mainAssetPath = null
        )
        val archive = archive(emptyData().copy(progressPhotos = listOf(managed, legacy)))

        val snapshot = archive.toDatabaseSnapshot(
            mapOf(7L to RestoredPhotoUris("file:///managed/main.jpg", null))
        )

        assertEquals("file:///managed/main.jpg", snapshot.progressPhotos.first().photoUri)
        assertEquals(2, snapshot.progressPhotos.first().storageVersion)
        assertEquals(0, snapshot.progressPhotos.last().storageVersion)
        assertEquals(
            "gymtracker://restored/legacy-photo/8",
            snapshot.progressPhotos.last().photoUri
        )
    }

    private fun archive(data: PortableData = emptyData()) = PortableArchive(
        exportedAtEpochMillis = 1_721_493_600_000,
        appVersionName = "1.0",
        settings = PortableSettings("DARK"),
        data = data
    )

    private fun emptyData() = PortableData(
        workoutSessions = emptyList(),
        workoutSets = emptyList(),
        bodyMeasurements = emptyList(),
        foodItems = emptyList(),
        foodEntries = emptyList(),
        progressPhotos = emptyList(),
        programs = emptyList(),
        programWorkouts = emptyList(),
        programDayExercises = emptyList(),
        nutritionGoals = emptyList(),
        seedMetadata = emptyList()
    )

    private fun writeZip(
        destination: File,
        archive: PortableArchive,
        extraEntries: Map<String, ByteArray> = emptyMap()
    ) {
        ZipOutputStream(destination.outputStream().buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(PORTABLE_DATA_ENTRY))
            zip.write(Json.encodeToString(archive).encodeToByteArray())
            zip.closeEntry()
            extraEntries.forEach { (path, bytes) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
    }

    private fun ByteArray.lastIndexOfSignature(signature: ByteArray): Int {
        for (index in size - signature.size downTo 0) {
            if (signature.indices.all { offset -> this[index + offset] == signature[offset] }) {
                return index
            }
        }
        error("ZIP central directory signature was not found.")
    }
}
