package com.example.taskmaster.core.model

import java.time.Instant

data class TaskInstance(
    val id: String,
    val projectId: String,
    val title: String,
    val assigneeUserId: String,
    val status: TaskStatus,
    val dueAt: Instant?,
    val createdAt: Instant?
)
