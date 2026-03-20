package com.example.taskmaster.core.time

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class DayRange(
    val startInclusive: Instant,
    val endExclusive: Instant
)

fun todayRange(zoneId: ZoneId): DayRange {
    val now = ZonedDateTime.now(zoneId)
    val start = now.toLocalDate().atStartOfDay(zoneId).toInstant()
    val end = start.plusSeconds(24 * 60 * 60)
    return DayRange(startInclusive = start, endExclusive = end)
}

fun isInRange(instant: Instant, range: DayRange): Boolean {
    return !instant.isBefore(range.startInclusive) && instant.isBefore(range.endExclusive)
}
