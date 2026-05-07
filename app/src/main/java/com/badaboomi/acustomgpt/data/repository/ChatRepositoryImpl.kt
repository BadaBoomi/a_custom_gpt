package com.badaboomi.acustomgpt.data.repository

import com.badaboomi.acustomgpt.data.local.dao.ChatDao
import com.badaboomi.acustomgpt.data.local.dao.MessageDao
import com.badaboomi.acustomgpt.data.local.dao.RoomDao
import com.badaboomi.acustomgpt.data.local.entity.ChatEntity
import com.badaboomi.acustomgpt.data.local.entity.MessageEntity
import com.badaboomi.acustomgpt.data.local.entity.RoomEntity
import com.badaboomi.acustomgpt.data.remote.OpenAiApiService
import com.badaboomi.acustomgpt.data.remote.dto.CreateMessageRequest
import com.badaboomi.acustomgpt.data.remote.dto.CreateRunRequest
import com.badaboomi.acustomgpt.domain.model.Chat
import com.badaboomi.acustomgpt.domain.model.Message
import com.badaboomi.acustomgpt.domain.model.Room
import com.badaboomi.acustomgpt.domain.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val roomDao: RoomDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val apiService: OpenAiApiService
) : ChatRepository {

    companion object {
        /** Interval between run-status poll requests in milliseconds. */
        private const val POLL_INTERVAL_MS = 1500L
        /** Maximum number of poll attempts before giving up on a run. */
        private const val MAX_POLL_ATTEMPTS = 40
    }

    override fun getAllRooms(): Flow<List<Room>> =
        roomDao.getAllRooms().map { list -> list.map { it.toDomain() } }

    override suspend fun createRoom(name: String) {
        roomDao.insertRoom(RoomEntity(UUID.randomUUID().toString(), name, System.currentTimeMillis()))
    }

    override suspend fun renameRoom(room: Room, newName: String) {
        roomDao.updateRoom(RoomEntity(room.id, newName, room.createdAt))
    }

    override suspend fun deleteRoom(room: Room) {
        chatDao.deleteChatsForRoom(room.id)
        roomDao.deleteRoom(RoomEntity(room.id, room.name, room.createdAt))
    }

    override fun getChatsForRoom(roomId: String): Flow<List<Chat>> =
        chatDao.getChatsForRoom(roomId).map { list -> list.map { it.toDomain() } }

    override suspend fun createChat(roomId: String, name: String): Chat {
        val thread = apiService.createThread()
        val chatEntity = ChatEntity(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            name = name,
            threadId = thread.id,
            createdAt = System.currentTimeMillis()
        )
        chatDao.insertChat(chatEntity)
        return chatEntity.toDomain()
    }

    override suspend fun renameChat(chat: Chat, newName: String) {
        chatDao.updateChat(ChatEntity(chat.id, chat.roomId, newName, chat.threadId, chat.createdAt))
    }

    override suspend fun deleteChat(chat: Chat) {
        messageDao.deleteMessagesForChat(chat.id)
        chatDao.deleteChat(ChatEntity(chat.id, chat.roomId, chat.name, chat.threadId, chat.createdAt))
    }

    override suspend fun moveChatToRoom(chat: Chat, newRoomId: String) {
        chatDao.updateChat(ChatEntity(chat.id, newRoomId, chat.name, chat.threadId, chat.createdAt))
    }

    override suspend fun getChatById(chatId: String): Chat? =
        chatDao.getChatById(chatId)?.toDomain()

    override fun getMessagesForChat(chatId: String): Flow<List<Message>> =
        messageDao.getMessagesForChat(chatId).map { list -> list.map { it.toDomain() } }

    override suspend fun sendMessage(chat: Chat, userText: String, assistantId: String) {
        val userMessageEntity = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chat.id,
            role = "user",
            content = userText,
            createdAt = System.currentTimeMillis()
        )
        messageDao.insertMessage(userMessageEntity)

        apiService.addMessage(chat.threadId, CreateMessageRequest(content = userText))

        val run = apiService.createRun(chat.threadId, CreateRunRequest(assistantId = assistantId))

        var runStatus = run.status
        var pollAttempts = 0
        while ((runStatus == "queued" || runStatus == "in_progress") && pollAttempts < MAX_POLL_ATTEMPTS) {
            delay(POLL_INTERVAL_MS)
            val updatedRun = apiService.getRun(chat.threadId, run.id)
            runStatus = updatedRun.status
            pollAttempts++
        }

        if (runStatus != "completed") {
            error("Run did not complete (status=$runStatus, attempts=$pollAttempts)")
        }

        val messagesResponse = apiService.getMessages(chat.threadId)
        val existingLatest = messageDao.getLatestMessage(chat.id)
        val existingTime = existingLatest?.createdAt ?: 0L

        val newMessages = messagesResponse.data
            .filter { it.role == "assistant" && it.createdAt * 1000 > existingTime }
            .map { msg ->
                MessageEntity(
                    id = msg.id,
                    chatId = chat.id,
                    role = msg.role,
                    content = msg.getTextContent(),
                    createdAt = msg.createdAt * 1000
                )
            }
        if (newMessages.isNotEmpty()) {
            messageDao.insertMessages(newMessages)
        }
    }

    private fun RoomEntity.toDomain() = Room(id, name, createdAt)
    private fun ChatEntity.toDomain() = Chat(id, roomId, name, threadId, createdAt)
    private fun MessageEntity.toDomain() = Message(id, chatId, role, content, createdAt)
}
