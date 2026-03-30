package ovo.sypw.autoglm4android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ovo.sypw.autoglm4android.domain.model.ModelConfig
import ovo.sypw.autoglm4android.domain.model.AgentConfig
import ovo.sypw.autoglm4android.domain.repository.ModelRepository
import ovo.sypw.autoglm4android.domain.repository.SettingsRepository
import ovo.sypw.autoglm4android.domain.repository.TestConnectionResult
import javax.inject.Inject

/**
 * 设置 ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val modelRepository: ModelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.observeModelConfig().collect { config ->
                _uiState.update { it.copy(modelConfig = config) }
            }
        }
        viewModelScope.launch {
            settingsRepository.observeAgentConfig().collect { config ->
                _uiState.update { it.copy(agentConfig = config) }
            }
        }
    }

    fun onBaseUrlChanged(url: String) {
        _uiState.update {
            it.copy(modelConfig = it.modelConfig.copy(baseUrl = url))
        }
    }

    fun onApiKeyChanged(apiKey: String) {
        _uiState.update {
            it.copy(modelConfig = it.modelConfig.copy(apiKey = apiKey))
        }
    }

    fun onModelNameChanged(modelName: String) {
        _uiState.update {
            it.copy(modelConfig = it.modelConfig.copy(modelName = modelName))
        }
    }

    fun onTemperatureChanged(temperature: Float) {
        _uiState.update {
            it.copy(modelConfig = it.modelConfig.copy(temperature = temperature))
        }
    }

    fun onMaxTokensChanged(maxTokens: Int) {
        _uiState.update {
            it.copy(modelConfig = it.modelConfig.copy(maxTokens = maxTokens))
        }
    }

    fun onMaxStepsChanged(maxSteps: Int) {
        _uiState.update {
            it.copy(agentConfig = it.agentConfig.copy(maxSteps = maxSteps))
        }
    }

    fun onEnableVoiceChanged(enable: Boolean) {
        _uiState.update {
            it.copy(agentConfig = it.agentConfig.copy(enableVoice = enable))
        }
    }

    fun onEnableFloatingWindowChanged(enable: Boolean) {
        _uiState.update {
            it.copy(agentConfig = it.agentConfig.copy(enableFloatingWindow = enable))
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            settingsRepository.saveModelConfig(_uiState.value.modelConfig)
            settingsRepository.saveAgentConfig(_uiState.value.agentConfig)
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, testResult = null) }

            val result = modelRepository.testConnection(_uiState.value.modelConfig)

            _uiState.update {
                it.copy(
                    isTestingConnection = false,
                    testResult = when (result) {
                        is TestConnectionResult.Success -> "连接成功 (${result.latencyMs}ms)"
                        is TestConnectionResult.Error -> "连接失败: ${result.message}"
                    }
                )
            }
        }
    }
}
