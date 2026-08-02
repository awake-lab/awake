// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import kotlin.time.TimeSource

/**
 * Opt-in instrumentation counting how many trial-measure passes (see
 * [UiContextMeasureState.measureColumnContent]/[measureRowContent]) run during a frame, and how
 * much wall-clock time they cost. Off by default -- [enabled] is false, and [record] is a no-op
 * measurement pass-through when disabled, so this has zero cost in normal builds. Flip [enabled]
 * on around a frame under test (e.g. a desktopTest) to inspect where trial-measurement time goes;
 * see samples/ui-showcase's desktopTest UiShowcaseLayoutCostTest for an example.
 */
object UiMeasureTrialStats {
    var enabled: Boolean = false

    /** Total number of trial-measure passes that ran, including nested ones (a trial pass whose
     * content itself contains a WrapContent row/column triggers a nested trial pass). */
    var trialCount: Int = 0
        private set

    /** Wall-clock time spent inside trial-measure passes, counted once per outermost pass so
     * nested trial time (already included in its parent pass's elapsed time) isn't double
     * counted -- this is directly comparable against total frame time. */
    var trialNanos: Long = 0L
        private set

    private var depth = 0
    private var outerStart: TimeSource.Monotonic.ValueTimeMark? = null

    fun reset() {
        trialCount = 0
        trialNanos = 0L
        depth = 0
        outerStart = null
    }

    internal inline fun <T> record(block: () -> T): T {
        if (!enabled) return block()
        trialCount += 1
        if (depth == 0) {
            outerStart = TimeSource.Monotonic.markNow()
        }
        depth += 1
        try {
            return block()
        } finally {
            depth -= 1
            if (depth == 0) {
                trialNanos += outerStart!!.elapsedNow().inWholeNanoseconds
            }
        }
    }
}
