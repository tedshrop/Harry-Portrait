package com.example.fireseamlesslooper

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.os.Handler
import android.os.Looper
import com.example.fireseamlesslooper.ui.LogOverlay

class AutoLaunchService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        LogOverlay.log("AutoLaunchService connected and monitoring for launcher events")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: ""
            val className = event.className?.toString() ?: ""

            // Check if we're on the launcher/home screen
            if (packageName.contains("launcher") || className.contains("launcher") ||
                className.contains("Launcher") || packageName.contains("android")) {
                LogOverlay.log("Launcher detected - launching Harry Portrait app")

                handler.postDelayed({
                    val intent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                }, 1000) // Shorter delay for responsive auto-launch
            }
        }
    }

    override fun onInterrupt() {
        LogOverlay.log("AutoLaunchService interrupted")
    }
}
