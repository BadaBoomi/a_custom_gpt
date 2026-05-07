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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.badaboomi.acustomgpt.domain.model.Chat
import com.badaboomi.acustomgpt.domain.model.Room
import com.badaboomi.acustomgpt.presentation.viewmodel.ChatListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (Chat) -> Unit,
    onBack: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var chatToRename by remember { mutableStateOf<Chat?>(null) }
    var chatToDelete by remember { mutableStateOf<Chat?>(null) }
    var chatToMove by remember { mutableStateOf<Chat?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.chats.isEmpty() -> Text(
                    "No chats yet. Create one with +",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.chats) { chat ->
                        ChatListItem(
                            chat = chat,
                            onClick = { onChatClick(chat) },
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
                    action = { TextButton(onClick = viewModel::clearError) { Text("Dismiss") } }
                ) { Text(error) }
            }
        }
    }

    if (showCreateDialog) {
        InputDialog(
            title = "New Chat",
            label = "Chat name",
            onConfirm = { name ->
                viewModel.createChat(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
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
                    Icon(Icons.Default.Edit, contentDescription = "Rename")
                }
                IconButton(onClick = onMove) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Move to Room")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
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
        title = { Text("Move to Room") },
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
            ) { Text("Move") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
