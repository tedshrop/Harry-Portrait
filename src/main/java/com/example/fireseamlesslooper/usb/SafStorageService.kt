package com.example.fireseamlesslooper.usb

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class SafStorageService : Service() {

    private val TAG = "SAF_STORAGE_SERVICE"

    private lateinit var usbAccessManager: UsbAccessManager
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SafStorageService created")

        usbAccessManager = UsbAccessManager(this)
        usbAccessManager.initialize()

        // Note: For now, we don't observe state changes in the service
        // State observation is handled in the Activity
        Log.d(TAG, "Service initialized - state observation handled by Activity")

        Log.d(TAG, "SafStorageService initialized with USB access manager")
    }

    private fun broadcastSafState(state: UsbState) {
        val intent = Intent(ACTION_SAF_STATE_CHANGED).apply {
            putExtra(EXTRA_SAF_STATE, state.javaClass.simpleName)
            // For SAF, we don't use paths - URI access is handled differently
        }
        sendBroadcast(intent)
        Log.d(TAG, "Broadcasted SAF state: $state")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "SafStorageService started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "SafStorageService destroyed")
        usbAccessManager.dispose()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    fun getUsbAccessManager(): UsbAccessManager = usbAccessManager

    companion object {
        const val ACTION_SAF_STATE_CHANGED = "com.example.fireseamlesslooper.SAF_STATE_CHANGED"
        const val EXTRA_SAF_STATE = "saf_state"
        const val EXTRA_SAF_DOCUMENT_URI = "saf_document_uri"
    }
}
