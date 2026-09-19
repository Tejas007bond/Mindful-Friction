package com.mindful.friction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the doomscroll detector.
 *
 * The tracker reports elapsed time since a steady scroll pattern was detected,
 * so the hazard is a stale start timestamp surviving a break in scrolling and
 * reporting far more "zombie" time than the user actually spent scrolling.
 *
 * Time is injected rather than slept through, which keeps these tests instant
 * and deterministic.
 */
class ScrollTrackerTest {

    private var clock = 0L
    private val tracker = ScrollTracker { clock }

    /** A plausible per-event scroll delta (pixels, or list indices for list feeds). */
    private val scrollDelta = 40

    /** Simulates one scroll event, then advances the clock by one typical frame. */
    private fun scroll(): Long {
        val duration = tracker.updateDataAndGetZombieDuration(scrollDelta)
        clock += FRAME_MS
        return duration
    }

    private fun scrollSteadily(events: Int): List<Long> = (1..events).map { scroll() }

    @Test
    fun steady_scrolling_ramps_zombie_duration_from_detection() {
        val durations = scrollSteadily(80)

        val firstNonZero = durations.first { it > 0 }
        assertTrue(
            "duration should begin near zero when the pattern is detected, was $firstNonZero",
            firstNonZero in 1L..1_000L
        )
        assertTrue(
            "after ~6s of steady scrolling the duration should reach the overlay point, was ${durations.last()}",
            durations.last() >= 6_000L
        )
    }

    /**
     * The original bug: putting the phone down for longer than the tracking
     * window cleared the event window but left the start timestamp behind, so
     * the first resumed burst reported the entire pause as doomscroll time and
     * tripped the haptic and overlay immediately.
     */
    @Test
    fun resuming_after_a_long_pause_starts_a_fresh_timer() {
        scrollSteadily(30)
        clock += PAUSE_LONGER_THAN_WINDOW_MS

        val resumed = scrollSteadily(30).first { it > 0 }

        assertTrue(
            "resumed run inherited the paused time instead of restarting, was $resumed ms",
            resumed in 1L..1_000L
        )
    }

    @Test
    fun a_short_idle_gap_restarts_the_run() {
        scrollSteadily(30)
        clock += 2_500L // just past the idle gap, still well inside the event window

        val resumed = scrollSteadily(30).first { it > 0 }

        assertTrue(
            "expected a fresh timer after the idle gap, was $resumed ms",
            resumed in 1L..1_000L
        )
    }

    @Test
    fun stopping_scrolling_resets_the_run() {
        scrollSteadily(30)
        assertTrue("expected an active run before the finger stops", scroll() > 0)

        // No movement means the user is no longer scrolling.
        assertEquals(0L, tracker.updateDataAndGetZombieDuration(0))

        val resumed = scrollSteadily(30).first { it > 0 }
        assertTrue(
            "expected a fresh timer after stopping, was $resumed ms",
            resumed in 1L..1_000L
        )
    }

    /** The service calls reset() when the user switches apps. */
    @Test
    fun reset_starts_the_next_run_from_scratch() {
        scrollSteadily(30)
        tracker.reset()

        val resumed = scrollSteadily(30).first { it > 0 }

        assertTrue(
            "reset did not clear the previous run, was $resumed ms",
            resumed in 1L..1_000L
        )
    }

    private companion object {
        const val FRAME_MS = 100L
        const val PAUSE_LONGER_THAN_WINDOW_MS = 20_000L
    }
}
