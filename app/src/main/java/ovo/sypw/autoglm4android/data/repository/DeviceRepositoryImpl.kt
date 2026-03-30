package ovo.sypw.autoglm4android.data.repository

import ovo.sypw.autoglm4android.domain.model.ActionResult
import ovo.sypw.autoglm4android.domain.repository.DeviceRepository
import ovo.sypw.autoglm4android.service.DeviceExecutor
import ovo.sypw.autoglm4android.util.HumanizedSwipeGenerator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 设备仓库实现
 */
@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val deviceExecutor: DeviceExecutor,
    private val swipeGenerator: HumanizedSwipeGenerator
) : DeviceRepository {

    override suspend fun tap(x: Int, y: Int): ActionResult {
        return try {
            deviceExecutor.tap(x, y)
            ActionResult(success = true, message = "点击 ($x, $y)")
        } catch (e: Exception) {
            ActionResult(success = false, message = "点击失败: ${e.message}")
        }
    }

    override suspend fun doubleTap(x: Int, y: Int): ActionResult {
        return try {
            deviceExecutor.doubleTap(x, y)
            ActionResult(success = true, message = "双击 ($x, $y)")
        } catch (e: Exception) {
            ActionResult(success = false, message = "双击失败: ${e.message}")
        }
    }

    override suspend fun longPress(x: Int, y: Int): ActionResult {
        return try {
            deviceExecutor.longPress(x, y)
            ActionResult(success = true, message = "长按 ($x, $y)")
        } catch (e: Exception) {
            ActionResult(success = false, message = "长按失败: ${e.message}")
        }
    }

    override suspend fun swipe(
        startX: Int, startY: Int,
        endX: Int, endY: Int,
        humanized: Boolean
    ): ActionResult {
        return try {
            deviceExecutor.swipe(startX, startY, endX, endY, humanized)
            ActionResult(success = true, message = "滑动 ($startX,$startY) -> ($endX,$endY)")
        } catch (e: Exception) {
            ActionResult(success = false, message = "滑动失败: ${e.message}")
        }
    }

    override suspend fun pressKey(keyCode: Int): ActionResult {
        return try {
            deviceExecutor.pressKey(keyCode)
            ActionResult(success = true, message = "按键 $keyCode")
        } catch (e: Exception) {
            ActionResult(success = false, message = "按键失败: ${e.message}")
        }
    }

    override suspend fun launchApp(packageName: String): ActionResult {
        return try {
            deviceExecutor.launchApp(packageName)
            ActionResult(success = true, message = "启动应用 $packageName")
        } catch (e: Exception) {
            ActionResult(success = false, message = "启动应用失败: ${e.message}")
        }
    }

    override suspend fun getCurrentApp(): String? {
        return try {
            deviceExecutor.getCurrentApp()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun back(): ActionResult {
        return try {
            deviceExecutor.pressKey(4) // KEYCODE_BACK
            ActionResult(success = true, message = "返回")
        } catch (e: Exception) {
            ActionResult(success = false, message = "返回失败: ${e.message}")
        }
    }

    override suspend fun home(): ActionResult {
        return try {
            deviceExecutor.pressKey(3) // KEYCODE_HOME
            ActionResult(success = true, message = "主页")
        } catch (e: Exception) {
            ActionResult(success = false, message = "主页失败: ${e.message}")
        }
    }
}
