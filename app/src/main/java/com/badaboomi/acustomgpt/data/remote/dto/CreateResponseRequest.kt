package com.badaboomi.acustomgpt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateResponseRequest(
    @SerializedName("model") val model: String? = null,
    @SerializedName("input") val input: List<InputItem>,
    @SerializedName("conversation") val conversation: String? = null,
    @SerializedName("prompt") val prompt: PromptRef? = null,
    @SerializedName("tools") val tools: List<Tool>? = null
) {
    data class InputItem(
        @SerializedName("role") val role: String,
        @SerializedName("content") val content: String
    )

    data class PromptRef(
        @SerializedName("id") val id: String
    )

    data class Tool(
        @SerializedName("type") val type: String,
        @SerializedName("vector_store_ids") val vectorStoreIds: List<String>? = null
    )
}
