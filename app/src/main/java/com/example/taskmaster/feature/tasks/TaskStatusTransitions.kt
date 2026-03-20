package com.example.taskmaster.feature.tasks

import com.example.taskmaster.core.model.TaskStatus

fun nextTaskStatus(current: TaskStatus): TaskStatus {
    return when (current) {
        TaskStatus.TODO -> TaskStatus.IN_PROGRESS
        TaskStatus.IN_PROGRESS -> TaskStatus.DONE
        TaskStatus.DONE -> TaskStatus.DONE
        TaskStatus.SKIPPED -> TaskStatus.SKIPPED
    }
}
