package com.example.fireseamlesslooper.usb

import android.content.Context
import android.util.Log

/**
 * Minimal stub implementation to satisfy compile-time references.
 * This is intentionally lightweight — it only provides lifecycle methods
 * and a simple UsbState sealed class so the SAF service can compile and run.
 *
 * You can extend this with real SAF permission flows later.
 */

sealed class UsbState {
    object NoAccess : UsbState()
    object PermissionRequested : UsbState()
    object AccessGranted : UsbState()
    object AccessDenied : UsbState()
}

class UsbAccessManager(private val context: Context) {

    private val TAG = "USB_ACCESS"

    fun initialize() {
        Log.d(TAG, "UsbAccessManager.initialize() called")
        // Real initialization (SAF flows) should be implemented here.
    }

    fun dispose() {
        Log.d(TAG, "UsbAccessManager.dispose() called")
        // Cleanup resources if required.
    }

    // Placeholder: notify or query current state if needed by other code.
    fun getState(): UsbState = UsbState.NoAccess
}
