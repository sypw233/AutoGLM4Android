package ovo.sypw.autoglm4android.input

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputBinding
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import ovo.sypw.autoglm4android.R

/**
 * AutoGLM 自定义输入法服务
 * 用于在目标应用中输入文本
 */
class AutoGLMKeyboardService : InputMethodService() {

    private var textInputManager: TextInputManager? = null
    private var keyboardView: LinearLayout? = null
    private var isVoiceInputActive = false

    override fun onCreate() {
        super.onCreate()
        textInputManager = TextInputManager(this)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        // 绑定输入连接
        val ic = currentInputConnection
        textInputManager?.setInputConnection(ic)

        // 创建简单键盘视图
        createKeyboardView()
    }

    override fun onDestroy() {
        textInputManager?.reset()
        super.onDestroy()
    }

    override fun onBindInput() {
        super.onBindInput()
    }

    override fun onUnbindInput() {
        textInputManager?.reset()
        super.onUnbindInput()
    }

    private fun createKeyboardView() {
        keyboardView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFEEEEEE.toInt())
        }

        // 文字显示区域
        val statusText = TextView(this).apply {
            text = "AutoGLM 输入法已激活"
            setPadding(16, 16, 16, 16)
        }
        keyboardView?.addView(statusText)

        // 快捷操作按钮
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        // 语音输入按钮
        val voiceButton = Button(this).apply {
            text = "语音输入"
            setOnClickListener {
                startVoiceInput()
            }
        }
        buttonLayout.addView(voiceButton)

        // 发送按钮
        val sendButton = Button(this).apply {
            text = "发送"
            setOnClickListener {
                textInputManager?.pressEnter()
            }
        }
        buttonLayout.addView(sendButton)

        // 删除按钮
        val deleteButton = Button(this).apply {
            text = "删除"
            setOnClickListener {
                textInputManager?.pressBackspace()
            }
        }
        buttonLayout.addView(deleteButton)

        keyboardView?.addView(buttonLayout)

        // 设置键盘视图
        setCandidatesView(keyboardView)
    }

    private fun startVoiceInput() {
        try {
            val intent = Intent(this, VoiceInputService::class.java)
            startService(intent)
            isVoiceInputActive = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopVoiceInput() {
        try {
            val intent = Intent(this, VoiceInputService::class.java)
            stopService(intent)
            isVoiceInputActive = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        return when (keyCode) {
            android.view.KeyEvent.KEYCODE_ENTER -> {
                textInputManager?.pressEnter()
                true
            }
            android.view.KeyEvent.KEYCODE_DEL -> {
                textInputManager?.pressBackspace()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    /**
     * 获取文本输入管理器
     */
    fun getTextInputManager(): TextInputManager? = textInputManager

    /**
     * 检查是否为语音输入模式
     */
    fun isVoiceInputMode(): Boolean = isVoiceInputActive
}
