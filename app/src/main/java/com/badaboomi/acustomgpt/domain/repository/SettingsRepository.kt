package com.badaboomi.acustomgpt.domain.repository

interface SettingsRepository {
    fun getApiKey(): String?
    fun saveApiKey(apiKey: String)
    fun getAssistantId(): String?
    fun saveAssistantId(assistantId: String)
    fun isSetupComplete(): Boolean
}
