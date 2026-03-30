package ovo.sypw.autoglm4android.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ovo.sypw.autoglm4android.data.local.room.TaskHistoryDao
import ovo.sypw.autoglm4android.data.local.room.entity.TaskHistoryEntity
import ovo.sypw.autoglm4android.domain.model.AgentAction
import ovo.sypw.autoglm4android.domain.model.Task
import ovo.sypw.autoglm4android.domain.model.TaskStep
import ovo.sypw.autoglm4android.domain.model.TaskStatus
import ovo.sypw.autoglm4android.domain.repository.TaskRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 任务仓库实现
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskHistoryDao: TaskHistoryDao
) : TaskRepository {

    private val _currentTask = MutableStateFlow<Task?>(null)
    private val json = Json { ignoreUnknownKeys = true }

    override fun observeCurrentTask(): Flow<Task?> = _currentTask.asStateFlow()

    override suspend fun startTask(task: Task): Flow<TaskStep> = flow {
        _currentTask.value = task.copy(status = TaskStatus.RUNNING)
    }

    override suspend fun pauseTask() {
        _currentTask.value?.let { task ->
            _currentTask.value = task.copy(status = TaskStatus.PAUSED)
        }
    }

    override suspend fun resumeTask() {
        _currentTask.value?.let { task ->
            _currentTask.value = task.copy(status = TaskStatus.RUNNING)
        }
    }

    override suspend fun cancelTask() {
        _currentTask.value?.let { task ->
            _currentTask.value = task.copy(
                status = TaskStatus.CANCELLED,
                completedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun saveTaskHistory(task: Task) {
        val entity = TaskHistoryEntity(
            id = task.id,
            description = task.description,
            status = task.status.name,
            stepsJson = serializeSteps(task.steps),
            createdAt = task.createdAt,
            completedAt = task.completedAt
        )
        taskHistoryDao.insertTask(entity)
    }

    override fun getTaskHistory(): Flow<List<Task>> {
        return taskHistoryDao.getAllTasks().map { entities ->
            entities.map { entity ->
                Task(
                    id = entity.id,
                    description = entity.description,
                    status = TaskStatus.valueOf(entity.status),
                    steps = deserializeSteps(entity.stepsJson),
                    createdAt = entity.createdAt,
                    completedAt = entity.completedAt
                )
            }
        }
    }

    private fun serializeSteps(steps: List<TaskStep>): String {
        return steps.joinToString("|||") { step ->
            listOf(
                step.stepNumber.toString(),
                step.thinking,
                step.action?.let { serializeAction(it) } ?: "",
                step.actionDescription,
                step.isSuccess.toString(),
                step.message ?: "",
                step.timestamp.toString()
            ).joinToString(":::")
        }
    }

    private fun deserializeSteps(json: String): List<TaskStep> {
        if (json.isBlank()) return emptyList()
        return json.split("|||").mapNotNull { stepStr ->
            val parts = stepStr.split(":::")
            if (parts.size >= 7) {
                TaskStep(
                    stepNumber = parts[0].toIntOrNull() ?: return@mapNotNull null,
                    thinking = parts[1],
                    action = if (parts[2].isNotBlank()) deserializeAction(parts[2]) else null,
                    actionDescription = parts[3],
                    isSuccess = parts[4].toBoolean(),
                    message = parts[5].ifBlank { null },
                    timestamp = parts[6].toLongOrNull() ?: System.currentTimeMillis()
                )
            } else null
        }
    }

    private fun serializeAction(action: AgentAction): String {
        return when (action) {
            is AgentAction.Tap -> "Tap:${action.x},${action.y},${action.message ?: ""}"
            is AgentAction.Swipe -> "Swipe:${action.startX},${action.startY},${action.endX},${action.endY},${action.humanized}"
            is AgentAction.LongPress -> "LongPress:${action.x},${action.y}"
            is AgentAction.DoubleTap -> "DoubleTap:${action.x},${action.y}"
            is AgentAction.Type -> "Type:${action.text}"
            is AgentAction.Launch -> "Launch:${action.app}"
            is AgentAction.Back -> "Back"
            is AgentAction.Home -> "Home"
            is AgentAction.Wait -> "Wait:${action.durationSeconds}"
            is AgentAction.Finish -> "Finish:${action.message}"
            is AgentAction.Batch -> "Batch:${action.steps.size}"
        }
    }

    private fun deserializeAction(data: String): AgentAction? {
        return try {
            val parts = data.split(":")
            when (parts[0]) {
                "Tap" -> {
                    val coords = parts[1].split(",")
                    AgentAction.Tap(coords[0].toInt(), coords[1].toInt(), coords.getOrNull(2)?.ifBlank { null })
                }
                "Swipe" -> {
                    val coords = parts[1].split(",")
                    AgentAction.Swipe(coords[0].toInt(), coords[1].toInt(), coords[2].toInt(), coords[3].toInt(), coords.getOrNull(4)?.toBoolean() ?: true)
                }
                "LongPress" -> {
                    val coords = parts[1].split(",")
                    AgentAction.LongPress(coords[0].toInt(), coords[1].toInt())
                }
                "DoubleTap" -> {
                    val coords = parts[1].split(",")
                    AgentAction.DoubleTap(coords[0].toInt(), coords[1].toInt())
                }
                "Type" -> AgentAction.Type(parts[1])
                "Launch" -> AgentAction.Launch(parts[1])
                "Back" -> AgentAction.Back
                "Home" -> AgentAction.Home
                "Wait" -> AgentAction.Wait(parts[1].toInt())
                "Finish" -> AgentAction.Finish(parts[1])
                "Batch" -> AgentAction.Batch(emptyList())
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
