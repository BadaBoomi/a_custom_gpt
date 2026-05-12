package com.badaboomi.acustomgpt.domain.model

data class Chat(
    val id: String,
    val roomId: String,
    val name: String,
    val threadId: String,
    val createdAt: Long
)
