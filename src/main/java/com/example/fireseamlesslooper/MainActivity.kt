package com.example.fireseamlesslooper

import android.os.Bundle
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.view.TextureView
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.os.PowerManager
import android.content.Context
import com.example.fireseamlesslooper.usb.UsbDirectWatcher
import com.example.fireseamlesslooper.usb.UsbFileRepository
import com.example.fireseamlesslooper.video.VideoPlaybackController
import com.example.fireseamlesslooper.DebugPrinter

class MainActivity : AppCompatActivity() {

    private val TAG = "USB_DIRECT"
    private var modeHandler: Handler? = null
    private var modeRunnable: Runnable? = null
    private var exitTapCount = 0
    private var lastTapTime = 0L

    // Core managers
    private val usbFileRepository = UsbFileRepository
    private lateinit var videoController: VideoPlaybackController
    private lateinit var usbWatcher: UsbDirectWatcher

    private lateinit var mediaSession: MediaSessionCompat
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        hideSystemUI()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        initMediaSession()

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
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "LooperMediaSession")

        val playbackState = PlaybackStateCompat.Builder()
            .setState(
                PlaybackStateCompat.STATE_PLAYING,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1.0f
            )
            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
            .build()

        mediaSession.setPlaybackState(playbackState)
        mediaSession.isActive = true

        // Acquire WakeLock to keep screen on during video playback
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "HarryPortrait::VideoPlaybackWakeLock"
        )
        wakeLock?.acquire(10*60*60*1000L) // 10 hours timeout
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
        try {
            mediaSession.release()
        } catch (e: Exception) {
            // ignore
        }
        // Release WakeLock
        try {
            if (wakeLock != null && wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // ignore
        }
        // Mode cycling disabled; nothing to stop
        try {
            modeHandler?.removeCallbacksAndMessages(null)
            modeHandler = null
            modeRunnable = null
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onPause() {
        super.onPause()
        // Only restart if we're actually being paused by user action, not destroyed
        // This prevents conflicts with the accessibility service
        if (!isFinishing) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isFinishing && !isDestroyed) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                }
            }, 1000) // Increased from 800ms to 1000ms
        }
    }

    override fun onUserInteraction() {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < 600) {  // 600ms double-tap window
            exitTapCount++
            if (exitTapCount >= 6) { // 6 rapid taps = exit
                finish()
                return
            }
        } else {
            exitTapCount = 1
        }
        lastTapTime = now
    }
}
