package ovo.sypw.autoglm4android.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.getSystemService

/**
 * 错误处理器
 */
object ErrorHandler {

    private val errorListeners = mutableListOf<ErrorListener>()

    interface ErrorListener {
        fun onError(error: ErrorInfo)
    }

    data class ErrorInfo(
        val code: Int,
        val message: String,
        val throwable: Throwable? = null,
        val context: Context? = null
    )

    fun registerListener(listener: ErrorListener) {
        errorListeners.add(listener)
    }

    fun unregisterListener(listener: ErrorListener) {
        errorListeners.remove(listener)
    }

    fun handle(error: Throwable, context: Context? = null) {
        val errorInfo = ErrorInfo(
            code = getErrorCode(error),
            message = error.message ?: "Unknown error",
            throwable = error,
            context = context
        )

        // 记录日志
        Logger.e("Error: ${errorInfo.message}", error)

        // 通知监听器
        errorListeners.forEach { it.onError(errorInfo) }
    }

    fun handle(message: String, context: Context? = null) {
        val errorInfo = ErrorInfo(
            code = -1,
            message = message,
            context = context
        )

        Logger.e("Error: $message")

        errorListeners.forEach { it.onError(errorInfo) }
    }

    private fun getErrorCode(error: Throwable): Int {
        return when (error) {
            is IllegalStateException -> 1001
            is SecurityException -> 1002
            is NullPointerException -> 1003
            is NetworkError -> 2001
            is ShizukuError -> 3001
            else -> 9999
        }
    }

    sealed class NetworkError : Exception() {
        data class ConnectionFailed(override val message: String) : NetworkError()
        data class Timeout(override val message: String) : NetworkError()
        data class ServerError(val code: Int, override val message: String) : NetworkError()
    }

    sealed class ShizukuError : Exception() {
        data object NotAvailable : ShizukuError() {
            override val message = "Shizuku not available"
        }
        data object NoPermission : ShizukuError() {
            override val message = "Shizuku permission denied"
        }
        data class ExecutionFailed(override val message: String) : ShizukuError()
    }

    companion object {
        fun isOverlayPermissionGranted(context: Context): Boolean {
            return Settings.canDrawOverlays(context)
        }

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return enabledServices?.contains(context.packageName) == true
        }

        fun isIgnoreBatteryOptimizationGranted(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService<PowerManager>()
                return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
            }
            return true
        }

        fun requestIgnoreBatteryOptimization(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        }
    }
}
