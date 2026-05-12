package com.badaboomi.acustomgpt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.badaboomi.acustomgpt.data.local.dao.ChatDao
import com.badaboomi.acustomgpt.data.local.dao.MessageDao
import com.badaboomi.acustomgpt.data.local.dao.RoomDao
import com.badaboomi.acustomgpt.data.local.entity.ChatEntity
import com.badaboomi.acustomgpt.data.local.entity.MessageEntity
import com.badaboomi.acustomgpt.data.local.entity.RoomEntity

@Database(
    entities = [RoomEntity::class, ChatEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}
