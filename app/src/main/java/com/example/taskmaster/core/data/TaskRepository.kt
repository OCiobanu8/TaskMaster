package com.example.taskmaster.core.data

import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(projectId: String, title: String, assigneeUserId: String): Result<TaskInstance>
    fun observeTasks(projectId: String): Flow<List<TaskInstance>>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit>
}
