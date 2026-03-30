package ovo.sypw.autoglm4android.domain.usecase

import ovo.sypw.autoglm4android.domain.model.ActionResult
import ovo.sypw.autoglm4android.domain.model.AgentAction
import ovo.sypw.autoglm4android.domain.repository.AppResolver
import ovo.sypw.autoglm4android.domain.repository.DeviceRepository
import javax.inject.Inject

/**
 * 执行动作用例
 */
class ExecuteActionUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val appResolver: AppResolver,
) {
    /**
     * 执行单个动作
     */
    suspend fun execute(action: AgentAction): ActionResult {
        return when (action) {
            is AgentAction.Tap -> {
                deviceRepository.tap(action.x, action.y)
            }

            is AgentAction.Swipe -> {
                deviceRepository.swipe(
                    action.startX, action.startY,
                    action.endX, action.endY,
                    action.humanized
                )
            }

            is AgentAction.LongPress -> {
                deviceRepository.longPress(action.x, action.y)
            }

            is AgentAction.DoubleTap -> {
                deviceRepository.doubleTap(action.x, action.y)
            }

            is AgentAction.Type -> {
                ActionResult(
                    success = false,
                    message = "文本输入需要通过输入法服务实现"
                )
            }

            is AgentAction.Launch -> {
                val packageName = appResolver.resolvePackageName(action.app)
                if (packageName != null) {
                    deviceRepository.launchApp(packageName)
                } else {
                    ActionResult(
                        success = false,
                        message = "未找到应用: ${action.app}"
                    )
                }
            }

            is AgentAction.Back -> {
                deviceRepository.back()
            }

            is AgentAction.Home -> {
                deviceRepository.home()
            }

            is AgentAction.Wait -> {
                kotlinx.coroutines.delay(action.durationSeconds * 1000L)
                ActionResult(
                    success = true,
                    message = "等待 ${action.durationSeconds} 秒"
                )
            }

            is AgentAction.Finish -> {
                ActionResult(
                    success = true,
                    message = action.message,
                    shouldFinish = true
                )
            }

            is AgentAction.Batch -> {
                executeBatch(action.steps)
            }
        }
    }

    /**
     * 执行批量动作
     */
    private suspend fun executeBatch(steps: List<AgentAction>): ActionResult {
        for (step in steps) {
            val result = execute(step)
            if (!result.success) {
                return result
            }
        }
        return ActionResult(
            success = true,
            message = "批量操作完成"
        )
    }
}
