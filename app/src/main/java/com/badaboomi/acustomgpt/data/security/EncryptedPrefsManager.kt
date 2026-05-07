package com.badaboomi.acustomgpt.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPrefsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(apiKey: String) = prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    fun getApiKey(): String? = prefs.getString(KEY_API_KEY, null)

    fun saveAssistantId(assistantId: String) = prefs.edit().putString(KEY_ASSISTANT_ID, assistantId).apply()
    fun getAssistantId(): String? = prefs.getString(KEY_ASSISTANT_ID, null)

    fun isSetupComplete(): Boolean {
        val key = getApiKey()
        val id = getAssistantId()
        return !key.isNullOrBlank() && !id.isNullOrBlank()
    }

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_ASSISTANT_ID = "assistant_id"
    }
}
