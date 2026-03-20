package com.example.taskmaster.navigation

import android.net.Uri

object AppRoutes {
    const val SIGN_IN = "sign_in"
    const val OVERVIEW = "overview"
    const val PROJECTS = "projects"
    const val ACCOUNT = "account"
    const val SETTINGS = "settings"
    const val TASK_BOARD = "task_board/{projectId}/{projectName}"
    const val TASK_DETAILS = "task_details/{taskId}/{projectId}/{assigneeUserId}/{status}/{dueAtMillis}/{createdAtMillis}/{title}"

    fun taskBoard(projectId: String, projectName: String): String {
        return "task_board/$projectId/${Uri.encode(projectName)}"
    }

    fun taskDetails(
        taskId: String,
        projectId: String,
        assigneeUserId: String,
        status: String,
        dueAtMillis: Long?,
        createdAtMillis: Long?,
        title: String
    ): String {
        val due = dueAtMillis ?: -1L
        val created = createdAtMillis ?: -1L
        return "task_details/${Uri.encode(taskId)}/${Uri.encode(projectId)}/${Uri.encode(assigneeUserId)}/${Uri.encode(status)}/$due/$created/${Uri.encode(title)}"
    }
}
