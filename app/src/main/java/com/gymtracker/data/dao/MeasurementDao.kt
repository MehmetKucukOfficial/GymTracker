package com.gymtracker.data.dao

import androidx.room.*
import com.gymtracker.data.entities.BodyMeasurement
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY date DESC")
    fun getAll(): Flow<List<BodyMeasurement>>

    @Query("SELECT * FROM body_measurements ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): BodyMeasurement?

    @Query("SELECT * FROM body_measurements WHERE date >= :since ORDER BY date ASC")
    fun getSince(since: Long): Flow<List<BodyMeasurement>>

    @Query("SELECT * FROM body_measurements WHERE date >= :startInclusive AND date < :endExclusive ORDER BY date ASC")
    fun getInRange(startInclusive: Long, endExclusive: Long): Flow<List<BodyMeasurement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: BodyMeasurement): Long

    @Update
    suspend fun update(measurement: BodyMeasurement)

    @Delete
    suspend fun delete(measurement: BodyMeasurement)
}
