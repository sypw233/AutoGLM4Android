package ovo.sypw.autoglm4android.ui.home

/**
 * 首页 UI 状态
 */
data class HomeUiState(
    val taskInput: String = "",
    val currentTask: ovo.sypw.autoglm4android.domain.model.Task? = null,
    val isTaskRunning: Boolean = false,
    val canStartTask: Boolean = false,
    val thinking: String = "",
    val permissionStates: PermissionStates = PermissionStates(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 权限状态
 */
data class PermissionStates(
    val shizuku: Boolean = false,
    val overlay: Boolean = false,
    val keyboard: Boolean = false,
)

/**
 * 首页事件
 */
sealed class HomeEvent {
    data object MinimizeApp : HomeEvent()
    data class ShowToast(val message: String) : HomeEvent()
    data object NavigateToSettings : HomeEvent()
}
