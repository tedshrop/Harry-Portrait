package com.example.fireseamlesslooper

import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Global, lightweight in-app logger that can publish logs to an on-screen debug panel
 * via a DebugLogListener registered by UI components (e.g., MainActivity).
 * Logs are emitted to Logcat as well for easy debugging outside the app UI.
 */
interface DebugLogListener {
    fun onNewLog(line: String)
}

object DebugPrinter {
    private val listeners = mutableSetOf<DebugLogListener>()
    private val mainHandler = Handler(Looper.getMainLooper())

    @Synchronized
    fun addListener(listener: DebugLogListener) {
        listeners.add(listener)
    }

    @Synchronized
    fun removeListener(listener: DebugLogListener) {
        listeners.remove(listener)
    }

    fun log(tag: String, message: String) {
        val line = "[$tag] $message"
        // Always log to Logcat
        Log.d(tag, message)
        // Publish to UI on the main thread
        mainHandler.post {
            synchronized(this) {
                for (l in listeners) {
                    l.onNewLog(line)
                }
            }
        }
    }
}
