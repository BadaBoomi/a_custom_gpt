package com.badaboomi.acustomgpt.domain.model

data class Message(
    val id: String,
    val chatId: String,
    val role: String,
    val content: String,
    val createdAt: Long
)
