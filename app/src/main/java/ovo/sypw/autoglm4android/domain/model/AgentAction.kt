package ovo.sypw.autoglm4android.domain.model

/**
 * Agent 动作密封类
 */
sealed class AgentAction {
    /**
     * 点击动作
     */
    data class Tap(
        val x: Int,
        val y: Int,
        val message: String? = null
    ) : AgentAction()

    /**
     * 滑动动作
     */
    data class Swipe(
        val startX: Int,
        val startY: Int,
        val endX: Int,
        val endY: Int,
        val humanized: Boolean = true
    ) : AgentAction()

    /**
     * 长按动作
     */
    data class LongPress(
        val x: Int,
        val y: Int
    ) : AgentAction()

    /**
     * 双击动作
     */
    data class DoubleTap(
        val x: Int,
        val y: Int
    ) : AgentAction()

    /**
     * 输入文本动作
     */
    data class Type(
        val text: String
    ) : AgentAction()

    /**
     * 启动应用动作
     */
    data class Launch(
        val app: String
    ) : AgentAction()

    /**
     * 返回动作
     */
    data object Back : AgentAction()

    /**
     * 主页动作
     */
    data object Home : AgentAction()

    /**
     * 等待动作
     */
    data class Wait(
        val durationSeconds: Int
    ) : AgentAction()

    /**
     * 完成动作
     */
    data class Finish(
        val message: String
    ) : AgentAction()

    /**
     * 批量动作
     */
    data class Batch(
        val steps: List<AgentAction>
    ) : AgentAction()
}

/**
 * 动作执行结果
 */
data class ActionResult(
    val success: Boolean,
    val message: String,
    val shouldFinish: Boolean = false
)
