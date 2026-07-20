package com.gymtracker.data.portability

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.settings.SettingsRepository
import com.gymtracker.data.settings.ThemeMode
import java.io.ByteArrayInputStream
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataExportRepositoryTest {
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
        settingsFile = File(context.cacheDir, "export-settings-${UUID.randomUUID()}.preferences_pb")
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
    }

    @Test
    fun exportTo_writesReadableArchiveToSelectedFileAndCleansTemporaryFile() = runBlocking {
        database.workoutDao().insertSession(
            WorkoutSession(id = 41, name = "Full body", date = 1_721_493_600_000)
        )
        settingsRepository.setThemeMode(ThemeMode.DARK)
        val destination = File(context.cacheDir, "export-${UUID.randomUUID()}.zip")
        val repository = repository(availableBytes = Long.MAX_VALUE)

        try {
            val result = repository.exportTo(Uri.fromFile(destination))

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().totalRecords)
            assertTrue(result.getOrThrow().archiveBytes > 0)
            val entries = unzip(destination.readBytes())
            val archive = Json.decodeFromString<PortableArchive>(
                requireNotNull(entries[PORTABLE_DATA_ENTRY]).decodeToString()
            )
            assertEquals(PORTABLE_ARCHIVE_VERSION, archive.formatVersion)
            assertEquals(ThemeMode.DARK.name, archive.settings.themeMode)
            assertEquals(listOf(41L), archive.data.workoutSessions.map { it.id })
            assertTrue(
                File(context.cacheDir, "data_exports")
                    .listFiles()
                    .orEmpty()
                    .isEmpty()
            )
        } finally {
            destination.delete()
        }
    }

    @Test
    fun exportTo_whenTemporaryStorageIsLow_preservesDestinationAndReturnsTypedFailure() =
        runBlocking {
            val destination = File(context.cacheDir, "export-${UUID.randomUUID()}.zip")
                .apply { writeText("existing") }
            val repository = repository(availableBytes = 0L)

            try {
                val result = repository.exportTo(Uri.fromFile(destination))

                assertTrue(result.exceptionOrNull() is InsufficientExportStorageException)
                assertEquals("existing", destination.readText())
                assertTrue(
                    File(context.cacheDir, "data_exports")
                        .listFiles()
                        .orEmpty()
                        .isEmpty()
                )
            } finally {
                destination.delete()
            }
        }

    private fun repository(availableBytes: Long): DataExportRepository =
        DataExportRepository(
            context = context,
            database = database,
            dao = database.dataPortabilityDao(),
            settingsRepository = settingsRepository,
            archiveWriter = PortableArchiveWriter { null },
            clock = Clock.fixed(Instant.parse("2026-07-20T12:00:00Z"), ZoneOffset.UTC),
            availableBytes = availableBytes
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
}
