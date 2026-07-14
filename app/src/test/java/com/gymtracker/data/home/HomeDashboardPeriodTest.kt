package com.gymtracker.data.home

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HomeDashboardPeriodTest {

    @Test
    fun period_usesCalendarBoundariesAcrossHistoricalIstanbulDst() {
        val zone = ZoneId.of("Europe/Istanbul")
        val period = homeDashboardPeriod(
            now = Instant.parse("2015-03-29T12:00:00Z"),
            zoneId = zone
        )

        assertEquals(
            LocalDate.of(2015, 3, 29).atStartOfDay(zone).toInstant().toEpochMilli(),
            period.dayStartMillis
        )
        assertEquals(
            LocalDate.of(2015, 3, 30).atStartOfDay(zone).toInstant().toEpochMilli(),
            period.nextDayStartMillis
        )
        assertNotEquals(86_400_000L, period.nextDayStartMillis - period.dayStartMillis)
    }

    @Test
    fun period_includesTodayAndPreviousSixCalendarDays() {
        val zone = ZoneId.of("Europe/Istanbul")
        val period = homeDashboardPeriod(
            now = Instant.parse("2026-07-14T12:00:00Z"),
            zoneId = zone
        )

        assertEquals(
            LocalDate.of(2026, 7, 8).atStartOfDay(zone).toInstant().toEpochMilli(),
            period.weekStartMillis
        )
    }
}
