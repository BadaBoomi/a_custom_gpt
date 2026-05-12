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

    fun savePromptId(promptId: String) = prefs.edit().putString(KEY_PROMPT_ID, promptId).apply()
    fun getPromptId(): String? = prefs.getString(KEY_PROMPT_ID, null)

    fun isSetupComplete(): Boolean {
        val key = getApiKey()
        val id = getPromptId()
        return !key.isNullOrBlank() && !id.isNullOrBlank()
    }

    fun saveUserEmail(email: String) = prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun saveStarters(starters: String) = prefs.edit().putString(KEY_STARTERS, starters).apply()
    fun getStarters(): String? = prefs.getString(KEY_STARTERS, null)

    fun saveVectorStoreIds(ids: String) = prefs.edit().putString(KEY_VECTOR_STORE_IDS, ids).apply()
    fun getVectorStoreIds(): String? = prefs.getString(KEY_VECTOR_STORE_IDS, null)

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_PROMPT_ID = "prompt_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_STARTERS = "starters"
        private const val KEY_VECTOR_STORE_IDS = "vector_store_ids"
    }
}
