package ovo.sypw.autoglm4android.domain.repository

import kotlinx.coroutines.flow.Flow
import ovo.sypw.autoglm4android.domain.model.Task
import ovo.sypw.autoglm4android.domain.model.TaskStep

/**
 * 任务仓库接口
 */
interface TaskRepository {
    fun observeCurrentTask(): Flow<Task?>
    suspend fun startTask(task: Task): Flow<TaskStep>
    suspend fun pauseTask()
    suspend fun resumeTask()
    suspend fun cancelTask()
    suspend fun saveTaskHistory(task: Task)
    fun getTaskHistory(): Flow<List<Task>>
}
