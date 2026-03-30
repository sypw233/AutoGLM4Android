package ovo.sypw.autoglm4android.config

/**
 * 国际化支持
 */
object I18n {

    private var currentLocale: String = "zh_CN"

    private val zh_CN = mapOf(
        "app_name" to "AutoGLM",
        "home_title" to "首页",
        "settings_title" to "设置",
        "history_title" to "历史记录",
        "task_input_hint" to "输入任务描述，例如：打开微信，给文件传输助手发送消息",
        "start_task" to "开始任务",
        "pause_task" to "暂停",
        "stop_task" to "停止",
        "resume_task" to "继续",
        "thinking" to "思考过程",
        "permission_shizuku" to "Shizuku 连接",
        "permission_overlay" to "悬浮窗权限",
        "permission_keyboard" to "输入法权限",
        "connected" to "已连接",
        "not_connected" to "未连接",
        "connecting" to "连接中...",
        "model_config" to "模型配置",
        "api_base_url" to "API Base URL",
        "api_key" to "API Key",
        "model_name" to "模型名称",
        "temperature" to "Temperature",
        "max_tokens" to "Max Tokens",
        "test_connection" to "测试连接",
        "connection_success" to "连接成功",
        "connection_failed" to "连接失败",
        "save_settings" to "保存设置",
        "agent_config" to "Agent 配置",
        "max_steps" to "最大步骤数",
        "enable_voice" to "语音输入",
        "enable_floating_window" to "悬浮窗",
        "empty_history" to "暂无历史记录",
        "task_completed" to "任务完成",
        "task_failed" to "任务失败",
        "task_cancelled" to "任务已取消",
        "max_steps_reached" to "已达到最大步骤数",
        "error" to "错误",
        "confirm" to "确定",
        "cancel" to "取消",
        "delete" to "删除",
        "clear_history" to "清空历史记录",
        "confirm_clear_history" to "确定要清空所有历史记录吗？此操作不可恢复。",
        "steps" to "步骤",
        "created_at" to "创建时间",
        "status_pending" to "等待中",
        "status_running" to "运行中",
        "status_paused" to "已暂停",
        "status_completed" to "已完成",
        "status_failed" to "失败",
        "status_cancelled" to "已取消",
        "voice_input" to "语音输入",
        "voice_listening" to "正在录音...",
        "voice_error" to "录音失败",
        "keyboard_activated" to "AutoGLM 输入法已激活",
        "action_tap" to "点击",
        "action_swipe" to "滑动",
        "action_type" to "输入",
        "action_launch" to "启动",
        "action_back" to "返回",
        "action_home" to "主页",
        "action_wait" to "等待",
        "action_finish" to "完成"
    )

    private val en_US = mapOf(
        "app_name" to "AutoGLM",
        "home_title" to "Home",
        "settings_title" to "Settings",
        "history_title" to "History",
        "task_input_hint" to "Enter task description, e.g., Open WeChat and send message",
        "start_task" to "Start Task",
        "pause_task" to "Pause",
        "stop_task" to "Stop",
        "resume_task" to "Resume",
        "thinking" to "Thinking",
        "permission_shizuku" to "Shizuku Connection",
        "permission_overlay" to "Overlay Permission",
        "permission_keyboard" to "Keyboard Permission",
        "connected" to "Connected",
        "not_connected" to "Not Connected",
        "connecting" to "Connecting...",
        "model_config" to "Model Config",
        "api_base_url" to "API Base URL",
        "api_key" to "API Key",
        "model_name" to "Model Name",
        "temperature" to "Temperature",
        "max_tokens" to "Max Tokens",
        "test_connection" to "Test Connection",
        "connection_success" to "Connection Success",
        "connection_failed" to "Connection Failed",
        "save_settings" to "Save Settings",
        "agent_config" to "Agent Config",
        "max_steps" to "Max Steps",
        "enable_voice" to "Voice Input",
        "enable_floating_window" to "Floating Window",
        "empty_history" to "No History",
        "task_completed" to "Task Completed",
        "task_failed" to "Task Failed",
        "task_cancelled" to "Task Cancelled",
        "max_steps_reached" to "Max Steps Reached",
        "error" to "Error",
        "confirm" to "Confirm",
        "cancel" to "Cancel",
        "delete" to "Delete",
        "clear_history" to "Clear History",
        "confirm_clear_history" to "Are you sure you want to clear all history? This action cannot be undone.",
        "steps" to "Steps",
        "created_at" to "Created At",
        "status_pending" to "Pending",
        "status_running" to "Running",
        "status_paused" to "Paused",
        "status_completed" to "Completed",
        "status_failed" to "Failed",
        "status_cancelled" to "Cancelled",
        "voice_input" to "Voice Input",
        "voice_listening" to "Recording...",
        "voice_error" to "Recording Failed",
        "keyboard_activated" to "AutoGLM Keyboard Activated",
        "action_tap" to "Tap",
        "action_swipe" to "Swipe",
        "action_type" to "Type",
        "action_launch" to "Launch",
        "action_back" to "Back",
        "action_home" to "Home",
        "action_wait" to "Wait",
        "action_finish" to "Finish"
    )

    fun setLocale(locale: String) {
        currentLocale = locale
    }

    fun getLocale(): String = currentLocale

    fun t(key: String): String {
        return when (currentLocale) {
            "zh_CN" -> zh_CN[key] ?: key
            "en_US" -> en_US[key] ?: key
            else -> zh_CN[key] ?: key
        }
    }

    fun getAvailableLocales(): List<Pair<String, String>> {
        return listOf(
            "zh_CN" to "简体中文",
            "en_US" to "English"
        )
    }
}
