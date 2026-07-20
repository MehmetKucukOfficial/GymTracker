package com.gymtracker.data.portability

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.photos.PhotoFileStorage
import com.gymtracker.data.settings.SettingsRepository
import com.gymtracker.data.settings.ThemeMode
import java.io.File
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataRestoreRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: AppDatabase
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var settingsFile: File
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        settingsFile = File(context.cacheDir, "restore-settings-${UUID.randomUUID()}.preferences_pb")
        settingsRepository = SettingsRepository(
            PreferenceDataStoreFactory.create(
                scope = dataStoreScope,
                produceFile = { settingsFile }
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
        dataStoreScope.cancel()
        settingsFile.delete()
        File(context.cacheDir, "data_exports").deleteRecursively()
        File(context.cacheDir, "data_restore").deleteRecursively()
    }

    @Test
    fun restoreFrom_replacesDatabaseAndSettingsAfterFullArchiveValidation() = runBlocking {
        val original = WorkoutSession(id = 41, name = "Backed up", date = 100)
        database.workoutDao().insertSession(original)
        settingsRepository.setThemeMode(ThemeMode.DARK)
        val backup = File(context.cacheDir, "restore-${UUID.randomUUID()}.zip")
        val export = DataExportRepository(
            context = context,
            database = database,
            dao = database.dataPortabilityDao(),
            settingsRepository = settingsRepository,
            archiveWriter = PortableArchiveWriter { null },
            clock = Clock.fixed(Instant.parse("2026-07-20T12:00:00Z"), ZoneOffset.UTC),
            availableBytes = Long.MAX_VALUE
        )
        assertTrue(export.exportTo(Uri.fromFile(backup)).isSuccess)

        database.workoutDao().deleteSession(original)
        database.workoutDao().insertSession(WorkoutSession(id = 99, name = "Current", date = 200))
        settingsRepository.setThemeMode(ThemeMode.LIGHT)

        try {
            val result = repository().restoreFrom(Uri.fromFile(backup))

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().totalRecords)
            assertEquals(
                listOf(WorkoutSession(id = 41, name = "Backed up", date = 100)),
                database.dataPortabilityDao().workoutSessions()
            )
            assertEquals(ThemeMode.DARK, settingsRepository.settings.first().themeMode)
            assertTrue(
                File(context.cacheDir, "data_restore").listFiles().orEmpty().isEmpty()
            )
        } finally {
            backup.delete()
        }
    }

    @Test
    fun restoreFrom_invalidZipLeavesCurrentDatabaseAndSettingsUntouched() = runBlocking {
        database.workoutDao().insertSession(
            WorkoutSession(id = 99, name = "Current", date = 200)
        )
        settingsRepository.setThemeMode(ThemeMode.LIGHT)
        val invalid = File(context.cacheDir, "restore-${UUID.randomUUID()}.zip")
            .apply { writeText("not a zip") }
        File(context.cacheDir, "data_restore/stale/partial.zip").apply {
            parentFile?.mkdirs()
            writeText("partial")
        }

        try {
            val result = repository().restoreFrom(Uri.fromFile(invalid))

            assertTrue(result.exceptionOrNull() is CorruptPortableArchiveException)
            assertEquals(listOf(99L), database.dataPortabilityDao().workoutSessions().map { it.id })
            assertEquals(ThemeMode.LIGHT, settingsRepository.settings.first().themeMode)
            assertTrue(
                File(context.cacheDir, "data_restore").listFiles().orEmpty().isEmpty()
            )
        } finally {
            invalid.delete()
        }
    }

    @Test
    fun restoreFrom_republishesManagedPhotoAndRemovesSupersededFiles() = runBlocking {
        val storage = PhotoFileStorage(context)
        val stored = storage.store(
            ByteArrayInputStream(createJpeg(width = 64, height = 32)),
            "image/jpeg"
        )
        val original = ProgressPhoto(
            id = 7,
            date = 300,
            photoUri = stored.uri,
            thumbnailUri = stored.thumbnailUri,
            angle = PhotoAngle.FRONT,
            mimeType = stored.mimeType,
            sizeBytes = stored.sizeBytes,
            storageVersion = 2,
            widthPx = stored.widthPx,
            heightPx = stored.heightPx
        )
        database.progressPhotoDao().insert(original)
        val oldMain = File(requireNotNull(Uri.parse(stored.uri).path))
        val oldThumbnail = File(requireNotNull(Uri.parse(stored.thumbnailUri).path))
        val backup = File(context.cacheDir, "restore-${UUID.randomUUID()}.zip")
        val export = DataExportRepository(
            context = context,
            database = database,
            dao = database.dataPortabilityDao(),
            settingsRepository = settingsRepository,
            archiveWriter = PortableArchiveWriter(ManagedPhotoMediaSource(storage)),
            clock = Clock.fixed(Instant.parse("2026-07-20T12:00:00Z"), ZoneOffset.UTC),
            availableBytes = Long.MAX_VALUE
        )
        assertTrue(export.exportTo(Uri.fromFile(backup)).isSuccess)

        try {
            val result = repository().restoreFrom(Uri.fromFile(backup))

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().restoredManagedPhotos)
            val restored = database.progressPhotoDao().getAllSync().single()
            assertTrue(restored.photoUri != original.photoUri)
            assertTrue(restored.thumbnailUri != original.thumbnailUri)
            assertTrue(File(requireNotNull(Uri.parse(restored.photoUri).path)).isFile)
            assertTrue(File(requireNotNull(Uri.parse(restored.thumbnailUri).path)).isFile)
            assertEquals(false, oldMain.exists())
            assertEquals(false, oldThumbnail.exists())
            storage.deleteManaged(restored.photoUri)
            storage.deleteManaged(restored.thumbnailUri)
        } finally {
            backup.delete()
            storage.deleteManaged(original.photoUri)
            storage.deleteManaged(original.thumbnailUri)
        }
        Unit
    }

    private fun repository() = DataRestoreRepository(
        context = context,
        database = database,
        dao = database.dataPortabilityDao(),
        settingsRepository = settingsRepository,
        archiveReader = PortableArchiveReader(),
        photoStorage = PhotoFileStorage(context),
        availableBytes = Long.MAX_VALUE
    )

    private fun createJpeg(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(30, 90, 160))
        return ByteArrayOutputStream().use { output ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output))
            bitmap.recycle()
            output.toByteArray()
        }
    }
}
