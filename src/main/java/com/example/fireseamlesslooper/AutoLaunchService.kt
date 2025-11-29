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

            // Only trigger on specific Fire TV launcher packages
            // Do NOT use .contains("android") as it triggers on ALL system events
            if (packageName == "com.amazon.tv.launcher" ||
                packageName == "com.amazon.tv.home" ||
                packageName == "com.amazon.tv.settings") {

                LogOverlay.log("Fire TV launcher detected - launching Harry Portrait app")

                handler.postDelayed({
                    val intent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)
                }, 1500)
            }
        }
    }

    override fun onInterrupt() {
        LogOverlay.log("AutoLaunchService interrupted")
    }
}
