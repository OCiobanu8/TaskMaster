package com.example.taskmaster.feature.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.core.auth.AuthRepository
import com.example.taskmaster.core.data.ProjectRepository
import com.example.taskmaster.core.debug.formatDebugError
import com.example.taskmaster.core.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectsUiState(
    val loading: Boolean = true,
    val projects: List<Project> = emptyList(),
    val errorMessage: String? = null
)

class ProjectsViewModel(
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser().collectLatest { user ->
                if (user == null) {
                    _uiState.value = ProjectsUiState(
                        loading = false,
                        projects = emptyList()
                    )
                    return@collectLatest
                }
                projectRepository.observeProjects(user.id).collectLatest { projects ->
                    _uiState.update { state ->
                        state.copy(loading = false, projects = projects, errorMessage = null)
                    }
                }
            }
        }
    }

    fun createProject(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            val result = projectRepository.createProject(name.trim())
            _uiState.update { state ->
                state.copy(
                    loading = false,
                    errorMessage = formatDebugError(result.exceptionOrNull())
                )
            }
        }
    }
}

class ProjectsViewModelFactory(
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectsViewModel::class.java)) {
            return ProjectsViewModel(authRepository, projectRepository) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
