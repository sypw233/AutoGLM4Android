package ovo.sypw.autoglm4android.util

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 人性化滑动生成器
 * 生成更自然的滑动路径
 */
@Singleton
class HumanizedSwipeGenerator @Inject constructor() {

    data class Point(val x: Int, val y: Int)

    companion object {
        /**
         * 生成滑动路径点
         */
        fun generatePath(
            startX: Int, startY: Int,
            endX: Int, endY: Int,
            numPoints: Int = 10
        ): List<Point> {
            val points = mutableListOf<Point>()

            for (i in 0..numPoints) {
                val t = i.toFloat() / numPoints
                val x = (startX + (endX - startX) * t).toInt()
                val y = (startY + (endY - startY) * t).toInt()

                // 添加微小的随机偏移，使滑动更自然
                val offsetX = if (i > 0 && i < numPoints) {
                    (Math.random() * 4 - 2).toInt()
                } else 0
                val offsetY = if (i > 0 && i < numPoints) {
                    (Math.random() * 4 - 2).toInt()
                } else 0

                points.add(Point(x + offsetX, y + offsetY))
            }

            return points
        }
    }
}
