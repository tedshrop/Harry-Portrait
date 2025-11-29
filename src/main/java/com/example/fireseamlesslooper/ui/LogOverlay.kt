package com.example.fireseamlesslooper.ui

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

object LogOverlay {

    private var overlayView: FrameLayout? = null
    private var logTextView: TextView? = null
    private val logBuffer = StringBuilder()

    fun attach(activity: Activity) {
        if (overlayView != null) return

        val decorView = activity.window.decorView as? ViewGroup ?: return
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            setMargins(20, 50, 20, 0) // top margin to not overlap notification bar
        }

        overlayView = FrameLayout(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000")) // semi-transparent black
            logTextView = TextView(activity).apply {
                setTextColor(Color.WHITE)
                textSize = 10f
                setPadding(10, 5, 10, 5)
                text = logBuffer.toString()
            }
            addView(logTextView)
        }

        decorView.addView(overlayView, params)
    }

    fun log(message: String) {
        logBuffer.append("${System.currentTimeMillis()} $message\n")
        if (logBuffer.length > 10000) { // trim if too long
            logBuffer.delete(0, 5000)
        }
        logTextView?.text = logBuffer.toString()
    }
}
