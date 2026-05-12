package com.badaboomi.acustomgpt.di

import android.content.Context
import androidx.room.Room
import com.badaboomi.acustomgpt.data.local.AppDatabase
import com.badaboomi.acustomgpt.data.local.dao.ChatDao
import com.badaboomi.acustomgpt.data.local.dao.MessageDao
import com.badaboomi.acustomgpt.data.local.dao.RoomDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "acustomgpt_db").build()

    @Provides
    fun provideRoomDao(db: AppDatabase): RoomDao = db.roomDao()

    @Provides
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()
}
