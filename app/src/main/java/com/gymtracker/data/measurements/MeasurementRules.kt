package com.gymtracker.data.measurements

import com.gymtracker.data.entities.BodyMeasurement
import java.time.Clock
import java.time.LocalDate

enum class MeasurementValidationError {
    EMPTY,
    WEIGHT_OUT_OF_RANGE,
    BODY_FAT_OUT_OF_RANGE,
    CIRCUMFERENCE_OUT_OF_RANGE,
    SAVE_FAILED
}

fun validateMeasurement(measurement: BodyMeasurement): MeasurementValidationError? {
    val circumferences = listOf(
        measurement.chestCm,
        measurement.waistCm,
        measurement.hipsCm,
        measurement.leftArmCm,
        measurement.rightArmCm,
        measurement.leftCalfCm,
        measurement.rightCalfCm,
        measurement.shouldersCm
    )
    if (measurement.weightKg == null && measurement.bodyFatPercent == null &&
        circumferences.all { it == null }
    ) return MeasurementValidationError.EMPTY

    if (measurement.weightKg?.let { !it.isFinite() || it !in 20f..400f } == true) {
        return MeasurementValidationError.WEIGHT_OUT_OF_RANGE
    }
    if (measurement.bodyFatPercent?.let { !it.isFinite() || it !in 1f..75f } == true) {
        return MeasurementValidationError.BODY_FAT_OUT_OF_RANGE
    }
    if (circumferences.any { it?.let { value -> !value.isFinite() || value !in 10f..300f } == true }) {
        return MeasurementValidationError.CIRCUMFERENCE_OUT_OF_RANGE
    }
    return null
}

data class WeightTrend(
    val latestKg: Float?,
    val changeKg: Float?
)

enum class MeasurementChartPeriod(val days: Long?) {
    SEVEN_DAYS(7),
    THIRTY_DAYS(30),
    NINETY_DAYS(90),
    ALL(null)
}

data class MeasurementDateRange(
    val startInclusiveMillis: Long,
    val endExclusiveMillis: Long
)

fun measurementDateRange(period: MeasurementChartPeriod, clock: Clock): MeasurementDateRange {
    val today = LocalDate.now(clock)
    val start = period.days
        ?.let { today.minusDays(it - 1) }
        ?.atStartOfDay(clock.zone)
        ?.toInstant()
        ?.toEpochMilli()
        ?: 0L
    val end = today.plusDays(1).atStartOfDay(clock.zone).toInstant().toEpochMilli()
    return MeasurementDateRange(start, end)
}

fun weightTrend(measurements: List<BodyMeasurement>): WeightTrend {
    val weighted = measurements
        .asSequence()
        .filter { it.weightKg != null }
        .sortedByDescending(BodyMeasurement::date)
        .take(2)
        .toList()
    val latest = weighted.firstOrNull()?.weightKg
    val previous = weighted.getOrNull(1)?.weightKg
    return WeightTrend(
        latestKg = latest,
        changeKg = if (latest != null && previous != null) latest - previous else null
    )
}
