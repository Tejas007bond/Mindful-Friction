package com.mindful.friction

import java.util.LinkedList
import kotlin.math.abs

class ScrollTracker {
    private val window = LinkedList<Pair<Long, Int>>()
    private val WINDOW_SIZE_MS = 10000
    private var zombieStartTimestamp: Long = 0L

    fun updateDataAndGetZombieDuration(deltaY: Int): Long {
        val now = System.currentTimeMillis()
        window.addLast(Pair(now, abs(deltaY)))

        // clear out data which is there for more than 10 seconds
        while (window.isNotEmpty() && (now - window.first().first > WINDOW_SIZE_MS)) {
            window.removeFirst()
        }

        if (window.size < 8) return 0L

        // Calculate avg velocity
        val totalDistance = window.sumOf { it.second }
        val timeSpan = window.last().first - window.first().first
        if (timeSpan <= 0) return 0L
        val avgVelocity = totalDistance.toDouble() / timeSpan

        //Calculate variance
        var varianceSum = 0.0
        for (event in window) {
            val deviation = avgVelocity - event.second.toDouble()
            varianceSum += (deviation * deviation)
        }
        val variance = varianceSum / window.size

        // Get a decision : True if scrolling speed is high with near zero variance
        val isZombie = avgVelocity > 0.8 && variance < 15.0

        return if (isZombie) {
            if (zombieStartTimestamp == 0L) zombieStartTimestamp = now
            now - zombieStartTimestamp // Return total millisecond in zombie state
        } else {
            zombieStartTimestamp = 0L // Instantly reset timeline if they break the uniform pattern
            0L
        }


    }

}
