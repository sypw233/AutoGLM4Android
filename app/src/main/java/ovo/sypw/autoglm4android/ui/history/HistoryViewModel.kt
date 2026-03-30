package ovo.sypw.autoglm4android.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ovo.sypw.autoglm4android.domain.model.Task
import ovo.sypw.autoglm4android.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * 历史记录 ViewModel
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HistoryEvent>()
    val events: SharedFlow<HistoryEvent> = _events.asSharedFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            taskRepository.getTaskHistory().collect { tasks ->
                _uiState.update {
                    it.copy(tasks = tasks, isLoading = false)
                }
            }
        }
    }

    fun onTaskSelected(task: Task) {
        _uiState.update { it.copy(selectedTask = task) }
    }

    fun onDeleteTask(task: Task) {
        viewModelScope.launch {
            try {
                // 从数据库删除
                loadHistory()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            try {
                loadHistory()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch {
            _events.emit(HistoryEvent.NavigateBack)
        }
    }
}
