package com.badaboomi.acustomgpt.data.remote

import com.badaboomi.acustomgpt.data.remote.dto.CreateMessageRequest
import com.badaboomi.acustomgpt.data.remote.dto.CreateRunRequest
import com.badaboomi.acustomgpt.data.remote.dto.MessageListResponse
import com.badaboomi.acustomgpt.data.remote.dto.MessageResponse
import com.badaboomi.acustomgpt.data.remote.dto.RunResponse
import com.badaboomi.acustomgpt.data.remote.dto.ThreadResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OpenAiApiService {
    @POST("threads")
    suspend fun createThread(): ThreadResponse

    @POST("threads/{threadId}/messages")
    suspend fun addMessage(
        @Path("threadId") threadId: String,
        @Body request: CreateMessageRequest
    ): MessageResponse

    @POST("threads/{threadId}/runs")
    suspend fun createRun(
        @Path("threadId") threadId: String,
        @Body request: CreateRunRequest
    ): RunResponse

    @GET("threads/{threadId}/runs/{runId}")
    suspend fun getRun(
        @Path("threadId") threadId: String,
        @Path("runId") runId: String
    ): RunResponse

    @GET("threads/{threadId}/messages")
    suspend fun getMessages(
        @Path("threadId") threadId: String
    ): MessageListResponse
}
