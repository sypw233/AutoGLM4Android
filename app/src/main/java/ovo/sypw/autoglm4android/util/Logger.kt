package ovo.sypw.autoglm4android.util

import android.util.Log

/**
 * 日志工具类
 */
object Logger {
    private const val TAG = "AutoGLM"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}
