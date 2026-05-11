package com.badaboomi.acustomgpt.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badaboomi.acustomgpt.domain.model.Chat
import com.badaboomi.acustomgpt.domain.model.Message
import com.badaboomi.acustomgpt.domain.repository.ChatRepository
import com.badaboomi.acustomgpt.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationUiState(
    val chat: Chat? = null,
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])
    private val initialPrompt: String = savedStateHandle["initialPrompt"] ?: ""

    private val _uiState = MutableStateFlow(
        ConversationUiState(
            isLoading = true,
            inputText = initialPrompt
        )
    )
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val chat = chatRepository.getChatById(chatId)
            _uiState.value = _uiState.value.copy(chat = chat, isLoading = false)
        }
        viewModelScope.launch {
            chatRepository.getMessagesForChat(chatId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val state = _uiState.value
        val chat = state.chat ?: return
        val text = state.inputText.trim()
        if (text.isBlank()) return

        val assistantId = settingsRepository.getAssistantId() ?: return

        _uiState.value = state.copy(inputText = "", isLoading = true, error = null)

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(chat, text, assistantId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
