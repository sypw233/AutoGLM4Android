package ovo.sypw.autoglm4android.data.remote.impl

import kotlinx.serialization.json.Json
import ovo.sypw.autoglm4android.data.remote.api.ModelApi
import ovo.sypw.autoglm4android.data.remote.dto.ChatCompletionRequest
import ovo.sypw.autoglm4android.data.remote.dto.ContentDto
import ovo.sypw.autoglm4android.data.remote.dto.ImageUrlDto
import ovo.sypw.autoglm4android.data.remote.dto.MessageDto
import ovo.sypw.autoglm4android.domain.model.ChatMessage
import ovo.sypw.autoglm4android.domain.model.ModelConfig
import ovo.sypw.autoglm4android.domain.model.ModelResponse
import ovo.sypw.autoglm4android.domain.repository.ModelRepository
import ovo.sypw.autoglm4android.domain.repository.ModelRequest
import ovo.sypw.autoglm4android.domain.repository.ModelResult
import ovo.sypw.autoglm4android.domain.repository.NetworkError
import ovo.sypw.autoglm4android.domain.repository.TestConnectionResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 模型客户端实现
 */
@Singleton
class ModelClientImpl @Inject constructor(
    private val api: ModelApi,
    private val json: Json
) : ModelRepository {

    private var isCancelled = false

    override suspend fun request(request: ModelRequest): ModelResult {
        return try {
            isCancelled = false

            val messages = request.messages.map { msg ->
                when (msg) {
                    is ChatMessage.System -> {
                        MessageDto(
                            role = "system",
                            content = listOf(ContentDto(type = "text", text = msg.content))
                        )
                    }
                    is ChatMessage.User -> {
                        val contentList = mutableListOf<ContentDto>()
                        if (request.screenshot != null) {
                            contentList.add(
                                ContentDto(
                                    type = "image_url",
                                    imageUrl = ImageUrlDto(url = "data:image/png;base64,${request.screenshot}")
                                )
                            )
                        }
                        contentList.add(ContentDto(type = "text", text = msg.content))
                        MessageDto(role = "user", content = contentList)
                    }
                    is ChatMessage.Assistant -> {
                        MessageDto(
                            role = "assistant",
                            content = listOf(ContentDto(type = "text", text = msg.content))
                        )
                    }
                }
            }

            val chatRequest = ChatCompletionRequest(
                model = "autoglm-phone",
                messages = messages,
                maxTokens = 4096,
                temperature = 0f,
                topP = 0.1f,
                stream = false
            )

            if (isCancelled) {
                return ModelResult.Error(NetworkError.ConnectionFailed("请求已取消"))
            }

            val response = api.chatCompletions(chatRequest)

            if (isCancelled) {
                return ModelResult.Error(NetworkError.ConnectionFailed("请求已取消"))
            }

            val content = response.choices.firstOrNull()?.message?.content ?: ""
            val (thinking, action) = parseResponse(content)

            ModelResult.Success(
                ModelResponse(
                    thinking = thinking,
                    action = action,
                    rawContent = content
                )
            )
        } catch (e: Exception) {
            when (e) {
                is java.net.SocketTimeoutException -> {
                    ModelResult.Error(NetworkError.Timeout("请求超时: ${e.message}"))
                }
                is java.net.UnknownHostException -> {
                    ModelResult.Error(NetworkError.ConnectionFailed("网络连接失败: ${e.message}"))
                }
                else -> {
                    ModelResult.Error(NetworkError.ConnectionFailed("请求失败: ${e.message}"))
                }
            }
        }
    }

    override suspend fun testConnection(config: ModelConfig): TestConnectionResult {
        return try {
            val testRequest = ChatCompletionRequest(
                model = config.modelName,
                messages = listOf(
                    MessageDto(
                        role = "user",
                        content = listOf(ContentDto(type = "text", text = "Hi"))
                    )
                ),
                maxTokens = 10,
                temperature = 0f
            )

            val startTime = System.currentTimeMillis()
            api.chatCompletions(testRequest)
            val latency = System.currentTimeMillis() - startTime

            TestConnectionResult.Success(latency)
        } catch (e: Exception) {
            TestConnectionResult.Error(e.message ?: "Connection failed")
        }
    }

    override fun cancelCurrentRequest() {
        isCancelled = true
    }

    /**
     * 解析模型响应
     */
    private fun parseResponse(content: String): Pair<String, String> {
        val thinkingPattern = Regex("<thinking>(.*?)</thinking>", RegexOption.DOT_MATCHES_ALL)
        val answerPattern = Regex("<answer>(.*?)</answer>", RegexOption.DOT_MATCHES_ALL)

        val thinking = thinkingPattern.find(content)?.groupValues?.get(1)?.trim() ?: ""
        val action = answerPattern.find(content)?.groupValues?.get(1)?.trim() ?: content

        return Pair(thinking, "<answer>$action</answer>")
    }
}
