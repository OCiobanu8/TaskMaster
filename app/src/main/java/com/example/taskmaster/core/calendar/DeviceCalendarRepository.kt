package com.example.taskmaster.core.calendar

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.CalendarContract
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class DeviceCalendarRepository(
    private val contentResolver: ContentResolver
) : CalendarRepository {

    private val accessState = MutableStateFlow<CalendarAccessState>(CalendarAccessState.Unknown)

    override fun observeAccessState(): Flow<CalendarAccessState> = accessState.asStateFlow()

    override fun setPermissionGranted(granted: Boolean) {
        accessState.value = if (granted) {
            CalendarAccessState.Granted
        } else {
            CalendarAccessState.PermissionDenied
        }
    }

    @SuppressLint("MissingPermission")
    override fun observeEventsForRange(
        startInclusive: Instant,
        endExclusive: Instant
    ): Flow<List<CalendarEvent>> = flow {
        if (accessState.value != CalendarAccessState.Granted) {
            emit(emptyList())
            return@flow
        }

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME
        )

        val instancesUri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(startInclusive.toEpochMilli().toString())
            .appendPath(endExclusive.toEpochMilli().toString())
            .build()

        runCatching {
            contentResolver.query(
                instancesUri,
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val titleIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val startIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val endIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
                val sourceIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)

                val events = mutableListOf<CalendarEvent>()
                while (cursor.moveToNext()) {
                    val startMillis = cursor.getLong(startIdx)
                    val rawEndMillis = cursor.getLong(endIdx)
                    val endMillis = if (rawEndMillis > startMillis) {
                        rawEndMillis
                    } else {
                        startMillis + 60_000L
                    }
                    events += CalendarEvent(
                        id = cursor.getLong(idIdx),
                        title = cursor.getString(titleIdx).orEmpty().ifBlank { "(No title)" },
                        startAt = Instant.ofEpochMilli(startMillis),
                        endAt = Instant.ofEpochMilli(endMillis),
                        source = cursor.getString(sourceIdx).orEmpty()
                    )
                }
                events
            }.orEmpty()
        }.onSuccess { events ->
            accessState.value = CalendarAccessState.Granted
            emit(events)
        }.onFailure { error ->
            accessState.value = CalendarAccessState.Error(error.message ?: "Calendar query failed")
            emit(emptyList())
        }
    }
}
