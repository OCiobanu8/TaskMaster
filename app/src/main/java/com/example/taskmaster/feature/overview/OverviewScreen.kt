package com.example.taskmaster.feature.overview

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.taskmaster.core.calendar.CalendarAccessState
import com.example.taskmaster.core.calendar.CalendarEvent
import com.example.taskmaster.core.model.TaskInstance
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OverviewScreen(
    uiState: OverviewUiState,
    contentPadding: PaddingValues,
    onTaskClick: (TaskInstance) -> Unit,
    onCalendarPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onCalendarPermissionResult
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            onCalendarPermissionResult(true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(text = "Already Planned")
        }
        item {
            when (uiState.calendarAccessState) {
                CalendarAccessState.Unknown,
                CalendarAccessState.PermissionDenied -> {
                    PermissionCard(
                        onGrantPermission = {
                            permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                        }
                    )
                }

                is CalendarAccessState.Error -> {
                    InfoCard(message = "Calendar error: ${uiState.calendarAccessState.message}")
                }

                CalendarAccessState.Granted -> {
                    if (uiState.alreadyPlanned.isEmpty()) {
                        InfoCard(message = "No calendar events planned for today.")
                    }
                }
            }
        }

        items(uiState.alreadyPlanned, key = { it.id }) { event ->
            CalendarEventCard(event = event)
        }

        item {
            SectionTitle(text = "Status Snapshot")
        }

        item {
            TaskGroupCard(label = "To Do", tasks = uiState.todo, onTaskClick = onTaskClick)
        }
        item {
            TaskGroupCard(label = "In Progress", tasks = uiState.inProgress, onTaskClick = onTaskClick)
        }
        item {
            TaskGroupCard(label = "Done", tasks = uiState.done, onTaskClick = onTaskClick)
        }
        item {
            TaskGroupCard(label = "Skipped", tasks = uiState.skipped, onTaskClick = onTaskClick)
        }

        item {
            SectionTitle(text = "Upcoming (7 Days)")
        }
        item {
            TaskGroupCard(label = "Upcoming", tasks = uiState.upcoming, onTaskClick = onTaskClick)
        }

        if (uiState.unscheduled.isNotEmpty()) {
            item {
                SectionTitle(text = "Unscheduled")
            }
            item {
                TaskGroupCard(label = "No due date", tasks = uiState.unscheduled, onTaskClick = onTaskClick)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun PermissionCard(onGrantPermission: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Enable calendar permission to show today's planned events.")
            TextButton(onClick = onGrantPermission) {
                Text("Grant calendar access")
            }
        }
    }
}

@Composable
private fun InfoCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun CalendarEventCard(event: CalendarEvent) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val zoneId = ZoneId.systemDefault()
    val start = formatter.format(event.startAt.atZone(zoneId))
    val end = formatter.format(event.endAt.atZone(zoneId))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "$start - $end")
            if (event.source.isNotBlank()) {
                Text(text = event.source, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TaskGroupCard(
    label: String,
    tasks: List<TaskInstance>,
    onTaskClick: (TaskInstance) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "$label (${tasks.size})", style = MaterialTheme.typography.titleMedium)
            if (tasks.isEmpty()) {
                Text("No tasks")
            } else {
                tasks.forEach { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTaskClick(task) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Created: ${formatShortDate(task.createdAt)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Due: ${formatShortDate(task.dueAt)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatShortDate(instant: Instant?): String {
    if (instant == null) return "Unscheduled"
    return DateTimeFormatter.ofPattern("MMM d, HH:mm")
        .format(instant.atZone(ZoneId.systemDefault()))
}
