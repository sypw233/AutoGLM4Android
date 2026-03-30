package ovo.sypw.autoglm4android.data.remote.api

import ovo.sypw.autoglm4android.data.remote.dto.ChatCompletionRequest
import ovo.sypw.autoglm4android.data.remote.dto.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * AI 模型 API 接口
 */
interface ModelApi {

    @POST("chat/completions")
    suspend fun chatCompletions(@Body request: ChatCompletionRequest): ChatCompletionResponse
}
