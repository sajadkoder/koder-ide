package com.koder.ide

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KoderApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_TERMINAL,
                    "Terminal",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Terminal session notifications"
                },
                NotificationChannel(
                    CHANNEL_BUILD,
                    "Build",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Build process notifications"
                },
                NotificationChannel(
                    CHANNEL_GIT,
                    "Git",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Git operation notifications"
                }
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    companion object {
        const val CHANNEL_TERMINAL = "terminal_channel"
        const val CHANNEL_BUILD = "build_channel"
        const val CHANNEL_GIT = "git_channel"
    }
}
