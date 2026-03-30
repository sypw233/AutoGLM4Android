package ovo.sypw.autoglm4android.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/**
 * 设置屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveSettings()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 模型配置卡片
            ModelConfigCard(
                baseUrl = uiState.modelConfig.baseUrl,
                apiKey = uiState.modelConfig.apiKey,
                modelName = uiState.modelConfig.modelName,
                temperature = uiState.modelConfig.temperature,
                maxTokens = uiState.modelConfig.maxTokens,
                onBaseUrlChanged = viewModel::onBaseUrlChanged,
                onApiKeyChanged = viewModel::onApiKeyChanged,
                onModelNameChanged = viewModel::onModelNameChanged,
                onTemperatureChanged = viewModel::onTemperatureChanged,
                onMaxTokensChanged = viewModel::onMaxTokensChanged,
                testResult = uiState.testResult,
                isTestingConnection = uiState.isTestingConnection,
                onTestConnection = viewModel::testConnection
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Agent 配置卡片
            AgentConfigCard(
                maxSteps = uiState.agentConfig.maxSteps,
                enableVoice = uiState.agentConfig.enableVoice,
                enableFloatingWindow = uiState.agentConfig.enableFloatingWindow,
                onMaxStepsChanged = viewModel::onMaxStepsChanged,
                onEnableVoiceChanged = viewModel::onEnableVoiceChanged,
                onEnableFloatingWindowChanged = viewModel::onEnableFloatingWindowChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 保存按钮
            Button(
                onClick = viewModel::saveSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存设置")
            }
        }
    }
}

/**
 * 模型配置卡片
 */
@SuppressLint("DefaultLocale")
@Composable
fun ModelConfigCard(
    baseUrl: String,
    apiKey: String,
    modelName: String,
    temperature: Float,
    maxTokens: Int,
    onBaseUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onModelNameChanged: (String) -> Unit,
    onTemperatureChanged: (Float) -> Unit,
    onMaxTokensChanged: (Int) -> Unit,
    testResult: String?,
    isTestingConnection: Boolean,
    onTestConnection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "模型配置",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = baseUrl,
                onValueChange = onBaseUrlChanged,
                label = { Text("API Base URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChanged,
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = modelName,
                onValueChange = onModelNameChanged,
                label = { Text("模型名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Temperature: ${String.format("%.1f", temperature)}")
            Slider(
                value = temperature,
                onValueChange = onTemperatureChanged,
                valueRange = 0f..2f,
                steps = 19
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = maxTokens.toString(),
                onValueChange = { onMaxTokensChanged(it.toIntOrNull() ?: 4096) },
                label = { Text("Max Tokens") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTestConnection,
                    enabled = !isTestingConnection
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("测试连接")
                }

                testResult?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(start = 16.dp),
                        color = if (it.contains("成功")) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}

/**
 * Agent 配置卡片
 */
@Composable
fun AgentConfigCard(
    maxSteps: Int,
    enableVoice: Boolean,
    enableFloatingWindow: Boolean,
    onMaxStepsChanged: (Int) -> Unit,
    onEnableVoiceChanged: (Boolean) -> Unit,
    onEnableFloatingWindowChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Agent 配置",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = maxSteps.toString(),
                onValueChange = { onMaxStepsChanged(it.toIntOrNull() ?: 100) },
                label = { Text("最大步骤数") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "语音输入",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = enableVoice,
                    onCheckedChange = onEnableVoiceChanged
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "悬浮窗",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = enableFloatingWindow,
                    onCheckedChange = onEnableFloatingWindowChanged
                )
            }
        }
    }
}
