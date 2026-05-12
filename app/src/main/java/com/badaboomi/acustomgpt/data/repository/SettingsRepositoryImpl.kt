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
    override fun getPromptId(): String? = encryptedPrefsManager.getPromptId()
    override fun savePromptId(promptId: String) = encryptedPrefsManager.savePromptId(promptId)
    override fun isSetupComplete(): Boolean = encryptedPrefsManager.isSetupComplete()

    override fun getVectorStoreIds(): List<String> =
        encryptedPrefsManager.getVectorStoreIds()
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    override fun saveVectorStoreIds(ids: List<String>) =
        encryptedPrefsManager.saveVectorStoreIds(ids.joinToString(","))

    override fun saveStarters(starters: String) = encryptedPrefsManager.saveStarters(starters)
    override fun getStarters(): String? = encryptedPrefsManager.getStarters()
}
