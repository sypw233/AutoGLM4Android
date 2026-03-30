package ovo.sypw.autoglm4android.domain.repository

import kotlinx.coroutines.flow.Flow
import ovo.sypw.autoglm4android.domain.model.ModelConfig
import ovo.sypw.autoglm4android.domain.model.AgentConfig

/**
 * 设置仓库接口
 */
interface SettingsRepository {
    fun observeModelConfig(): Flow<ModelConfig>
    suspend fun getModelConfig(): ModelConfig
    suspend fun saveModelConfig(config: ModelConfig)
    
    fun observeAgentConfig(): Flow<AgentConfig>
    suspend fun getAgentConfig(): AgentConfig
    suspend fun saveAgentConfig(config: AgentConfig)
}
