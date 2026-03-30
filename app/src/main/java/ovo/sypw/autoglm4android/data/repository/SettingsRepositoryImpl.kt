package ovo.sypw.autoglm4android.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import ovo.sypw.autoglm4android.data.local.preferences.SettingsPreferences
import ovo.sypw.autoglm4android.domain.model.AgentConfig
import ovo.sypw.autoglm4android.domain.model.ModelConfig
import ovo.sypw.autoglm4android.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 设置仓库实现
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) : SettingsRepository {

    override fun observeModelConfig(): Flow<ModelConfig> = settingsPreferences.modelConfig

    override suspend fun getModelConfig(): ModelConfig = settingsPreferences.modelConfig.first()

    override suspend fun saveModelConfig(config: ModelConfig) {
        settingsPreferences.saveModelConfig(config)
    }

    override fun observeAgentConfig(): Flow<AgentConfig> = settingsPreferences.agentConfig

    override suspend fun getAgentConfig(): AgentConfig = settingsPreferences.agentConfig.first()

    override suspend fun saveAgentConfig(config: AgentConfig) {
        settingsPreferences.saveAgentConfig(config)
    }
}
