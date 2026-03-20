package com.example.taskmaster.navigation

import android.net.Uri

object AppRoutes {
    const val SIGN_IN = "sign_in"
    const val PROJECTS = "projects"
    const val TASK_BOARD = "task_board/{projectId}/{projectName}"

    fun taskBoard(projectId: String, projectName: String): String {
        return "task_board/$projectId/${Uri.encode(projectName)}"
    }
}
