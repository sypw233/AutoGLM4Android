package ovo.sypw.autoglm4android.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ovo.sypw.autoglm4android.domain.model.AgentAction
import ovo.sypw.autoglm4android.domain.model.ModelResponse
import ovo.sypw.autoglm4android.domain.model.Screenshot
import ovo.sypw.autoglm4android.domain.repository.ModelRepository
import ovo.sypw.autoglm4android.domain.repository.ModelResult
import ovo.sypw.autoglm4android.domain.repository.ScreenshotRepository

/**
 * ExecuteTaskUseCase 单元测试
 */
class ExecuteTaskUseCaseTest {

    private lateinit var modelRepository: ModelRepository
    private lateinit var screenshotRepository: ScreenshotRepository
    private lateinit var executeActionUseCase: ExecuteActionUseCase
    private lateinit var useCase: ExecuteTaskUseCase

    @Before
    fun setup() {
        modelRepository = mockk()
        screenshotRepository = mockk()
        executeActionUseCase = mockk()
        useCase = ExecuteTaskUseCase(modelRepository, screenshotRepository, executeActionUseCase)
    }

    @Test
    fun `execute - 任务成功完成`() = runTest {
        // Given
        val screenshot = Screenshot("base64data", 1080, 1920)
        coEvery { screenshotRepository.capture() } returns screenshot
        coEvery { modelRepository.request(any()) } returns ModelResult.Success(
            ModelResponse(
                thinking = "我需要打开微信",
                action = "<answer>do(launch, app=\"微信\")</answer>",
                rawContent = "<thinking>我需要打开微信</thinking><answer>do(launch, app=\"微信\")</answer>"
            )
        )
        coEvery { executeActionUseCase.execute(any()) } returns ovo.sypw.autoglm4android.domain.model.ActionResult(
            success = true,
            message = "启动应用",
            shouldFinish = true
        )

        // When
        val events = mutableListOf<TaskExecutionEvent>()
        useCase.execute("打开微信").collect { event ->
            events.add(event)
        }

        // Then
        assertTrue(events.any { it is TaskExecutionEvent.Started })
        assertTrue(events.any { it is TaskExecutionEvent.Completed })
    }

    @Test
    fun `execute - 模型请求失败`() = runTest {
        // Given
        val screenshot = Screenshot("base64data", 1080, 1920)
        coEvery { screenshotRepository.capture() } returns screenshot
        coEvery { modelRepository.request(any()) } returns ModelResult.Error(
            ovo.sypw.autoglm4android.domain.repository.NetworkError.ConnectionFailed("网络错误")
        )

        // When
        val events = mutableListOf<TaskExecutionEvent>()
        useCase.execute("打开微信").collect { event ->
            events.add(event)
        }

        // Then
        assertTrue(events.any { it is TaskExecutionEvent.Error })
    }
}
