// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Deliberately un-eliminable float work: the result is asserted on, so no compiler/JIT is free
 * to delete the loop and make the timing assertions below pass vacuously. */
private fun busyWork(iterations: Int): Double {
    var sink = 0.0
    for (index in 1..iterations) {
        sink += sqrt(index.toDouble())
    }
    return sink
}

class FrameSpansTest {

    @Test
    fun spansDistinguishCheapWorkFromExpensiveWork() {
        val spans = FrameSpans()
        var sink = 0.0
        repeat(SAMPLES) {
            spans.span("cheap") { sink += busyWork(CHEAP_ITERATIONS) }
            spans.span("expensive") { sink += busyWork(EXPENSIVE_ITERATIONS) }
        }
        spans.requireAllSpansClosed()

        val cheapMs = spans.meanMs("cheap")
        val expensiveMs = spans.meanMs("expensive")

        assertTrue(sink > 0.0, "busyWork was optimized away -- the timings below mean nothing")
        assertTrue(
            expensiveMs > 0.0,
            "harness measured 0ms for $EXPENSIVE_ITERATIONS float ops -- spans are not recording, " +
                "or this platform's monotonic clock is too coarse to be used as a perf gate",
        )
        assertTrue(
            expensiveMs > cheapMs * MIN_DISTINGUISHING_RATIO,
            "harness cannot tell ${EXPENSIVE_ITERATIONS}x work from ${CHEAP_ITERATIONS}x work: " +
                "cheap=${cheapMs}ms expensive=${expensiveMs}ms",
        )
        assertEquals(SAMPLES, spans.sampleCount("expensive"))
        assertEquals(setOf("cheap", "expensive"), spans.labels)
    }

    @Test
    fun meanIsIndependentOfSampleCount() {
        // The property that lets a harness change its own loop length without invalidating a
        // checked-in baseline.
        val few = FrameSpans().also { spans -> repeat(4) { spans.span("work") { busyWork(EXPENSIVE_ITERATIONS) } } }
        val many = FrameSpans().also { spans -> repeat(12) { spans.span("work") { busyWork(EXPENSIVE_ITERATIONS) } } }

        assertTrue(many.totalMs("work") > few.totalMs("work"), "12 samples must cost more in total than 4")
        val result = compareTimings(actual = many.meansMs(), baseline = few.meansMs(), toleranceRatio = 1.0)
        assertTrue(result.matches, "means drifted with sample count: ${result.summary}")
    }

    @Test
    fun resetClearsEverything() {
        val spans = FrameSpans()
        spans.span("work") { busyWork(CHEAP_ITERATIONS) }
        spans.reset()

        assertEquals(emptySet(), spans.labels)
        assertEquals(0, spans.sampleCount("work"))
        assertEquals(0.0, spans.meanMs("work"))
    }

    @Test
    fun mismatchedStartStopFailsLoudly() {
        val spans = FrameSpans()
        spans.start("pass")
        assertFailsWith<IllegalStateException> { spans.start("pass") }

        val unclosed = FrameSpans()
        unclosed.start("pass")
        assertFailsWith<IllegalStateException> { unclosed.requireAllSpansClosed() }

        assertFailsWith<IllegalStateException> { FrameSpans().stop("never-started") }
    }

    @Test
    fun spanStopsEvenWhenBlockThrows() {
        val spans = FrameSpans()
        assertFailsWith<IllegalArgumentException> {
            spans.span("pass") { require(false) { "boom" } }
        }
        spans.requireAllSpansClosed()
        assertEquals(1, spans.sampleCount("pass"))
    }

    private companion object {
        const val SAMPLES = 5
        const val CHEAP_ITERATIONS = 1

        // Big enough that one span clears even a browser's coarsened performance.now() resolution
        // (~0.1ms) by well over an order of magnitude.
        const val EXPENSIVE_ITERATIONS = 2_000_000
        const val MIN_DISTINGUISHING_RATIO = 5.0
    }
}
