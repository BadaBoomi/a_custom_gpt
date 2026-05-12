package com.badaboomi.acustomgpt.data.repository

import com.badaboomi.acustomgpt.data.local.dao.ChatDao
import com.badaboomi.acustomgpt.data.local.dao.MessageDao
import com.badaboomi.acustomgpt.data.local.dao.RoomDao
import com.badaboomi.acustomgpt.data.local.entity.ChatEntity
import com.badaboomi.acustomgpt.data.local.entity.MessageEntity
import com.badaboomi.acustomgpt.data.local.entity.RoomEntity
import com.badaboomi.acustomgpt.data.remote.OpenAiApiService
import com.badaboomi.acustomgpt.data.remote.dto.CreateResponseRequest
import com.badaboomi.acustomgpt.domain.model.Chat
import com.badaboomi.acustomgpt.domain.model.Message
import com.badaboomi.acustomgpt.domain.model.Message.Companion.ROLE_ASSISTANT
import com.badaboomi.acustomgpt.domain.model.Message.Companion.ROLE_USER
import com.badaboomi.acustomgpt.domain.model.Room
import com.badaboomi.acustomgpt.domain.repository.ChatRepository
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
        private const val RESPONSE_STATUS_COMPLETED = "completed"
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
        val conversation = apiService.createConversation()
        val chatEntity = ChatEntity(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            name = name,
            threadId = conversation.id,
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

    override suspend fun sendMessage(chat: Chat, userText: String, promptId: String, vectorStoreIds: List<String>) {
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

        val tools = if (vectorStoreIds.isNotEmpty()) {
            listOf(CreateResponseRequest.Tool(type = "file_search", vectorStoreIds = vectorStoreIds))
        } else null

        val request = CreateResponseRequest(
            prompt = CreateResponseRequest.PromptRef(id = promptId),
            input = listOf(CreateResponseRequest.InputItem(role = ROLE_USER, content = contentWithUser)),
            conversation = chat.threadId,
            tools = tools
        )
        if (ToolConfig.logModelPayload) {
            Log.d("ModelPayload", "createResponse: " + Gson().toJson(request))
        }

        val response = apiService.createResponse(request)

        if (ToolConfig.logModelPayload) {
            Log.d("ModelPayload", "response.status: ${response.status}")
            Log.d("ModelPayload", "response.output: " + Gson().toJson(response.output))
        }

        if (response.status != RESPONSE_STATUS_COMPLETED) {
            error("Response did not complete (status=${response.status})")
        }

        val now = System.currentTimeMillis()
        val newMessages = response.output
            .filterIndexed { msgIndex, item ->
                item.type == "message" && item.role == ROLE_ASSISTANT
            }
            .flatMapIndexed { msgIndex, item ->
                val parts = splitAssistantResponse(item.getTextContent())
                parts.mapIndexed { partIndex, part ->
                    MessageEntity(
                        id = if (partIndex == 0) item.id else "${item.id}_$partIndex",
                        chatId = chat.id,
                        role = ROLE_ASSISTANT,
                        content = part,
                        createdAt = now + msgIndex * 10L + partIndex
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
