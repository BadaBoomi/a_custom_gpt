package com.badaboomi.acustomgpt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ResponseApiResult(
    @SerializedName("id") val id: String,
    @SerializedName("object") val obj: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("status") val status: String,
    @SerializedName("output") val output: List<OutputItem>
) {
    data class OutputItem(
        @SerializedName("id") val id: String,
        @SerializedName("type") val type: String,
        @SerializedName("role") val role: String?,
        @SerializedName("status") val status: String?,
        @SerializedName("content") val content: List<ContentItem>?
    ) {
        data class ContentItem(
            @SerializedName("type") val type: String,
            @SerializedName("text") val text: String?
        )

        fun getTextContent(): String =
            content?.firstOrNull { it.type == "output_text" }?.text ?: ""
    }
}
