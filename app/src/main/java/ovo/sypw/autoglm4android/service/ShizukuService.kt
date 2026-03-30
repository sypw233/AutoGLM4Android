package ovo.sypw.autoglm4android.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shizuku 服务管理
 * 使用 Shizuku UserService 方式执行命令
 */
@Singleton
class ShizukuService @Inject constructor() {

    private var userService: UserService.IUserServiceInterface? = null
    private var isBound = false

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            userService = if (service is UserService.IUserServiceInterface) {
                service
            } else {
                // 尝试通过 asInterface 获取
                service as? UserService.IUserServiceInterface
            }
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
            isBound = false
        }
    }

    /**
     * 检查 Shizuku 是否可用
     */
    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查是否有 Shizuku 权限
     */
    fun hasPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 请求 Shizuku 权限
     */
    fun requestPermission(requestCode: Int) {
        Shizuku.requestPermission(requestCode)
    }

    /**
     * 绑定 UserService
     */
    fun bindUserService(context: android.content.Context) {
        if (!isAvailable()) return

        try {
            val args = UserServiceArgs(
                ComponentName(
                    context.packageName,
                    UserService::class.java.name
                )
            )
                .daemon(false)
                .processNameSuffix("shizuku")
                .debuggable(false)
                .version(1)

            Shizuku.bindUserService(args, userServiceConnection)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 解绑 UserService
     */
    fun unbindUserService(context: android.content.Context) {
        if (isBound) {
            try {
                val args = UserServiceArgs(
                    ComponentName(
                        context.packageName,
                        UserService::class.java.name
                    )
                )
                Shizuku.unbindUserService(args, userServiceConnection, true)
            } catch (e: Exception) {
                // ignore
            }
            isBound = false
            userService = null
        }
    }

    /**
     * 执行 Shell 命令
     */
    suspend fun executeCommand(command: String): String = withContext(Dispatchers.IO) {
        try {
            if (!isAvailable()) {
                throw IllegalStateException("Shizuku 不可用")
            }

            if (!hasPermission()) {
                throw IllegalStateException("Shizuku 无权限")
            }

            if (!isBound || userService == null) {
                // UserService 未绑定，直接返回提示
                throw IllegalStateException("UserService 未绑定，请确保应用已授权 Shizuku 权限")
            }

            val result = userService?.executeCommand(command)
                ?: throw RuntimeException("UserService 未初始化")

            result
        } catch (e: Exception) {
            throw RuntimeException("执行命令失败: ${e.message}", e)
        }
    }
}
