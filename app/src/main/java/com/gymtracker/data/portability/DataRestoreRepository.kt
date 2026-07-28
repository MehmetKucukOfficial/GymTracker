package com.gymtracker.data.portability

import android.content.Context
import android.net.Uri
import android.os.StatFs
import androidx.room.withTransaction
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.photos.PhotoFileStorage
import com.gymtracker.data.settings.SettingsRepository
import com.gymtracker.data.settings.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class InsufficientRestoreStorageException(
    val requiredBytes: Long,
    val availableBytes: Long
) : IOException("Restore requires $requiredBytes bytes; $availableBytes bytes are available.")

class RestoreRollbackException(
    cause: Throwable,
    val rollbackFailure: Throwable
) : IOException("Restore failed and rollback could not be completed.", cause)

data class DataRestoreSummary(
    val totalRecords: Int,
    val restoredManagedPhotos: Int,
    val metadataOnlyLegacyPhotos: Int,
    val oldMediaCleanupFailures: Int
)

@Singleton
class DataRestoreRepository private constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val dao: DataPortabilityDao,
    private val settingsRepository: SettingsRepository,
    private val archiveReader: PortableArchiveReader,
    private val photoStorage: PhotoFileStorage,
    private val availableBytes: (File) -> Long
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: AppDatabase,
        dao: DataPortabilityDao,
        settingsRepository: SettingsRepository,
        archiveReader: PortableArchiveReader,
        photoStorage: PhotoFileStorage
    ) : this(
        context = context,
        database = database,
        dao = dao,
        settingsRepository = settingsRepository,
        archiveReader = archiveReader,
        photoStorage = photoStorage,
        availableBytes = { directory -> StatFs(directory.path).availableBytes }
    )

    internal constructor(
        context: Context,
        database: AppDatabase,
        dao: DataPortabilityDao,
        settingsRepository: SettingsRepository,
        archiveReader: PortableArchiveReader,
        photoStorage: PhotoFileStorage,
        availableBytes: Long
    ) : this(
        context = context,
        database = database,
        dao = dao,
        settingsRepository = settingsRepository,
        archiveReader = archiveReader,
        photoStorage = photoStorage,
        availableBytes = { availableBytes }
    )

    suspend fun restoreFrom(source: Uri): Result<DataRestoreSummary> =
        DataPortabilityOperationLock.mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    Result.success(restore(source))
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Result.failure(error)
                }
            }
        }

    private suspend fun restore(source: Uri): DataRestoreSummary {
        val restoreRoot = File(context.cacheDir, RESTORE_CACHE_DIRECTORY)
        check(restoreRoot.exists() || restoreRoot.mkdirs()) {
            "Restore cache directory could not be created."
        }
        check(restoreRoot.clearChildren()) {
            "Stale restore cache could not be cleared."
        }
        val freeBeforeCopy = availableBytes(restoreRoot)
        if (freeBeforeCopy < MINIMUM_RESTORE_FREE_BYTES) {
            throw InsufficientRestoreStorageException(
                MINIMUM_RESTORE_FREE_BYTES,
                freeBeforeCopy
            )
        }
        val archiveFile = File.createTempFile(RESTORE_ARCHIVE_PREFIX, ".zip", restoreRoot)
        val stagingDirectory = File(restoreRoot, "staging-${archiveFile.nameWithoutExtension}")
        try {
            val input = requireNotNull(context.contentResolver.openInputStream(source)) {
                "The selected restore source cannot be opened."
            }
            input.buffered().use { sourceInput ->
                archiveFile.outputStream().buffered().use { output ->
                    sourceInput.copyBounded(
                        output = output,
                        limit = minOf(
                            MAX_ARCHIVE_BYTES,
                            (freeBeforeCopy - RESTORE_SAFETY_BYTES).coerceAtLeast(0)
                        )
                    )
                }
            }
            check(archiveFile.length() > 0L) { "Restore archive is empty." }
            val requiredAfterCopy = archiveFile.length()
                .coerceAtMost(MAX_ARCHIVE_BYTES / 2)
                .times(2)
                .plus(RESTORE_SAFETY_BYTES)
            val freeAfterCopy = availableBytes(restoreRoot)
            if (freeAfterCopy < requiredAfterCopy) {
                throw InsufficientRestoreStorageException(requiredAfterCopy, freeAfterCopy)
            }

            archiveReader.stage(archiveFile, stagingDirectory).use { staged ->
                val oldSnapshot = database.withTransaction { dao.readSnapshot() }
                val oldTheme = settingsRepository.settings.first().themeMode
                val publishedUris = linkedMapOf<Long, RestoredPhotoUris>()
                try {
                    staged.archive.data.progressPhotos
                        .filter { photo -> photo.mediaState == PortablePhotoMediaState.INCLUDED }
                        .forEach { photo ->
                            val main = requireNotNull(staged.mediaFiles[photo.mainAssetPath])
                            val thumbnail = photo.thumbnailAssetPath?.let(staged.mediaFiles::get)
                            val published = photoStorage.publishRestored(main, thumbnail)
                            publishedUris[photo.id] = RestoredPhotoUris(
                                mainUri = published.uri,
                                thumbnailUri = published.thumbnailUri
                            )
                        }
                    val restoredSnapshot = staged.archive.toDatabaseSnapshot(publishedUris)
                    val restoredTheme = ThemeMode.valueOf(staged.archive.settings.themeMode)
                    return withContext(NonCancellable) {
                        commitOrRollback(
                            restoredSnapshot = restoredSnapshot,
                            restoredTheme = restoredTheme,
                            oldSnapshot = oldSnapshot,
                            oldTheme = oldTheme,
                            publishedUris = publishedUris,
                            legacyPhotoCount = staged.archive.data.progressPhotos.count { photo ->
                                photo.mediaState == PortablePhotoMediaState.LEGACY_METADATA_ONLY
                            }
                        )
                    }
                } catch (error: Exception) {
                    deletePublished(publishedUris.values)
                    throw error
                }
            }
        } finally {
            archiveFile.delete()
            stagingDirectory.deleteRecursively()
        }
    }

    private suspend fun commitOrRollback(
        restoredSnapshot: DatabaseSnapshot,
        restoredTheme: ThemeMode,
        oldSnapshot: DatabaseSnapshot,
        oldTheme: ThemeMode,
        publishedUris: Map<Long, RestoredPhotoUris>,
        legacyPhotoCount: Int
    ): DataRestoreSummary {
        try {
            database.withTransaction { dao.replaceAll(restoredSnapshot) }
            settingsRepository.setThemeMode(restoredTheme)
        } catch (restoreFailure: Exception) {
            val rollback = runCatching {
                database.withTransaction { dao.replaceAll(oldSnapshot) }
                settingsRepository.setThemeMode(oldTheme)
            }
            deletePublished(publishedUris.values)
            rollback.exceptionOrNull()?.let { rollbackFailure ->
                throw RestoreRollbackException(restoreFailure, rollbackFailure)
            }
            throw restoreFailure
        }

        val restoredManagedUris = publishedUris.values.flatMapTo(mutableSetOf()) { photo ->
            listOfNotNull(photo.mainUri, photo.thumbnailUri)
        }
        val oldManagedUris = oldSnapshot.progressPhotos
            .asSequence()
            .filter { photo -> photo.storageVersion >= 1 }
            .flatMap { photo -> sequenceOf(photo.photoUri, photo.thumbnailUri).filterNotNull() }
            .filterNot { uri -> uri in restoredManagedUris }
            .toList()
        val cleanupFailures = oldManagedUris.count { uri -> !photoStorage.deleteManaged(uri) }
        return DataRestoreSummary(
            totalRecords = restoredSnapshot.totalRecords(),
            restoredManagedPhotos = publishedUris.size,
            metadataOnlyLegacyPhotos = legacyPhotoCount,
            oldMediaCleanupFailures = cleanupFailures
        )
    }

    private fun deletePublished(photos: Collection<RestoredPhotoUris>) {
        photos.forEach { photo ->
            photoStorage.deleteManaged(photo.mainUri)
            photoStorage.deleteManaged(photo.thumbnailUri)
        }
    }

    private fun InputStream.copyBounded(output: java.io.OutputStream, limit: Long) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = read(buffer)
            if (read < 0) break
            total += read
            if (total > limit) {
                throw InsufficientRestoreStorageException(total, limit)
            }
            output.write(buffer, 0, read)
        }
    }

    private fun File.clearChildren(): Boolean {
        var cleared = true
        listFiles().orEmpty().forEach { child ->
            if (!child.deleteRecursively()) cleared = false
        }
        return cleared
    }

    private companion object {
        const val RESTORE_CACHE_DIRECTORY = "data_restore"
        const val RESTORE_ARCHIVE_PREFIX = "gymtracker-restore-"
        const val RESTORE_SAFETY_BYTES = 64L * 1024L * 1024L
        const val MINIMUM_RESTORE_FREE_BYTES = 96L * 1024L * 1024L
        const val MAX_ARCHIVE_BYTES = 2L * 1024L * 1024L * 1024L
    }
}
