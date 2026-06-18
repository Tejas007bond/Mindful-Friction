package com.mindful.friction

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class FrictionEngine(private val context: Context, private val onTriggerUI: () -> Unit) {

    private val vibrator: Vibrator by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    enum class FrictionState { CLEAR, HAPTIC_NUDGE, OVERLAY_TRIGGERED }

    private var currentState = FrictionState.CLEAR

    fun evaluateFrictionLevel(zombieDurationMs: Long) {
        when {
            // Stage 1: User is zombie-scrolling for 5 seconds -> Execute Subconscious Tap
            zombieDurationMs in 5000..9999 -> {
                if (currentState == FrictionState.CLEAR) {
                    triggerSoftHaptic()
                    currentState = FrictionState.HAPTIC_NUDGE
                }
            }
        }
    }

}
