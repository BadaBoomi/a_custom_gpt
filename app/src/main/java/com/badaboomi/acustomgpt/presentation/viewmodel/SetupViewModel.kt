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
    val assistantId: String = "",
    val apiKeyError: String? = null,
    val assistantIdError: String? = null,
    val isSetupComplete: Boolean = false
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun onApiKeyChange(value: String) {
        _uiState.value = _uiState.value.copy(apiKey = value, apiKeyError = null)
    }

    fun onAssistantIdChange(value: String) {
        _uiState.value = _uiState.value.copy(assistantId = value, assistantIdError = null)
    }

    fun onSave() {
        val state = _uiState.value

        val apiKeyError = if (!state.apiKey.startsWith("sk-")) "API key must start with 'sk-'" else null
        val assistantIdError = if (!state.assistantId.startsWith("asst_")) "Assistant ID must start with 'asst_'" else null

        if (apiKeyError != null || assistantIdError != null) {
            _uiState.value = state.copy(apiKeyError = apiKeyError, assistantIdError = assistantIdError)
            return
        }

        settingsRepository.saveApiKey(state.apiKey)
        settingsRepository.saveAssistantId(state.assistantId)
        _uiState.value = state.copy(isSetupComplete = true)
    }
}
