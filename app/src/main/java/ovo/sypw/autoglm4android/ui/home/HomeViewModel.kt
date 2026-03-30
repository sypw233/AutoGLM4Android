package ovo.sypw.autoglm4android.ui.home

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
import ovo.sypw.autoglm4android.domain.model.TaskStatus
import ovo.sypw.autoglm4android.domain.repository.TaskRepository
import ovo.sypw.autoglm4android.domain.usecase.ExecuteTaskUseCase
import ovo.sypw.autoglm4android.domain.usecase.TaskExecutionEvent
import ovo.sypw.autoglm4android.service.ShizukuService
import javax.inject.Inject

/**
 * 首页 ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val executeTaskUseCase: ExecuteTaskUseCase,
    private val taskRepository: TaskRepository,
    private val shizukuService: ShizukuService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        observeTaskState()
        checkPermissions()
    }

    private fun observeTaskState() {
        viewModelScope.launch {
            taskRepository.observeCurrentTask().collect { task ->
                _uiState.update {
                    it.copy(
                        currentTask = task,
                        isTaskRunning = task?.status == TaskStatus.RUNNING,
                        canStartTask = calculateCanStartTask(task)
                    )
                }
            }
        }
    }

    private fun checkPermissions() {
        _uiState.update {
            it.copy(
                permissionStates = it.permissionStates.copy(
                    shizuku = shizukuService.isAvailable() && shizukuService.hasPermission()
                )
            )
        }
    }

    fun onTaskInputChanged(text: String) {
        _uiState.update {
            it.copy(taskInput = text, canStartTask = text.isNotBlank() && it.permissionStates.shizuku)
        }
    }

    fun onStartTask() {
        val taskDescription = _uiState.value.taskInput
        if (!canStartTask(taskDescription)) return

        val task = Task(description = taskDescription)

        viewModelScope.launch {
            executeTaskUseCase.reset()
            executeTaskUseCase.execute(taskDescription).collect { event ->
                when (event) {
                    is TaskExecutionEvent.Started -> {
                        _events.emit(HomeEvent.MinimizeApp)
                        _uiState.update { it.copy(isTaskRunning = true) }
                    }
                    is TaskExecutionEvent.ThinkingUpdated -> {
                        _uiState.update { it.copy(thinking = event.thinking) }
                    }
                    is TaskExecutionEvent.ActionExecuted -> {
                        // 可以在这里更新步骤列表
                    }
                    is TaskExecutionEvent.Completed -> {
                        _events.emit(HomeEvent.ShowToast("任务完成: ${event.message}"))
                        _uiState.update {
                            it.copy(isTaskRunning = false, thinking = "")
                        }
                    }
                    is TaskExecutionEvent.Error -> {
                        _events.emit(HomeEvent.ShowToast("错误: ${event.message}"))
                        _uiState.update {
                            it.copy(isTaskRunning = false, errorMessage = event.message)
                        }
                    }
                    is TaskExecutionEvent.MaxStepsReached -> {
                        _events.emit(HomeEvent.ShowToast("已达到最大步骤数"))
                        _uiState.update { it.copy(isTaskRunning = false) }
                    }
                    is TaskExecutionEvent.Cancelled -> {
                        _events.emit(HomeEvent.ShowToast("任务已取消"))
                        _uiState.update { it.copy(isTaskRunning = false) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun onPauseTask() {
        executeTaskUseCase.pause()
        _uiState.update { it.copy(isTaskRunning = false) }
    }

    fun onResumeTask() {
        executeTaskUseCase.resume()
        _uiState.update { it.copy(isTaskRunning = true) }
    }

    fun onCancelTask() {
        executeTaskUseCase.cancel()
        _uiState.update { it.copy(isTaskRunning = false, thinking = "") }
    }

    private fun canStartTask(taskDescription: String): Boolean {
        val state = _uiState.value
        return state.permissionStates.shizuku &&
                taskDescription.isNotBlank() &&
                !state.isTaskRunning
    }

    private fun calculateCanStartTask(task: Task?): Boolean {
        return task?.status != TaskStatus.RUNNING
    }
}
