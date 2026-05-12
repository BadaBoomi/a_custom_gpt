package com.badaboomi.acustomgpt.tools

object ToolFactory {
    fun createEnabledTools(): List<Tool> {
        return ToolConfig.enabledTools.mapNotNull { type ->
            when (type) {
                "web_search" -> WebSearchTool()
                // Weitere Tools hier ergänzen
                else -> null
            }
        }
    }
}
