package ovo.sypw.autoglm4android.domain.repository

import ovo.sypw.autoglm4android.domain.model.ActionResult
import ovo.sypw.autoglm4android.domain.model.AgentAction

/**
 * 设备仓库接口
 */
interface DeviceRepository {
    suspend fun tap(x: Int, y: Int): ActionResult
    suspend fun doubleTap(x: Int, y: Int): ActionResult
    suspend fun longPress(x: Int, y: Int): ActionResult
    suspend fun swipe(
        startX: Int, startY: Int,
        endX: Int, endY: Int,
        humanized: Boolean = true
    ): ActionResult

    suspend fun pressKey(keyCode: Int): ActionResult
    suspend fun launchApp(packageName: String): ActionResult
    suspend fun getCurrentApp(): String?
    suspend fun back(): ActionResult
    suspend fun home(): ActionResult
}

/**
 * 应用解析器接口
 */
interface AppResolver {
    suspend fun resolvePackageName(appName: String): String?
    suspend fun resolveAppName(packageName: String): String?
}
