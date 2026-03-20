package com.example.taskmaster.feature.overview

import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.model.TaskStatus
import com.example.taskmaster.core.time.DayRange
import com.example.taskmaster.core.time.isInRange

data class StatusSnapshot(
    val todo: List<TaskInstance>,
    val inProgress: List<TaskInstance>,
    val done: List<TaskInstance>,
    val skipped: List<TaskInstance>,
    val unscheduled: List<TaskInstance>
)

fun buildStatusSnapshot(tasks: List<TaskInstance>, todayRange: DayRange): StatusSnapshot {
    val todayTasks = tasks.filter { task ->
        task.dueAt?.let { isInRange(it, todayRange) } == true
    }
    return StatusSnapshot(
        todo = todayTasks.filter { it.status == TaskStatus.TODO },
        inProgress = todayTasks.filter { it.status == TaskStatus.IN_PROGRESS },
        done = todayTasks.filter { it.status == TaskStatus.DONE },
        skipped = todayTasks.filter { it.status == TaskStatus.SKIPPED },
        unscheduled = tasks.filter { it.dueAt == null }
    )
}
