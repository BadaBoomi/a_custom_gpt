package com.badaboomi.acustomgpt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MessageResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val obj: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("thread_id") val threadId: String,
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: List<ContentItem>
) {
    data class ContentItem(
        @SerializedName("type") val type: String,
        @SerializedName("text") val text: TextValue?
    ) {
        data class TextValue(
            @SerializedName("value") val value: String
        )
    }

    fun getTextContent(): String =
        content.firstOrNull { it.type == "text" }?.text?.value ?: ""
}
