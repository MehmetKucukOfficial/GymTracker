package com.gymtracker.data.measurements

import com.gymtracker.data.dao.MeasurementDao
import com.gymtracker.data.entities.BodyMeasurement
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class MeasurementRepository @Inject constructor(
    private val dao: MeasurementDao,
    private val clock: Clock
) {
    fun observeAll(): Flow<List<BodyMeasurement>> = dao.getAll()

    fun observeChart(period: MeasurementChartPeriod): Flow<List<BodyMeasurement>> {
        val range = measurementDateRange(period, clock)
        return dao.getInRange(range.startInclusiveMillis, range.endExclusiveMillis)
    }

    suspend fun save(measurement: BodyMeasurement): Long = dao.insert(measurement)

    suspend fun update(measurement: BodyMeasurement) = dao.update(measurement)

    suspend fun delete(measurement: BodyMeasurement) = dao.delete(measurement)
}
