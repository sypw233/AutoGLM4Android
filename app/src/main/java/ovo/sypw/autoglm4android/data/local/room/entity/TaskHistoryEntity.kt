package ovo.sypw.autoglm4android.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 任务历史实体
 */
@Entity(tableName = "task_history")
data class TaskHistoryEntity(
    @PrimaryKey
    val id: String,
    val description: String,
    val status: String,
    val stepsJson: String,
    val createdAt: Long,
    val completedAt: Long?
)

/**
 * 任务步骤实体
 */
data class TaskStepEntity(
    val stepNumber: Int,
    val thinking: String,
    val actionType: String?,
    val actionData: String?,
    val actionDescription: String,
    val isSuccess: Boolean,
    val message: String?,
    val timestamp: Long
)
