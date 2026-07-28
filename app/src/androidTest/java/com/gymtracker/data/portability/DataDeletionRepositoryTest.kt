package com.gymtracker.data.portability

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.photos.PhotoFileStorage
import com.gymtracker.data.programs.ProgramSeedInitializer
import com.gymtracker.data.settings.SettingsRepository
import com.gymtracker.data.settings.ThemeMode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataDeletionRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: AppDatabase
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var settingsFile: File
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var photoStorage: PhotoFileStorage

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        settingsFile = File(context.cacheDir, "delete-settings-${UUID.randomUUID()}.preferences_pb")
        settingsRepository = SettingsRepository(
            PreferenceDataStoreFactory.create(
                scope = dataStoreScope,
                produceFile = { settingsFile }
            )
        )
        photoStorage = PhotoFileStorage(context)
        photoStorage.reconcile(emptySet())
    }

    @After
    fun tearDown() {
        database.close()
        dataStoreScope.cancel()
        settingsFile.delete()
        photoStorage.reconcile(emptySet())
    }

    @Test
    fun deleteAll_clearsEveryTableSettingsAndManagedPhotoFiles() = runBlocking {
        database.workoutDao().insertSession(WorkoutSession(id = 41, name = "Private workout"))
        val stored = photoStorage.store(
            ByteArrayInputStream(createJpeg(width = 64, height = 32)),
            "image/jpeg"
        )
        database.progressPhotoDao().insert(
            ProgressPhoto(
                id = 7,
                photoUri = stored.uri,
                thumbnailUri = stored.thumbnailUri,
                mimeType = stored.mimeType,
                sizeBytes = stored.sizeBytes,
                storageVersion = 2,
                widthPx = stored.widthPx,
                heightPx = stored.heightPx
            )
        )
        settingsRepository.setThemeMode(ThemeMode.DARK)
        val main = File(requireNotNull(Uri.parse(stored.uri).path))
        val thumbnail = File(requireNotNull(Uri.parse(stored.thumbnailUri).path))

        val result = DataDeletionRepository(
            database = database,
            dao = database.dataPortabilityDao(),
            settingsRepository = settingsRepository,
            photoStorage = photoStorage,
            programSeedInitializer = ProgramSeedInitializer(
                database = database,
                programDao = database.programDao()
            )
        ).deleteAll()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().deletedRecords)
        assertEquals(2, result.getOrThrow().deletedManagedFiles)
        assertEquals(0, result.getOrThrow().managedFileCleanupFailures)
        val remaining = database.dataPortabilityDao().readSnapshot()
        assertEquals(emptyList<WorkoutSession>(), remaining.workoutSessions)
        assertEquals(emptyList<ProgressPhoto>(), remaining.progressPhotos)
        assertEquals(5, remaining.programs.size)
        assertEquals(1, remaining.seedMetadata.size)
        assertEquals(ThemeMode.SYSTEM, settingsRepository.settings.first().themeMode)
        assertFalse(main.exists())
        assertFalse(thumbnail.exists())
        Unit
    }

    private fun createJpeg(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(140, 35, 45))
        return ByteArrayOutputStream().use { output ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output))
            bitmap.recycle()
            output.toByteArray()
        }
    }
}
