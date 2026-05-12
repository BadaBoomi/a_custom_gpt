package com.badaboomi.acustomgpt.domain.repository

interface SettingsRepository {
    fun getApiKey(): String?
    fun saveApiKey(apiKey: String)
    fun getPromptId(): String?
    fun savePromptId(promptId: String)
    fun getVectorStoreIds(): List<String>
    fun saveVectorStoreIds(ids: List<String>)
    fun isSetupComplete(): Boolean
    fun saveStarters(starters: String)
    fun getStarters(): String?
}
