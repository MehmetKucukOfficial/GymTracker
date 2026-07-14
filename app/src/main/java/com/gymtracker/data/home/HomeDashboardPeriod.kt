package com.gymtracker.data.home

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
