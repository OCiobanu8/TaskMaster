package com.example.taskmaster.feature.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskmaster.core.model.TaskStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    contentPadding: PaddingValues,
    taskId: String,
    title: String,
    projectId: String,
    assigneeUserId: String,
    dueAt: Instant?,
    createdAt: Instant?,
    status: TaskStatus,
    persistedStatus: TaskStatus,
    savingStatus: Boolean,
    onStatusSelected: (TaskStatus) -> Unit,
    onSaveStatus: () -> Unit,
    errorMessage: String?
) {
    val dueText = dueAt?.let {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .format(it.atZone(ZoneId.systemDefault()))
    } ?: "Unscheduled"
    val createdText = createdAt?.let {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .format(it.atZone(ZoneId.systemDefault()))
    } ?: "Unscheduled"
    var statusExpanded by remember { mutableStateOf(false) }
    val hasUnsavedStatus = status != persistedStatus

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = status.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        TaskStatus.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = {
                                    onStatusSelected(option)
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
                if (hasUnsavedStatus || savingStatus) {
                    Button(
                        onClick = onSaveStatus,
                        enabled = !savingStatus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (savingStatus) "Saving..." else "Save status")
                    }
                }
                Text(text = "Created: $createdText")
                Text(text = "Due: $dueText")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Task ID: $taskId")
                Text(text = "Project ID: $projectId")
                Text(text = "Assignee User ID: $assigneeUserId")
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
