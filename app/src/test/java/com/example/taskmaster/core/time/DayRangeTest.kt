package com.example.taskmaster.core.time

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.ZoneId
import org.junit.Test

class DayRangeTest {

    @Test
    fun todayRange_containsCurrentInstant() {
        val zoneId = ZoneId.of("Europe/Bucharest")
        val range = todayRange(zoneId)
        val now = Instant.now()

        assertThat(isInRange(now, range)).isTrue()
    }

    @Test
    fun isInRange_honorsInclusiveStartExclusiveEnd() {
        val zoneId = ZoneId.of("UTC")
        val range = todayRange(zoneId)

        assertThat(isInRange(range.startInclusive, range)).isTrue()
        assertThat(isInRange(range.endExclusive, range)).isFalse()
    }
}
