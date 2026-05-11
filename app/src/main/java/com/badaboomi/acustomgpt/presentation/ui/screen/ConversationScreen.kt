package com.badaboomi.acustomgpt.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.badaboomi.acustomgpt.domain.model.Message
import com.badaboomi.acustomgpt.domain.model.Message.Companion.ROLE_USER
import com.badaboomi.acustomgpt.presentation.ui.theme.AssistantBubble
import com.badaboomi.acustomgpt.presentation.ui.theme.UserBubble
import com.badaboomi.acustomgpt.presentation.viewmodel.ConversationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    onBack: () -> Unit,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.chat?.name ?: "Conversation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        maxLines = 4,
                        enabled = !uiState.isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = viewModel::sendMessage,
                        enabled = !uiState.isLoading && uiState.inputText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.messages.isEmpty() && !uiState.isLoading -> {
                    Text(
                        "No messages yet. Start the conversation!",
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
                    action = { TextButton(onClick = viewModel::clearError) { Text("Dismiss") } }
                ) { Text(error) }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isUser = message.role == ROLE_USER
    val displayText = parseAntwortField(message.content)
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
    return if (trimmed.startsWith("{") && trimmed.endsWith("}") && trimmed.contains("\"antwort\"")) {
        // Versuche das Feld 'antwort' zu extrahieren
        val regex = Regex("\"antwort\"\\s*:\\s*\"(.*?)\"")
        val match = regex.find(trimmed)
        match?.groups?.get(1)?.value ?: text
    } else {
        text
    }
}
