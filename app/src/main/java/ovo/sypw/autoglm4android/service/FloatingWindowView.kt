package ovo.sypw.autoglm4android.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import ovo.sypw.autoglm4android.R
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 悬浮窗视图
 */
class FloatingWindowView(context: Context) : View(context) {

    // 创建根布局
    private var rootView: CardView = CardView(context).apply {
        radius = 24f
        cardElevation = 8f
        setCardBackgroundColor(Color.parseColor("#E6FFFFFF"))
        setContentPadding(24, 16, 24, 16)
    }

    // 状态文本
    private var statusText: TextView = TextView(context).apply {
        text = "等待任务..."
        textSize = 14f
        setTextColor(Color.BLACK)
    }
    private var closeButton: ImageView
    private var lastX = 0
    private var lastY = 0
    private var firstX = 0
    private var firstY = 0
    private var isClick = true
    private var onCloseListener: (() -> Unit)? = null

    private val steps = CopyOnWriteArrayList<String>()

    init {

        // 关闭按钮
        closeButton = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setOnClickListener {
                onCloseListener?.invoke()
            }
        }

        // 简单布局
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            addView(statusText)
            addView(closeButton)
        }

        rootView.addView(layout)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                firstX = lastX
                firstY = lastY
                isClick = true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - lastX).toInt()
                val dy = (event.rawY - lastY).toInt()

                if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                    isClick = false
                }

                // 更新位置
                val params = layoutParams as? WindowManager.LayoutParams
                params?.let {
                    it.x += dx
                    it.y += dy
                }

                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
            }
            MotionEvent.ACTION_UP -> {
                if (isClick) {
                    // 点击事件，可以展开详情
                }
            }
        }
        return true
    }

    fun setOnCloseListener(listener: () -> Unit) {
        onCloseListener = listener
    }

    fun updateStatus(status: String) {
        post {
            statusText.text = status
        }
    }

    fun addStep(step: String) {
        steps.add(step)
        post {
            // 更新显示最近几步
            val recentSteps = steps.takeLast(3)
            statusText.text = recentSteps.joinToString("\n")
        }
    }

    fun clearSteps() {
        steps.clear()
        post {
            statusText.text = "等待任务..."
        }
    }
}
