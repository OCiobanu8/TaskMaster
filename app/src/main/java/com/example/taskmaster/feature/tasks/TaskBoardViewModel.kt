package com.example.taskmaster.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.core.auth.AuthRepository
import com.example.taskmaster.core.data.ProjectRepository
import com.example.taskmaster.core.data.TaskRepository
import com.example.taskmaster.core.debug.formatDebugError
import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.model.TaskStatus
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskBoardUiState(
    val loading: Boolean = true,
    val tasks: List<TaskInstance> = emptyList(),
    val deletingProject: Boolean = false,
    val projectDeleted: Boolean = false,
    val errorMessage: String? = null
)

class TaskBoardViewModel(
    private val projectId: String,
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskBoardUiState())
    val uiState: StateFlow<TaskBoardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.observeTasks(projectId).collectLatest { tasks ->
                _uiState.update { state ->
                    state.copy(loading = false, tasks = tasks, errorMessage = null)
                }
            }
        }
    }

    fun addTask(title: String, dueAt: Instant) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val user = authRepository.currentUser().first()
                ?: run {
                    _uiState.update { it.copy(errorMessage = "Sign in required") }
                    return@launch
                }
            val result = taskRepository.createTask(
                projectId = projectId,
                title = title.trim(),
                assigneeUserId = user.id,
                dueAt = dueAt
            )
            _uiState.update { state ->
                state.copy(errorMessage = formatDebugError(result.exceptionOrNull()))
            }
        }
    }

    fun advanceStatus(task: TaskInstance) {
        val next = nextTaskStatus(task.status)
        if (next == task.status) return
        updateTaskStatus(task.id, next)
    }

    fun skipTask(task: TaskInstance) {
        updateTaskStatus(task.id, TaskStatus.SKIPPED)
    }

    fun deleteProject() {
        if (_uiState.value.deletingProject) return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingProject = true, errorMessage = null) }
            val result = projectRepository.deleteProject(projectId)
            _uiState.update { state ->
                state.copy(
                    deletingProject = false,
                    projectDeleted = result.isSuccess,
                    errorMessage = formatDebugError(result.exceptionOrNull())
                )
            }
        }
    }

    private fun updateTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            val result = taskRepository.updateTaskStatus(taskId, status)
            _uiState.update { state ->
                state.copy(errorMessage = formatDebugError(result.exceptionOrNull()))
            }
        }
    }
}

class TaskBoardViewModelFactory(
    private val projectId: String,
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskBoardViewModel::class.java)) {
            return TaskBoardViewModel(projectId, authRepository, projectRepository, taskRepository) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
