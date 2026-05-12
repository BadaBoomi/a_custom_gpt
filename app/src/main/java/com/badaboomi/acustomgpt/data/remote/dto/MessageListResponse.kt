package com.badaboomi.acustomgpt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MessageListResponse(
    @SerializedName("object") val obj: String,
    @SerializedName("data") val data: List<MessageResponse>,
    @SerializedName("first_id") val firstId: String?,
    @SerializedName("last_id") val lastId: String?,
    @SerializedName("has_more") val hasMore: Boolean
)
