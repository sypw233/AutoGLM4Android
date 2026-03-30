package ovo.sypw.autoglm4android.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ovo.sypw.autoglm4android.data.local.room.entity.TaskHistoryEntity

/**
 * 任务历史 DAO
 */
@Dao
interface TaskHistoryDao {

    @Query("SELECT * FROM task_history ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskHistoryEntity>>

    @Query("SELECT * FROM task_history WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskHistoryEntity)

    @Query("DELETE FROM task_history WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("DELETE FROM task_history")
    suspend fun deleteAllTasks()

    @Query("SELECT COUNT(*) FROM task_history")
    suspend fun getTaskCount(): Int
}
