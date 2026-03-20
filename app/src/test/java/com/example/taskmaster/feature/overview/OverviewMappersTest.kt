package com.example.taskmaster.feature.overview

import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.model.TaskStatus
import com.example.taskmaster.core.time.DayRange
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test

class OverviewMappersTest {

    @Test
    fun buildStatusSnapshot_groupsTasksAndKeepsUnscheduled() {
        val range = DayRange(
            startInclusive = Instant.parse("2026-03-20T00:00:00Z"),
            endExclusive = Instant.parse("2026-03-21T00:00:00Z")
        )
        val tasks = listOf(
            TaskInstance("1", "p1", "Todo", "u1", TaskStatus.TODO, Instant.parse("2026-03-20T10:00:00Z"), null),
            TaskInstance("2", "p1", "Progress", "u1", TaskStatus.IN_PROGRESS, Instant.parse("2026-03-20T11:00:00Z"), null),
            TaskInstance("3", "p1", "Done", "u1", TaskStatus.DONE, Instant.parse("2026-03-20T12:00:00Z"), null),
            TaskInstance("4", "p1", "Skipped", "u1", TaskStatus.SKIPPED, Instant.parse("2026-03-20T13:00:00Z"), null),
            TaskInstance("5", "p1", "Tomorrow", "u1", TaskStatus.TODO, Instant.parse("2026-03-21T10:00:00Z"), null),
            TaskInstance("6", "p1", "Legacy", "u1", TaskStatus.TODO, null, null)
        )

        val snapshot = buildStatusSnapshot(tasks, range)

        assertThat(snapshot.todo.map { it.id }).containsExactly("1")
        assertThat(snapshot.inProgress.map { it.id }).containsExactly("2")
        assertThat(snapshot.done.map { it.id }).containsExactly("3")
        assertThat(snapshot.skipped.map { it.id }).containsExactly("4")
        assertThat(snapshot.unscheduled.map { it.id }).containsExactly("6")
    }
}
