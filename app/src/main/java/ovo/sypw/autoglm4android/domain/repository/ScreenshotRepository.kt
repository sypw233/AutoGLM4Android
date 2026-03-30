package ovo.sypw.autoglm4android.domain.repository

import ovo.sypw.autoglm4android.domain.model.Screenshot

/**
 * 截图仓库接口
 */
interface ScreenshotRepository {
    suspend fun capture(): Screenshot
}
