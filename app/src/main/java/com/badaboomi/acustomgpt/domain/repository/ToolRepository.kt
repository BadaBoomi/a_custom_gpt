package com.badaboomi.acustomgpt.domain.repository

interface ToolRepository {
    suspend fun executeTool(type: String, query: String): String?
    fun enabledTools(): List<String>
}
