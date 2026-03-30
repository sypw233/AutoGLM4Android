package ovo.sypw.autoglm4android.ui.settings

import ovo.sypw.autoglm4android.domain.model.ModelConfig
import ovo.sypw.autoglm4android.domain.model.AgentConfig

/**
 * 设置 UI 状态
 */
data class SettingsUiState(
    val modelConfig: ModelConfig = ModelConfig(),
    val agentConfig: AgentConfig = AgentConfig(),
    val isLoading: Boolean = false,
    val testResult: String? = null,
    val isTestingConnection: Boolean = false
)
