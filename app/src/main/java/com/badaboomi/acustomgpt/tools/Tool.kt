package com.badaboomi.acustomgpt.tools

interface Tool {
    val type: String
    suspend fun execute(query: String): String
}
