package com.badaboomi.acustomgpt.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebSearchTool : Tool {
    override val type: String = "web_search"

    override suspend fun execute(query: String): String = withContext(Dispatchers.IO) {
        // Hier sollte eine echte Websuche implementiert werden
        // Beispiel: Rückgabe eines Platzhalter-Strings
        "[Websuche nicht implementiert] Ergebnis für: $query"
    }
}
