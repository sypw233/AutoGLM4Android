package ovo.sypw.autoglm4android.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 服务状态管理器
 */
object ServiceStateManager {

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isScreenOn = MutableStateFlow(true)
    val isScreenOn: StateFlow<Boolean> = _isScreenOn.asStateFlow()

    fun updateBatteryState(context: Context) {
        try {
            val batteryIntent = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

            _batteryLevel.value = if (level >= 0 && scale > 0) {
                (level * 100) / scale
            } else {
                100
            }

            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        } catch (e: Exception) {
            Logger.e("ServiceStateManager", "Failed to update battery state", e)
        }
    }

    fun updateScreenState(isOn: Boolean) {
        _isScreenOn.value = isOn
    }

    fun shouldAutoPauseTask(): Boolean {
        // 电量低于20%且未充电时建议暂停
        return _batteryLevel.value < 20 && !_isCharging.value
    }

    fun shouldShowNotification(): Boolean {
        // 屏幕关闭时显示通知
        return !_isScreenOn.value
    }

    data class ServiceState(
        val floatingWindowRunning: Boolean = false,
        val voiceInputRunning: Boolean = false,
        val taskRunning: Boolean = false,
        val batteryLevel: Int = 100,
        val isCharging: Boolean = false,
        val isScreenOn: Boolean = true
    )

    fun getServiceState(
        context: Context,
        isFloatingWindowRunning: Boolean = false,
        isVoiceInputRunning: Boolean = false,
        isTaskRunning: Boolean = false
    ): ServiceState {
        return ServiceState(
            floatingWindowRunning = isFloatingWindowRunning,
            voiceInputRunning = isVoiceInputRunning,
            taskRunning = isTaskRunning,
            batteryLevel = _batteryLevel.value,
            isCharging = _isCharging.value,
            isScreenOn = _isScreenOn.value
        )
    }
}
