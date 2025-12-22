package com.example.mobile_final

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RunTrackerApp : Application() {

    companion object {
        const val TRACKING_NOTIFICATION_CHANNEL_ID = "tracking_channel"
        const val TRACKING_NOTIFICATION_CHANNEL_NAME = "Activity Tracking"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TRACKING_NOTIFICATION_CHANNEL_ID,
                TRACKING_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows notification while tracking your activity"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
