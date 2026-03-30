package ovo.sypw.autoglm4android.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.content.Intent

/**
 * 悬浮窗快捷设置磁贴服务
 */
class FloatingWindowTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()

        val intent = Intent(this, FloatingWindowToggleActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivityAndCollapse(intent)
    }

    private fun updateTile() {
        val tile = qsTile ?: return

        // 检查悬浮窗服务是否运行
        val isRunning = isServiceRunning()

        tile.state = if (isRunning) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }

        tile.label = "AutoGLM"
        tile.updateTile()
    }

    private fun isServiceRunning(): Boolean {
        val activityManager = getSystemService(android.content.Context.ACTIVITY_SERVICE)
                as android.app.ActivityManager

        val services = activityManager.getRunningServices(Integer.MAX_VALUE)
        return services.any { serviceInfo ->
            serviceInfo.service.className == FloatingWindowService::class.java.name
        }
    }
}
