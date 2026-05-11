package com.badaboomi.acustomgpt.di

import com.badaboomi.acustomgpt.data.repository.ChatRepositoryImpl
import com.badaboomi.acustomgpt.data.repository.SettingsRepositoryImpl
import com.badaboomi.acustomgpt.domain.repository.ChatRepository
import com.badaboomi.acustomgpt.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: com.badaboomi.acustomgpt.data.repository.UserRepositoryImpl): com.badaboomi.acustomgpt.domain.repository.UserRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindToolRepository(impl: com.badaboomi.acustomgpt.data.repository.ToolRepositoryImpl): com.badaboomi.acustomgpt.domain.repository.ToolRepository
}
