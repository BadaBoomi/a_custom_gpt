package com.badaboomi.acustomgpt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.badaboomi.acustomgpt.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SetupUiState(
    val apiKey: String = "",
    val promptId: String = "",
    val vectorStoreIds: String = "",
    val userId: String = "",
    val apiKeyError: String? = null,
    val promptIdError: String? = null,
    val isSetupComplete: Boolean = false
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: com.badaboomi.acustomgpt.domain.repository.UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SetupUiState(userId = userRepository.getUserEmail() ?: "")
    )
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun onApiKeyChange(value: String) {
        val cleaned = value.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("")
        _uiState.value = _uiState.value.copy(apiKey = cleaned, apiKeyError = null)
    }

    fun onPromptIdChange(value: String) {
        val cleaned = value.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("")
        _uiState.value = _uiState.value.copy(promptId = cleaned, promptIdError = null)
    }

    fun onVectorStoreIdsChange(value: String) {
        _uiState.value = _uiState.value.copy(vectorStoreIds = value)
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
        _uiState.value = state.copy(isSetupComplete = true)
    }
}
