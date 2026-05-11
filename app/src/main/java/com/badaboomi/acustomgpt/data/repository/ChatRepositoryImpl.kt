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
import com.badaboomi.acustomgpt.domain.model.Message.Companion.ROLE_ASSISTANT
import com.badaboomi.acustomgpt.domain.model.Message.Companion.ROLE_USER
import com.badaboomi.acustomgpt.domain.model.Room
import com.badaboomi.acustomgpt.domain.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.badaboomi.acustomgpt.tools.ToolConfig
import com.google.gson.Gson
import com.google.gson.JsonParser

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val roomDao: RoomDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val apiService: OpenAiApiService,
    private val userRepository: com.badaboomi.acustomgpt.domain.repository.UserRepository
) : ChatRepository {

    companion object {
        /** Interval between run-status poll requests in milliseconds. */
        private const val POLL_INTERVAL_MS = 1500L
        /** Maximum number of poll attempts before giving up on a run. */
        private const val MAX_POLL_ATTEMPTS = 40

        private const val RUN_STATUS_QUEUED = "queued"
        private const val RUN_STATUS_IN_PROGRESS = "in_progress"
        private const val RUN_STATUS_COMPLETED = "completed"
        /** OpenAI timestamps are in seconds; local timestamps are in milliseconds. */
        private const val OPENAI_TIMESTAMP_TO_MS = 1000L
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
            role = ROLE_USER,
            content = userText,
            createdAt = System.currentTimeMillis()
        )
        messageDao.insertMessage(userMessageEntity)


        val userId = userRepository.getUserEmail()?.takeIf { it.isNotBlank() } ?: ""
        val contentWithUser = "[user-id: $userId] $userText"
        val messagePayload = CreateMessageRequest(content = contentWithUser)
        if (ToolConfig.logModelPayload) {
            Log.d("ModelPayload", "addMessage: " + Gson().toJson(messagePayload))
        }
        apiService.addMessage(chat.threadId, messagePayload)

        val runPayload = CreateRunRequest(assistantId = assistantId)
        if (ToolConfig.logModelPayload) {
            Log.d("ModelPayload", "createRun: " + Gson().toJson(runPayload))
        }
        val run = apiService.createRun(chat.threadId, runPayload)

        var runStatus = run.status
        var pollAttempts = 0
        while ((runStatus == RUN_STATUS_QUEUED || runStatus == RUN_STATUS_IN_PROGRESS) && pollAttempts < MAX_POLL_ATTEMPTS) {
            delay(POLL_INTERVAL_MS)
            val updatedRun = apiService.getRun(chat.threadId, run.id)
            runStatus = updatedRun.status
            pollAttempts++
        }

        if (runStatus != RUN_STATUS_COMPLETED) {
            error("Run did not complete (status=$runStatus, attempts=$pollAttempts)")
        }

        val messagesResponse = apiService.getMessages(chat.threadId)
        if (ToolConfig.logModelPayload) {
            val assistantRawMessages = messagesResponse.data.filter { it.role == ROLE_ASSISTANT }
            val assistantTextMessages = assistantRawMessages.map { it.getTextContent() }
            Log.d("ModelPayload", "assistantResponse.raw: " + Gson().toJson(assistantRawMessages))
            Log.d("ModelPayload", "assistantResponse.text: " + Gson().toJson(assistantTextMessages))
        }
        val existingLatest = messageDao.getLatestMessage(chat.id)
        val existingTime = existingLatest?.createdAt ?: 0L

        val newMessages = messagesResponse.data
            .filter { it.role == ROLE_ASSISTANT && it.createdAt * OPENAI_TIMESTAMP_TO_MS > existingTime }
            .flatMap { msg ->
                val parts = splitAssistantResponse(msg.getTextContent())
                parts.mapIndexed { index, part ->
                    MessageEntity(
                        id = if (index == 0) msg.id else "${msg.id}_$index",
                        chatId = chat.id,
                        role = msg.role,
                        content = part,
                        createdAt = (msg.createdAt * OPENAI_TIMESTAMP_TO_MS) + index
                    )
                }
            }
        if (newMessages.isNotEmpty()) {
            messageDao.insertMessages(newMessages)
        }
    }

    /**
     * Splitte Antworten mit führendem JSON-Objekt wie {"message":"..."} in
     * 1) Begrüßungstext aus "message" und 2) den restlichen Freitext.
     */
    private fun splitAssistantResponse(text: String): List<String> {
        val trimmed = text.trim()
        val leadingJson = extractLeadingJsonObject(trimmed) ?: return listOf(text)
        val (jsonPart, remainingText) = leadingJson

        val messageFromJson = try {
            val jsonElement = JsonParser.parseString(jsonPart)
            if (jsonElement.isJsonObject) {
                val message = jsonElement.asJsonObject.get("message")
                if (message != null && message.isJsonPrimitive && message.asJsonPrimitive.isString) {
                    message.asString.trim()
                } else {
                    ""
                }
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }

        if (messageFromJson.isBlank()) {
            return listOf(text)
        }

        val result = mutableListOf<String>()
        result.add(messageFromJson)
        if (remainingText.isNotBlank()) {
            result.add(remainingText)
        }
        return result
    }

    /**
     * Extrahiert ein führendes JSON-Objekt inkl. Resttext durch einfache
     * Klammerzählung (beachtet Strings/Escapes).
     */
    private fun extractLeadingJsonObject(text: String): Pair<String, String>? {
        val start = text.indexOfFirst { !it.isWhitespace() }
        if (start == -1 || text[start] != '{') return null

        var depth = 0
        var inString = false
        var escaped = false

        for (i in start until text.length) {
            val ch = text[i]
            if (escaped) {
                escaped = false
                continue
            }
            when {
                ch == '\\' && inString -> escaped = true
                ch == '"' -> inString = !inString
                !inString && ch == '{' -> depth++
                !inString && ch == '}' -> {
                    depth--
                    if (depth == 0) {
                        val jsonPart = text.substring(start, i + 1)
                        val remaining = text.substring(i + 1).trim()
                        return jsonPart to remaining
                    }
                }
            }
        }
        return null
    }

    private fun RoomEntity.toDomain() = Room(id, name, createdAt)
    private fun ChatEntity.toDomain() = Chat(id, roomId, name, threadId, createdAt)
    private fun MessageEntity.toDomain() = Message(id, chatId, role, content, createdAt)
}
