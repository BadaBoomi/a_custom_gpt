package com.badaboomi.acustomgpt.data.repository

import com.badaboomi.acustomgpt.data.security.EncryptedPrefsManager
import com.badaboomi.acustomgpt.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val encryptedPrefsManager: EncryptedPrefsManager
) : SettingsRepository {
    override fun getApiKey(): String? = encryptedPrefsManager.getApiKey()
    override fun saveApiKey(apiKey: String) = encryptedPrefsManager.saveApiKey(apiKey)
    override fun getAssistantId(): String? = encryptedPrefsManager.getAssistantId()
    override fun saveAssistantId(assistantId: String) = encryptedPrefsManager.saveAssistantId(assistantId)
    override fun isSetupComplete(): Boolean = encryptedPrefsManager.isSetupComplete()

    override fun saveStarters(starters: String) = encryptedPrefsManager.saveStarters(starters)
    override fun getStarters(): String? = encryptedPrefsManager.getStarters()
}
