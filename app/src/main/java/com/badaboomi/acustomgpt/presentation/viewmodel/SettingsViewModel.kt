package com.badaboomi.acustomgpt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.badaboomi.acustomgpt.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val promptId: String = "",
    val vectorStoreIds: String = "",
    val userId: String = "",
    val apiKeyError: String? = null,
    val promptIdError: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: com.badaboomi.acustomgpt.domain.repository.UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = SettingsUiState(
            apiKey = settingsRepository.getApiKey() ?: "",
            promptId = settingsRepository.getPromptId() ?: "",
            vectorStoreIds = settingsRepository.getVectorStoreIds().joinToString(", "),
            userId = userRepository.getUserEmail() ?: ""
        )
    }

    fun onApiKeyChange(value: String) {
        val cleaned = value.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("")
        _uiState.value = _uiState.value.copy(apiKey = cleaned, apiKeyError = null, isSaved = false)
    }

    fun onPromptIdChange(value: String) {
        val cleaned = value.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("")
        _uiState.value = _uiState.value.copy(promptId = cleaned, promptIdError = null, isSaved = false)
    }

    fun onVectorStoreIdsChange(value: String) {
        _uiState.value = _uiState.value.copy(vectorStoreIds = value, isSaved = false)
    }

    fun onSave() {
        val state = _uiState.value
        val apiKeyError = if (!state.apiKey.startsWith("sk-")) "API-Schlüssel muss mit 'sk-' beginnen" else null
        val promptIdError = if (state.promptId.isBlank()) "Prompt-ID darf nicht leer sein" else null

        if (apiKeyError != null || promptIdError != null) {
            _uiState.value = state.copy(apiKeyError = apiKeyError, promptIdError = promptIdError)
            return
        }

        val cleanedApiKey = state.apiKey.trim()
        val cleanedPromptId = state.promptId.trim()
        val parsedVsIds = state.vectorStoreIds.split(",").map { it.trim() }.filter { it.isNotBlank() }
        settingsRepository.saveApiKey(cleanedApiKey)
        settingsRepository.savePromptId(cleanedPromptId)
        settingsRepository.saveVectorStoreIds(parsedVsIds)

        // TODO: GET_CONFIGURATION vom Assistenten holen, hier Platzhalter: Starters aus Datei lesen
        // In Produktion: Netzwerkaufruf, hier Demo: Lese aus assets/starters.md
        try {
            val starters = loadStartersFromAssets()
            settingsRepository.saveStarters(starters)
        } catch (e: Exception) {
            // Fehlerbehandlung, falls Datei nicht gefunden
        }

        _uiState.value = state.copy(isSaved = true)
    }

    // Platzhalter: Lese Starters aus assets/starters.md (in Produktion: aus Netzwerk)
    private fun loadStartersFromAssets(): String {
        // Diese Methode muss an die tatsächliche Asset- oder Dateistruktur angepasst werden
        // Hier: Rückgabe eines statischen Beispiels
        return "|Zweck|Prompt|\n|--|--|\n|Frage nach einer berühmten Persönlichkeit| Wer war eigentlich |\n|Rechenaufgabe| Wieviel ist |\n|Humor| Erzähle einen Witz über |"
    }
}
