package com.badaboomi.acustomgpt.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.badaboomi.acustomgpt.presentation.viewmodel.SetupUiState

@Composable
fun SetupScreen(
    uiState: SetupUiState,
    onApiKeyChange: (String) -> Unit,
    onAssistantIdChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Willkommen bei ACustomGPT",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Bitte geben Sie Ihre OpenAI-Anmeldeinformationen ein, um zu beginnen.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = uiState.apiKey,
            onValueChange = onApiKeyChange,
            label = { Text("API-Schlüssel") },
            placeholder = { Text("sk-...") },
            isError = uiState.apiKeyError != null,
            supportingText = uiState.apiKeyError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.userId,
            onValueChange = {},
            label = { Text("User-ID (E-Mail)") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.assistantId,
            onValueChange = onAssistantIdChange,
            label = { Text("Assistant-ID") },
            placeholder = { Text("asst_...") },
            isError = uiState.assistantIdError != null,
            supportingText = uiState.assistantIdError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aktualisieren")
        }
    }
}
