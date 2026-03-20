package com.example.taskmaster.core.auth

import com.example.taskmaster.core.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun currentUser(): Flow<User?>
    suspend fun signInWithGoogle(): Result<User>
    suspend fun signOut()
}
