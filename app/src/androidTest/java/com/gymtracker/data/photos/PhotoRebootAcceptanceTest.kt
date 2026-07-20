package com.gymtracker.data.photos

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.database.DatabaseMigrations
import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PhotoRebootAcceptance

@PhotoRebootAcceptance
@RunWith(AndroidJUnit4::class)
class PhotoRebootSeedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun seedManagedPhotoDatasetBeforeHostReboot() = runBlocking {
        context.deleteDatabase(PhotoRebootFixture.DATABASE_NAME)
        File(context.filesDir, PhotoRebootFixture.DIRECTORY_NAME)
            .listFiles()
            ?.forEach(File::delete)

        val database = PhotoRebootFixture.openDatabase()
        try {
            val storage = PhotoFileStorage(context)
            val jpeg = PhotoRebootFixture.createJpeg()
            repeat(PhotoRebootFixture.PHOTO_COUNT) { index ->
                val stored = storage.store(ByteArrayInputStream(jpeg), "image/jpeg")
                database.progressPhotoDao().insert(
                    ProgressPhoto(
                        date = PhotoRebootFixture.BASE_DATE + index,
                        photoUri = stored.uri,
                        angle = PhotoAngle.values()[index % PhotoAngle.values().size],
                        notes = "reboot-acceptance-$index",
                        mimeType = stored.mimeType,
                        sizeBytes = stored.sizeBytes,
                        storageVersion = 2,
                        thumbnailUri = stored.thumbnailUri,
                        widthPx = stored.widthPx,
                        heightPx = stored.heightPx
                    )
                )
            }
            assertEquals(
                PhotoRebootFixture.PHOTO_COUNT,
                database.progressPhotoDao().getAllSync().size
            )
            database.measurementDao().insert(
                BodyMeasurement(
                    date = System.currentTimeMillis(),
                    weightKg = 80f,
                    notes = "reboot-acceptance"
                )
            )
            assertEquals(
                80f,
                database.measurementDao().getLatest()?.weightKg
            )
        } finally {
            database.close()
        }

        val bootCount = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.BOOT_COUNT,
            -1
        )
        assertTrue("Android boot count was unavailable before reboot", bootCount >= 0)
        assertTrue(
            "Could not persist reboot acceptance marker",
            context.getSharedPreferences(PhotoRebootFixture.PREFERENCES, 0)
                .edit()
                .putInt(PhotoRebootFixture.BOOT_COUNT_KEY, bootCount)
                .putInt(PhotoRebootFixture.PHOTO_COUNT_KEY, PhotoRebootFixture.PHOTO_COUNT)
                .commit()
        )
    }
}

@PhotoRebootAcceptance
@RunWith(AndroidJUnit4::class)
class PhotoRebootVerifyTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun managedPhotoDatasetSurvivesRealHostReboot() = runBlocking {
        val preferences = context.getSharedPreferences(PhotoRebootFixture.PREFERENCES, 0)
        val previousBootCount = preferences.getInt(PhotoRebootFixture.BOOT_COUNT_KEY, -1)
        val expectedCount = preferences.getInt(PhotoRebootFixture.PHOTO_COUNT_KEY, -1)
        val currentBootCount = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.BOOT_COUNT,
            -1
        )

        assertTrue("Seed phase marker is missing", previousBootCount >= 0)
        assertTrue(
            "Device did not reboot between acceptance phases: $previousBootCount -> $currentBootCount",
            currentBootCount > previousBootCount
        )
        assertEquals(PhotoRebootFixture.PHOTO_COUNT, expectedCount)

        val database = PhotoRebootFixture.openDatabase()
        try {
            val photos = database.progressPhotoDao().getAllSync()
            assertEquals(expectedCount, photos.size)
            photos.forEach { photo ->
                assertTrue(File(requireNotNull(Uri.parse(photo.photoUri).path)).isFile)
                assertTrue(File(requireNotNull(Uri.parse(photo.thumbnailUri).path)).isFile)
            }

            val storage = PhotoFileStorage(context)
            val reconciliation = storage.reconcile(
                photos.flatMapTo(mutableSetOf()) {
                    listOfNotNull(it.photoUri, it.thumbnailUri)
                }
            )
            assertEquals(0, reconciliation.orphanFilesDeleted)
            assertTrue(reconciliation.missingManagedUris.isEmpty())
            assertEquals(
                80f,
                database.measurementDao().getLatest()?.weightKg
            )
        } finally {
            database.close()
        }
    }
}

private object PhotoRebootFixture {
    const val DATABASE_NAME = "gymtracker.db"
    const val DIRECTORY_NAME = "progress_photos"
    const val PREFERENCES = "photo_reboot_acceptance"
    const val BOOT_COUNT_KEY = "boot_count"
    const val PHOTO_COUNT_KEY = "photo_count"
    const val PHOTO_COUNT = 120
    const val BASE_DATE = 1_700_000_000_000L

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    fun openDatabase(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(*DatabaseMigrations.ALL)
            .build()

    fun createJpeg(): ByteArray {
        val bitmap = Bitmap.createBitmap(96, 120, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(38, 92, 168))
        return ByteArrayOutputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output))
            bitmap.recycle()
            output.toByteArray()
        }
    }
}
