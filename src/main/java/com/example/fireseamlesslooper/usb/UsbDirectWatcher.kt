package com.example.fireseamlesslooper.usb

import android.util.Log
import com.example.fireseamlesslooper.DebugPrinter

class UsbDirectWatcher(
    private val onAvailable: (UsbRoot) -> Unit,
    private val onUnavailable: () -> Unit
) {

    private val TAG = "USB_DIRECT"
    private var running = true

    fun start() {
Log.d(TAG, "Starting USB watcher")
DebugPrinter.log(TAG, "Starting USB watcher")
        Thread {
            var lastState: UsbRoot? = null

            while (running) {
                val root = UsbDirectScanner.detectUsbRoot()

if (root != null && lastState == null) {
    Log.d(TAG, "USB became available: ${root.path.absolutePath}")
    DebugPrinter.log(TAG, "USB became available: ${root.path.absolutePath}")
    onAvailable(root)
}

if (root == null && lastState != null) {
    Log.d(TAG, "USB became unavailable")
    DebugPrinter.log(TAG, "USB became unavailable")
    onUnavailable()
}

                lastState = root
                Thread.sleep(2000)
            }
        }.start()
    }

    fun stop() {
Log.d(TAG, "Stopping USB watcher")
DebugPrinter.log(TAG, "Stopping USB watcher")
running = false
    }
}
