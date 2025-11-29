package com.example.fireseamlesslooper

import android.graphics.Matrix
import android.util.Log
import android.view.TextureView

object PortraitVideoTransform2 {
    private const val TAG = "PortraitVideoTransform2"

    // Initial horizontal scale + center-rotation + uniform scale around the TextureView's own center
    fun applyPortraitRotateAndScale(textureView: TextureView, scale: Float) {
        textureView.post {
            try {
                val w = textureView.width.toFloat()
                val h = textureView.height.toFloat()
                if (w <= 0f || h <= 0f) {
                    Log.w(TAG, "TextureView not laid out yet (size 0). Skipping rotate/scale.")
                    return@post
                }
                val cx = w / 2f
                val cy = h / 2f
                val matrix = Matrix()
                matrix.postScale(0.5625f, 1f, cx, cy)
                matrix.postRotate(90f, cx, cy)
                matrix.postScale(scale, scale, cx, cy)
                textureView.setTransform(matrix)
                Log.d(TAG, "applyPortraitRotateAndScale: w=$w h=$h cx=$cx cy=$cy initialScaleX=0.5625 scale=$scale")
            } catch (e: Exception) {
                Log.e(TAG, "Error in applyPortraitRotateAndScale: ${e.message}", e)
            }
        }
    }

    // Center-rotate + horizontal scale with translation to re-center
    fun applyPortraitRotateAndScaleHorizontal(textureView: TextureView, scaleX: Float) {
        textureView.post {
            try {
                val w = textureView.width.toFloat()
                val h = textureView.height.toFloat()
                if (w <= 0f || h <= 0f) {
                    Log.w(TAG, "TextureView not laid out yet (size 0). Skipping rotate/scale.")
                    return@post
                }
                val cx = w / 2f
                val cy = h / 2f

                // Build matrix: rotate around center, then horizontal scale around center
                val matrix = Matrix()
                matrix.postRotate(90f, cx, cy)
                matrix.postScale(scaleX, 1f, cx, cy)

                // After rotation the content footprint (before scale) is rotatedW = h, rotatedH = w
                val rotatedW = h
                val rotatedH = w

                // After applying horizontal scale, width becomes scaledW
                val scaledW = rotatedW * scaleX
                val scaledH = rotatedH * 1f

                // Compute translation to center the scaled-rotated content inside the TextureView
                // Positive translate moves content right/down, so compute (view - content)/2
                val translatedX = (w - scaledW) / 2f
                val translatedY = (h - scaledH) / 2f

                matrix.postTranslate(translatedX, translatedY)

                textureView.setTransform(matrix)

                Log.d(TAG, "applyPortraitRotateAndScaleHorizontal: w=$w h=$h cx=$cx cy=$cy scaleX=$scaleX translated=($translatedX,$translatedY) scaledW=$scaledW scaledH=$scaledH")
            } catch (e: Exception) {
                Log.e(TAG, "Error in applyPortraitRotateAndScaleHorizontal: ${e.message}", e)
            }
        }
    }
}
