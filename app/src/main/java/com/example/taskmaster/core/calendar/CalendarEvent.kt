package com.example.taskmaster.core.calendar

import java.time.Instant

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startAt: Instant,
    val endAt: Instant,
    val source: String
)
