package com.example.taskmaster.core.data

import com.example.taskmaster.core.model.TaskStatus
import com.google.firebase.Timestamp
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test

class FirestoreMappersTest {

    @Test
    fun projectFromMap_mapsExpectedFields() {
        val project = projectFromMap(
            id = "project-1",
            data = mapOf(
                "name" to "Home",
                "ownerUserId" to "user-1",
                "memberIds" to listOf("user-1", "user-2")
            )
        )

        assertThat(project).isNotNull()
        assertThat(project?.id).isEqualTo("project-1")
        assertThat(project?.name).isEqualTo("Home")
        assertThat(project?.ownerUserId).isEqualTo("user-1")
        assertThat(project?.memberIds).containsExactly("user-1", "user-2")
    }

    @Test
    fun taskInstanceFromMap_mapsExpectedFields() {
        val task = taskInstanceFromMap(
            id = "task-1",
            data = mapOf(
                "projectId" to "project-1",
                "title" to "Take out trash",
                "assigneeUserId" to "user-1",
                "status" to "IN_PROGRESS",
                "dueAt" to Timestamp(1_735_683_600, 0),
                "createdAt" to Timestamp(1_735_600_000, 0)
            )
        )

        assertThat(task).isNotNull()
        assertThat(task?.id).isEqualTo("task-1")
        assertThat(task?.projectId).isEqualTo("project-1")
        assertThat(task?.title).isEqualTo("Take out trash")
        assertThat(task?.assigneeUserId).isEqualTo("user-1")
        assertThat(task?.status).isEqualTo(TaskStatus.IN_PROGRESS)
        assertThat(task?.dueAt).isEqualTo(Instant.ofEpochSecond(1_735_683_600))
        assertThat(task?.createdAt).isEqualTo(Instant.ofEpochSecond(1_735_600_000))
    }

    @Test
    fun taskInstanceFromMap_keepsNullDueAtForLegacyTasks() {
        val task = taskInstanceFromMap(
            id = "task-legacy",
            data = mapOf(
                "projectId" to "project-1",
                "title" to "Legacy task",
                "assigneeUserId" to "user-1",
                "status" to "TODO"
            )
        )

        assertThat(task).isNotNull()
        assertThat(task?.dueAt).isNull()
        assertThat(task?.createdAt).isNull()
    }
}
