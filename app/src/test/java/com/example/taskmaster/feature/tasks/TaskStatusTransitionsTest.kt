package com.example.taskmaster.feature.tasks

import com.example.taskmaster.core.model.TaskStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TaskStatusTransitionsTest {

    @Test
    fun nextTaskStatus_movesTodoToInProgress() {
        assertThat(nextTaskStatus(TaskStatus.TODO)).isEqualTo(TaskStatus.IN_PROGRESS)
    }

    @Test
    fun nextTaskStatus_movesInProgressToDone() {
        assertThat(nextTaskStatus(TaskStatus.IN_PROGRESS)).isEqualTo(TaskStatus.DONE)
    }

    @Test
    fun nextTaskStatus_keepsTerminalStates() {
        assertThat(nextTaskStatus(TaskStatus.DONE)).isEqualTo(TaskStatus.DONE)
        assertThat(nextTaskStatus(TaskStatus.SKIPPED)).isEqualTo(TaskStatus.SKIPPED)
    }
}
