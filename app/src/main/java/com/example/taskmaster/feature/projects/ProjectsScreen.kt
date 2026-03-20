package com.example.taskmaster.feature.projects

import androidx.compose.foundation.clickable
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
import com.example.taskmaster.core.model.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    uiState: ProjectsUiState,
    onCreateProject: (String) -> Unit,
    onOpenProject: (Project) -> Unit,
    onSignOut: () -> Unit
) {
    var projectName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text("Sign out")
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
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Project name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        onCreateProject(projectName)
                        projectName = ""
                    },
                    enabled = projectName.isNotBlank() && !uiState.loading
                ) {
                    Text("Create")
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
                items(uiState.projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onOpenProject(project) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = project.name)
            Text(text = "Members: ${project.memberIds.size}")
        }
    }
}
