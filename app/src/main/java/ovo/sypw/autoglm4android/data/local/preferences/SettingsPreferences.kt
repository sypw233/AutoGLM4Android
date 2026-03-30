package ovo.sypw.autoglm4android.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ovo.sypw.autoglm4android.domain.model.ModelConfig
import ovo.sypw.autoglm4android.domain.model.AgentConfig

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 设置偏好存储
 */
class SettingsPreferences(private val context: Context) {

    companion object {
        private val BASE_URL = stringPreferencesKey("base_url")
        private val API_KEY = stringPreferencesKey("api_key")
        private val MODEL_NAME = stringPreferencesKey("model_name")
        private val TEMPERATURE = floatPreferencesKey("temperature")
        private val MAX_TOKENS = intPreferencesKey("max_tokens")
        private val TOP_P = floatPreferencesKey("top_p")

        private val MAX_STEPS = intPreferencesKey("max_steps")
        private val RETRY_COUNT = intPreferencesKey("retry_count")
        private val ENABLE_VOICE = stringPreferencesKey("enable_voice")
        private val ENABLE_FLOATING_WINDOW = stringPreferencesKey("enable_floating_window")
    }

    val modelConfig: Flow<ModelConfig> = context.dataStore.data.map { preferences ->
        ModelConfig(
            baseUrl = preferences[BASE_URL] ?: "https://open.bigmodel.cn/api/paas/v4",
            apiKey = preferences[API_KEY] ?: "",
            modelName = preferences[MODEL_NAME] ?: "autoglm-phone",
            temperature = preferences[TEMPERATURE] ?: 0f,
            maxTokens = preferences[MAX_TOKENS] ?: 4096,
            topP = preferences[TOP_P] ?: 0.1f
        )
    }

    val agentConfig: Flow<AgentConfig> = context.dataStore.data.map { preferences ->
        AgentConfig(
            maxSteps = preferences[MAX_STEPS] ?: 100,
            retryCount = preferences[RETRY_COUNT] ?: 3,
            enableVoice = preferences[ENABLE_VOICE]?.toBoolean() ?: false,
            enableFloatingWindow = preferences[ENABLE_FLOATING_WINDOW]?.toBoolean() ?: true
        )
    }

    suspend fun saveModelConfig(config: ModelConfig) {
        context.dataStore.edit { preferences ->
            preferences[BASE_URL] = config.baseUrl
            preferences[API_KEY] = config.apiKey
            preferences[MODEL_NAME] = config.modelName
            preferences[TEMPERATURE] = config.temperature
            preferences[MAX_TOKENS] = config.maxTokens
            preferences[TOP_P] = config.topP
        }
    }

    suspend fun saveAgentConfig(config: AgentConfig) {
        context.dataStore.edit { preferences ->
            preferences[MAX_STEPS] = config.maxSteps
            preferences[RETRY_COUNT] = config.retryCount
            preferences[ENABLE_VOICE] = config.enableVoice.toString()
            preferences[ENABLE_FLOATING_WINDOW] = config.enableFloatingWindow.toString()
        }
    }
}
