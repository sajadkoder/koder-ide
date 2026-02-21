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
import java.io.File

@AndroidEntryPoint
class BuildService : Service() {

    private val binder = BuildBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    private val _buildOutput = MutableStateFlow<String>("")
    val buildOutput: StateFlow<String> = _buildOutput.asStateFlow()

    private val _buildProgress = MutableStateFlow(0)
    val buildProgress: StateFlow<Int> = _buildProgress.asStateFlow()

    private val _isBuilding = MutableStateFlow(false)
    val isBuilding: StateFlow<Boolean> = _isBuilding.asStateFlow()

    private var buildProcess: Process? = null

    inner class BuildBinder : Binder() {
        fun getService(): BuildService = this@BuildService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        buildProcess?.destroy()
    }

    fun buildProject(projectPath: String, buildType: BuildType = BuildType.GRADLE) {
        serviceScope.launch {
            _isBuilding.value = true
            _buildOutput.value = "Starting build...\n"
            _buildProgress.value = 0

            try {
                when (buildType) {
                    BuildType.GRADLE -> buildGradle(projectPath)
                    BuildType.CMAKE -> buildCMake(projectPath)
                    BuildType.MAKE -> buildMake(projectPath)
                }
            } catch (e: Exception) {
                _buildOutput.value += "Build failed: ${e.message}\n"
            } finally {
                _isBuilding.value = false
            }
        }
    }

    private suspend fun buildGradle(projectPath: String) {
        _buildOutput.value += "Running Gradle build...\n"
        _buildProgress.value = 10

        val processBuilder = ProcessBuilder("./gradlew", "assembleDebug")
        processBuilder.directory(File(projectPath))
        
        buildProcess = processBuilder.start()
        
        val reader = buildProcess!!.inputStream.bufferedReader()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            _buildOutput.value += "$line\n"
            
            when {
                line?.contains("BUILD SUCCESSFUL") == true -> _buildProgress.value = 100
                line?.contains("Task ") == true -> _buildProgress.value = (_buildProgress.value + 5).coerceAtMost(95)
            }
        }
        
        buildProcess!!.waitFor()
    }

    private suspend fun buildCMake(projectPath: String) {
        _buildOutput.value += "Running CMake build...\n"
        _buildProgress.value = 10

        val buildDir = File(projectPath, "build")
        buildDir.mkdirs()

        val processBuilder = ProcessBuilder("cmake", "..")
        processBuilder.directory(buildDir)
        
        buildProcess = processBuilder.start()
        buildProcess!!.waitFor()
        _buildProgress.value = 100
    }

    private suspend fun buildMake(projectPath: String) {
        _buildOutput.value += "Running Make build...\n"
        _buildProgress.value = 10

        val processBuilder = ProcessBuilder("make")
        processBuilder.directory(File(projectPath))
        
        buildProcess = processBuilder.start()
        buildProcess!!.waitFor()
        _buildProgress.value = 100
    }

    fun stopBuild() {
        buildProcess?.destroy()
        _isBuilding.value = false
        _buildOutput.value += "Build stopped\n"
    }

    fun clearOutput() {
        _buildOutput.value = ""
        _buildProgress.value = 0
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, KoderApp.CHANNEL_BUILD)
            .setContentTitle("Building Project")
            .setContentText("Build in progress...")
            .setSmallIcon(R.drawable.ic_terminal)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setProgress(100, 0, true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1002
    }
}

enum class BuildType {
    GRADLE,
    CMAKE,
    MAKE
}
