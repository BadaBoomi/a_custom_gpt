package com.badaboomi.acustomgpt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RunResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val obj: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("thread_id") val threadId: String,
    @SerializedName("assistant_id") val assistantId: String,
    @SerializedName("status") val status: String
)
