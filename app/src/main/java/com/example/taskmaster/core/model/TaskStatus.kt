package com.example.taskmaster.core.model

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
    SKIPPED;

    companion object {
        fun fromStorage(value: String): TaskStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: TODO
        }
    }
}
