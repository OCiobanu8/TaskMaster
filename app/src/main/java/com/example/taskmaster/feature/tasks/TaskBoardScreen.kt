package com.example.taskmaster.feature.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBoardScreen(
    contentPadding: PaddingValues,
    uiState: TaskBoardUiState,
    onAddTask: (String, Instant) -> Unit,
    onAdvanceTask: (TaskInstance) -> Unit,
    onSkipTask: (TaskInstance) -> Unit
) {
    var showCreateSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateSheet = true }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.loading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onAdvanceTask = { onAdvanceTask(task) },
                        onSkipTask = { onSkipTask(task) }
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateTaskSheet(
            loading = uiState.loading,
            onDismiss = { showCreateSheet = false },
            onCreateTask = { title, dueAt ->
                onAddTask(title, dueAt)
                showCreateSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTaskSheet(
    loading: Boolean,
    onDismiss: () -> Unit,
    onCreateTask: (String, Instant) -> Unit
) {
    val context = LocalContext.current
    var taskTitle by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    val dateText = selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "Pick due date"
    val timeText = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Optional time"

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Create task")
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Task title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val now = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(dateText)
            }

            Button(
                onClick = {
                    val now = Calendar.getInstance()
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            selectedTime = LocalTime.of(hourOfDay, minute)
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(timeText)
            }

            val dueInstant = selectedDate?.let { date ->
                val time = selectedTime ?: LocalTime.of(9, 0)
                date.atTime(time).atZone(ZoneId.systemDefault()).toInstant()
            }
            Button(
                onClick = { onCreateTask(taskTitle, dueInstant!!) },
                enabled = taskTitle.isNotBlank() && dueInstant != null && !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add task")
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskInstance,
    onAdvanceTask: () -> Unit,
    onSkipTask: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.title)
            Text(text = "Status: ${task.status.name}")
            task.dueAt?.let {
                val dueText = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .format(it.atZone(ZoneId.systemDefault()))
                Text(text = "Due: $dueText")
            } ?: Text(text = "Due: Unscheduled")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAdvanceTask,
                    enabled = task.status != TaskStatus.DONE && task.status != TaskStatus.SKIPPED
                ) {
                    Text("Advance")
                }
                TextButton(onClick = onSkipTask, enabled = task.status != TaskStatus.SKIPPED) {
                    Text("Skip")
                }
            }
        }
    }
}
