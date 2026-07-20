package com.gymtracker.data.portability

import android.content.Context
import android.net.Uri
import android.os.StatFs
import androidx.room.withTransaction
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class InsufficientExportStorageException(
    val requiredBytes: Long,
    val availableBytes: Long
) : IOException("Export requires $requiredBytes bytes; $availableBytes bytes are available.")

data class DataExportSummary(
    val totalRecords: Int,
    val includedManagedPhotos: Int,
    val metadataOnlyLegacyPhotos: Int,
    val mediaEntries: Int,
    val archiveBytes: Long
)

@Singleton
class DataExportRepository private constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val dao: DataPortabilityDao,
    private val settingsRepository: SettingsRepository,
    private val archiveWriter: PortableArchiveWriter,
    private val clock: Clock,
    private val availableBytes: (File) -> Long
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: AppDatabase,
        dao: DataPortabilityDao,
        settingsRepository: SettingsRepository,
        archiveWriter: PortableArchiveWriter,
        clock: Clock
    ) : this(
        context = context,
        database = database,
        dao = dao,
        settingsRepository = settingsRepository,
        archiveWriter = archiveWriter,
        clock = clock,
        availableBytes = { directory -> StatFs(directory.path).availableBytes }
    )

    internal constructor(
        context: Context,
        database: AppDatabase,
        dao: DataPortabilityDao,
        settingsRepository: SettingsRepository,
        archiveWriter: PortableArchiveWriter,
        clock: Clock,
        availableBytes: Long
    ) : this(
        context = context,
        database = database,
        dao = dao,
        settingsRepository = settingsRepository,
        archiveWriter = archiveWriter,
        clock = clock,
        availableBytes = { availableBytes }
    )

    suspend fun exportTo(destination: Uri): Result<DataExportSummary> =
        DataPortabilityOperationLock.mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    val snapshot = database.withTransaction { dao.readSnapshot() }
                    val settings = settingsRepository.settings.first()
                    val exportedAt = clock.millis()
                    val archive = PortableArchive(
                        exportedAtEpochMillis = exportedAt,
                        appVersionName = appVersionName(),
                        settings = PortableSettings(themeMode = settings.themeMode.name),
                        data = snapshot.toPortableData()
                    )
                    val exportDirectory = File(context.cacheDir, EXPORT_CACHE_DIRECTORY)
                    check(exportDirectory.exists() || exportDirectory.mkdirs()) {
                        "Export cache directory could not be created."
                    }
                    check(exportDirectory.clearChildren()) {
                        "Stale export cache could not be cleared."
                    }
                    val requiredBytes = estimatedTemporaryBytes(snapshot)
                    val freeBytes = availableBytes(exportDirectory)
                    if (freeBytes < requiredBytes) {
                        throw InsufficientExportStorageException(requiredBytes, freeBytes)
                    }

                    val temporaryArchive = File.createTempFile(
                        EXPORT_TEMP_PREFIX,
                        EXPORT_FILE_SUFFIX,
                        exportDirectory
                    )
                    try {
                        val writeSummary =
                            temporaryArchive.outputStream().buffered().use { output ->
                                archiveWriter.write(archive, snapshot, output)
                            }
                        check(temporaryArchive.length() > 0L) { "Export archive is empty." }
                        copyToDestination(temporaryArchive, destination)
                        Result.success(
                            DataExportSummary(
                                totalRecords = snapshot.totalRecords(),
                                includedManagedPhotos = writeSummary.includedManagedPhotos,
                                metadataOnlyLegacyPhotos =
                                    writeSummary.metadataOnlyLegacyPhotos,
                                mediaEntries = writeSummary.mediaEntries,
                                archiveBytes = temporaryArchive.length()
                            )
                        )
                    } finally {
                        temporaryArchive.delete()
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Result.failure(error)
                }
            }
        }

    private fun copyToDestination(archive: File, destination: Uri) {
        try {
            val output = requireNotNull(
                context.contentResolver.openOutputStream(destination, "wt")
            ) { "The selected export destination cannot be opened." }
            archive.inputStream().buffered().use { input ->
                output.buffered().use { destinationOutput ->
                    input.copyTo(destinationOutput, COPY_BUFFER_BYTES)
                }
            }
        } catch (error: Exception) {
            runCatching { context.contentResolver.delete(destination, null, null) }
            throw error
        }
    }

    private fun appVersionName(): String =
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            .orEmpty()
            .ifBlank { "unknown" }

    private fun estimatedTemporaryBytes(snapshot: DatabaseSnapshot): Long {
        val managedMainBytes = snapshot.progressPhotos
            .asSequence()
            .filter { photo -> photo.storageVersion >= 1 }
            .sumOf { photo -> photo.sizeBytes.coerceAtLeast(0L) }
        return managedMainBytes + EXPORT_SAFETY_BYTES
    }

    private fun File.clearChildren(): Boolean {
        var cleared = true
        listFiles().orEmpty().forEach { child ->
            if (!child.deleteRecursively()) cleared = false
        }
        return cleared
    }

    private companion object {
        const val EXPORT_CACHE_DIRECTORY = "data_exports"
        const val EXPORT_TEMP_PREFIX = "gymtracker-"
        const val EXPORT_FILE_SUFFIX = ".zip"
        const val EXPORT_SAFETY_BYTES = 32L * 1024L * 1024L
        const val COPY_BUFFER_BYTES = 64 * 1024
    }
}
