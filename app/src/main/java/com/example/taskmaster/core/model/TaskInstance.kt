package com.example.taskmaster.core.model

data class TaskInstance(
    val id: String,
    val projectId: String,
    val title: String,
    val assigneeUserId: String,
    val status: TaskStatus
)
