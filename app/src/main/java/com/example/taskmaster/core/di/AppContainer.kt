package com.example.taskmaster.core.di

import android.app.Activity
import com.example.taskmaster.core.auth.AuthRepository
import com.example.taskmaster.core.auth.FirebaseAuthRepository
import com.example.taskmaster.core.data.FirebaseProjectRepository
import com.example.taskmaster.core.data.FirebaseTaskRepository
import com.example.taskmaster.core.data.ProjectRepository
import com.example.taskmaster.core.data.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(
    activityProvider: () -> Activity?
) {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(
            firebaseAuth = firebaseAuth,
            activityProvider = activityProvider
        )
    }

    val projectRepository: ProjectRepository by lazy {
        FirebaseProjectRepository(
            firestore = firestore,
            authRepository = authRepository
        )
    }

    val taskRepository: TaskRepository by lazy {
        FirebaseTaskRepository(firestore = firestore)
    }
}
