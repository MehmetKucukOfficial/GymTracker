package com.gymtracker.data.measurements

import com.gymtracker.data.entities.BodyMeasurement
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
}
