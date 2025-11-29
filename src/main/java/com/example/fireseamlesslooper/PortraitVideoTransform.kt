package com.example.fireseamlesslooper

import android.graphics.Matrix
import android.util.Log
import android.view.TextureView

/**
 * PortraitVideoTransform.kt
 *
 * Utility to apply portrait transform to a TextureView so the video content can be rotated
 * and scaled entirely in the view/compositor layer (rotation only to start; scaling can be added later).
 *
 * Public API:
 *   fun applyPortraitFillTransform(textureView: TextureView, screenCenterX: Float, screenCenterY: Float)
 *
 * For this iteration we implement a center-rotation-only path, with a separate rotate-only path.
 */
object PortraitVideoTransform {
    private const val TAG = "PortraitVideoTransform"

    // Existing fill transform (kept for compatibility; can be used if needed later)
    fun applyPortraitFillTransform(textureView: TextureView, screenCenterX: Float, screenCenterY: Float) {
        textureView.post {
            try {
                val viewW = textureView.width.toFloat()
                val viewH = textureView.height.toFloat()

                if (viewW <= 0f || viewH <= 0f) {
                    Log.w(TAG, "TextureView not laid out yet (size 0). Skipping transform.")
                    return@post
                }

                val matrix = Matrix()

                // 1. Rotate 90 degrees around the provided pivot
                matrix.postRotate(90f, screenCenterX, screenCenterY)

                // 2. Optional: keep this as a placeholder for future scaling if needed
                // Currently we are not scaling in this path to avoid aggressive cropping.

                // 3. Apply transform
                textureView.setTransform(matrix)

                Log.d(TAG, "applyPortraitFillTransform: view=${viewW}x${viewH} centerPivot=($screenCenterX,$screenCenterY)")
            } catch (e: Exception) {
                Log.e(TAG, "Error in applyPortraitFillTransform: ${e.message}", e)
            }
        }
    }

    // New: rotation-only around the TextureView's own center
    fun applyPortraitRotateOnly(textureView: TextureView) {
        textureView.post {
            try {
                val w = textureView.width.toFloat()
                val h = textureView.height.toFloat()
                if (w <= 0f || h <= 0f) {
                    Log.w(TAG, "TextureView not laid out yet (size 0). Skipping rotate.")
                    return@post
                }
                val cx = w / 2f
                val cy = h / 2f
                val matrix = Matrix()
                matrix.postRotate(90f, cx, cy)
                textureView.setTransform(matrix)
                Log.d(TAG, "applyPortraitRotateOnly: w=$w h=$h cx=$cx cy=$cy")
            } catch (e: Exception) {
                Log.e(TAG, "Error in applyPortraitRotateOnly: ${e.message}", e)
            }
        }
    }
}
