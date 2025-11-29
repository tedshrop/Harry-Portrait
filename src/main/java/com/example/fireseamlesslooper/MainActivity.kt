package com.example.fireseamlesslooper

import android.os.Bundle
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.example.fireseamlesslooper.usb.UsbDirectWatcher
import com.example.fireseamlesslooper.usb.UsbFileRepository
import com.example.fireseamlesslooper.video.VideoPlaybackController
import com.example.fireseamlesslooper.DebugPrinter

class MainActivity : AppCompatActivity() {

    private val TAG = "USB_DIRECT"
    private var modeHandler: Handler? = null
    private var modeRunnable: Runnable? = null

    // Core managers
    private val usbFileRepository = UsbFileRepository
    private lateinit var videoController: VideoPlaybackController
    private lateinit var usbWatcher: UsbDirectWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        hideSystemUI()
        setContentView(R.layout.activity_main)

        // Initialize managers
        videoController = VideoPlaybackController(application, usbFileRepository)

        // Initialize players and managers
        videoController.initializePlayer()

        // Initialize USB watcher
        usbWatcher = UsbDirectWatcher(
            onAvailable = { usbRoot ->
                Log.d(TAG, "USB available: ${usbRoot.path.absolutePath}")
                videoController.loadVideosForUsb(usbRoot.path)
            },
            onUnavailable = {
                Log.d(TAG, "USB unavailable")
                videoController.clearVideos()
            }
        )
        usbWatcher.start()

        // Fallback: if no USB detected within a short delay, attempt to load videos from internal storage
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val internal = java.io.File("/sdcard/Download/Output")
                DebugPrinter.log(TAG, "Checking internal path for videos: ${internal.absolutePath}")
                val folders = listOf("Common", "Uncommon", "Rare", "Legendary")
                var total = 0
                if (internal.exists() && internal.isDirectory && internal.canRead()) {
                    for (f in folders) {
                        val dir = java.io.File(internal, f)
                        if (dir.exists() && dir.isDirectory) {
                            val videos = dir.listFiles { file -> file.isFile && file.name.lowercase()
                                .matches(Regex(".*\\.(mp4|mov|mkv|avi)")) } ?: emptyArray()
                            total += videos.size
                        }
                    }
                }
                if (total > 0) {
                    DebugPrinter.log(TAG, "Internal videos found: $total. Loading from ${internal.absolutePath}")
                    videoController.loadVideosForUsb(internal)
                } else {
                    DebugPrinter.log(TAG, "No internal videos at ${internal.absolutePath}")
                }
            } catch (e: Exception) {
                DebugPrinter.log(TAG, "Error checking internal path: ${e.message}")
            }
        }, 2000)

        // Attach player to texture view
        val textureView = findViewById<TextureView>(R.id.textureView)
        try {
            videoController.attachTextureView(textureView)
            DebugPrinter.log(TAG, "Attached TextureView to video controller")
        } catch (e: Exception) {
            DebugPrinter.log(TAG, "Failed to attach TextureView: ${e.message}")
        }


        textureView.post {

            // Apply center-rotate + scale using PortraitVideoTransform2
            try {
                com.example.fireseamlesslooper.PortraitVideoTransform2.applyPortraitRotateAndScale(textureView, 1.778f)
                DebugPrinter.log(TAG, "Requested PortraitVideoTransform2.applyPortraitRotateAndScale")
            } catch (e: Exception) {
                DebugPrinter.log(TAG, "Failed to request portrait rotate/scale: ${e.message}")
            }
        }

        // Mode cycling disabled — we keep a static rotated view for now (no on-screen debug)

        Log.d(TAG, "MainActivity initialization complete")
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            (android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        actionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        usbWatcher.stop()
        videoController.releasePlayer()
        // Mode cycling disabled; nothing to stop
        try {
            modeHandler?.removeCallbacksAndMessages(null)
            modeHandler = null
            modeRunnable = null
        } catch (e: Exception) {
            // ignore
        }
        Log.d(TAG, "MainActivity destroyed")
    }
}
