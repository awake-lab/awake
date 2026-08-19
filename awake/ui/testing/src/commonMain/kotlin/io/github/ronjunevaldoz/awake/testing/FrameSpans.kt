// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing

import kotlin.time.TimeSource

/**
 * Named wall-clock spans, accumulated across many frames.
 *
 * Wraps whatever `StudioFramePerfProbeTest` (CPU-side UI composition) and
 * `RendererHeadlessFrameTimingTest` (real-GPU frames through MoltenVK) already measure by hand,
 * so both stop hand-rolling `TimeSource.markNow()`/`println` and produce a comparable
 * label -> mean-ms map instead. The natural hook point on the render side is one span per
 * `RenderFeature.recordCommands` call -- each pass is already a single method call.
 *
 * ```
 * val spans = FrameSpans()
 * repeat(frames) {
 *     spans.span("opaque-pass") { opaque.recordCommands(context) }
 *     spans.span("ui-pass") { ui.recordCommands(context) }
 * }
 * compareTimings(spans.meansMs(), parseTimingBaseline(baselineText))
 * ```
 *
 * Measures CPU wall-clock only. On a backend that records commands asynchronously this times the
 * *recording*, not the GPU's execution of it -- for real GPU-side cost a span has to bracket a
 * point the backend actually synchronizes at (`readPixels`, a fence wait), which is exactly what
 * the two headless harnesses above already do.
 */
class FrameSpans {
    // LinkedHashMap (what mutableMapOf returns): first-seen order, so an un-sorted dump reads in
    // pass order rather than hash order.
    private val totalNanos = mutableMapOf<String, Long>()
    private val sampleCounts = mutableMapOf<String, Int>()
    private val openMarks = mutableMapOf<String, TimeSource.Monotonic.ValueTimeMark>()

    /** Labels recorded so far, in first-seen order. */
    val labels: Set<String> get() = totalNanos.keys

    fun start(label: String) {
        val previous = openMarks.put(label, TimeSource.Monotonic.markNow())
        check(previous == null) {
            "span '$label' was started twice without a stop -- spans of the same label can't nest"
        }
    }

    fun stop(label: String) {
        val mark = openMarks.remove(label)
        checkNotNull(mark) { "span '$label' was stopped without being started" }
        totalNanos[label] = (totalNanos[label] ?: 0L) + mark.elapsedNow().inWholeNanoseconds
        sampleCounts[label] = (sampleCounts[label] ?: 0) + 1
    }

    /** [start]/[stop] around [block], stopping even if it throws. */
    inline fun <T> span(label: String, block: () -> T): T {
        start(label)
        try {
            return block()
        } finally {
            stop(label)
        }
    }

    /** How many times [label] was measured. */
    fun sampleCount(label: String): Int = sampleCounts[label] ?: 0

    /** Total time in [label], across every sample. */
    fun totalMs(label: String): Double = (totalNanos[label] ?: 0L) / NANOS_PER_MS

    /** Mean time per occurrence of [label], 0.0 if it was never measured. */
    fun meanMs(label: String): Double {
        val samples = sampleCount(label)
        return if (samples == 0) 0.0 else totalMs(label) / samples
    }

    /**
     * Every label's [meanMs], in first-seen order -- the shape [compareTimings] and
     * [formatTimingBaseline] take.
     *
     * Mean per occurrence, not total, deliberately: it stays comparable against a baseline
     * recorded over a different frame count, so a harness can change its own loop length without
     * invalidating the checked-in numbers.
     */
    fun meansMs(): Map<String, Double> = labels.associateWith { meanMs(it) }

    fun reset() {
        totalNanos.clear()
        sampleCounts.clear()
        openMarks.clear()
    }

    /** Fails if any span is still open -- catches a `start` whose `stop` never ran, which would
     * otherwise silently drop that pass from [meansMs] instead of failing. */
    fun requireAllSpansClosed() {
        check(openMarks.isEmpty()) {
            "spans still open at measurement end: ${openMarks.keys.joinToString()}"
        }
    }

    private companion object {
        const val NANOS_PER_MS = 1_000_000.0
    }
}
