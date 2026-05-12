package com.badaboomi.acustomgpt.domain.repository

import com.badaboomi.acustomgpt.domain.model.Chat
import com.badaboomi.acustomgpt.domain.model.Message
import com.badaboomi.acustomgpt.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllRooms(): Flow<List<Room>>
    suspend fun createRoom(name: String)
    suspend fun renameRoom(room: Room, newName: String)
    suspend fun deleteRoom(room: Room)

    fun getChatsForRoom(roomId: String): Flow<List<Chat>>
    suspend fun createChat(roomId: String, name: String): Chat
    suspend fun renameChat(chat: Chat, newName: String)
    suspend fun deleteChat(chat: Chat)
    suspend fun moveChatToRoom(chat: Chat, newRoomId: String)
    suspend fun getChatById(chatId: String): Chat?

    fun getMessagesForChat(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(chat: Chat, userText: String, promptId: String, vectorStoreIds: List<String>)
}
