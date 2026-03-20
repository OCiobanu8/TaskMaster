package com.example.taskmaster.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.core.auth.AuthRepository
import com.example.taskmaster.core.calendar.CalendarAccessState
import com.example.taskmaster.core.calendar.CalendarEvent
import com.example.taskmaster.core.calendar.CalendarRepository
import com.example.taskmaster.core.data.TaskRepository
import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.time.todayRange
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OverviewUiState(
    val loadingTasks: Boolean = true,
    val loadingCalendar: Boolean = true,
    val calendarAccessState: CalendarAccessState = CalendarAccessState.Unknown,
    val alreadyPlanned: List<CalendarEvent> = emptyList(),
    val todo: List<TaskInstance> = emptyList(),
    val inProgress: List<TaskInstance> = emptyList(),
    val done: List<TaskInstance> = emptyList(),
    val skipped: List<TaskInstance> = emptyList(),
    val upcoming: List<TaskInstance> = emptyList(),
    val unscheduled: List<TaskInstance> = emptyList(),
    val errorMessage: String? = null
)

class OverviewViewModel(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {
    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
        observeCalendar()
    }

    fun setCalendarPermissionGranted(granted: Boolean) {
        calendarRepository.setPermissionGranted(granted)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTasks() {
        viewModelScope.launch {
            authRepository.currentUser()
                .flatMapLatest { user ->
                    if (user == null) {
                        flowOf(emptyList())
                    } else {
                        taskRepository.observeTasksForUser(user.id)
                    }
                }
                .collectLatest { tasks ->
                    val today = todayRange(zoneId)
                    val snapshot = buildStatusSnapshot(tasks, today)
                    val upcomingWindowEnd = today.endExclusive.plusSeconds(7 * 24 * 60 * 60L)
                    val upcomingTasks = tasks
                        .filter { task ->
                            val due = task.dueAt ?: return@filter false
                            !due.isBefore(today.endExclusive) && due.isBefore(upcomingWindowEnd)
                        }
                        .sortedBy { it.dueAt }
                    _uiState.update { state ->
                        state.copy(
                            loadingTasks = false,
                            todo = snapshot.todo,
                            inProgress = snapshot.inProgress,
                            done = snapshot.done,
                            skipped = snapshot.skipped,
                            upcoming = upcomingTasks,
                            unscheduled = snapshot.unscheduled
                        )
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCalendar() {
        viewModelScope.launch {
            calendarRepository.observeAccessState()
                .flatMapLatest { access ->
                    _uiState.update { state -> state.copy(calendarAccessState = access) }
                    if (access == CalendarAccessState.Granted) {
                        val today = todayRange(zoneId)
                        calendarRepository.observeEventsForRange(today.startInclusive, today.endExclusive)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collectLatest { events ->
                    _uiState.update { state ->
                        state.copy(
                            loadingCalendar = false,
                            alreadyPlanned = events,
                            errorMessage = (state.calendarAccessState as? CalendarAccessState.Error)?.message
                        )
                    }
                }
        }
    }
}

class OverviewViewModelFactory(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OverviewViewModel::class.java)) {
            return OverviewViewModel(
                authRepository = authRepository,
                taskRepository = taskRepository,
                calendarRepository = calendarRepository
            ) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
