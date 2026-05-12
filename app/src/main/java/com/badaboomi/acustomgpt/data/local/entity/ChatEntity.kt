package com.badaboomi.acustomgpt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val name: String,
    val threadId: String,
    val createdAt: Long
)
