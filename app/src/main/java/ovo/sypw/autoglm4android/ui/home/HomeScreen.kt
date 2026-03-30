package ovo.sypw.autoglm4android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/**
 * 首页屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理一次性事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is HomeEvent.NavigateToSettings -> {
                    onNavigateToSettings()
                }
                else -> {}
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
                .verticalScroll(rememberScrollState())
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
        }
    }
}

/**
 * 权限状态卡片
 */
@Composable
fun PermissionStatusCard(
    shizukuConnected: Boolean,
    hasOverlayPermission: Boolean,
    onRequestShizuku: () -> Unit,
    onRequestOverlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "权限状态",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shizuku 连接",
                    color = if (shizukuConnected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                if (!shizukuConnected) {
                    Button(onClick = onRequestShizuku) {
                        Text("连接")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "悬浮窗权限",
                    color = if (hasOverlayPermission) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                if (!hasOverlayPermission) {
                    Button(onClick = onRequestOverlay) {
                        Text("授权")
                    }
                }
            }
        }
    }
}

/**
 * 任务输入卡片
 */
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

/**
 * 思考过程卡片
 */
@Composable
fun ThinkingCard(thinking: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "思考过程",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = thinking,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
