package com.gymtracker.data.dao

import androidx.room.*
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressPhotoDao {
    @Query("SELECT * FROM progress_photos ORDER BY date DESC")
    fun getAll(): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM progress_photos WHERE angle = :angle ORDER BY date DESC")
    fun getByAngle(angle: PhotoAngle): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM progress_photos WHERE date >= :since ORDER BY date DESC")
    fun getSince(since: Long): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM progress_photos ORDER BY date DESC")
    suspend fun getAllSync(): List<ProgressPhoto>

    @Insert
    suspend fun insert(photo: ProgressPhoto): Long

    @Update
    suspend fun update(photo: ProgressPhoto): Int

    @Delete
    suspend fun delete(photo: ProgressPhoto)
}
