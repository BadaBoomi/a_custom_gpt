
package com.badaboomi.acustomgpt.presentation.ui.screen
import androidx.compose.material.icons.filled.Send

import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface

import androidx.compose.material3.ExperimentalMaterial3Api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonElement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.badaboomi.acustomgpt.domain.model.Message
import com.badaboomi.acustomgpt.domain.model.Message.Companion.ROLE_USER
import com.badaboomi.acustomgpt.presentation.ui.theme.AssistantBubble
import com.badaboomi.acustomgpt.presentation.ui.theme.UserBubble
import com.badaboomi.acustomgpt.presentation.viewmodel.ConversationViewModel

// Extrahiere alle Werte aus JSON, falls vorhanden, sonst gib den Text zurück
private fun extractJsonValuesOrText(text: String): String {
    val trimmed = text.trim()
    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
        return try {
            val json = kotlinx.serialization.json.Json.parseToJsonElement(trimmed)
            fun extractAllValues(element: kotlinx.serialization.json.JsonElement): List<String> = when {
                element is kotlinx.serialization.json.JsonPrimitive && !element.isString -> listOf(element.toString())
                element is kotlinx.serialization.json.JsonPrimitive -> listOf(element.content)
                element is kotlinx.serialization.json.JsonObject -> element.values.flatMap { extractAllValues(it) }
                element is kotlinx.serialization.json.JsonArray -> element.flatMap { extractAllValues(it) }
                else -> emptyList()
            }
            val values = extractAllValues(json)
            if (values.isNotEmpty()) values.joinToString("\n") else text
        } catch (e: Exception) {
            // Fallback: Versuche einfache Regex für Werte
            val regex = Regex(":\\s*\"(.*?)\"")
            val matches = regex.findAll(trimmed).map { it.groupValues[1] }.toList()
            if (matches.isNotEmpty()) matches.joinToString("\n") else text
        }
    }
    return text
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConversationScreen(
    onBack: () -> Unit,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val starterPrompts = remember { viewModel.getStarterPrompts() }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(uiState.chat?.name ?: "Konversation")
                        if (uiState.roomName.isNotBlank()) {
                            Text(
                                text = uiState.roomName,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (starterPrompts.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                starterPrompts.forEach { (zweck, prompt) ->
                                    Surface(
                                        onClick = { viewModel.onInputChange(prompt) },
                                        shape = RoundedCornerShape(16.dp),
                                        color = AssistantBubble,
                                        enabled = !uiState.isLoading
                                    ) {
                                        Text(
                                            text = zweck,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            color = Color.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = viewModel::onInputChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nachricht eingeben…") },
                            maxLines = 4,
                            enabled = !uiState.isLoading
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = viewModel::sendMessage,
                        enabled = !uiState.isLoading && uiState.inputText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Senden")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.messages.isEmpty() && !uiState.isLoading -> {
                    Text(
                        "Noch keine Nachrichten. Starte die Unterhaltung!",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.messages) { message ->
                            MessageBubble(message = message)
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = { TextButton(onClick = viewModel::clearError) { Text("Schließen") } }
                ) { Text(error) }
            }
        }
    }

}

@Composable
private fun MessageBubble(message: Message) {
    val isUser = message.role == ROLE_USER
    val displayText = extractJsonValuesOrText(message.content)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isUser) UserBubble else AssistantBubble,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = displayText,
                color = if (isUser) Color.White else Color.Black
            )
        }
    }
}

private fun parseAntwortField(text: String): String {
    val trimmed = text.trim()
    // Versuche JSON zu erkennen und das Feld 'antwort' zu extrahieren
    return if ((trimmed.startsWith("{") && trimmed.endsWith("}")) && trimmed.contains("\"antwort\"")) {
        try {
            // Nutze kotlinx.serialization für robustes Parsing
            val json = kotlinx.serialization.json.Json.parseToJsonElement(trimmed).jsonObject
            val antwort = json["antwort"]?.jsonPrimitive?.content
            antwort ?: text
        } catch (e: Exception) {
            // Fallback auf Regex falls JSON-Parsing fehlschlägt
            val regex = Regex("\"antwort\"\\s*:\\s*\"(.*?)\"")
            val match = regex.find(trimmed)
            match?.groups?.get(1)?.value ?: text
        }
    } else {
        text
    }
}
