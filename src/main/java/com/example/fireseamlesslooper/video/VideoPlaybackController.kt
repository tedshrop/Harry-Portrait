package com.example.fireseamlesslooper.video

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.fireseamlesslooper.usb.UsbFileRepository
import java.io.File
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.video.VideoSize
import com.example.fireseamlesslooper.DebugPrinter
import android.view.TextureView

class VideoPlaybackController(
    private val application: Application,
    private val usbFileRepository: UsbFileRepository
) {

    private val TAG = "VIDEO_CONTROLLER"
    var videoRepository: VideoRepository? = null
    var exoPlayer: ExoPlayer? = null

    // TextureView and video sizing for correct rotation/scale
    private var textureViewRef: TextureView? = null
    private var lastVideoWidth: Int = 0
    private var lastVideoHeight: Int = 0
    private var lastRotationDegrees: Int = 0

    // Render mode used for quick testing:
    // 0 = center-crop (fill)
    // 1 = rotated-portrait-fullscreen (rotate 90 and scale so rotated width == view width)
    // 2 = fit (letterbox)
    private var renderMode: Int = 0

    private var isInitialized = false

    fun initializePlayer() {
        if (isInitialized) return

        exoPlayer = ExoPlayer.Builder(application).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_ENDED -> {
                            playNextVideo()
                        }
                        Player.STATE_READY -> {
                            // keep status silent
                        }
                        Player.STATE_BUFFERING -> {
                            // do not show buffering text
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    val errorMsg = "Video error: ${error.message}"
                    Log.e(TAG, errorMsg, error)

                    // Retry after 5 seconds
                    Handler(Looper.getMainLooper()).postDelayed({
                        playNextVideo()
                    }, 5000)
                }
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    try {
                        lastVideoWidth = videoSize.width
                        lastVideoHeight = videoSize.height
                        // rotationDegrees may not be available on older ExoPlayer versions; use reflection fallback
                        var rot = 0
                        try {
                            val f = videoSize.javaClass.getDeclaredField("rotationDegrees")
                            f.isAccessible = true
                            rot = f.getInt(videoSize)
                        } catch (ex: Exception) {
                            // field not present — keep rot = 0
                        }
                        lastRotationDegrees = rot
                        DebugPrinter.log(TAG, "onVideoSizeChanged: w=${lastVideoWidth} h=${lastVideoHeight} rot=${lastRotationDegrees}")
                        textureViewRef?.let { updateTextureViewTransform(it) }
                    } catch (e: Exception) {
                        DebugPrinter.log(TAG, "Error in onVideoSizeChanged: ${e.message}")
                    }
                }
            })
        }

        // Note: USB state is checked directly when needed, not observed continuously
        Log.d(TAG, "VideoPlaybackController initialized without continuous USB observation")

        isInitialized = true
        Log.d(TAG, "VideoPlaybackController initialized")
    }

    /**
     * Load videos for a new USB root
     */
    fun loadVideosForUsb(usbRoot: File) {
        Log.d(TAG, "Loading videos for USB: ${usbRoot.absolutePath}")
        videoRepository = VideoRepository(usbFileRepository, usbRoot)
        videoRepository?.loadVideosForRoot(usbRoot)

        val videoCount = videoRepository?.getTotalVideoCount() ?: 0
        Log.d(TAG, "USB available with $videoCount videos")
        playNextVideo()
    }

    /**
     * Clear videos when USB becomes unavailable
     */
    fun clearVideos() {
        Log.d(TAG, "Clearing videos - USB unavailable")
        exoPlayer?.pause()
        videoRepository?.clearVideos()
        videoRepository = null
    }

    private fun playNextVideo() {
        if (videoRepository?.hasVideos() != true) {
            Log.w(TAG, "No videos available for playback")
            return
        }

        val videoUri = videoRepository?.nextVideo()
        if (videoUri != null) {
            Log.d(TAG, "Playing video from SAF URI: $videoUri")
            val mediaItem = MediaItem.fromUri(videoUri)
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.play()
        } else {
            Log.w(TAG, "No video selected for playback")
        }
    }

    /**
     * Manually trigger video selection and playback
     */
    fun playNextVideoManually() {
        if (videoRepository?.hasVideos() == true) {
            playNextVideo()
        } else {
            Log.w(TAG, "Cannot play next video - no videos available")
        }
    }

    /**
     * Pause current playback
     */
    fun pausePlayback() {
        exoPlayer?.pause()
    }

    /**
     * Resume playback
     */
    fun resumePlayback() {
        if (videoRepository?.hasVideos() == true) {
            exoPlayer?.play()
        }
    }

    /**
     * Attach the player's video output to a TextureView so video is rendered on screen.
     * Call this after initializePlayer() and after the TextureView is available.
     */
    fun attachTextureView(textureView: TextureView) {
        try {
            exoPlayer?.setVideoTextureView(textureView)
            textureViewRef = textureView
            DebugPrinter.log(TAG, "Attached ExoPlayer to TextureView")
            // Apply any existing transform immediately
            updateTextureViewTransform(textureView)
        } catch (e: Exception) {
            DebugPrinter.log(TAG, "Failed to attach TextureView: ${e.message}")
            Log.e(TAG, "Failed to attach TextureView", e)
        }
    }

    /**
     * Set render mode (used for on-device cycling to test different fill/fit strategies).
     */
    fun setRenderMode(mode: Int) {
        renderMode = mode
        DebugPrinter.log(TAG, "Render mode set to $mode")
        // Re-apply transform with the new mode
        textureViewRef?.let { updateTextureViewTransform(it) }
    }

    /**
     * Update the TextureView transform to rotate and scale the video to fill the view
     * while preserving aspect ratio (center-crop). Handles rotation degrees reported by ExoPlayer.
     */
    private fun updateTextureViewTransform(tv: TextureView) {
        val vWidth = tv.width
        val vHeight = tv.height
        if (vWidth == 0 || vHeight == 0) return
        if (lastVideoWidth <= 0 || lastVideoHeight <= 0) return

        // Determine effective rotation:
        // Prefer ExoPlayer-provided rotation; otherwise infer from video dimensions (portrait -> rotate 90)
        var effectiveRotation = lastRotationDegrees
        if (effectiveRotation == 0) {
            effectiveRotation = if (lastVideoHeight > lastVideoWidth) 90 else 0
        }
        val rotated = (effectiveRotation == 90 || effectiveRotation == 270)

        // Video display size after rotation
        val videoDisplayWidth = if (rotated) lastVideoHeight else lastVideoWidth
        val videoDisplayHeight = if (rotated) lastVideoWidth else lastVideoHeight

        if (videoDisplayWidth == 0 || videoDisplayHeight == 0) return

        // Do not apply any TextureView transform. Let the system map the video surface to the view.
        try {
            DebugPrinter.log(TAG, "No TextureView transform applied. effectiveRotation=$effectiveRotation rotated=$rotated videoWxH=${videoDisplayWidth}x${videoDisplayHeight} viewWxH=${vWidth}x${vHeight} renderMode=$renderMode")
        } catch (e: Exception) {
            DebugPrinter.log(TAG, "Logging failed in updateTextureViewTransform: ${e.message}")
        }
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        Log.d(TAG, "Player released")
    }
}
