package com.example.fireseamlesslooper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class BootLaunchService : Service() {

    private val TAG = "BootLaunchService"
    private val CHANNEL_ID = "boot_launch_channel"
    private val NOTIFICATION_ID = 1001

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BootLaunchService created - starting foreground")

        // Create notification channel (required for Android 8.0+)
        createNotificationChannel()

        // Start as foreground service with notification
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Launch app after 3-second delay to ensure Fire TV is ready
        Handler(Looper.getMainLooper()).postDelayed({
            launchMainActivity()
        }, 3000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Boot Launch",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Launches Harry Portrait on boot"
                setShowBadge(false)
                setSound(null, null)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Starting Harry Portrait")
            .setContentText("Loading video player...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(false)
            .build()
    }

    private fun launchMainActivity() {
        Log.d(TAG, "Launching MainActivity")

        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            Log.d(TAG, "MainActivity launched successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch MainActivity: ${e.message}", e)
        }

        // Stop foreground service and remove notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()

        Log.d(TAG, "Service stopped and notification removed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "BootLaunchService destroyed")
    }
}
