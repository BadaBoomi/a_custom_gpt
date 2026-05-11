package com.badaboomi.acustomgpt.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badaboomi.acustomgpt.domain.model.Chat
import com.badaboomi.acustomgpt.domain.model.Room
import com.badaboomi.acustomgpt.domain.repository.ChatRepository
import com.badaboomi.acustomgpt.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val rooms: List<Room> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: String = checkNotNull(savedStateHandle["roomId"])

    private val _uiState = MutableStateFlow(ChatListUiState(isLoading = true))
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatRepository.getChatsForRoom(roomId).collect { chats ->
                _uiState.value = _uiState.value.copy(chats = chats, isLoading = false)
            }
        }
        viewModelScope.launch {
            chatRepository.getAllRooms().collect { rooms ->
                _uiState.value = _uiState.value.copy(rooms = rooms)
            }
        }
    }

    fun createChat(name: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val chat = chatRepository.createChat(roomId, name)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onCreated(chat.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun renameChat(chat: Chat, newName: String) {
        viewModelScope.launch {
            try {
                chatRepository.renameChat(chat, newName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteChat(chat: Chat) {
        viewModelScope.launch {
            try {
                chatRepository.deleteChat(chat)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun moveChatToRoom(chat: Chat, targetRoomId: String) {
        viewModelScope.launch {
            try {
                chatRepository.moveChatToRoom(chat, targetRoomId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getStartersMarkdown(): String = settingsRepository.getStarters().orEmpty()
}
