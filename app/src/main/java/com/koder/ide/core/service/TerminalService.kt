package com.koder.ide.core.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.koder.ide.KoderApp
import com.koder.ide.R
import com.koder.ide.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TerminalService : Service() {

    private val binder = TerminalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    private val _output = MutableStateFlow<String>("")
    val output: StateFlow<String> = _output.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var process: Process? = null

    inner class TerminalBinder : Binder() {
        fun getService(): TerminalService = this@TerminalService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        process?.destroy()
    }

    fun executeCommand(command: String, workingDirectory: String? = null) {
        serviceScope.launch {
            _isRunning.value = true
            try {
                val processBuilder = ProcessBuilder("sh", "-c", command)
                workingDirectory?.let { processBuilder.directory(java.io.File(it)) }
                
                process = processBuilder.start()
                
                val reader = process!!.inputStream.bufferedReader()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    _output.value += "$line\n"
                }
                
                process!!.waitFor()
            } catch (e: Exception) {
                _output.value += "Error: ${e.message}\n"
            } finally {
                _isRunning.value = false
            }
        }
    }

    fun stopExecution() {
        process?.destroy()
        _isRunning.value = false
    }

    fun clearOutput() {
        _output.value = ""
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, KoderApp.CHANNEL_TERMINAL)
            .setContentTitle("Terminal Running")
            .setContentText("Terminal session is active")
            .setSmallIcon(R.drawable.ic_terminal)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
