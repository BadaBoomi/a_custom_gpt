package com.badaboomi.acustomgpt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ThreadResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val obj: String,
    @SerializedName("created_at") val createdAt: Long
)
