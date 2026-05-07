package com.badaboomi.acustomgpt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateRunRequest(
    @SerializedName("assistant_id") val assistantId: String
)
