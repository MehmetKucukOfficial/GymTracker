package com.gymtracker.data.portability

import androidx.room.withTransaction
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.photos.PhotoFileStorage
import com.gymtracker.data.programs.ProgramSeedInitializer
import com.gymtracker.data.settings.SettingsRepository
import com.gymtracker.data.settings.ThemeMode
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DataDeletionRollbackException(
    cause: Throwable,
    val rollbackFailure: Throwable
) : IOException("Data deletion failed and rollback could not be completed.", cause)

data class DataDeletionSummary(
    val deletedRecords: Int,
    val deletedManagedFiles: Int,
    val managedFileCleanupFailures: Int
)

@Singleton
class DataDeletionRepository @Inject constructor(
    private val database: AppDatabase,
    private val dao: DataPortabilityDao,
    private val settingsRepository: SettingsRepository,
    private val photoStorage: PhotoFileStorage,
    private val programSeedInitializer: ProgramSeedInitializer
) {
    suspend fun deleteAll(): Result<DataDeletionSummary> =
        DataPortabilityOperationLock.mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    Result.success(deleteAllInternal())
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Result.failure(error)
                }
            }
        }

    private suspend fun deleteAllInternal(): DataDeletionSummary {
        val oldSnapshot = database.withTransaction { dao.readSnapshot() }
        val oldTheme = settingsRepository.settings.first().themeMode
        return withContext(NonCancellable) {
            try {
                database.withTransaction {
                    dao.replaceAll(emptyDatabaseSnapshot())
                    programSeedInitializer.seedIfNeeded()
                }
                settingsRepository.setThemeMode(ThemeMode.SYSTEM)
            } catch (deleteFailure: Exception) {
                val rollback = runCatching {
                    database.withTransaction { dao.replaceAll(oldSnapshot) }
                    settingsRepository.setThemeMode(oldTheme)
                }
                rollback.exceptionOrNull()?.let { rollbackFailure ->
                    throw DataDeletionRollbackException(deleteFailure, rollbackFailure)
                }
                throw deleteFailure
            }

            val cleanup = runCatching { photoStorage.reconcile(emptySet()) }.getOrNull()
            DataDeletionSummary(
                deletedRecords = oldSnapshot.totalRecords(),
                deletedManagedFiles = cleanup?.orphanFilesDeleted ?: 0,
                managedFileCleanupFailures = cleanup?.orphanFileDeleteFailures ?: 1
            )
        }
    }
}
