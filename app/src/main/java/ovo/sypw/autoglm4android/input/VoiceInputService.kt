package ovo.sypw.autoglm4android.input

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ovo.sypw.autoglm4android.MainActivity

/**
 * 语音输入服务
 * 使用 Android 内置语音识别
 */
class VoiceInputService : Service() {

    private val binder = LocalBinder()
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _voiceLevel = MutableStateFlow(0f)
    val voiceLevel: StateFlow<Float> = _voiceLevel.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private var listener: VoiceRecognitionListener? = null

    companion object {
        const val CHANNEL_ID = "voice_input_channel"
        const val NOTIFICATION_ID = 1002

        interface VoiceRecognitionListener {
            fun onResult(text: String)
            fun onError(error: String)
            fun onLevelChange(level: Float)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): VoiceInputService = this@VoiceInputService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "start_recording" -> startRecording()
            "stop_recording" -> stopRecording()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    fun setListener(listener: VoiceRecognitionListener?) {
        this.listener = listener
    }

    fun startRecording() {
        if (_isRecording.value) return

        try {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
            }

            audioRecord?.startRecording()
            _isRecording.value = true

            startForeground(NOTIFICATION_ID, createNotification())

            recordingJob = serviceScope.launch {
                val buffer = ByteArray(bufferSize)

                while (isActive && _isRecording.value) {
                    val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (read > 0) {
                        // 计算音量级别
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i].toDouble() * buffer[i].toDouble()
                        }
                        val rms = Math.sqrt(sum / read)
                        val level = (rms / 32768.0 * 100).toFloat().coerceIn(0f, 100f)
                        _voiceLevel.value = level
                        listener?.onLevelChange(level)
                    }
                }
            }
        } catch (e: SecurityException) {
            listener?.onError("麦克风权限被拒绝")
            stopRecording()
        } catch (e: Exception) {
            listener?.onError("录音失败: ${e.message}")
            stopRecording()
        }
    }

    fun stopRecording() {
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // ignore
        }
        audioRecord = null

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun setRecognizedText(text: String) {
        _recognizedText.value = text
        listener?.onResult(text)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "语音输入",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "语音输入服务通知"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, VoiceInputService::class.java).apply {
                action = "stop_recording"
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoGLM")
            .setContentText("正在录音...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "停止", stopIntent)
            .build()
    }
}
