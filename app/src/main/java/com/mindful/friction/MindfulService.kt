package com.mindful.friction

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import kotlin.math.abs

class MindfulService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null
    private val scrollTracker = ScrollTracker()
    private lateinit var frictionEngine: FrictionEngine
    private var lastScrollY = -1
    private var lastFromIndex = -1
    private var lastToIndex = -1
    private var activePackage: String? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        frictionEngine = FrictionEngine(this, mainHandler) {
            showMindfulOverlay()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString()
                if (packageName != null && packageName != activePackage) {
                    activePackage = packageName
                    resetScrollTracking()
                }
            }

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                handleScrollEvent(event)
            }
        }
    }

    private fun handleScrollEvent(event: AccessibilityEvent) {
        val scrollDelta = if (lastScrollY >= 0) abs(event.scrollY - lastScrollY) else 0
        val fromIndexDelta = if (lastFromIndex >= 0) abs(event.fromIndex - lastFromIndex) else 0
        val toIndexDelta = if (lastToIndex >= 0) abs(event.toIndex - lastToIndex) else 0

        lastScrollY = event.scrollY
        lastFromIndex = event.fromIndex
        lastToIndex = event.toIndex

        val rawDelta = when {
            scrollDelta > 0 -> scrollDelta
            fromIndexDelta > 0 -> fromIndexDelta
            toIndexDelta > 0 -> toIndexDelta
            else -> 0
        }

        if (rawDelta == 0) return

        val zombieDuration = scrollTracker.updateDataAndGetZombieDuration(rawDelta)
        frictionEngine.evaluateFrictionLevel(zombieDuration)
    }

    private fun resetScrollTracking() {
        lastScrollY = -1
        lastFromIndex = -1
        lastToIndex = -1
        scrollTracker.reset()
        frictionEngine.reset()
    }

    private fun showMindfulOverlay() {
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            y = 120
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_mindful_friction, null)

        overlayView?.findViewById<Button>(R.id.dismissButton)?.setOnClickListener {
            removeMindfulOverlay()
            frictionEngine.reset()
            resetScrollTracking()
        }

        try {
            windowManager.addView(overlayView, params)
        } catch (_: Exception) {
            overlayView = null
        }
    }

    private fun removeMindfulOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
            overlayView = null
        }
    }

    override fun onDestroy() {
        removeMindfulOverlay()
        super.onDestroy()
    }

    override fun onInterrupt() {}
}
