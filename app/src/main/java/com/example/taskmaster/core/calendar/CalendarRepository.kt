package com.example.taskmaster.core.calendar

import java.time.Instant
import kotlinx.coroutines.flow.Flow

sealed interface CalendarAccessState {
    data object Unknown : CalendarAccessState
    data object Granted : CalendarAccessState
    data object PermissionDenied : CalendarAccessState
    data class Error(val message: String) : CalendarAccessState
}

interface CalendarRepository {
    fun observeAccessState(): Flow<CalendarAccessState>
    fun setPermissionGranted(granted: Boolean)
    fun observeEventsForRange(startInclusive: Instant, endExclusive: Instant): Flow<List<CalendarEvent>>
}
