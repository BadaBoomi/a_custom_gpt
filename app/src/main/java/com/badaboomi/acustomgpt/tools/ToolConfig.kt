package com.badaboomi.acustomgpt.tools

import com.badaboomi.acustomgpt.BuildConfig
import java.io.File

object ToolConfig {
    val logLevel: String by lazy {
        BuildConfig.LOG_LEVEL.ifBlank { env["LOG_LEVEL"] ?: "INFO" }
    }

    val logModelPayload: Boolean by lazy {
        BuildConfig.LOG_MODEL_PAYLOAD ||
            (env["LOG_MODEL_PAYLOAD"]?.equals("true", ignoreCase = true) == true)
    }

    private val env: Map<String, String> by lazy { loadEnv() }

    val enabledTools: List<String> by lazy {
        val toolsRaw = if (BuildConfig.TOOLS.isNotBlank()) BuildConfig.TOOLS else (env["TOOLS"] ?: "")
        toolsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun loadEnv(): Map<String, String> {
        val envFile = File(".env")
        if (!envFile.exists()) return emptyMap()
        return envFile.readLines()
            .mapNotNull { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("#") || !trimmed.contains("=")) null
                else {
                    val (key, value) = trimmed.split("=", limit = 2)
                    key.trim() to value.trim()
                }
            }
            .toMap()
    }
}
