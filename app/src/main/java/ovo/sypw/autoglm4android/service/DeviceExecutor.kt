package ovo.sypw.autoglm4android.service

import ovo.sypw.autoglm4android.util.HumanizedSwipeGenerator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 设备执行器
 * 通过 Shizuku 执行设备操作
 */
@Singleton
class DeviceExecutor @Inject constructor(
    private val shizukuService: ShizukuService
) {
    /**
     * 点击屏幕
     */
    suspend fun tap(x: Int, y: Int) {
        shizukuService.executeCommand("input tap $x $y")
    }

    /**
     * 双击屏幕
     */
    suspend fun doubleTap(x: Int, y: Int) {
        tap(x, y)
        kotlinx.coroutines.delay(50)
        tap(x, y)
    }

    /**
     * 长按屏幕
     */
    suspend fun longPress(x: Int, y: Int, durationMs: Long = 1000) {
        shizukuService.executeCommand("input swipe $x $y $x $y $durationMs")
    }

    /**
     * 滑动屏幕
     */
    suspend fun swipe(
        startX: Int, startY: Int,
        endX: Int, endY: Int,
        humanized: Boolean = true
    ) {
        if (humanized) {
            val path = HumanizedSwipeGenerator.generatePath(
                startX, startY, endX, endY
            )
            for (point in path) {
                shizukuService.executeCommand("input tap ${point.x} ${point.y}")
                kotlinx.coroutines.delay(16)
            }
        } else {
            shizukuService.executeCommand("input swipe $startX $startY $endX $endY 300")
        }
    }

    /**
     * 按键
     */
    suspend fun pressKey(keyCode: Int) {
        shizukuService.executeCommand("input keyevent $keyCode")
    }

    /**
     * 启动应用
     */
    suspend fun launchApp(packageName: String) {
        shizukuService.executeCommand(
            "monkey -p $packageName -c android.intent.category.LAUNCHER 1"
        )
    }

    /**
     * 获取当前应用包名
     */
    suspend fun getCurrentApp(): String? {
        return try {
            val output = shizukuService.executeCommand(
                "dumpsys window | grep mCurrentFocus"
            )
            parsePackageName(output)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 输入文本
     */
    suspend fun inputText(text: String) {
        val escapedText = text.replace("'", "'\\''")
        shizukuService.executeCommand("input text '$escapedText'")
    }

    /**
     * 截图
     */
    suspend fun takeScreenshot(): String {
        return shizukuService.executeCommand("screencap -p")
    }

    private fun parsePackageName(output: String): String? {
        val pattern = Regex("u0 ([^/]+)/")
        return pattern.find(output)?.groupValues?.get(1)
    }
}
