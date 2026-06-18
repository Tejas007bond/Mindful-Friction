package com.mindful.friction

import java.util.LinkedList

class ScrollTracker {
    private val scrollEvents = LinkedList<Long>()
    private val windowSizeMs = 10_000L
    private var zombieStartTimestamp = 0L

    /**
     * Records scroll activity and returns how long (ms) the user has been in a
     * steady doomscroll pattern. Works with both pixel deltas and list index steps.
     */
    fun updateDataAndGetZombieDuration(activityDelta: Int): Long {
        if (activityDelta <= 0) {
            zombieStartTimestamp = 0L
            return 0L
        }

        val now = System.currentTimeMillis()
        scrollEvents.addLast(now)

        while (scrollEvents.isNotEmpty() && now - scrollEvents.first() > windowSizeMs) {
            scrollEvents.removeFirst()
        }

        if (scrollEvents.size < 4) return 0L

        val spanMs = scrollEvents.last() - scrollEvents.first()
        if (spanMs < 1_500) return 0L

        val intervals = (1 until scrollEvents.size).map { scrollEvents[it] - scrollEvents[it - 1] }
        val avgInterval = intervals.average()
        val intervalVariance = intervals.map { (it - avgInterval) * (it - avgInterval) }.average()
        val eventsPerSecond = scrollEvents.size.toDouble() / spanMs * 1_000

        // Steady feed scrolling: frequent events with a regular rhythm
        val isZombie = eventsPerSecond >= 1.0 &&
            avgInterval in 50.0..1_500.0 &&
            intervalVariance < 200_000

        return if (isZombie) {
            if (zombieStartTimestamp == 0L) zombieStartTimestamp = now
            now - zombieStartTimestamp
        } else {
            zombieStartTimestamp = 0L
            0L
        }
    }
}
