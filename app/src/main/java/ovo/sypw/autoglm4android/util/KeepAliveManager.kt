package ovo.sypw.autoglm4android.util

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.content.getSystemService

/**
 * 保活管理器
 */
object KeepAliveManager {

    private var isInitialized = false
    private var wakeLock: PowerManager.WakeLock? = null

    fun init(context: Context) {
        if (isInitialized) return

        try {
            val powerManager = context.getSystemService<PowerManager>()
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "AutoGLM::TaskWakeLock"
            )
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun acquire(context: Context, timeoutMs: Long = Long.MAX_VALUE): Boolean {
        return try {
            if (wakeLock == null) {
                init(context)
            }

            if (wakeLock?.isHeld != true) {
                wakeLock?.acquire(timeoutMs)
            }
            true
        } catch (e: Exception) {
            Logger.e("KeepAliveManager", "Failed to acquire wake lock", e)
            false
        }
    }

    fun release() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Logger.e("KeepAliveManager", "Failed to release wake lock", e)
        }
    }

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Integer.MAX_VALUE)

        return services.any { serviceInfo ->
            serviceInfo.service.className == serviceClass.name
        }
    }

    fun startServiceIfNeeded(context: Context, serviceClass: Class<*>) {
        if (!isServiceRunning(context, serviceClass)) {
            try {
                val intent = Intent(context, serviceClass)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Logger.e("KeepAliveManager", "Failed to start service: ${serviceClass.name}", e)
            }
        }
    }

    fun stopServiceIfNeeded(context: Context, serviceClass: Class<*>) {
        if (isServiceRunning(context, serviceClass)) {
            try {
                context.stopService(Intent(context, serviceClass))
            } catch (e: Exception) {
                Logger.e("KeepAliveManager", "Failed to stop service: ${serviceClass.name}", e)
            }
        }
    }

    fun moveTaskToFront(context: Context) {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val recentTasks = activityManager.getRecentTasks(
                Integer.MAX_VALUE,
                ActivityManager.RECENT_WITH_EXCLUDED
            )

            for (taskInfo in recentTasks) {
                val baseIntent = taskInfo.baseIntent
                if (baseIntent?.component?.packageName == context.packageName) {
                    activityManager.moveTaskToFront(taskInfo.id)
                    break
                }
            }
        } catch (e: Exception) {
            Logger.e("KeepAliveManager", "Failed to move task to front", e)
        }
    }

    fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps = activityManager.runningAppProcesses

        return runningApps.any { processInfo ->
            processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    processInfo.pkgList.contains(context.packageName)
        }
    }
}
