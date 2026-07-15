package com.gymtracker.data.measurements

import com.gymtracker.data.entities.BodyMeasurement
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MeasurementRulesTest {

    @Test
    fun rejectsEmptyAndOutOfRangeMeasurements() {
        assertEquals(MeasurementValidationError.EMPTY, validateMeasurement(BodyMeasurement()))
        assertEquals(
            MeasurementValidationError.WEIGHT_OUT_OF_RANGE,
            validateMeasurement(BodyMeasurement(weightKg = -1f))
        )
        assertEquals(
            MeasurementValidationError.BODY_FAT_OUT_OF_RANGE,
            validateMeasurement(BodyMeasurement(bodyFatPercent = 90f))
        )
        assertNull(validateMeasurement(BodyMeasurement(weightKg = 81.5f, waistCm = 84f)))
    }

    @Test
    fun trendUsesTwoNewestWeightReadingsOnly() {
        val trend = weightTrend(
            listOf(
                BodyMeasurement(date = 1, weightKg = 90f),
                BodyMeasurement(date = 3, weightKg = 87.5f),
                BodyMeasurement(date = 2, waistCm = 85f),
                BodyMeasurement(date = 2, weightKg = 88f)
            )
        )

        assertEquals(87.5f, trend.latestKg)
        assertEquals(-0.5f, trend.changeKg)
        assertNull(weightTrend(listOf(BodyMeasurement(weightKg = 81f))).changeKg)
    }

    @Test
    fun chartRangeUsesCalendarDaysAndExclusiveTomorrowBoundary() {
        val clock = Clock.fixed(Instant.parse("2026-07-15T12:00:00Z"), ZoneId.of("Europe/Istanbul"))

        val sevenDays = measurementDateRange(MeasurementChartPeriod.SEVEN_DAYS, clock)
        val all = measurementDateRange(MeasurementChartPeriod.ALL, clock)

        assertEquals(
            Instant.parse("2026-07-08T21:00:00Z").toEpochMilli(),
            sevenDays.startInclusiveMillis
        )
        assertEquals(
            Instant.parse("2026-07-15T21:00:00Z").toEpochMilli(),
            sevenDays.endExclusiveMillis
        )
        assertEquals(0L, all.startInclusiveMillis)
        assertEquals(sevenDays.endExclusiveMillis, all.endExclusiveMillis)
    }
}
