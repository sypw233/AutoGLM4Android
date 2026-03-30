package ovo.sypw.autoglm4android.service

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View

/**
 * 悬浮窗切换透明 Activity
 * 用于从快捷设置磁贴切换悬浮窗状态
 */
class FloatingWindowToggleActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查当前悬浮窗状态并切换
        toggleFloatingWindow()

        // 立即结束Activity
        finish()
    }

    private fun toggleFloatingWindow() {
        val isRunning = isFloatingWindowRunning()

        if (isRunning) {
            // 停止悬浮窗
            FloatingWindowService.stop(this)
        } else {
            // 启动悬浮窗
            FloatingWindowService.start(this)
        }
    }

    private fun isFloatingWindowRunning(): Boolean {
        val activityManager = getSystemService(android.content.Context.ACTIVITY_SERVICE)
                as android.app.ActivityManager

        val services = activityManager.getRunningServices(Integer.MAX_VALUE)
        return services.any { serviceInfo ->
            serviceInfo.service.className == FloatingWindowService::class.java.name
        }
    }
}
