package ovo.sypw.autoglm4android.input

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 文本输入管理器
 * 用于通过输入法发送文本到目标应用
 */
class TextInputManager(private val context: Context) {

    private var inputConnection: InputConnection? = null
    private var currentInputConnection: InputConnection? = null

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    /**
     * 绑定输入连接
     */
    fun setInputConnection(ic: InputConnection?) {
        currentInputConnection = ic
        _isActive.value = ic != null
    }

    /**
     * 输入文本
     */
    fun typeText(text: String): Boolean {
        return try {
            currentInputConnection?.commitText(text, 1) ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 删除文本
     */
    fun deleteText(count: Int = 1): Boolean {
        return try {
            for (i in 0 until count) {
                currentInputConnection?.deleteSurroundingText(1, 0)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 发送软键盘动作
     */
    fun sendEditorAction(actionId: Int): Boolean {
        return try {
            currentInputConnection?.performEditorAction(actionId) ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 按下按键
     */
    fun pressKey(keyCode: Int): Boolean {
        return try {
            currentInputConnection?.sendKeyEvent(
                android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_DOWN,
                    keyCode
                )
            )
            currentInputConnection?.sendKeyEvent(
                android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_UP,
                    keyCode
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 模拟回车键
     */
    fun pressEnter(): Boolean {
        return pressKey(android.view.KeyEvent.KEYCODE_ENTER)
    }

    /**
     * 模拟删除键
     */
    fun pressBackspace(): Boolean {
        return pressKey(android.view.KeyEvent.KEYCODE_DEL)
    }

    /**
     * 获取当前光标位置
     */
    fun getCursorPosition(): Int {
        return try {
            val ext = currentInputConnection?.getExtractedText(
                android.view.inputmethod.ExtractedTextRequest(),
                0
            )
            ext?.selectionStart ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * 清理
     */
    fun reset() {
        currentInputConnection = null
        _isActive.value = false
    }
}
