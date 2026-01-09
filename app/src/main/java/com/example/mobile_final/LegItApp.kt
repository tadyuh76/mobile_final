package com.example.mobile_final

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LegItApp : Application() {

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
                NotificationManager.IMPORTANCE_DEFAULT  // Changed from LOW to ensure visibility
            ).apply {
                description = "Shows notification while tracking your activity"
                setShowBadge(false)  // Don't show badge since this is an ongoing service
                enableLights(false)  // No notification light
                enableVibration(false)  // No vibration on updates
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
