package com.gymtracker.data.photos

import android.net.Uri
import com.gymtracker.data.dao.ProgressPhotoDao
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressPhotoRepository @Inject constructor(
    private val dao: ProgressPhotoDao,
    private val sourceReader: PhotoSourceReader,
    private val fileStorage: PhotoFileStorage
) {
    val photos: Flow<List<ProgressPhoto>> = dao.getAll()

    suspend fun reconcileStorage(): Result<PhotoReconciliationSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val managedPhotos = dao.getAllSync().filter { it.storageVersion >= MANAGED_STORAGE_VERSION }
            val managedUris = managedPhotos.flatMapTo(mutableSetOf()) { photo ->
                listOfNotNull(photo.photoUri, photo.thumbnailUri)
            }
            val reconciliation = fileStorage.reconcile(managedUris)
            val missingPhotos = managedPhotos.filter { it.photoUri in reconciliation.missingManagedUris }
            missingPhotos.forEach { photo ->
                fileStorage.deleteManaged(photo.thumbnailUri)
                dao.delete(photo)
            }
            val missingThumbnails = managedPhotos.filter { photo ->
                photo !in missingPhotos && photo.thumbnailUri in reconciliation.missingManagedUris
            }
            missingThumbnails.forEach { photo -> dao.update(photo.copy(thumbnailUri = null)) }
            PhotoReconciliationSummary(
                orphanFilesDeleted = reconciliation.orphanFilesDeleted,
                missingRowsDeleted = missingPhotos.size,
                missingThumbnailsCleared = missingThumbnails.size
            )
        }
    }

    suspend fun importPhoto(
        sourceUri: Uri,
        angle: PhotoAngle,
        weightKg: Float?,
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val source = sourceReader.open(sourceUri)
            val stored = source.inputStream.use { fileStorage.store(it, source.mimeType) }
            try {
                val id = dao.insert(
                    ProgressPhoto(
                        photoUri = stored.uri,
                        angle = angle,
                        weightKg = weightKg,
                        notes = notes.trim(),
                        mimeType = stored.mimeType,
                        sizeBytes = stored.sizeBytes,
                        storageVersion = CURRENT_STORAGE_VERSION,
                        thumbnailUri = stored.thumbnailUri,
                        widthPx = stored.widthPx,
                        heightPx = stored.heightPx
                    )
                )
                Result.success(id)
            } catch (error: Throwable) {
                fileStorage.deleteManaged(stored.uri)
                fileStorage.deleteManaged(stored.thumbnailUri)
                throw error
            }
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    /**
     * Replaces a legacy photo source without changing the record identity or user metadata.
     * New managed files are published before the row is updated; they are rolled back when
     * the database update fails. Old managed files are deleted only after the row points at
     * the new v2 media, so an interrupted upgrade never leaves the record without a source.
     */
    suspend fun upgradePhoto(
        photo: ProgressPhoto,
        sourceUri: Uri
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(photo.storageVersion < CURRENT_STORAGE_VERSION) {
                "Fotoğraf zaten güncel depolama biçimini kullanıyor."
            }
            val source = sourceReader.open(sourceUri)
            val stored = source.inputStream.use { fileStorage.store(it, source.mimeType) }
            try {
                val updatedRows = dao.update(
                    photo.copy(
                        photoUri = stored.uri,
                        mimeType = stored.mimeType,
                        sizeBytes = stored.sizeBytes,
                        storageVersion = CURRENT_STORAGE_VERSION,
                        thumbnailUri = stored.thumbnailUri,
                        widthPx = stored.widthPx,
                        heightPx = stored.heightPx
                    )
                )
                check(updatedRows == 1) { "Fotoğraf kaydı artık mevcut değil." }
            } catch (error: Throwable) {
                fileStorage.deleteManaged(stored.uri)
                fileStorage.deleteManaged(stored.thumbnailUri)
                throw error
            }

            // Cleanup is best effort after the durable row swap. Reconciliation removes any
            // old managed orphan on the next screen start if the filesystem rejects deletion.
            fileStorage.deleteManaged(photo.photoUri)
            fileStorage.deleteManaged(photo.thumbnailUri)
            Result.success(Unit)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    suspend fun deletePhoto(photo: ProgressPhoto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.delete(photo)
            check(fileStorage.deleteManaged(photo.photoUri)) {
                "Fotoğraf dosyası temizlenemedi."
            }
            check(fileStorage.deleteManaged(photo.thumbnailUri)) {
                "Küçük görsel dosyası temizlenemedi."
            }
            Result.success(Unit)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    private companion object {
        const val MANAGED_STORAGE_VERSION = 1
        const val CURRENT_STORAGE_VERSION = 2
    }
}

data class PhotoReconciliationSummary(
    val orphanFilesDeleted: Int,
    val missingRowsDeleted: Int,
    val missingThumbnailsCleared: Int
)
