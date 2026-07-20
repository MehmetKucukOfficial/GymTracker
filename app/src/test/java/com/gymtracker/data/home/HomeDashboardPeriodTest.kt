package com.gymtracker.data.home

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
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

    @Test
    fun periodFlow_emitsNextCalendarDayAtLocalMidnight() = runBlocking {
        val zone = ZoneId.of("Europe/Istanbul")
        val clock = MutableClock(Instant.parse("2026-07-20T20:59:59Z"), zone)
        val periods = currentDashboardPeriods(
            clock = clock,
            zonePollMillis = 60_000L,
            pause = { clock.advanceMillis(it) }
        ).take(2).toList()

        assertEquals(
            LocalDate.of(2026, 7, 20).atStartOfDay(zone).toInstant().toEpochMilli(),
            periods[0].dayStartMillis
        )
        assertEquals(
            LocalDate.of(2026, 7, 21).atStartOfDay(zone).toInstant().toEpochMilli(),
            periods[1].dayStartMillis
        )
    }

    @Test
    fun periodFlow_recalculatesBoundariesAfterRuntimeZoneChange() = runBlocking {
        val utc = ZoneId.of("UTC")
        val tokyo = ZoneId.of("Asia/Tokyo")
        val clock = MutableClock(Instant.parse("2026-07-20T12:00:00Z"), utc)
        var pauseCount = 0

        val periods = currentDashboardPeriods(
            clock = clock,
            zonePollMillis = 60_000L,
            pause = { millis ->
                clock.advanceMillis(millis)
                if (pauseCount++ == 0) clock.currentZone = tokyo
            }
        ).take(2).toList()

        assertEquals(
            LocalDate.of(2026, 7, 20).atStartOfDay(utc).toInstant().toEpochMilli(),
            periods[0].dayStartMillis
        )
        assertEquals(
            LocalDate.of(2026, 7, 20).atStartOfDay(tokyo).toInstant().toEpochMilli(),
            periods[1].dayStartMillis
        )
        assertNotEquals(periods[0].dayStartMillis, periods[1].dayStartMillis)
    }

    private class MutableClock(
        private var currentInstant: Instant,
        var currentZone: ZoneId
    ) : Clock() {
        override fun getZone(): ZoneId = currentZone

        override fun withZone(zone: ZoneId): Clock = MutableClock(currentInstant, zone)

        override fun instant(): Instant = currentInstant

        fun advanceMillis(millis: Long) {
            currentInstant = currentInstant.plusMillis(millis)
        }
    }
}
