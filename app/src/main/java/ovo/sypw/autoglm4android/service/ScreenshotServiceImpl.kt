package ovo.sypw.autoglm4android.service

import android.util.Base64
import ovo.sypw.autoglm4android.domain.model.Screenshot
import ovo.sypw.autoglm4android.domain.repository.ScreenshotRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 截图服务实现
 */
@Singleton
class ScreenshotServiceImpl @Inject constructor(
    private val shizukuService: ShizukuService
) : ScreenshotRepository {

    override suspend fun capture(): Screenshot {
        val rawOutput = shizukuService.executeCommand("screencap -p | base64")
        val base64Data = rawOutput.replace("\n", "").trim()

        // 获取屏幕尺寸
        val sizeOutput = shizukuService.executeCommand("wm size")
        val sizePattern = Regex("(\\d+)x(\\d+)")
        val match = sizePattern.find(sizeOutput)
        val width = match?.groupValues?.get(1)?.toIntOrNull() ?: 1080
        val height = match?.groupValues?.get(2)?.toIntOrNull() ?: 1920

        return Screenshot(
            base64Data = base64Data,
            width = width,
            height = height
        )
    }
}
