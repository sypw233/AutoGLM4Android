package ovo.sypw.autoglm4android.util

/**
 * 坐标转换器
 * 将相对坐标(0-999)转换为绝对像素坐标
 */
object CoordinateConverter {

    /**
     * 相对坐标转绝对坐标
     */
    fun toAbsolute(
        relX: Int, relY: Int,
        screenWidth: Int, screenHeight: Int
    ): Pair<Int, Int> {
        val absX = (relX * screenWidth) / 1000
        val absY = (relY * screenHeight) / 1000
        return Pair(absX, absY)
    }

    /**
     * 绝对坐标转相对坐标
     */
    fun toRelative(
        absX: Int, absY: Int,
        screenWidth: Int, screenHeight: Int
    ): Pair<Int, Int> {
        val relX = (absX * 1000) / screenWidth
        val relY = (absY * 1000) / screenHeight
        return Pair(relX, relY)
    }
}
