package com.mindful.friction

import java.util.LinkedList

class ScrollTracker {
    private val scrollEvents = LinkedList<Long>()
    private val windowSizeMs = 10_000L

    /** A quiet gap this long between scroll events means the user stopped scrolling. */
    private val idleGapMs = 2_000L

    private var zombieStartTimestamp = 0L
    private var lastEventTimestamp = 0L

    /**
     * Records scroll activity and returns how long (ms) the user has been in a
     * steady doomscroll pattern. Works with both pixel deltas and list index steps.
     */
    fun updateDataAndGetZombieDuration(activityDelta: Int): Long {
        if (activityDelta <= 0) {
            reset()
            return 0L
        }

        val now = System.currentTimeMillis()

        // A long gap means the user stopped scrolling. Drop the previous run so
        // the next burst starts a fresh timer instead of inheriting the paused
        // time, which fired the haptic nudge and overlay the moment they resumed.
        if (lastEventTimestamp != 0L && now - lastEventTimestamp > idleGapMs) {
            reset()
        }
        lastEventTimestamp = now

        scrollEvents.addLast(now)

        while (scrollEvents.isNotEmpty() && now - scrollEvents.first() > windowSizeMs) {
            scrollEvents.removeFirst()
        }

        if (scrollEvents.size < 4) {
            zombieStartTimestamp = 0L
            return 0L
        }

        val spanMs = scrollEvents.last() - scrollEvents.first()
        if (spanMs < 1_500) {
            zombieStartTimestamp = 0L
            return 0L
        }

        val intervals = (1 until scrollEvents.size).map { scrollEvents[it] - scrollEvents[it - 1] }
        val avgInterval = intervals.average()
        val intervalVariance = intervals.map { (it - avgInterval) * (it - avgInterval) }.average()
        val eventsPerSecond = scrollEvents.size.toDouble() / spanMs * 1_000

        // Steady feed scrolling: frequent events with a regular rhythm
        val isZombie = eventsPerSecond >= 1.0 &&
            avgInterval in 50.0..1_500.0 &&
            intervalVariance < 200_000

        if (!isZombie) {
            zombieStartTimestamp = 0L
            return 0L
        }

        if (zombieStartTimestamp == 0L) zombieStartTimestamp = now
        return now - zombieStartTimestamp
    }

    /** Drops all tracking state, e.g. when the user stops scrolling or changes app. */
    fun reset() {
        scrollEvents.clear()
        zombieStartTimestamp = 0L
        lastEventTimestamp = 0L
    }
}
