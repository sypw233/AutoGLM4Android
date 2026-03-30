package ovo.sypw.autoglm4android.domain.usecase

/**
 * 任务执行事件
 */
sealed class TaskExecutionEvent {
    data class Started(val taskDescription: String) : TaskExecutionEvent()
    data object ScreenshotCaptured : TaskExecutionEvent()
    data class ThinkingUpdated(val thinking: String) : TaskExecutionEvent()
    data class ActionParsed(val action: ovo.sypw.autoglm4android.domain.model.AgentAction) : TaskExecutionEvent()
    data class ActionExecuted(val result: ovo.sypw.autoglm4android.domain.model.ActionResult) : TaskExecutionEvent()
    data class Completed(val message: String) : TaskExecutionEvent()
    data class Error(val message: String) : TaskExecutionEvent()
    data object MaxStepsReached : TaskExecutionEvent()
    data object Paused : TaskExecutionEvent()
    data object Resumed : TaskExecutionEvent()
    data object Cancelled : TaskExecutionEvent()
}
