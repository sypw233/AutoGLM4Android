package ovo.sypw.autoglm4android.domain.repository

import ovo.sypw.autoglm4android.domain.model.ModelConfig
import ovo.sypw.autoglm4android.domain.model.ModelResponse
import ovo.sypw.autoglm4android.domain.model.ChatMessage

/**
 * 模型请求
 */
data class ModelRequest(
    val messages: List<ChatMessage>,
    val screenshot: String? = null
)

/**
 * 模型结果
 */
sealed class ModelResult {
    data class Success(val response: ModelResponse) : ModelResult()
    data class Error(val error: NetworkError) : ModelResult()
}

/**
 * 测试连接结果
 */
sealed class TestConnectionResult {
    data class Success(val latencyMs: Long) : TestConnectionResult()
    data class Error(val message: String) : TestConnectionResult()
}

/**
 * 网络错误
 */
sealed class NetworkError : Exception() {
    data class ConnectionFailed(override val message: String) : NetworkError()
    data class Timeout(override val message: String) : NetworkError()
    data class ServerError(val code: Int, override val message: String) : NetworkError()
    data class AuthError(override val message: String) : NetworkError()
    data class ParseError(override val message: String) : NetworkError()
}

/**
 * 模型仓库接口
 */
interface ModelRepository {
    suspend fun request(request: ModelRequest): ModelResult
    suspend fun testConnection(config: ModelConfig): TestConnectionResult
    fun cancelCurrentRequest()
}
