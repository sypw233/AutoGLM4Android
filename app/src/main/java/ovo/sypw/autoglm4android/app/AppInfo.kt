package ovo.sypw.autoglm4android.app

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

/**
 * 应用信息工具类
 */
object AppInfo {

    fun getVersionName(): String {
        return getPackageInfo()?.versionName ?: "Unknown"
    }

    fun getVersionCode(): Long {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                getPackageInfo()?.longVersionCode ?: 0
            } else {
                @Suppress("DEPRECATION")
                getPackageInfo()?.versionCode?.toLong() ?: 0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun getPackageName(context: Context): String {
        return context.packageName ?: "Unknown"
    }

    fun getAppName(context: Context): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager?.getApplicationInfo(getPackageName(context), 0)
            packageManager?.getApplicationLabel(appInfo!!)?.toString() ?: "AutoGLM"
        } catch (e: Exception) {
            "AutoGLM"
        }
    }

    private fun getPackageInfo(context: Context): PackageInfo? {
        return try {
            context.packageManager?.getPackageInfo(getPackageName(context), 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun isInstalled(packageName: String,context: Context): Boolean {
        return try {
            context.packageManager?.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getAppInfo(packageName: String,context: Context): AppInfoData? {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager?.getApplicationInfo(packageName, 0)
            AppInfoData(
                packageName = packageName,
                appName = packageManager?.getApplicationLabel(appInfo!!)?.toString() ?: packageName,
                versionName = try {
                    packageManager?.getPackageInfo(packageName, 0)?.versionName
                } catch (e: Exception) {
                    null
                },
                versionCode = try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        packageManager?.getPackageInfo(packageName, 0)?.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager?.getPackageInfo(packageName, 0)?.versionCode?.toLong()
                    }
                } catch (e: Exception) {
                    null
                }
            )
        } catch (e: Exception) {
            null
        }
    }

    data class AppInfoData(
        val packageName: String,
        val appName: String,
        val versionName: String?,
        val versionCode: Long?
    )

}
