package com.example.taskmaster.feature.tasks

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskmaster.core.model.TaskInstance
import com.example.taskmaster.core.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBoardScreen(
    projectName: String,
    uiState: TaskBoardUiState,
    onAddTask: (String) -> Unit,
    onAdvanceTask: (TaskInstance) -> Unit,
    onSkipTask: (TaskInstance) -> Unit,
    onBack: () -> Unit
) {
    var taskTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(projectName) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task title") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        onAddTask(taskTitle)
                        taskTitle = ""
                    },
                    enabled = taskTitle.isNotBlank() && !uiState.loading
                ) {
                    Text("Add")
                }
            }

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
                contentPadding = PaddingValues(top = 16.dp),
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
