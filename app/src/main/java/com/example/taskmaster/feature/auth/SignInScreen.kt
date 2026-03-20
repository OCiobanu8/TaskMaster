package com.example.taskmaster.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SignInScreen(
    uiState: AuthUiState,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TaskMaster",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Sign in with Google to start managing projects and tasks.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Button(
            onClick = onSignInClick,
            enabled = !uiState.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign in with Google")
        }

        if (uiState.loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
