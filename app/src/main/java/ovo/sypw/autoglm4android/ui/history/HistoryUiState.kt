package ovo.sypw.autoglm4android.ui.history

import ovo.sypw.autoglm4android.domain.model.Task
import ovo.sypw.autoglm4android.domain.model.TaskStatus

/**
 * 历史记录 UI 状态
 */
data class HistoryUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTask: Task? = null,
    val errorMessage: String? = null
)

/**
 * 历史记录事件
 */
sealed class HistoryEvent {
    data class SelectTask(val task: Task) : HistoryEvent()
    data object DeleteTask : HistoryEvent()
    data object ClearHistory : HistoryEvent()
    data object NavigateBack : HistoryEvent()
}
