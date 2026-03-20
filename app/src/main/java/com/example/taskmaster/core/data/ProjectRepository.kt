package com.example.taskmaster.core.data

import com.example.taskmaster.core.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    suspend fun createProject(name: String): Result<Project>
    fun observeProjects(userId: String): Flow<List<Project>>
}
