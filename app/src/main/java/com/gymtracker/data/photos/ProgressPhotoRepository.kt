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
                        storageVersion = CURRENT_STORAGE_VERSION
                    )
                )
                Result.success(id)
            } catch (error: Throwable) {
                fileStorage.deleteManaged(stored.uri)
                throw error
            }
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
            Result.success(Unit)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    private companion object {
        const val CURRENT_STORAGE_VERSION = 1
    }
}
