package com.mindful.friction

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class FrictionEngine(
    context: Context,
    private val mainHandler: Handler,
    private val onTriggerUI: () -> Unit
) {
    private val appContext = context.applicationContext

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    enum class FrictionState { CLEAR, HAPTIC_NUDGE, OVERLAY_TRIGGERED }

    private var currentState = FrictionState.CLEAR

    fun evaluateFrictionLevel(zombieDurationMs: Long) {
        when {
            zombieDurationMs == 0L -> {
                currentState = FrictionState.CLEAR
            }

            zombieDurationMs >= OVERLAY_THRESHOLD_MS -> {
                if (currentState == FrictionState.OVERLAY_TRIGGERED) return
                if (currentState == FrictionState.CLEAR) triggerSoftHaptic()
                mainHandler.post(onTriggerUI)
                currentState = FrictionState.OVERLAY_TRIGGERED
            }

            zombieDurationMs >= HAPTIC_THRESHOLD_MS -> {
                if (currentState == FrictionState.CLEAR) {
                    triggerSoftHaptic()
                    currentState = FrictionState.HAPTIC_NUDGE
                }
            }
        }
    }

    fun reset() {
        currentState = FrictionState.CLEAR
    }

    private fun triggerSoftHaptic() {
        if (!vibrator.hasVibrator()) return

        // minSdk is 26, so VibrationEffect is always available here.
        val timings = longArrayOf(0, 50, 100, 50)
        val amplitudes = if (vibrator.hasAmplitudeControl()) {
            intArrayOf(0, 120, 0, 120)
        } else {
            intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }

    companion object {
        private const val HAPTIC_THRESHOLD_MS = 3_000L
        private const val OVERLAY_THRESHOLD_MS = 6_000L
    }
}
