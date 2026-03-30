package ovo.sypw.autoglm4android.domain.model

import java.util.UUID

/**
 * 任务状态枚举
 */
enum class TaskStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * 任务领域模型
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val steps: List<TaskStep> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

/**
 * 任务步骤
 */
data class TaskStep(
    val stepNumber: Int,
    val thinking: String = "",
    val action: AgentAction? = null,
    val actionDescription: String = "",
    val isSuccess: Boolean = true,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 任务执行结果
 */
data class TaskResult(
    val success: Boolean,
    val message: String,
    val steps: List<TaskStep> = emptyList()
)
