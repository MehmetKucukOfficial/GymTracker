package com.gymtracker.data.portability

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Process
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.photos.PhotoRebootAcceptance
import com.gymtracker.data.settings.ThemeMode
import dagger.hilt.EntryPoints
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@PhotoRebootAcceptance
@RunWith(AndroidJUnit4::class)
class RestoreProcessKillSeedTest {
    @Test
    fun seedCurrentStateAndPortableArchive() = runBlocking {
        val fixture = RestoreProcessKillFixture()
        fixture.markerFile.delete()
        fixture.archiveFile.delete()
        fixture.photoStorage.reconcile(emptySet())

        val originalSnapshot = fixture.database.withTransaction {
            fixture.dao.readSnapshot()
        }
        val targetStored = fixture.photoStorage.store(
            ByteArrayInputStream(fixture.createJpeg(Color.rgb(150, 45, 55))),
            "image/jpeg"
        )
        val targetSnapshot = originalSnapshot.copy(
            workoutSessions = listOf(
                WorkoutSession(
                    id = RestoreProcessKillFixture.TARGET_SESSION_ID,
                    name = "Archive state",
                    date = 200L
                )
            ),
            workoutSets = emptyList(),
            progressPhotos = listOf(
                fixture.progressPhoto(
                    id = RestoreProcessKillFixture.TARGET_PHOTO_ID,
                    stored = targetStored,
                    date = 200L
                )
            )
        )
        fixture.database.withTransaction { fixture.dao.replaceAll(targetSnapshot) }
        fixture.settingsRepository.setThemeMode(ThemeMode.DARK)

        val export = fixture.dataExportRepository.exportTo(Uri.fromFile(fixture.archiveFile))
        assertTrue("Target archive could not be exported: $export", export.isSuccess)

        val currentStored = fixture.photoStorage.store(
            ByteArrayInputStream(fixture.createJpeg(Color.rgb(35, 95, 155))),
            "image/jpeg"
        )
        val currentSnapshot = originalSnapshot.copy(
            workoutSessions = listOf(
                WorkoutSession(
                    id = RestoreProcessKillFixture.CURRENT_SESSION_ID,
                    name = "Current state",
                    date = 100L
                )
            ),
            workoutSets = emptyList(),
            progressPhotos = listOf(
                fixture.progressPhoto(
                    id = RestoreProcessKillFixture.CURRENT_PHOTO_ID,
                    stored = currentStored,
                    date = 100L
                )
            )
        )
        fixture.database.withTransaction { fixture.dao.replaceAll(currentSnapshot) }
        fixture.settingsRepository.setThemeMode(ThemeMode.LIGHT)
        fixture.photoStorage.deleteManaged(targetStored.uri)
        fixture.photoStorage.deleteManaged(targetStored.thumbnailUri)

        assertTrue(fixture.archiveFile.isFile)
        assertEquals(
            listOf(RestoreProcessKillFixture.CURRENT_SESSION_ID),
            fixture.dao.workoutSessions().map { session -> session.id }
        )
        assertEquals(ThemeMode.LIGHT, fixture.settingsRepository.settings.first().themeMode)
        assertTrue(fixture.managedFile(currentStored.uri).isFile)
        assertTrue(fixture.managedFile(currentStored.thumbnailUri).isFile)
    }
}

@PhotoRebootAcceptance
@RunWith(AndroidJUnit4::class)
class RestoreProcessKillTriggerTest {
    @Test
    fun blockAfterMediaPublicationAndBeforeRoomCommit(): Unit = runBlocking {
        val fixture = RestoreProcessKillFixture()
        assertTrue("Seed archive is missing", fixture.archiveFile.isFile)
        assertEquals(
            listOf(RestoreProcessKillFixture.CURRENT_SESSION_ID),
            fixture.dao.workoutSessions().map { session -> session.id }
        )

        val repository = DataRestoreRepository(
            context = fixture.context,
            database = fixture.database,
            dao = fixture.dao,
            settingsRepository = fixture.settingsRepository,
            archiveReader = fixture.archiveReader,
            photoStorage = fixture.photoStorage,
            availableBytes = Long.MAX_VALUE,
            onReadyToCommit = {
                fixture.markerFile.writeText("pid=${Process.myPid()}")
                awaitCancellation()
            }
        )

        val unexpected = repository.restoreFrom(Uri.fromFile(fixture.archiveFile))
        throw AssertionError("Restore crossed the pre-commit kill checkpoint: $unexpected")
    }
}

@PhotoRebootAcceptance
@RunWith(AndroidJUnit4::class)
class RestoreProcessKillVerifyTest {
    @Test
    fun newProcessKeepsCurrentStateAndCleansInterruptedArtifacts(): Unit = runBlocking {
        val fixture = RestoreProcessKillFixture()
        val killedPid = fixture.markerFile.readText()
            .substringAfter("pid=")
            .trim()
            .toInt()
        assertTrue("Verification reused the killed process", killedPid != Process.myPid())

        assertEquals(
            listOf(RestoreProcessKillFixture.CURRENT_SESSION_ID),
            fixture.dao.workoutSessions().map { session -> session.id }
        )
        assertEquals(ThemeMode.LIGHT, fixture.settingsRepository.settings.first().themeMode)
        val currentPhoto = fixture.dao.progressPhotos().single()
        assertEquals(RestoreProcessKillFixture.CURRENT_PHOTO_ID, currentPhoto.id)
        assertTrue(fixture.managedFile(currentPhoto.photoUri).isFile)
        assertTrue(fixture.managedFile(requireNotNull(currentPhoto.thumbnailUri)).isFile)

        val restoreRoot = File(fixture.context.cacheDir, "data_restore")
        assertTrue(
            "The process was not killed while restore staging was live",
            restoreRoot.walkTopDown().any { file -> file.isFile }
        )

        val knownUris = setOf(currentPhoto.photoUri, requireNotNull(currentPhoto.thumbnailUri))
        val reconciliation = fixture.photoStorage.reconcile(knownUris)
        assertEquals(2, reconciliation.orphanFilesDeleted)
        assertTrue(reconciliation.missingManagedUris.isEmpty())
        assertEquals(0, reconciliation.orphanFileDeleteFailures)

        val invalidArchive = File(
            requireNotNull(fixture.context.getExternalFilesDir(null)),
            "restore-process-kill-invalid.zip"
        ).apply { writeText("not a zip") }
        val cleanupResult = DataRestoreRepository(
            context = fixture.context,
            database = fixture.database,
            dao = fixture.dao,
            settingsRepository = fixture.settingsRepository,
            archiveReader = fixture.archiveReader,
            photoStorage = fixture.photoStorage,
            availableBytes = Long.MAX_VALUE
        ).restoreFrom(Uri.fromFile(invalidArchive))

        assertTrue(cleanupResult.exceptionOrNull() is CorruptPortableArchiveException)
        assertTrue(restoreRoot.listFiles().orEmpty().isEmpty())
        assertEquals(
            listOf(RestoreProcessKillFixture.CURRENT_SESSION_ID),
            fixture.dao.workoutSessions().map { session -> session.id }
        )
        assertEquals(ThemeMode.LIGHT, fixture.settingsRepository.settings.first().themeMode)
        assertTrue(fixture.archiveFile.delete() || !fixture.archiveFile.exists())
        invalidArchive.delete()
        fixture.markerFile.delete()
    }
}

private class RestoreProcessKillFixture {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val entryPoint = EntryPoints.get(
        context.applicationContext,
        DataPortabilityEntryPoint::class.java
    )
    val database = entryPoint.database()
    val dao = database.dataPortabilityDao()
    val settingsRepository = entryPoint.settingsRepository()
    val dataExportRepository = entryPoint.dataExportRepository()
    val archiveReader = entryPoint.archiveReader()
    val photoStorage = entryPoint.photoFileStorage()
    val archiveFile = File(
        requireNotNull(context.getExternalFilesDir(null)),
        ARCHIVE_FILE_NAME
    )
    val markerFile = File(context.filesDir, MARKER_FILE_NAME)

    fun progressPhoto(
        id: Long,
        stored: com.gymtracker.data.photos.StoredPhoto,
        date: Long
    ) = ProgressPhoto(
        id = id,
        date = date,
        photoUri = stored.uri,
        thumbnailUri = stored.thumbnailUri,
        angle = PhotoAngle.FRONT,
        mimeType = stored.mimeType,
        sizeBytes = stored.sizeBytes,
        storageVersion = 2,
        widthPx = stored.widthPx,
        heightPx = stored.heightPx
    )

    fun managedFile(uri: String): File = File(requireNotNull(Uri.parse(uri).path))

    fun createJpeg(color: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(64, 48, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return ByteArrayOutputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output))
            bitmap.recycle()
            output.toByteArray()
        }
    }

    companion object {
        const val CURRENT_SESSION_ID = 901L
        const val TARGET_SESSION_ID = 902L
        const val CURRENT_PHOTO_ID = 903L
        const val TARGET_PHOTO_ID = 904L
        const val ARCHIVE_FILE_NAME = "restore-process-kill.zip"
        const val MARKER_FILE_NAME = "restore-process-kill-ready"
    }
}
