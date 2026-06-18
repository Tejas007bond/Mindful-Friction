package com.mindful.friction

import java.util.LinkedList
import kotlin.math.abs

class ScrollTracker {
    private val window = LinkedList<Pair<Long, Int>>()
    private val WINDOW_SIZE_MS = 10000
    private var zombieStartTimestamp: Long = 0L

}
