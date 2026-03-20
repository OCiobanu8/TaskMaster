package com.example.taskmaster.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.core.data.TaskRepository
import com.example.taskmaster.core.debug.formatDebugError
import com.example.taskmaster.core.model.TaskStatus
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailsUiState(
    val currentStatus: TaskStatus = TaskStatus.TODO,
    val persistedStatus: TaskStatus = TaskStatus.TODO,
    val savingStatus: Boolean = false,
    val deleting: Boolean = false,
    val deleted: Boolean = false,
    val errorMessage: String? = null
)

class TaskDetailsViewModel(
    private val taskId: String,
    initialStatus: TaskStatus,
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TaskDetailsUiState(
            currentStatus = initialStatus,
            persistedStatus = initialStatus
        )
    )
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    fun selectStatus(status: TaskStatus) {
        _uiState.update { it.copy(currentStatus = status, errorMessage = null) }
    }

    fun saveStatus() {
        if (_uiState.value.savingStatus) return
        if (_uiState.value.currentStatus == _uiState.value.persistedStatus) return
        viewModelScope.launch {
            _uiState.update { it.copy(savingStatus = true, errorMessage = null) }
            val selectedStatus = _uiState.value.currentStatus
            val result = taskRepository.updateTaskStatus(taskId, selectedStatus)
            _uiState.update {
                it.copy(
                    savingStatus = false,
                    persistedStatus = if (result.isSuccess) selectedStatus else it.persistedStatus,
                    errorMessage = formatDebugError(result.exceptionOrNull())
                )
            }
        }
    }

    fun deleteTask() {
        if (_uiState.value.deleting) return
        viewModelScope.launch {
            _uiState.update { it.copy(deleting = true, errorMessage = null) }
            val result = taskRepository.deleteTask(taskId)
            _uiState.update {
                it.copy(
                    deleting = false,
                    deleted = result.isSuccess,
                    errorMessage = mapDeleteError(result.exceptionOrNull())
                )
            }
        }
    }

    private fun mapDeleteError(error: Throwable?): String? {
        return when (error) {
            null -> null
            is TimeoutCancellationException -> {
                val details = formatDebugError(error)
                "Delete timed out after 10s. $details"
            }
            else -> formatDebugError(error)
        }
    }
}

class TaskDetailsViewModelFactory(
    private val taskId: String,
    private val initialStatus: TaskStatus,
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailsViewModel::class.java)) {
            return TaskDetailsViewModel(taskId, initialStatus, taskRepository) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
