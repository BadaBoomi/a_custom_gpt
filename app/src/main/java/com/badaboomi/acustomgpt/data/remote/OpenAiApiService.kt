package com.badaboomi.acustomgpt.data.remote

import com.badaboomi.acustomgpt.data.remote.dto.ConversationResponse
import com.badaboomi.acustomgpt.data.remote.dto.CreateResponseRequest
import com.badaboomi.acustomgpt.data.remote.dto.ResponseApiResult
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAiApiService {
    @POST("conversations")
    suspend fun createConversation(): ConversationResponse

    @POST("responses")
    suspend fun createResponse(
        @Body request: CreateResponseRequest
    ): ResponseApiResult
}
