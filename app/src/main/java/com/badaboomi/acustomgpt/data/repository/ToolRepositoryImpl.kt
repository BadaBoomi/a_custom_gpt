package com.badaboomi.acustomgpt.data.repository

import com.badaboomi.acustomgpt.domain.repository.ToolRepository
import com.badaboomi.acustomgpt.tools.ToolFactory

class ToolRepositoryImpl : ToolRepository {
    private val tools = ToolFactory.createEnabledTools()

    override suspend fun executeTool(type: String, query: String): String? {
        val tool = tools.find { it.type == type }
        return tool?.execute(query)
    }

    override fun enabledTools(): List<String> = tools.map { it.type }
}
