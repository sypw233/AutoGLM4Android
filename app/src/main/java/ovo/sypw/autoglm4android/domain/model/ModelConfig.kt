package ovo.sypw.autoglm4android.domain.model

/**
 * 模型配置
 */
data class ModelConfig(
    val baseUrl: String = "https://open.bigmodel.cn/api/paas/v4",
    val apiKey: String = "",
    val modelName: String = "autoglm-phone",
    val temperature: Float = 0f,
    val maxTokens: Int = 4096,
    val topP: Float = 0.1f
)

/**
 * Agent 配置
 */
data class AgentConfig(
    val maxSteps: Int = 100,
    val retryCount: Int = 3,
    val enableVoice: Boolean = false,
    val enableFloatingWindow: Boolean = true
)

/**
 * 模型响应
 */
data class ModelResponse(
    val thinking: String = "",
    val action: String = "",
    val rawContent: String = ""
)

/**
 * 聊天消息
 */
sealed class ChatMessage {
    abstract val role: String
    abstract val content: String

    data class System(override val content: String) : ChatMessage() {
        override val role = "system"
    }

    data class User(override val content: String) : ChatMessage() {
        override val role = "user"
    }

    data class Assistant(override val content: String) : ChatMessage() {
        override val role = "assistant"
    }
}
