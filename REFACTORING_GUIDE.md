# AutoGLM For Android 重构与迁移指南

> 基于现代 Android 开发规范 (Jetpack Compose + MVVM + Clean Architecture)

---

## 目录

1. [项目概述与重构目标](#1-项目概述与重构目标)
2. [原项目架构分析](#2-原项目架构分析)
3. [新项目架构设计](#3-新项目架构设计)
4. [模块映射与迁移指南](#4-模块映射与迁移指南)
5. [核心模块实现详解](#5-核心模块实现详解)
6. [依赖注入方案](#6-依赖注入方案)
7. [测试策略](#7-测试策略)
8. [CI/CD 配置](#8-cicd-配置)
9. [完整代码示例](#9-完整代码示例)
10. [迁移检查清单](#10-迁移检查清单)

---

## 1. 项目概述与重构目标

### 1.1 项目简介

AutoGLM For Android 是一个基于自然语言的手机自动化助手应用。用户通过输入任务描述，AI Agent 自动分析屏幕截图并执行相应操作（点击、滑动、输入等）。

### 1.2 原项目问题

| 问题 | 描述 |
|------|------|
| 无清晰架构 | 代码混在一起，缺乏分层 |
| 缺少 DI | 手动单例管理，耦合严重 |
| 非响应式 | 未使用 Kotlin Flow，管理状态繁琐 |
| 非 Compose | 使用传统 View 系统，维护困难 |
| 测试困难 | 直接依赖具体类，单元测试受限 |
| 模块化不足 | 组件间耦合高，难以复用 |

### 1.3 重构目标

- ✅ 采用 **Clean Architecture** (UI → Domain → Data)
- ✅ 采用 **MVVM** + **Unidirectional Data Flow**
- ✅ 使用 **Jetpack Compose** 构建 UI
- ✅ 使用 **Hilt** 进行依赖注入
- ✅ 使用 **Kotlin Coroutines + Flow** 处理异步和状态
- ✅ 完全 **模块化**，便于测试和复用
- ✅ 完善的 **单元测试** 和 **集成测试**

---

## 2. 原项目架构分析

### 2.1 当前目录结构

```
app/src/main/java/com/kevinluo/autoglm/
├── action/                 # 动作处理模块
│   ├── ActionHandler.kt    # 动作执行器 ⚠️ 耦合重
│   ├── ActionParser.kt     # 动作解析器
│   └── AgentAction.kt      # 动作数据类
├── agent/                  # Agent 核心模块 ⚠️ 业务核心
│   ├── PhoneAgent.kt       # 手机 Agent 主类
│   └── AgentContext.kt     # 对话上下文管理
├── app/                    # 应用基础模块
│   ├── AppInfo.kt
│   ├── AppResolver.kt
│   └── AutoGLMApplication.kt
├── config/                 # 配置模块
├── device/                 # 设备操作模块 ⚠️ 强耦合 Shizuku
│   └── DeviceExecutor.kt
├── history/                # 历史记录模块
├── input/                  # 输入模块
├── model/                  # 模型通信模块
│   └── ModelClient.kt
├── screenshot/             # 截图模块
├── settings/               # 设置模块
├── task/                   # 任务管理模块
│   └── TaskExecutionManager.kt  ⚠️ 单例对象
├── ui/                     # UI 模块 ⚠️ 非 Compose
│   ├── ...
│   └── MainViewModel.kt
└── util/                   # 工具模块
```

### 2.2 核心问题分析

#### 2.2.1 PhoneAgent (500+ 行单类)

```kotlin
// 原项目: 巨大的 PhoneAgent 类
class PhoneAgent(
    private val modelClient: ModelClient,
    private val actionHandler: ActionHandler,
    private val screenshotService: ScreenshotService,
    // ...
) {
    suspend fun run(task: String): TaskResult { ... }
    suspend fun executeStep(task: String?, hint: String?): StepResult { ... }
    // 数百行业务逻辑...
}
```

**问题**: 所有逻辑（状态管理、流程控制、错误处理）都在一个类里

#### 2.2.2 无依赖注入

```kotlin
// 原项目: 手动单例
object TaskExecutionManager {
    private val _taskState = MutableStateFlow(TaskExecutionState())
    val taskState: StateFlow<TaskExecutionState> = _taskState.asStateFlow()
    // ...
}
```

**问题**: 无法 mock，难以测试，状态管理混乱

#### 2.2.3 UI 层问题

```kotlin
// 原项目: 非 Compose + ViewBinding
class MainActivity : BaseActivity() {
    private lateinit var viewModel: MainViewModel
    // findViewById  everywhere...
}
```

**问题**: 无法响应式更新，代码冗余

---

## 3. 新项目架构设计

### 3.1 Clean Architecture 分层

```
┌────────────────────────────────────────────────────────────────────────────┐
│                           Presentation Layer (UI)                          │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  Screens (Compose)  ←→  ViewModels  ←→  UI State (StateFlow)         │  │
│  │  • HomeScreen     • HomeViewModel      • HomeUiState                 │  │
│  │  • TaskScreen     • TaskViewModel      • TaskUiState                 │  │
│  │  • SettingsScreen • SettingsViewModel  • SettingsUiState            │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
├────────────────────────────────────────────────────────────────────────────┤
│                              Domain Layer                                  │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  Use Cases         Repositories (Interfaces)    Domain Models        │  │
│  │  • ExecuteTaskUseCase     • TaskRepository      • Task                │  │
│  │  • ScreenshotUseCase      • ModelRepository     • TaskStep            │  │
│  │  • ActionHandlerUseCase   • ScreenshotService   • AgentAction         │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
├────────────────────────────────────────────────────────────────────────────┤
│                               Data Layer                                   │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  Repository Implementations  Data Sources        DTOs                │  │
│  │  • TaskRepositoryImpl       • ShizukuService     • ModelRequest      │  │
│  │  • ModelClientImpl          • ScreenshotService  • ModelResponse     │  │
│  │  • HistoryRepositoryImpl   • LocalStorage       • ActionDto          │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 MVVM + UDF 架构

```kotlin
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Unidirectional Data Flow                            │
│                                                                             │
│  ┌─────────────┐     ┌──────────────┐     ┌─────────────┐                  │
│  │   Intent    │────▶│   ViewModel  │────▶│    View     │                  │
│  │ (User_func) │     │  (Process)   │     │  (Compose)  │                  │
│  └─────────────┘     └──────────────┘     └─────────────┘                  │
│       ▲                    │                      │                         │
│       │                    ▼                      │                         │
│       │             ┌──────────────┐              │                         │
│       │             │  UI State    │◀─────────────┘                         │
│       │             │ (StateFlow)  │                                        │
│       │             └──────────────┘                                        │
│       │                    │                                                │
│       └────────────────────┘                                                │
│                  ┌──────────────┐                                           │
│                  │ Use Cases    │                                           │
│                  │ (Business)   │                                           │
│                  └──────────────┘                                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.3 新项目目录结构

```
app/src/main/kotlin/com/autoglm/
├── di/                                 # 依赖注入 (Hilt)
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   ├── DeviceModule.kt
│   └── RepositoryModule.kt
│
├── data/                               # Data Layer
│   ├── remote/
│   │   ├── api/
│   │   │   ├── ModelApi.kt             # AI Model API 接口
│   │   │   └── dto/
│   │   │       ├── ChatCompletionRequest.kt
│   │   │       └── ChatCompletionResponse.kt
│   │   └── impl/
│   │       └── ModelClientImpl.kt      # API 实现
│   ├── local/
│   │   ├── preferences/
│   │   │   └── SettingsPreferences.kt  # DataStore
│   │   └── room/
│   │       ├── Database.kt
│   │       ├── TaskHistoryDao.kt
│   │       └── entity/
│   │           └── TaskHistoryEntity.kt
│   └── repository/
│       ├── TaskRepositoryImpl.kt
│       └── SettingsRepositoryImpl.kt
│
├── domain/                             # Domain Layer
│   ├── model/
│   │   ├── Task.kt
│   │   ├── TaskStep.kt
│   │   ├── TaskStatus.kt
│   │   ├── AgentAction.kt
│   │   ├── ModelConfig.kt
│   │   └── Screenshot.kt
│   ├── repository/
│   │   ├── TaskRepository.kt           # 接口
│   │   ├── ModelRepository.kt
│   │   └── SettingsRepository.kt
│   ├── usecase/
│   │   ├── ExecuteTaskUseCase.kt
│   │   ├── PauseTaskUseCase.kt
│   │   ├── CancelTaskUseCase.kt
│   │   ├── CaptureScreenshotUseCase.kt
│   │   └── ExecuteActionUseCase.kt
│   └── mapper/
│       ├── TaskMapper.kt
│       └── ActionMapper.kt
│
├── ui/                                 # Presentation Layer
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── components/
│   │   ├── TaskInputCard.kt
│   │   ├── ThinkingText.kt
│   │   ├── ActionCard.kt
│   │   └── PermissionCard.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── task/
│   │   ├── TaskScreen.kt
│   │   ├── TaskViewModel.kt
│   │   └── TaskUiState.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   ├── SettingsViewModel.kt
│   │   └── SettingsUiState.kt
│   ├── history/
│   │   ├── HistoryScreen.kt
│   │   ├── HistoryViewModel.kt
│   │   └── HistoryDetailScreen.kt
│   └── floating/
│       ├── FloatingWindowService.kt
│       └── FloatingWindowScreen.kt
│
├── service/                            # Android Services
│   ├── ShizukuService.kt
│   ├── ScreenshotService.kt
│   ├── DeviceExecutor.kt
│   └── VoiceInputService.kt
│
├── input/                              # Input Module
│   ├── AutoGLMKeyboardService.kt
│   └── TextInputManager.kt
│
└── App.kt                              # Application (Hilt)
```

---

## 4. 模块映射与迁移指南

### 4.1 模块映射表

| 原模块 | 新模块 | 迁移难度 | 说明 |
|--------|--------|----------|------|
| `agent/PhoneAgent.kt` | `domain/usecase/ExecuteTaskUseCase.kt` | ⭐⭐⭐⭐⭐ | 核心逻辑，需要拆分 |
| `model/ModelClient.kt` | `data/remote/impl/ModelClientImpl.kt` | ⭐⭐ | 改为接口实现 |
| `action/ActionHandler.kt` | `domain/usecase/ExecuteActionUseCase.kt` | ⭐⭐⭐ | 提取业务逻辑 |
| `device/DeviceExecutor.kt` | `service/DeviceExecutor.kt` | ⭐⭐⭐ | 保持原样，加接口 |
| `task/TaskExecutionManager.kt` | `domain/TaskStatus + ViewModel` | ⭐⭐⭐⭐ | 重构为 Flow |
| `ui/MainViewModel.kt` | `ui/home/HomeViewModel.kt` | ⭐⭐ | 拆分多个 VM |
| `history/HistoryManager.kt` | `data/local/room/` | ⭐⭐⭐ | 引入 Room |
| 传统 View + XML | `ui/*/Screen.kt` (Compose) | ⭐⭐⭐⭐⭐ | 完全重写 |

### 4.2 迁移优先级

```
Phase 1: 基础架构 (Week 1-2)
├── 搭建 Compose 项目骨架
├── 配置 Hilt 依赖注入
├── 创建基础 UI 组件
└── 实现 DataStore 偏好设置

Phase 2: 核心业务 (Week 2-3)
├── 迁移 ModelClient (接口化)
├── 迁移 DeviceExecutor (接口化)
├── 实现 ExecuteTaskUseCase
└── 实现 ExecuteActionUseCase

Phase 3: UI 重构 (Week 3-4)
├── 实现 HomeScreen + ViewModel
├── 实现 TaskScreen + ViewModel
├── 实现 SettingsScreen + ViewModel
└── 实现 HistoryScreen

Phase 4: 服务与功能 (Week 4-5)
├── 迁移 ScreenshotService
├── 迁移 VoiceInputService
├── 实现 FloatingWindowService
└── 实现快捷磁贴

Phase 5: 测试与优化 (Week 5-6)
├── 单元测试 (Use Cases)
├── UI 测试 (Compose)
├── 集成测试
└── 性能优化
```

---

## 5. 核心模块实现详解

### 5.1 Domain Layer - 模型定义

```kotlin
// domain/model/Task.kt
package com.autoglm.domain.model

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val steps: List<TaskStep> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class TaskStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

// domain/model/TaskStep.kt
data class TaskStep(
    val stepNumber: Int,
    val thinking: String = "",
    val action: AgentAction? = null,
    val actionDescription: String = "",
    val isSuccess: Boolean = true,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// domain/model/AgentAction.kt
sealed class AgentAction {
    data class Tap(val x: Int, val y: Int, val message: String? = null) : AgentAction()
    data class Swipe(
        val startX: Int, val startY: Int,
        val endX: Int, val endY: Int,
        val humanized: Boolean = true
    ) : AgentAction()
    data class Type(val text: String) : AgentAction()
    data class Launch(val app: String) : AgentAction()
    data object Back : AgentAction()
    data object Home : AgentAction()
    data class Wait(val durationSeconds: Int) : AgentAction()
    data class Finish(val message: String) : AgentAction()
    
    // 其他动作...
}
```

### 5.2 Domain Layer - Repository 接口

```kotlin
// domain/repository/ModelRepository.kt
package com.autoglm.domain.repository

import com.autoglm.domain.model.ModelConfig
import com.autoglm.domain.model.ModelResponse
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    suspend fun request(request: ModelRequest): ModelResult
    
    suspend fun testConnection(config: ModelConfig): TestConnectionResult
    
    fun cancelCurrentRequest()
}

data class ModelRequest(
    val messages: List<ChatMessage>,
    val screenshot: String? = null
)

sealed class ModelResult {
    data class Success(val response: ModelResponse) : ModelResult()
    data class Error(val error: NetworkError) : ModelResult()
}

sealed class TestConnectionResult {
    data class Success(val latencyMs: Long) : TestConnectionResult()
    data class Error(val message: String) : TestConnectionResult()
}

sealed class NetworkError : Exception() {
    data class ConnectionFailed(override val message: String) : NetworkError()
    data class Timeout(override val message: String) : NetworkError()
    data class ServerError(val code: Int, override val message: String) : NetworkError()
    data class AuthError(override val message: String) : NetworkError()
}

// domain/repository/TaskRepository.kt
interface TaskRepository {
    fun observeCurrentTask(): Flow<Task?>
    suspend fun startTask(task: Task): Flow<TaskStep>
    suspend fun pauseTask()
    suspend fun resumeTask()
    suspend fun cancelTask()
    suspend fun saveTaskHistory(task: Task)
    fun getTaskHistory(): Flow<List<Task>>
}
```

### 5.3 Domain Layer - Use Cases

```kotlin
// domain/usecase/ExecuteTaskUseCase.kt
package com.autoglm.domain.usecase

import com.autoglm.domain.model.TaskResult
import com.autoglm.domain.model.TaskStatus
import com.autoglm.domain.repository.ModelRepository
import com.autoglm.domain.repository.TaskRepository
import com.autoglm.domain.repository.ScreenshotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ExecuteTaskUseCase @Inject constructor(
    private val modelRepository: ModelRepository,
    private val taskRepository: TaskRepository,
    private val screenshotRepository: ScreenshotRepository,
) {
    /**
     * 执行任务的核心 Use Case
     * 
     * 流程:
     * 1. 截图 -> 发送给模型
     * 2. 解析模型响应 (thinking + action)
     * 3. 执行动作
     * 4. 判断是否完成，循环或结束
     */
    suspend fun execute(taskDescription: String): Flow<TaskExecutionEvent> = flow {
        emit(TaskExecutionEvent.Started(taskDescription))
        
        var stepCount = 0
        var isFinished = false
        var lastHint: String? = null
        
        while (!isFinished && stepCount < MAX_STEPS) {
            // 1. 截图
            val screenshot = screenshotRepository.capture()
            emit(TaskExecutionEvent.ScreenshotCaptured)
            
            // 2. 构建请求
            val request = buildModelRequest(taskDescription, screenshot, lastHint)
            
            // 3. 调用模型
            when (val result = modelRepository.request(request)) {
                is ModelResult.Success -> {
                    emit(TaskExecutionEvent.ThinkingUpdated(result.response.thinking))
                    
                    // 4. 解析动作
                    val action = ActionParser.parse(result.response.action)
                    emit(TaskExecutionEvent.ActionParsed(action))
                    
                    // 5. 执行动作
                    val executeResult = actionExecutor.execute(action)
                    emit(TaskExecutionEvent.ActionExecuted(executeResult))
                    
                    // 6. 判断是否完成
                    if (executeResult.shouldFinish) {
                        isFinished = true
                        emit(TaskExecutionEvent.Completed(executeResult.message))
                    } else {
                        lastHint = executeResult.message
                        stepCount++
                    }
                }
                
                is ModelResult.Error -> {
                    emit(TaskExecutionEvent.Error(result.error.message))
                    break
                }
            }
        }
        
        if (!isFinished) {
            emit(TaskExecutionEvent.MaxStepsReached)
        }
    }
    
    companion object {
        private const val MAX_STEPS = 100
    }
}

// domain/usecase/ExecuteActionUseCase.kt
package com.autoglm.domain.usecase

import com.autoglm.domain.model.AgentAction
import com.autoglm.domain.model.ActionResult
import com.autoglm.domain.repository.DeviceRepository
import com.autoglm.domain.repository.AppResolver
import javax.inject.Inject

class ExecuteActionUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val appResolver: AppResolver,
) {
    suspend fun execute(action: AgentAction, screenWidth: Int, screenHeight: Int): ActionResult {
        return when (action) {
            is AgentAction.Tap -> {
                val (absX, absY) = coordinateConverter.toAbsolute(
                    action.x, action.y, screenWidth, screenHeight
                )
                deviceRepository.tap(absX, absY)
            }
            
            is AgentAction.Swipe -> {
                val path = swipeGenerator.generatePath(...)
                deviceRepository.swipe(path.points, path.durationMs)
            }
            
            is AgentAction.Type -> {
                textInputManager.typeText(action.text)
            }
            
            is AgentAction.Launch -> {
                val packageName = appResolver.resolvePackageName(action.app)
                deviceRepository.launchApp(packageName)
            }
            
            // ... 其他动作
        }
    }
}
```

### 5.4 Data Layer - Repository 实现

```kotlin
// data/remote/impl/ModelClientImpl.kt
package com.autoglm.data.remote.impl

import com.autoglm.domain.model.*
import com.autoglm.domain.repository.ModelRepository
import com.autoglm.data.remote.api.ModelApi
import com.autoglm.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelClientImpl @Inject constructor(
    private val api: ModelApi,
    private val config: ModelConfig,
) : ModelRepository {
    
    private var currentEventSource: EventSource? = null
    
    override suspend fun request(request: ModelRequest): ModelResult = withContext(Dispatchers.IO) {
        try {
            val messages = request.messages.mapNotNull { msg ->
                when (msg) {
                    is ChatMessage.System -> MessageDto(msg.role, msg.content)
                    is ChatMessage.User -> MessageDto(msg.role, msg.content, request.screenshot)
                    is ChatMessage.Assistant -> MessageDto(msg.role, msg.content)
                }
            }
            
            api.chatCompletions(
                model = config.modelName,
                messages = messages,
                stream = true
            )
        } catch (e: Exception) {
            handleException(e)
        }
    }
    
    override suspend fun testConnection(config: ModelConfig): TestConnectionResult {
        return try {
            val testRequest = ChatCompletionRequest(
                model = config.modelName,
                messages = listOf(MessageDto("user", "Hi")),
                maxTokens = 10,
                temperature = 0f
            )
            
            val startTime = System.currentTimeMillis()
            val response = api.testConnection(testRequest)
            val latency = System.currentTimeMillis() - startTime
            
            TestConnectionResult.Success(latency)
        } catch (e: Exception) {
            TestConnectionResult.Error(e.message ?: "Connection failed")
        }
    }
    
    override fun cancelCurrentRequest() {
        currentEventSource?.cancel()
    }
}

// data/repository/TaskRepositoryImpl.kt
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val mapper: TaskMapper,
) : TaskRepository {
    
    private val _currentTask = MutableStateFlow<Task?>(null)
    
    override fun observeCurrentTask(): Flow<Task?> = _currentTask.asStateFlow()
    
    override suspend fun startTask(task: Task): Flow<TaskStep> = callbackFlow {
        _currentTask.value = task
        
        // 保存到数据库
        taskDao.insert(mapper.toEntity(task))
        
        // 监听任务步骤...
        awaitClose { }
    }
    
    override suspend fun saveTaskHistory(task: Task) {
        taskDao.insert(mapper.toEntity(task))
    }
}
```

### 5.5 UI Layer - ViewModels

```kotlin
// ui/home/HomeViewModel.kt
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val executeTaskUseCase: ExecuteTaskUseCase,
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()
    
    init {
        observeTaskState()
        observePermissions()
    }
    
    private fun observeTaskState() {
        viewModelScope.launch {
            taskRepository.observeCurrentTask().collect { task ->
                _uiState.update {
                    it.copy(
                        currentTask = task,
                        isTaskRunning = task?.status == TaskStatus.RUNNING,
                        canStartTask = calculateCanStartTask()
                    )
                }
            }
        }
    }
    
    fun onTaskInputChanged(text: String) {
        _uiState.update {
            it.copy(taskInput = text, canStartTask = text.isNotBlank())
        }
    }
    
    fun onStartTask() {
        val taskDescription = _uiState.value.taskInput
        if (!canStartTask(taskDescription)) return
        
        viewModelScope.launch {
            executeTaskUseCase.execute(taskDescription).collect { event ->
                when (event) {
                    is TaskExecutionEvent.Started -> {
                        _events.emit(HomeEvent.MinimizeApp)
                    }
                    is TaskExecutionEvent.ThinkingUpdated -> {
                        _uiState.update { it.copy(thinking = event.thinking) }
                    }
                    is TaskExecutionEvent.ActionExecuted -> {
                        _events.emit(HomeEvent.ShowToast(event.result.message))
                    }
                    is TaskExecutionEvent.Completed -> {
                        _events.emit(HomeEvent.ShowToast("任务完成: ${event.message}"))
                    }
                    is TaskExecutionEvent.Error -> {
                        _events.emit(HomeEvent.ShowToast("错误: ${event.message}"))
                    }
                }
            }
        }
    }
    
    private fun canStartTask(taskDescription: String): Boolean {
        val state = _uiState.value
        return state.permissionStates.shizuku &&
               state.permissionStates.overlay &&
               taskDescription.isNotBlank() &&
               !state.isTaskRunning
    }
}

data class HomeUiState(
    val taskInput: String = "",
    val currentTask: Task? = null,
    val isTaskRunning: Boolean = false,
    val canStartTask: Boolean = false,
    val thinking: String = "",
    val permissionStates: PermissionStates = PermissionStates(),
    val isLoading: Boolean = false,
)

sealed class HomeEvent {
    data object MinimizeApp : HomeEvent()
    data class ShowToast(val message: String) : HomeEvent()
    data object NavigateToSettings : HomeEvent()
}

data class PermissionStates(
    val shizuku: Boolean = false,
    val overlay: Boolean = false,
    val keyboard: Boolean = false,
)
```

### 5.6 UI Layer - Compose Screens

```kotlin
// ui/home/HomeScreen.kt
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 处理一次性事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.MinimizeApp -> {
                    // 最小化应用
                }
                is HomeEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is HomeEvent.NavigateToSettings -> {
                    onNavigateToSettings()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AutoGLM") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, "历史记录")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "设置")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 权限状态卡片
            PermissionStatusCard(
                shizukuConnected = uiState.permissionStates.shizuku,
                hasOverlayPermission = uiState.permissionStates.overlay,
                onRequestShizuku = { /* 请求 Shizuku 权限 */ },
                onRequestOverlay = { /* 请求悬浮窗权限 */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 任务输入卡片
            TaskInputCard(
                taskInput = uiState.taskInput,
                onTaskInputChanged = viewModel::onTaskInputChanged,
                onStartTask = viewModel::onStartTask,
                onPauseTask = viewModel::onPauseTask,
                onResumeTask = viewModel::onResumeTask,
                onCancelTask = viewModel::onCancelTask,
                isTaskRunning = uiState.isTaskRunning,
                canStartTask = uiState.canStartTask
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 思考过程卡片
            if (uiState.thinking.isNotBlank()) {
                ThinkingCard(thinking = uiState.thinking)
            }
            
            // 运行中的步骤
            if (uiState.isTaskRunning) {
                Spacer(modifier = Modifier.height(16.dp))
                RunningStepsCard(steps = uiState.currentTask?.steps ?: emptyList())
            }
        }
    }
}

// ui/components/TaskInputCard.kt
@Composable
fun TaskInputCard(
    taskInput: String,
    onTaskInputChanged: (String) -> Unit,
    onStartTask: () -> Unit,
    onPauseTask: () -> Unit,
    onResumeTask: () -> Unit,
    onCancelTask: () -> Unit,
    isTaskRunning: Boolean,
    canStartTask: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = taskInput,
                onValueChange = onTaskInputChanged,
                label = { Text("输入任务描述") },
                placeholder = { Text("例如：打开微信，给文件传输助手发送消息") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTaskRunning,
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isTaskRunning) {
                    Button(
                        onClick = onStartTask,
                        enabled = canStartTask,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("开始任务")
                    }
                } else {
                    Button(
                        onClick = onPauseTask,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Pause, null)
                        Text("暂停")
                    }
                    
                    Button(
                        onClick = onCancelTask,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, null)
                        Text("停止")
                    }
                }
            }
        }
    }
}
```

---

## 6. 依赖注入方案

### 6.1 Hilt 模块配置

```kotlin
// di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
    
    @Provides
    @Singleton
    fun provideCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

// di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideModelConfig(settingsRepository: SettingsRepository): Flow<ModelConfig> {
        return settingsRepository.observeModelConfig()
    }
    
    @Provides
    @Singleton
    fun provideModelApi(
        okHttpClient: OkHttpClient,
        modelConfig: ModelConfig
    ): ModelApi {
        return Retrofit.Builder()
            .baseUrl(modelConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ModelApi::class.java)
    }
}

// di/DeviceModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DeviceModule {
    
    @Provides
    @Singleton
    fun provideDeviceExecutor(shizukuService: ShizukuService): DeviceExecutor {
        return DeviceExecutor(shizukuService)
    }
    
    @Provides
    @Singleton
    fun provideHumanizedSwipeGenerator(): HumanizedSwipeGenerator {
        return HumanizedSwipeGenerator()
    }
}
```

---

## 7. 测试策略

### 7.1 单元测试 (Use Cases)

```kotlin
// test/domain/usecase/ExecuteTaskUseCaseTest.kt
@OptIn(ExperimentalCoroutinesApi::class)
class ExecuteTaskUseCaseTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val modelRepository: ModelRepository = mockk()
    private val taskRepository: TaskRepository = mockk()
    private val screenshotRepository: ScreenshotRepository = mockk()
    
    private lateinit var useCase: ExecuteTaskUseCase
    
    @Before
    fun setup() {
        useCase = ExecuteTaskUseCase(
            modelRepository,
            taskRepository,
            screenshotRepository
        )
    }
    
    @Test
    fun `execute task - success flow`() = runTest {
        // Given
        val mockScreenshot = "base64_image_data"
        coEvery { screenshotRepository.capture() } returns Screenshot(mockScreenshot, 1080, 1920)
        coEvery { modelRepository.request(any()) } returns ModelResult.Success(
            ModelResponse(
                thinking = "我需要打开微信应用",
                action = "do(launch, app=\"微信\")"
            )
        )
        
        // When
        val events = mutableListOf<TaskExecutionEvent>()
        useCase.execute("打开微信").collect { event ->
            events.add(event)
        }
        
        // Then
        assertTrue(events.any { it is TaskExecutionEvent.Started })
        assertTrue(events.any { it is TaskExecutionEvent.Completed })
    }
    
    @Test
    fun `execute task - model error should emit error event`() = runTest {
        // Given
        coEvery { screenshotRepository.capture() } returns Screenshot("data", 1080, 1920)
        coEvery { modelRepository.request(any()) } returns ModelResult.Error(
            NetworkError.ConnectionFailed("Network error")
        )
        
        // When
        val events = mutableListOf<TaskExecutionEvent>()
        useCase.execute("打开微信").collect { event ->
            events.add(event)
        }
        
        // Then
        assertTrue(events.any { it is TaskExecutionEvent.Error })
    }
}
```

### 7.2 ViewModel 测试

```kotlin
// test/ui/home/HomeViewModelTest.kt
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val executeTaskUseCase: ExecuteTaskUseCase = mockk()
    private val taskRepository: TaskRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    
    private lateinit var viewModel: HomeViewModel
    
    @Before
    fun setup() {
        viewModel = HomeViewModel(
            executeTaskUseCase,
            taskRepository,
            settingsRepository
        )
    }
    
    @Test
    fun `onTaskInputChanged updates state`() {
        viewModel.onTaskInputChanged("打开微信")
        
        val state = viewModel.uiState.value
        assertEquals("打开微信", state.taskInput)
        assertTrue(state.canStartTask)
    }
    
    @Test
    fun `onStartTask emits minimize event when permissions granted`() = runTest {
        // Given
        val taskDescription = "打开微信"
        coEvery { executeTaskUseCase.execute(any()) } returns flow {
            emit(TaskExecutionEvent.Started(taskDescription))
            emit(TaskExecutionEvent.Completed("任务完成"))
        }
        
        // When
        val events = mutableListOf<HomeEvent>()
        viewModel.events.collect { event ->
            events.add(event)
        }
        
        // Then
        assertTrue(events.any { it is HomeEvent.MinimizeApp })
    }
}
```

### 7.3 Compose UI 测试

```kotlin
// test/ui/home/HomeScreenTest.kt
@ComposeUiTest
class HomeScreenTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun `HomeScreen displays task input field`() {
        composeTestRule.setContent {
            HomeScreen(
                viewModel = hiltViewModel(),
                onNavigateToSettings = {},
                onNavigateToHistory = {}
            )
        }
        
        onNodeWithText("输入任务描述").assertIsDisplayed()
        onNodeWithText("开始任务").assertIsDisplayed()
    }
    
    @Test
    fun `start button is disabled when input is empty`() {
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockHomeViewModel(
                    taskInput = "",
                    canStartTask = false
                ),
                onNavigateToSettings = {},
                onNavigateToHistory = {}
            )
        }
        
        onNodeWithText("开始任务").assertIsNotEnabled()
    }
}
```

---

## 8. CI/CD 配置

```yaml
# .github/workflows/android-ci.yml
name: Android CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle
          
      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v3
        
      - name: Run ktlint
        run: ./gradlew ktlintCheck
        
      - name: Run Detekt
        run: ./gradlew detekt
        
      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest
        continue-on-error: false
        
      - name: Build Debug APK
        run: ./gradlew assembleDebug
        
      - name: Build Release APK
        run: ./gradlew assembleRelease
        if: github.event_name == 'push'
        
      - name: Upload APK
        uses: actions/upload-artifact@v4
        if: github.event_name == 'push'
        with:
          name: autoglm-release
          path: app/build/outputs/apk/release/*.apk
          
      - name: Upload Test Reports
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: app/build/reports/tests/testDebugUnitTest/
```

---

## 9. 完整代码示例

### 9.1 Application 类

```kotlin
// App.kt
@HiltAndroidApp
class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 Shizuku
        Shizuku.init(this)
    }
}
```

### 9.2 MainActivity

```kotlin
// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            AutoGLMTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onNavigateToSettings = {
                                navController.navigate(Screen.Settings.route)
                            },
                            onNavigateToHistory = {
                                navController.navigate(Screen.History.route)
                            }
                        )
                    }
                    
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.History.route) {
                        HistoryScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetail = { taskId ->
                                navController.navigate(Screen.HistoryDetail.createRoute(taskId))
                            }
                        )
                    }
                }
            }
        }
        
        // 处理权限请求
        handlePermissions()
    }
    
    private fun handlePermissions() {
        // Shizuku 权限处理
    }
}
```

---

## 10. 迁移检查清单

### Phase 1: 基础架构 ✅
- [ ] 创建 Compose 项目骨架
- [ ] 配置 Hilt 依赖注入
- [ ] 创建基础 theme 和组件库
- [ ] 实现 DataStore 偏好设置

### Phase 2: 核心业务
- [ ] 定义 Domain Models (Task, AgentAction, etc.)
- [ ] 定义 Repository Interfaces
- [ ] 实现 ModelClient 封装
- [ ] 实现 ExecuteTaskUseCase
- [ ] 实现 ExecuteActionUseCase

### Phase 3: UI 重构
- [ ] 实现 HomeScreen + HomeViewModel
- [ ] 实现 TaskScreen + TaskViewModel (含步骤瀑布流)
- [ ] 实现 SettingsScreen + SettingsViewModel
- [ ] 实现 HistoryScreen

### Phase 4: 服务与功能
- [ ] 迁移 ScreenshotService
- [ ] 实现 FloatingWindowService (Compose)
- [ ] 实现权限请求逻辑
- [ ] 添加多语言支持

### Phase 5: 测试与优化
- [ ] Use Case 单元测试 (>80% 覆盖率)
- [ ] ViewModel 测试
- [ ] Compose UI 测试
- [ ] 性能优化 (decompose, 优化重组)

---

## 附录 A: 关键类映射

| 原项目类 | 新项目类 | 变化 |
|----------|----------|------|
| `PhoneAgent` | `ExecuteTaskUseCase` | 单例 → UseCase，可测试 |
| `ModelClient` | `ModelRepository` | 具体类 → 接口 |
| `ActionHandler` | `ExecuteActionUseCase` | 提取业务逻辑 |
| `DeviceExecutor` | `DeviceRepository` | 强耦合 → 接口抽象 |
| `TaskExecutionManager` | `TaskStatus Flow` | 单例对象 → 多个 Flow |
| `MainViewModel` | `HomeViewModel` + `TaskViewModel` | 拆分 |
| `MainActivity` | `MainActivity` (Compose) | XML → Compose |
| `SettingActivity` | `SettingsScreen` | XML → Compose |

---

## 附录 B: API 兼容列表

| 模型 | Base URL | 模型名称 | 状态 |
|------|----------|----------|------|
| 智谱 BigModel | `https://open.bigmodel.cn/api/paas/v4` | `autoglm-phone` | ✅ |
| ModelScope | `https://api-inference.modelscope.cn/v1` | `ZhipuAI/AutoGLM-Phone-9B` | ✅ |
| OpenAI | `https://api.openai.com/v1` | `gpt-4o` | ✅ 需自定义 Prompt |
| Anthropic | `https://api.anthropic.com/v1` | `claude-3-opus` | ✅ 需自定义 Prompt |

---

## 附录 C: 资源链接

- [Jetpack Compose 文档](https://developer.android.com/compose)
- [Hilt 依赖注入](https://dagger.dev/hilt/)
- [Kotlin Coroutines Flow](https://kotlinlang.org/docs/flow.html)
- [Shizuku 文档](https://shizuku.rikka.app/)
- [Material Design 3](https://m3.material.io/)

---

> 📝 **注意**: 本文档是迁移指南的初稿。在实际迁移过程中，可能需要根据业务逻辑的复杂性进行适当调整。建议采用增量迁移的方式，每次只迁移一个模块，确保原有功能正常工作。