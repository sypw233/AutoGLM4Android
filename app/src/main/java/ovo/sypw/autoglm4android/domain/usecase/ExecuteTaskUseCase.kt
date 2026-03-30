package ovo.sypw.autoglm4android.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ovo.sypw.autoglm4android.domain.model.AgentAction
import ovo.sypw.autoglm4android.domain.model.ChatMessage
import ovo.sypw.autoglm4android.domain.repository.ModelRepository
import ovo.sypw.autoglm4android.domain.repository.ScreenshotRepository
import ovo.sypw.autoglm4android.domain.repository.ModelResult
import ovo.sypw.autoglm4android.domain.repository.ModelRequest
import ovo.sypw.autoglm4android.domain.repository.NetworkError
import javax.inject.Inject

/**
 * 执行任务用例
 *
 * 流程:
 * 1. 截图 -> 发送给模型
 * 2. 解析模型响应 (thinking + action)
 * 3. 执行动作
 * 4. 判断是否完成，循环或结束
 */
class ExecuteTaskUseCase @Inject constructor(
    private val modelRepository: ModelRepository,
    private val screenshotRepository: ScreenshotRepository,
    private val executeActionUseCase: ExecuteActionUseCase,
) {
    private var isPaused = false
    private var isCancelled = false

    suspend fun execute(taskDescription: String): Flow<TaskExecutionEvent> = flow {
        emit(TaskExecutionEvent.Started(taskDescription))

        var stepCount = 0
        var isFinished = false
        var lastHint: String? = null
        val messages = mutableListOf<ChatMessage>(
            ChatMessage.System(SYSTEM_PROMPT),
            ChatMessage.User(taskDescription)
        )

        while (!isFinished && stepCount < MAX_STEPS && !isCancelled) {
            // 检查暂停状态
            while (isPaused && !isCancelled) {
                kotlinx.coroutines.delay(100)
            }

            if (isCancelled) {
                emit(TaskExecutionEvent.Cancelled)
                break
            }

            // 1. 截图
            val screenshot = screenshotRepository.capture()
            emit(TaskExecutionEvent.ScreenshotCaptured)

            // 2. 构建请求
            val request = ModelRequest(
                messages = messages,
                screenshot = screenshot.base64Data
            )

            // 3. 调用模型
            when (val result = modelRepository.request(request)) {
                is ModelResult.Success -> {
                    emit(TaskExecutionEvent.ThinkingUpdated(result.response.thinking))

                    // 4. 解析动作
                    val action = ActionParser.parse(result.response.action)
                    emit(TaskExecutionEvent.ActionParsed(action))

                    // 5. 执行动作
                    val executeResult = executeActionUseCase.execute(action)
                    emit(TaskExecutionEvent.ActionExecuted(executeResult))

                    // 6. 判断是否完成
                    if (action is AgentAction.Finish || executeResult.shouldFinish) {
                        isFinished = true
                        emit(TaskExecutionEvent.Completed(
                            (action as? AgentAction.Finish)?.message ?: executeResult.message
                        ))
                    } else {
                        lastHint = executeResult.message
                        messages.add(ChatMessage.Assistant(result.response.rawContent))
                        stepCount++
                    }
                }

                is ModelResult.Error -> {
                    emit(TaskExecutionEvent.Error(result.error.message ?: "Unknown error"))
                    break
                }
            }
        }

        if (!isFinished && !isCancelled && stepCount >= MAX_STEPS) {
            emit(TaskExecutionEvent.MaxStepsReached)
        }
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun cancel() {
        isCancelled = true
        isPaused = false
        modelRepository.cancelCurrentRequest()
    }

    fun reset() {
        isPaused = false
        isCancelled = false
    }

    companion object {
        private const val MAX_STEPS = 100

        private const val SYSTEM_PROMPT = """你是一个手机自动化助手。你需要根据用户的任务描述，分析屏幕截图，然后执行相应的操作。

可用操作:
- do(tap, x=<0-999>, y=<0-999>) - 点击屏幕坐标
- do(swipe, startX=<0-999>, startY=<0-999>, endX=<0-999>, endY=<0-999>) - 滑动
- do(longPress, x=<0-999>, y=<0-999>) - 长按
- do(doubleTap, x=<0-999>, y=<0-999>) - 双击
- do(type, text="...") - 输入文本
- do(launch, app="应用名称") - 启动应用
- do(back) - 返回
- do(home) - 回到主页
- do(wait, seconds=<数字>) - 等待
- finish(message="完成信息") - 完成任务

规则:
1. 坐标使用相对坐标(0-999)
2. 先分析截图再操作
3. 用<answer>标签包裹响应
4. 用中文回复"""
    }
}

/**
 * 动作解析器
 */
object ActionParser {
    fun parse(actionString: String): AgentAction {
        val content = extractAnswerContent(actionString)

        return when {
            content.startsWith("do(tap") -> parseTap(content)
            content.startsWith("do(swipe") -> parseSwipe(content)
            content.startsWith("do(longPress") -> parseLongPress(content)
            content.startsWith("do(doubleTap") -> parseDoubleTap(content)
            content.startsWith("do(type") -> parseType(content)
            content.startsWith("do(launch") -> parseLaunch(content)
            content == "do(back)" -> AgentAction.Back
            content == "do(home)" -> AgentAction.Home
            content.startsWith("do(wait") -> parseWait(content)
            content.startsWith("finish") -> parseFinish(content)
            else -> AgentAction.Finish("无法解析的动作: $content")
        }
    }

    private fun extractAnswerContent(response: String): String {
        val answerPattern = Regex("<answer>(.*?)</answer>", RegexOption.DOT_MATCHES_ALL)
        val match = answerPattern.find(response)
        return match?.groupValues?.get(1)?.trim() ?: response.trim()
    }

    private fun parseTap(content: String): AgentAction.Tap {
        val params = parseParams(content)
        val x = params["x"]?.toIntOrNull() ?: 0
        val y = params["y"]?.toIntOrNull() ?: 0
        val message = params["message"]
        return AgentAction.Tap(x.coerceIn(0, 999), y.coerceIn(0, 999), message)
    }

    private fun parseSwipe(content: String): AgentAction.Swipe {
        val params = parseParams(content)
        val startX = params["startX"]?.toIntOrNull() ?: 0
        val startY = params["startY"]?.toIntOrNull() ?: 0
        val endX = params["endX"]?.toIntOrNull() ?: 0
        val endY = params["endY"]?.toIntOrNull() ?: 0
        return AgentAction.Swipe(
            startX.coerceIn(0, 999), startY.coerceIn(0, 999),
            endX.coerceIn(0, 999), endY.coerceIn(0, 999)
        )
    }

    private fun parseLongPress(content: String): AgentAction.LongPress {
        val params = parseParams(content)
        val x = params["x"]?.toIntOrNull() ?: 0
        val y = params["y"]?.toIntOrNull() ?: 0
        return AgentAction.LongPress(x.coerceIn(0, 999), y.coerceIn(0, 999))
    }

    private fun parseDoubleTap(content: String): AgentAction.DoubleTap {
        val params = parseParams(content)
        val x = params["x"]?.toIntOrNull() ?: 0
        val y = params["y"]?.toIntOrNull() ?: 0
        return AgentAction.DoubleTap(x.coerceIn(0, 999), y.coerceIn(0, 999))
    }

    private fun parseType(content: String): AgentAction.Type {
        val params = parseParams(content)
        val text = params["text"] ?: ""
        return AgentAction.Type(text)
    }

    private fun parseLaunch(content: String): AgentAction.Launch {
        val params = parseParams(content)
        val app = params["app"] ?: ""
        return AgentAction.Launch(app)
    }

    private fun parseWait(content: String): AgentAction.Wait {
        val params = parseParams(content)
        val seconds = params["seconds"]?.toIntOrNull() ?: 1
        return AgentAction.Wait(seconds)
    }

    private fun parseFinish(content: String): AgentAction.Finish {
        val params = parseParams(content)
        val message = params["message"] ?: "任务完成"
        return AgentAction.Finish(message)
    }

    private fun parseParams(content: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val paramPattern = Regex("""(\w+)=(?:"([^"]*)"|([^,)]+))""")
        paramPattern.findAll(content).forEach { match ->
            val key = match.groupValues[1]
            val value = match.groupValues[2].ifEmpty { match.groupValues[3] }
            params[key] = value
        }
        return params
    }
}
