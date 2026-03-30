package ovo.sypw.autoglm4android.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 日志文件管理器
 */
object LogFileManager {

    private const val LOG_DIR = "logs"
    private const val MAX_LOG_SIZE = 10 * 1024 * 1024 // 10MB
    private const val MAX_LOG_FILES = 5

    private var logFile: File? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return

        try {
            val logDir = File(context.getExternalFilesDir(null), LOG_DIR)
            if (!logDir.exists()) {
                logDir.mkdirs()
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fileName = "autoglm_${dateFormat.format(Date())}.log"
            logFile = File(logDir, fileName)

            // 清理旧日志
            cleanOldLogs(logDir)

            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun d(tag: String, message: String) {
        log("D", tag, message)
    }

    fun i(tag: String, message: String) {
        log("I", tag, message)
    }

    fun w(tag: String, message: String) {
        log("W", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log("E", tag, message)
        throwable?.let {
            log("E", tag, it.stackTraceToString())
        }
    }

    private fun log(level: String, tag: String, message: String) {
        if (!isInitialized || logFile == null) return

        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                .format(Date())
            val logLine = "$timestamp $level/$tag: $message\n"

            // 检查文件大小
            if (logFile?.length() ?: 0 > MAX_LOG_SIZE) {
                rotateLogFile()
            }

            FileOutputStream(logFile, true).use { fos ->
                fos.write(logLine.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun rotateLogFile() {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                .format(Date())
            val rotatedFile = File(logFile?.parent, "autoglm_$timestamp.log.old")
            logFile?.renameTo(rotatedFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanOldLogs(logDir: File) {
        try {
            val logFiles = logDir.listFiles { file ->
                file.name.startsWith("autoglm_") && file.name.endsWith(".log.old")
            } ?: return

            if (logFiles.size > MAX_LOG_FILES) {
                logFiles.sortedByDescending { it.lastModified() }
                    .drop(MAX_LOG_FILES)
                    .forEach { it.delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogFile(): File? = logFile

    fun clearLogs() {
        try {
            logFile?.delete()
            logFile = null
            isInitialized = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRecentLogs(lines: Int = 100): String {
        if (logFile == null || !logFile!!.exists()) return ""

        return try {
            val content = logFile!!.readText()
            content.lines().takeLast(lines).joinToString("\n")
        } catch (e: Exception) {
            ""
        }
    }
}
