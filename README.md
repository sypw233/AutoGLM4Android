# AutoGLM For Android

[![License](https://img.shields.io/github/license/AnomalyCO/AutoGLM4Android)](LICENSE)
[![Android](https://img.shields.io/badge/Android-7.0%2B-brightgreen)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)]()

基于现代 Android 开发规范 (Jetpack Compose + MVVM + Clean Architecture) 的手机自动化助手应用。

## 项目介绍

AutoGLM For Android 是一个基于自然语言的手机自动化助手应用。用户通过输入任务描述，AI Agent 自动分析屏幕截图并执行相应操作（点击、滑动、输入等）。

### 功能特性

- 🤖 **AI 自动化** - 基于智谱 AutoGLM 模型，理解任务并自动执行
- 📱 **无障碍操作** - 通过 Shizuku 实现 Root 级别的设备控制
- 🎨 **现代 UI** - 使用 Jetpack Compose 构建的 Material Design 3 界面
- 🌐 **国际化** - 支持中文和英文
- 📝 **任务历史** - 使用 Room 数据库存储任务记录
- 🪟 **悬浮窗** - 实时显示任务执行状态
- 🎤 **语音输入** - 支持语音输入任务
- ⌨️ **自定义输入法** - AutoGLM 输入法用于文本输入

## 架构说明

本项目是从 [AutoGLM-For-Android](https://github.com/AnomalyCO/AutoGLM-For-Android) 重构迁移而来，采用现代 Android 开发规范：

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ HomeScreen  │  │SettingsScreen│  │  HistoryScreen     │ │
│  │ HomeViewModel│ │SettingsVM   │  │  HistoryViewModel  │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  UseCases   │  │ Repositories│  │   Domain Models    │ │
│  │ExecuteTask  │  │(Interfaces) │  │Task,AgentAction   │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                       Data Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ Repository  │  │   Network   │  │     DataStore      │ │
│  │  Impl       │  │(Retrofit)   │  │     Preferences    │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

| 分类 | 技术 |
|------|------|
| **UI** | Jetpack Compose, Material Design 3 |
| **架构** | MVVM, Clean Architecture, Unidirectional Data Flow |
| **DI** | Hilt |
| **异步** | Kotlin Coroutines, Flow |
| **网络** | Retrofit, OkHttp, Kotlin Serialization |
| **本地存储** | Room, DataStore Preferences |
| **设备控制** | Shizuku |

## 项目结构

```
app/src/main/java/ovo/sypw/autoglm4android/
├── App.kt                    # Hilt Application
├── MainActivity.kt           # 主界面 (Compose Navigation)
│
├── config/                   # 配置
│   └── I18n.kt              # 国际化支持
│
├── app/                      # 应用工具
│   └── AppInfo.kt           # 应用信息
│
├── di/                       # 依赖注入
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   ├── DeviceModule.kt
│   └── RepositoryModule.kt
│
├── domain/                   # 领域层
│   ├── model/               # 领域模型
│   │   ├── Task.kt
│   │   ├── TaskStep.kt
│   │   ├── AgentAction.kt
│   │   ├── ModelConfig.kt
│   │   └── Screenshot.kt
│   │
│   ├── repository/          # 仓库接口
│   │   ├── ModelRepository.kt
│   │   ├── TaskRepository.kt
│   │   ├── ScreenshotRepository.kt
│   │   ├── SettingsRepository.kt
│   │   └── DeviceRepository.kt
│   │
│   └── usecase/             # 用例
│       ├── ExecuteTaskUseCase.kt
│       ├── ExecuteActionUseCase.kt
│       └── TaskExecutionEvent.kt
│
├── data/                     # 数据层
│   ├── remote/
│   │   ├── api/ModelApi.kt
│   │   ├── dto/ChatCompletionDto.kt
│   │   └── impl/ModelClientImpl.kt
│   │
│   ├── local/
│   │   ├── room/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── TaskHistoryDao.kt
│   │   │   └── entity/TaskHistoryEntity.kt
│   │   └── preferences/SettingsPreferences.kt
│   │
│   └── repository/          # 仓库实现
│       ├── TaskRepositoryImpl.kt
│       ├── SettingsRepositoryImpl.kt
│       └── DeviceRepositoryImpl.kt
│
├── service/                  # Android 服务
│   ├── ShizukuService.kt
│   ├── UserService.kt
│   ├── DeviceExecutor.kt
│   ├── ScreenshotServiceImpl.kt
│   ├── AppResolverImpl.kt
│   ├── FloatingWindowService.kt
│   ├── FloatingWindowTileService.kt
│   └── FloatingWindowToggleActivity.kt
│
├── input/                    # 输入模块
│   ├── AutoGLMKeyboardService.kt
│   ├── TextInputManager.kt
│   └── VoiceInputService.kt
│
├── ui/                       # UI 层
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   │
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── HomeUiState.kt
│   │
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   ├── SettingsViewModel.kt
│   │   └── SettingsUiState.kt
│   │
│   └── history/
│       ├── HistoryScreen.kt
│       ├── HistoryViewModel.kt
│       └── HistoryUiState.kt
│
└── util/                     # 工具类
    ├── Logger.kt
    ├── CoordinateConverter.kt
    ├── HumanizedSwipeGenerator.kt
    ├── ErrorHandler.kt
    ├── KeepAliveManager.kt
    ├── LogFileManager.kt
    └── ServiceStateManager.kt
```

## 迁移对照表

| 原项目 (AutoGLM-For-Android) | 新项目 (AutoGLM4Android) |
|------------------------------|--------------------------|
| `PhoneAgent.kt` | `ExecuteTaskUseCase.kt` |
| `ModelClient.kt` | `ModelRepository.kt` + `ModelClientImpl.kt` |
| `ActionHandler.kt` | `ExecuteActionUseCase.kt` |
| `DeviceExecutor.kt` | `DeviceRepository.kt` + `DeviceExecutor.kt` |
| `TaskExecutionManager.kt` | `TaskRepository.kt` + `Flow` |
| `MainViewModel.kt` | `HomeViewModel.kt` + `TaskViewModel.kt` |
| `MainActivity` (XML) | `MainActivity.kt` (Compose) |
| `SettingsActivity` | `SettingsScreen.kt` (Compose) |
| `HistoryActivity` | `HistoryScreen.kt` (Compose) |
| `FloatingWindowService.kt` | `FloatingWindowService.kt` |
| `HistoryManager.kt` | `Room Database` |
| 手动单例 | **Hilt** 依赖注入 |
| LiveData | **Kotlin Flow** |
| View + XML | **Jetpack Compose** |

## 构建要求

- JDK 17+
- Android SDK 34
- Gradle 8.x

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/AnomalyCO/AutoGLM4Android.git
cd AutoGLM4Android
```

### 2. 配置 Shizuku

本应用需要 Shizuku 才能实现自动化控制功能。请先安装并配置 Shizuku：

1. 从 GitHub 或酷安下载 [Shizuku](https://github.com/RikkaApps/Shizuku)
2. 按照指引启动 Shizuku 并授予必要权限

### 3. 配置 API

在设置页面配置 AI 模型 API：

- **智谱 BigModel**: `https://open.bigmodel.cn/api/paas/v4`
- **模型名称**: `autoglm-phone`
- **API Key**: 智谱开放平台申请

### 4. 权限说明

| 权限 | 用途 |
|------|------|
| Shizuku | 执行 shell 命令控制设备 |
| 悬浮窗 | 显示任务执行状态 |
| 录音 | 语音输入功能 |
| 无障碍 | 可选，用于高级自动化 |

## 贡献指南

欢迎提交 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/xxx`)
3. 提交更改 (`git commit -m 'Add xxx'`)
4. 推送分支 (`git push origin feature/xxx`)
5. 创建 Pull Request

## 参考项目

- [AutoGLM-For-Android](https://github.com/AnomalyCO/AutoGLM-For-Android) - 原始项目
- [Shizuku](https://github.com/RikkaApps/Shizuku) - Root 权限管理
- [智谱 AI](https://open.bigmodel.cn/) - 大语言模型

## 许可证

本项目仅供学习交流使用，使用请遵守相关法律法规。

```
Copyright (c) 2024 AutoGLM Contributors
```

##  Star 历史

[![Star History Chart](https://api.star-history.com/svg?repos=AnomalyCO/AutoGLM4Android)]()
