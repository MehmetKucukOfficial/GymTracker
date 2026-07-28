package com.gymtracker.data.portability

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@PhotoRebootAcceptance
@RunWith(AndroidJUnit4::class)
class AndroidBackupTransportSeedTest {
    @Test
    fun seedDatabaseSettingsAndExcludedManagedPhoto(): Unit = runBlocking {
        val fixture = AndroidBackupTransportFixture()
        fixture.photoStorage.reconcile(emptySet())
        val original = fixture.database.withTransaction { fixture.dao.readSnapshot() }
        val stored = fixture.photoStorage.store(
            ByteArrayInputStream(fixture.createJpeg()),
            "image/jpeg"
        )
        val backupSnapshot = original.copy(
            workoutSessions = listOf(
                WorkoutSession(
                    id = AndroidBackupTransportFixture.SESSION_ID,
                    name = "Android transport state",
                    date = 300L
                )
            ),
            workoutSets = emptyList(),
            progressPhotos = listOf(
                ProgressPhoto(
                    id = AndroidBackupTransportFixture.PHOTO_ID,
                    date = 300L,
                    photoUri = stored.uri,
                    thumbnailUri = stored.thumbnailUri,
                    angle = PhotoAngle.BACK,
                    mimeType = stored.mimeType,
                    sizeBytes = stored.sizeBytes,
                    storageVersion = 2,
                    widthPx = stored.widthPx,
                    heightPx = stored.heightPx
                )
            )
        )
        fixture.database.withTransaction { fixture.dao.replaceAll(backupSnapshot) }
        fixture.settingsRepository.setThemeMode(ThemeMode.DARK)

        assertEquals(
            listOf(AndroidBackupTransportFixture.SESSION_ID),
            fixture.dao.workoutSessions().map { session -> session.id }
        )
        assertEquals(ThemeMode.DARK, fixture.settingsRepository.settings.first().themeMode)
        assertTrue(fixture.managedFile(stored.uri).isFile)
        assertTrue(fixture.managedFile(stored.thumbnailUri).isFile)
    }
}

@PhotoRebootAcceptance
@RunWith(AndroidJUnit4::class)
class AndroidBackupTransportVerifyTest {
    @Test
    fun restoreKeepsDatabaseAndSettingsButReconcilesExcludedPhotoFiles(): Unit = runBlocking {
        val fixture = AndroidBackupTransportFixture()
        assertEquals(
            listOf(AndroidBackupTransportFixture.SESSION_ID),
            fixture.dao.workoutSessions().map { session -> session.id }
        )
        assertEquals(ThemeMode.DARK, fixture.settingsRepository.settings.first().themeMode)

        val restoredPhoto = fixture.dao.progressPhotos().single()
        assertEquals(AndroidBackupTransportFixture.PHOTO_ID, restoredPhoto.id)
        assertFalse(fixture.managedFile(restoredPhoto.photoUri).exists())
        assertFalse(
            fixture.managedFile(requireNotNull(restoredPhoto.thumbnailUri)).exists()
        )

        val reconciliation = fixture.progressPhotoRepository.reconcileStorage()
        assertTrue(reconciliation.isSuccess)
        assertEquals(0, reconciliation.getOrThrow().orphanFilesDeleted)
        assertEquals(1, reconciliation.getOrThrow().missingRowsDeleted)
        assertEquals(0, reconciliation.getOrThrow().missingThumbnailsCleared)
        assertTrue(fixture.dao.progressPhotos().isEmpty())
        assertEquals(
            listOf(AndroidBackupTransportFixture.SESSION_ID),
            fixture.dao.workoutSessions().map { session -> session.id }
        )
        assertEquals(ThemeMode.DARK, fixture.settingsRepository.settings.first().themeMode)
    }
}

private class AndroidBackupTransportFixture {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val entryPoint = EntryPoints.get(
        context.applicationContext,
        DataPortabilityEntryPoint::class.java
    )
    val database = entryPoint.database()
    val dao = database.dataPortabilityDao()
    val settingsRepository = entryPoint.settingsRepository()
    val photoStorage = entryPoint.photoFileStorage()
    val progressPhotoRepository = entryPoint.progressPhotoRepository()

    fun managedFile(uri: String): File = File(requireNotNull(Uri.parse(uri).path))

    fun createJpeg(): ByteArray {
        val bitmap = Bitmap.createBitmap(72, 48, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(70, 35, 130))
        return ByteArrayOutputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output))
            bitmap.recycle()
            output.toByteArray()
        }
    }

    companion object {
        const val SESSION_ID = 951L
        const val PHOTO_ID = 952L
    }
}
