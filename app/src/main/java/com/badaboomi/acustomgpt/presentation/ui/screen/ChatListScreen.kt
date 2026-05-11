
package com.badaboomi.acustomgpt.presentation.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.badaboomi.acustomgpt.domain.model.Chat
import com.badaboomi.acustomgpt.domain.model.Room
import com.badaboomi.acustomgpt.presentation.viewmodel.ChatListViewModel

// Hilfsfunktion zum Parsen der Starters-Markdown-Tabelle
private fun parseStartersTable(md: String): List<Pair<String, String>> {
    return md.lines()
        .drop(2) // Kopfzeile und Trennzeile überspringen
        .mapNotNull {
            val cols = it.split("|").map { s -> s.trim() }.filter { s -> s.isNotEmpty() }
            if (cols.size >= 2) Pair(cols[0], cols[1]) else null
        }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (Chat, String?) -> Unit,
    onBack: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showStarterDialog by remember { mutableStateOf(false) }
    var starterPrompts by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var selectedStarter by remember { mutableStateOf<Pair<String, String>?>(null) }
    var promptText by remember { mutableStateOf("") }
    var chatToRename by remember { mutableStateOf<Chat?>(null) }
    var chatToDelete by remember { mutableStateOf<Chat?>(null) }
    var chatToMove by remember { mutableStateOf<Chat?>(null) }
    var pendingNavigation: Pair<String, String?>? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val startersMd = viewModel.getStartersMarkdown()
                starterPrompts = parseStartersTable(startersMd)
                showStarterDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Neuer Chat")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.chats.isEmpty() -> Text(
                    "Noch keine Chats. Erstelle einen mit +",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.chats) { chat ->
                        ChatListItem(
                            chat = chat,
                            onClick = { onChatClick(chat, null) },
                            onRename = { chatToRename = chat },
                            onDelete = { chatToDelete = chat },
                            onMove = { chatToMove = chat }
                        )
                        HorizontalDivider()
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

    if (showStarterDialog) {
        AlertDialog(
            onDismissRequest = { showStarterDialog = false },
            title = { Text("Starte mit Vorlage") },
            text = {
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStarter = "Ohne Vorlage" to ""
                                    promptText = ""
                                    showStarterDialog = false
                                    showCreateDialog = true
                                }
                                .padding(8.dp)
                                .background(
                                    if (selectedStarter?.first == "Ohne Vorlage") Color.LightGray else Color.Transparent
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ohne Vorlage", fontWeight = FontWeight.Bold)
                            Text("", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    items(starterPrompts) { (zweck, prompt) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStarter = zweck to prompt
                                    promptText = prompt
                                    showStarterDialog = false
                                    showCreateDialog = true
                                }
                                .padding(8.dp)
                                .background(if (selectedStarter?.first == zweck) Color.LightGray else Color.Transparent),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(zweck, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(prompt, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStarterDialog = false }) { Text("Abbrechen") }
            }
        )
    }

    if (showCreateDialog) {
        InputDialogWithPrompt(
            title = "Neuer Chat",
            label = "Chat-Name",
            promptLabel = "Ausgewählter Prompt",
            initialPrompt = promptText,
            onConfirm = { name ->
                val selectedPrompt = promptText.ifBlank { null }
                viewModel.createChat(name) { newChatId ->
                    pendingNavigation = newChatId to selectedPrompt
                }
                showCreateDialog = false
                promptText = ""
            },
            onDismiss = {
                showCreateDialog = false
                promptText = ""
            }
        )
    }

    // Navigation nach Chat-Erstellung
    LaunchedEffect(pendingNavigation) {
        pendingNavigation?.let { (chatId, initialPrompt) ->
            val chat = Chat(
                id = chatId,
                roomId = "",
                name = "",
                threadId = "",
                createdAt = 0L
            )
            onChatClick(chat, initialPrompt)
            pendingNavigation = null
        }
    }

    chatToRename?.let { chat ->
        InputDialog(
            title = "Rename Chat",
            label = "New name",
            initialValue = chat.name,
            onConfirm = { newName ->
                viewModel.renameChat(chat, newName)
                chatToRename = null
            },
            onDismiss = { chatToRename = null }
        )
    }

    chatToDelete?.let { chat ->
        ConfirmDeleteDialog(
            itemName = chat.name,
            onConfirm = {
                viewModel.deleteChat(chat)
                chatToDelete = null
            },
            onDismiss = { chatToDelete = null }
        )
    }

    chatToMove?.let { chat ->
        MoveToRoomDialog(
            rooms = uiState.rooms,
            currentRoomId = chat.roomId,
            onConfirm = { targetRoomId ->
                viewModel.moveChatToRoom(chat, targetRoomId)
                chatToMove = null
            },
            onDismiss = { chatToMove = null }
        )
    }
}

// Dialog mit Chat-Name und editierbarem Prompt
@Composable
private fun InputDialogWithPrompt(
    title: String,
    label: String,
    promptLabel: String,
    initialPrompt: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(label) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = initialPrompt,
                    onValueChange = {},
                    label = { Text(promptLabel) },
                    maxLines = 3,
                    readOnly = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

@Composable
private fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit
) {
    ListItem(
        headlineContent = { Text(chat.name) },
        trailingContent = {
            Row {
                IconButton(onClick = onRename) {
                    Icon(Icons.Default.Edit, contentDescription = "Umbenennen")
                }
                IconButton(onClick = onMove) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "In Raum verschieben")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Löschen")
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun MoveToRoomDialog(
    rooms: List<Room>,
    currentRoomId: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRoomId by remember { mutableStateOf(currentRoomId) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("In Raum verschieben") },
        text = {
            LazyColumn {
                items(rooms) { room ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedRoomId == room.id,
                            onClick = { selectedRoomId = room.id }
                        )
                        Text(room.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedRoomId) },
                enabled = selectedRoomId != currentRoomId
            ) { Text("Verschieben") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
