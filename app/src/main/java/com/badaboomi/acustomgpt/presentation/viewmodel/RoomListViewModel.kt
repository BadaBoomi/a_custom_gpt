package com.badaboomi.acustomgpt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badaboomi.acustomgpt.domain.model.Room
import com.badaboomi.acustomgpt.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomListUiState(
    val rooms: List<Room> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RoomListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomListUiState(isLoading = true))
    val uiState: StateFlow<RoomListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatRepository.getAllRooms().collect { rooms ->
                _uiState.value = RoomListUiState(rooms = rooms, isLoading = false)
            }
        }
    }

    fun createRoom(name: String) {
        viewModelScope.launch {
            try {
                chatRepository.createRoom(name)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun renameRoom(room: Room, newName: String) {
        viewModelScope.launch {
            try {
                chatRepository.renameRoom(room, newName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteRoom(room: Room) {
        viewModelScope.launch {
            try {
                chatRepository.deleteRoom(room)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
