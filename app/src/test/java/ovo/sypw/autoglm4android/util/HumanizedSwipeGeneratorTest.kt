package ovo.sypw.autoglm4android.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * HumanizedSwipeGenerator 单元测试
 */
class HumanizedSwipeGeneratorTest {

    @Test
    fun `generatePath - 生成正确数量的点`() {
        val path = HumanizedSwipeGenerator.generatePath(0, 0, 500, 500, 10)

        assertEquals(11, path.size)
    }

    @Test
    fun `generatePath - 起点和终点正确`() {
        val path = HumanizedSwipeGenerator.generatePath(100, 200, 500, 800, 5)

        val first = path.first()
        assertEquals(100, first.x)
        assertEquals(200, first.y)

        val last = path.last()
        assertEquals(500, last.x)
        assertEquals(800, last.y)
    }

    @Test
    fun `generatePath - 点在路径范围内`() {
        val path = HumanizedSwipeGenerator.generatePath(100, 100, 500, 500, 10)

        path.forEach { point ->
            assertTrue(point.x in 0..600)
            assertTrue(point.y in 0..600)
        }
    }

    @Test
    fun `generatePath - 默认点数为10`() {
        val path = HumanizedSwipeGenerator.generatePath(0, 0, 100, 100)

        assertEquals(11, path.size)
    }

    @Test
    fun `generatePath - 处理负坐标`() {
        val path = HumanizedSwipeGenerator.generatePath(-100, -100, 100, 100, 5)

        // 负坐标会被转换为0
        val first = path.first()
        assertTrue(first.x >= 0)
        assertTrue(first.y >= 0)
    }
}
