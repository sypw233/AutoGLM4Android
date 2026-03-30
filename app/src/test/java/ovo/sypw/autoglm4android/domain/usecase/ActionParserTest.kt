package ovo.sypw.autoglm4android.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ovo.sypw.autoglm4android.domain.model.AgentAction

/**
 * ActionParser 单元测试
 */
class ActionParserTest {

    @Test
    fun `parse - tap action`() {
        val action = ActionParser.parse("<answer>do(tap, x=500, y=300)</answer>")

        assertTrue(action is AgentAction.Tap)
        action as AgentAction.Tap
        assertEquals(500, action.x)
        assertEquals(300, action.y)
    }

    @Test
    fun `parse - swipe action`() {
        val action = ActionParser.parse("<answer>do(swipe, startX=100, startY=200, endX=500, endY=800)</answer>")

        assertTrue(action is AgentAction.Swipe)
        action as AgentAction.Swipe
        assertEquals(100, action.startX)
        assertEquals(200, action.startY)
        assertEquals(500, action.endX)
        assertEquals(800, action.endY)
    }

    @Test
    fun `parse - type action`() {
        val action = ActionParser.parse("<answer>do(type, text=\"Hello World\")</answer>")

        assertTrue(action is AgentAction.Type)
        action as AgentAction.Type
        assertEquals("Hello World", action.text)
    }

    @Test
    fun `parse - launch action`() {
        val action = ActionParser.parse("<answer>do(launch, app=\"微信\")</answer>")

        assertTrue(action is AgentAction.Launch)
        action as AgentAction.Launch
        assertEquals("微信", action.app)
    }

    @Test
    fun `parse - back action`() {
        val action = ActionParser.parse("<answer>do(back)</answer>")

        assertTrue(action is AgentAction.Back)
    }

    @Test
    fun `parse - home action`() {
        val action = ActionParser.parse("<answer>do(home)</answer>")

        assertTrue(action is AgentAction.Home)
    }

    @Test
    fun `parse - wait action`() {
        val action = ActionParser.parse("<answer>do(wait, seconds=3)</answer>")

        assertTrue(action is AgentAction.Wait)
        action as AgentAction.Wait
        assertEquals(3, action.durationSeconds)
    }

    @Test
    fun `parse - finish action`() {
        val action = ActionParser.parse("<answer>finish(message=\"任务完成\")</answer>")

        assertTrue(action is AgentAction.Finish)
        action as AgentAction.Finish
        assertEquals("任务完成", action.message)
    }

    @Test
    fun `parse - long press action`() {
        val action = ActionParser.parse("<answer>do(longPress, x=250, y=500)</answer>")

        assertTrue(action is AgentAction.LongPress)
        action as AgentAction.LongPress
        assertEquals(250, action.x)
        assertEquals(500, action.y)
    }

    @Test
    fun `parse - double tap action`() {
        val action = ActionParser.parse("<answer>do(doubleTap, x=500, y=500)</answer>")

        assertTrue(action is AgentAction.DoubleTap)
        action as AgentAction.DoubleTap
        assertEquals(500, action.x)
        assertEquals(500, action.y)
    }

    @Test
    fun `parse - unknown action returns finish`() {
        val action = ActionParser.parse("<answer>unknown_action</answer>")

        assertTrue(action is AgentAction.Finish)
    }

    @Test
    fun `parse - coordinates clamped to 0-999`() {
        val action = ActionParser.parse("<answer>do(tap, x=1500, y=-100)</answer>")

        assertTrue(action is AgentAction.Tap)
        action as AgentAction.Tap
        assertEquals(999, action.x)
        assertEquals(0, action.y)
    }
}
