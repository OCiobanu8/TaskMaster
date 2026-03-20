package com.example.taskmaster.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.core.auth.AuthRepository
import com.example.taskmaster.core.data.TaskRepository
import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.model.TaskStatus
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
    val errorMessage: String? = null
)

class TaskBoardViewModel(
    private val projectId: String,
    private val authRepository: AuthRepository,
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

    fun addTask(title: String) {
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
                assigneeUserId = user.id
            )
            _uiState.update { state ->
                state.copy(errorMessage = result.exceptionOrNull()?.message)
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

    private fun updateTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            val result = taskRepository.updateTaskStatus(taskId, status)
            _uiState.update { state ->
                state.copy(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }
}

class TaskBoardViewModelFactory(
    private val projectId: String,
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskBoardViewModel::class.java)) {
            return TaskBoardViewModel(projectId, authRepository, taskRepository) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
