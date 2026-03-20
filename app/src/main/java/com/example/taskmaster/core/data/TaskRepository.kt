package com.example.taskmaster.core.data

import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.model.TaskStatus
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(
        projectId: String,
        title: String,
        assigneeUserId: String,
        dueAt: Instant
    ): Result<TaskInstance>
    fun observeTasks(projectId: String): Flow<List<TaskInstance>>
    fun observeTasksForUser(userId: String): Flow<List<TaskInstance>>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit>
}
