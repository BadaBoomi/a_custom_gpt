package com.badaboomi.acustomgpt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateMessageRequest(
    @SerializedName("role") val role: String = "user",
    @SerializedName("content") val content: String
)
