package com.example.taskmaster.core.model

data class Project(
    val id: String,
    val name: String,
    val ownerUserId: String,
    val memberIds: List<String> = emptyList()
)
