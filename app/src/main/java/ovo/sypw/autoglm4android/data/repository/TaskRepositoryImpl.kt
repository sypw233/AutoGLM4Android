package ovo.sypw.autoglm4android.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import ovo.sypw.autoglm4android.domain.model.Task
import ovo.sypw.autoglm4android.domain.model.TaskStep
import ovo.sypw.autoglm4android.domain.repository.TaskRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 任务仓库实现
 */
@Singleton
class TaskRepositoryImpl @Inject constructor() : TaskRepository {

    private val _currentTask = MutableStateFlow<Task?>(null)

    override fun observeCurrentTask(): Flow<Task?> = _currentTask.asStateFlow()

    override suspend fun startTask(task: Task): Flow<TaskStep> = flow {
        _currentTask.value = task.copy(status = ovo.sypw.autoglm4android.domain.model.TaskStatus.RUNNING)
    }

    override suspend fun pauseTask() {
        _currentTask.value?.let { task ->
            _currentTask.value = task.copy(status = ovo.sypw.autoglm4android.domain.model.TaskStatus.PAUSED)
        }
    }

    override suspend fun resumeTask() {
        _currentTask.value?.let { task ->
            _currentTask.value = task.copy(status = ovo.sypw.autoglm4android.domain.model.TaskStatus.RUNNING)
        }
    }

    override suspend fun cancelTask() {
        _currentTask.value?.let { task ->
            _currentTask.value = task.copy(
                status = ovo.sypw.autoglm4android.domain.model.TaskStatus.CANCELLED,
                completedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun saveTaskHistory(task: Task) {
        // TODO: 实现 Room 数据库存储
    }

    override fun getTaskHistory(): Flow<List<Task>> = flow {
        emit(emptyList())
    }
}
