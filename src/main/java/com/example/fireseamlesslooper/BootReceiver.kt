package com.example.fireseamlesslooper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called - action: ${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "BOOT_COMPLETED detected - starting BootLaunchService")

            val serviceIntent = Intent(context, BootLaunchService::class.java)

            try {
                // Fire OS requires startForegroundService for API 26+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Log.d(TAG, "BootLaunchService started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start BootLaunchService: ${e.message}")
            }
        }
    }
}
