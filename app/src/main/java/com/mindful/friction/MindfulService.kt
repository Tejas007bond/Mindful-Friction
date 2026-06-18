package com.mindful.friction

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button

class MindfulService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val scrollTracker = ScrollTracker()
    private lateinit var frictionEngine: FrictionEngine

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Pass a lambda function handler directly into our engine configuration
        frictionEngine = FrictionEngine(this) {
            showMindfulOverlay()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val rawDelta = event.fromIndex - event.toIndex

            // Pass movement values into the tracker to calculate behavior durations
            val zombieDuration = scrollTracker.updateDataAndGetZombieDuration(rawDelta)
            frictionEngine.evaluateFrictionLevel(zombieDuration)
        }
    }

    private fun showMindfulOverlay() {
        if (overlayView != null) return // Safety check: avoid double stacking views

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            y = 120 // Graceful offset positioning above navigation bars
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_mindful_friction, null)

        overlayView?.findViewById<Button>(R.id.dismissButton)?.setOnClickListener {
            removeMindfulOverlay()
            frictionEngine.evaluateFrictionLevel(0L) // Reset baseline tracking clock
        }

        windowManager.addView(overlayView, params)
    }

    private fun removeMindfulOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    override fun onInterrupt() {}
}
