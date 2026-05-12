package com.badaboomi.acustomgpt.domain.repository

interface UserRepository {
    fun getUserEmail(): String?
    fun saveUserEmail(email: String)
}
