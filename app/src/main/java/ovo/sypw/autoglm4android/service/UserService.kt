package ovo.sypw.autoglm4android.service

import android.app.Service
import android.content.Context
import android.os.Binder
import android.os.IBinder
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Shizuku UserService
 * 在 Shizuku 进程中运行，执行 shell 命令
 * 
 * 内联定义 IUserService 接口以避免 AIDL 生成问题
 */
class UserService : Service() {

    /**
     * IUserService 接口 - 内联定义避免 AIDL 依赖
     */
    interface IUserServiceInterface {
        fun executeCommand(command: String): String
        fun destroy()
    }

    /**
     * Binder 实现
     */
    private inner class UserServiceBinder : Binder(), IUserServiceInterface {
        
        override fun executeCommand(command: String): String {
            return try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val output = BufferedReader(InputStreamReader(process.inputStream)).use {
                    it.readText()
                }
                val error = BufferedReader(InputStreamReader(process.errorStream)).use {
                    it.readText()
                }
                val exitCode = process.waitFor()

                if (exitCode != 0) {
                    "ERROR (exit code: $exitCode): $error"
                } else {
                    output
                }
            } catch (e: Exception) {
                "ERROR: ${e.message}"
            }
        }

        override fun destroy() {
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    private val binder = UserServiceBinder()

    override fun onBind(intent: android.content.Intent?): IBinder {
        return binder
    }
}
