package com.badaboomi.acustomgpt.data.repository

import com.badaboomi.acustomgpt.data.security.EncryptedPrefsManager
import com.badaboomi.acustomgpt.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val encryptedPrefsManager: EncryptedPrefsManager
) : UserRepository {
    override fun getUserEmail(): String? = encryptedPrefsManager.getUserEmail()
    override fun saveUserEmail(email: String) = encryptedPrefsManager.saveUserEmail(email)
}
