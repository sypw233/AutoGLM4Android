package ovo.sypw.autoglm4android.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 聊天补全请求
 */
@Serializable
data class ChatCompletionRequest(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<MessageDto>,
    @SerialName("max_tokens") val maxTokens: Int = 4096,
    @SerialName("temperature") val temperature: Float = 0f,
    @SerialName("top_p") val topP: Float = 0.1f,
    @SerialName("stream") val stream: Boolean = false
)

/**
 * 消息 DTO
 */
@Serializable
data class MessageDto(
    @SerialName("role") val role: String,
    @SerialName("content") val content: List<ContentDto>
)

/**
 * 内容 DTO
 */
@Serializable
data class ContentDto(
    @SerialName("type") val type: String,
    @SerialName("text") val text: String? = null,
    @SerialName("image_url") val imageUrl: ImageUrlDto? = null
)

/**
 * 图片 URL DTO
 */
@Serializable
data class ImageUrlDto(
    @SerialName("url") val url: String
)

/**
 * 聊天补全响应
 */
@Serializable
data class ChatCompletionResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("choices") val choices: List<ChoiceDto> = emptyList(),
    @SerialName("usage") val usage: UsageDto? = null
)

/**
 * 选择 DTO
 */
@Serializable
data class ChoiceDto(
    @SerialName("index") val index: Int = 0,
    @SerialName("message") val message: ResponseMessageDto,
    @SerialName("finish_reason") val finishReason: String? = null
)

/**
 * 响应消息 DTO
 */
@Serializable
data class ResponseMessageDto(
    @SerialName("role") val role: String = "",
    @SerialName("content") val content: String = ""
)

/**
 * 用量 DTO
 */
@Serializable
data class UsageDto(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0
)
