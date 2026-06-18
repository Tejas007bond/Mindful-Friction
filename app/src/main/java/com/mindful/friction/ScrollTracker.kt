package com.mindful.friction

import java.util.LinkedList
import kotlin.math.abs
<<<<<<< HEAD
=======

class ScrollTracker {
    private val window = LinkedList<Pair<Long, Int>>()
    private val WINDOW_SIZE_MS = 10000 // Look at a 10-second history window
    private var zombieStartTimestamp: Long = 0L

    fun updateDataAndGetZombieDuration(deltaY: Int): Long {
        val now = System.currentTimeMillis()
        window.addLast(Pair(now, abs(deltaY)))

        // 1. Clear out stale data older than our 10-second sliding scale
        while (window.isNotEmpty() && (now - window.first.first > WINDOW_SIZE_MS)) {
            window.removeFirst()
        }

        if (window.size < 8) return 0L

        // 2. Compute Average Velocity (Distance over Time)
        val totalDistance = window.sumOf { it.second }
        val timeSpan = window.last.first - window.first.first
        if (timeSpan <= 0) return 0L
        val avgVelocity = totalDistance.toDouble() / timeSpan

        // 3. Compute Statistical Variance (Speed Consistency)
        var varianceSum = 0.0
        for (event in window) {
            val deviation = avgVelocity - event.second.toDouble()
            varianceSum += (deviation * deviation)
        }
        val variance = varianceSum / window.size

        // 4. Evaluate: True Zombie scrolling is moderate/high speed WITH near-zero variance
        val isZombie = avgVelocity > 0.8 && variance < 15.0

        return if (isZombie) {
            if (zombieStartTimestamp == 0L) zombieStartTimestamp = now
            now - zombieStartTimestamp // Return total milliseconds spent in zombie state
        } else {
            zombieStartTimestamp = 0L // Instantly reset timeline if they break the uniform pattern
            0L
        }
    }
}
>>>>>>> master
