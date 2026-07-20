package com.gymtracker.data.home

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class HomeDashboardPeriod(
    val weekStartMillis: Long,
    val dayStartMillis: Long,
    val nextDayStartMillis: Long
)

fun homeDashboardPeriod(
    now: Instant = Instant.now(),
    zoneId: ZoneId = ZoneId.systemDefault()
): HomeDashboardPeriod {
    val today = now.atZone(zoneId).toLocalDate()
    return HomeDashboardPeriod(
        weekStartMillis = today.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli(),
        dayStartMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli(),
        nextDayStartMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    )
}

/**
 * Emits a new calendar window at local midnight and after a runtime time-zone change.
 *
 * The bounded poll does not re-emit identical periods, but prevents a process that stays alive
 * across a system time-zone change from waiting until midnight in the previous zone.
 */
fun currentDashboardPeriods(
    clock: Clock,
    zonePollMillis: Long = 60_000L,
    pause: suspend (Long) -> Unit = { delay(it) }
): Flow<HomeDashboardPeriod> = flow {
    require(zonePollMillis > 0L)
    var previous: HomeDashboardPeriod? = null
    while (true) {
        val period = homeDashboardPeriod(clock.instant(), clock.zone)
        if (period != previous) {
            emit(period)
            previous = period
        }
        val untilNextDay = (period.nextDayStartMillis - clock.millis()).coerceAtLeast(1_000L)
        pause(minOf(untilNextDay, zonePollMillis).coerceAtLeast(1_000L))
    }
}
