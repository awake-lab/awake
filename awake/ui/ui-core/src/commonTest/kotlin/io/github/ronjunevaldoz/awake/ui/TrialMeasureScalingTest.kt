// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Synthetic, ui-core-only regression gate for the trial-measure fix in
 * docs/tasks/2026-08-02-trial-measure-double-execution.md -- unlike
 * samples/ui-showcase's UiShowcaseLayoutCostTest (which measures the real Checkout Form page and
 * therefore shifts whenever that page's content changes for unrelated reasons), this builds a
 * deliberately controlled N-deep nested row()/column() chain and isolates depth-driven trial-pass
 * growth from page-content growth.
 *
 * Fixture shape: each level is a `row(SpaceBetween) { column(SpaceBetween, weight(1f)) { <next
 * level> } }` chain -- `Arrangement.SpaceBetween` (`requiresMeasuredDistribution() == true`) plus
 * a `weight(1f)` child at every level is exactly the shape the fix in Row.kt/Column.kt touches:
 * before the fix, every one of these nodes paid an unconditional `hasWeightedChild`-detection
 * trial *in addition to* the plannedSlots branch's own real-bound trial and the real content
 * execution -- 3 full executions of the remaining subtree per level (3^depth growth, the same
 * shape `3f86853e` already fixed for the WrapContent-wrapper case). The fix removes the wasted
 * detection trial for this arrangement shape, dropping it to 2 full executions per level
 * (2^depth). See the design doc for why this does NOT eliminate the exponential shape entirely --
 * that's explicitly out of scope here; this test only confirms the 3^depth -> 2^depth reduction
 * the narrow reorder fix actually delivers.
 */
class TrialMeasureScalingTest {

    @Test
    fun spaceBetweenWeightedChainTrialCountGrowsByBaseTwoNotBaseThreePerLevel() {
        val counts = listOf(4, 6, 8).associateWith { depth -> measureTrialCount(depth) }
        println("trial-measure counts by chain depth (post-fix): $counts")

        // Each `depth` unit nests one row() *and* one column() (2 true nesting levels). Post-fix,
        // every level pays exactly 2 full content executions (trial + real) instead of 3
        // (wasted hasWeightedChild trial + plannedSlots' own trial + real) -- so +2 `depth`
        // units (= 4 true nesting levels) predicts a ratio near 2^4=16 if the fix holds, vs.
        // 3^4=81 if the extra unconditional trial this fix removes ever comes back. (Measured
        // post-fix: depth 4/6/8 -> 511/8191/131071 trials, ratios ~16.03/~16.01 -- matches the
        // 2^4 prediction almost exactly.) 30 leaves headroom above 16 for fixed leaf/root
        // overhead while still failing hard well short of the 81 a regression would produce.
        val ratio4to6 = counts.getValue(6).toDouble() / counts.getValue(4)
        val ratio6to8 = counts.getValue(8).toDouble() / counts.getValue(6)
        assertTrue(
            ratio4to6 < 30.0,
            "depth 4->6 trial-count ratio was $ratio4to6, expected close to 2^4=16 (base-2 " +
                "per-level growth); a ratio near 3^4=81 would mean the requiresMeasuredDistribution() " +
                "trial-skip regressed",
        )
        assertTrue(
            ratio6to8 < 30.0,
            "depth 6->8 trial-count ratio was $ratio6to8, expected close to 2^4=16 (base-2 " +
                "per-level growth); a ratio near 3^4=81 would mean the requiresMeasuredDistribution() " +
                "trial-skip regressed",
        )
    }

    private fun measureTrialCount(depth: Int): Int {
        val ui = UiContext()
        ui.beginFrame(1000f, 1000f, testSnapshot())
        UiMeasureTrialStats.reset()
        UiMeasureTrialStats.enabled = true
        try {
            ui.createColumn(x = 0f, y = 0f, width = 1000f).column(
                modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax),
            ) {
                buildSpaceBetweenWeightedChain(depth)
            }
            ui.endFrame()
            return UiMeasureTrialStats.trialCount
        } finally {
            UiMeasureTrialStats.enabled = false
            UiMeasureTrialStats.reset()
        }
    }

    private fun UiPrimitiveScope.buildSpaceBetweenWeightedChain(depth: Int) {
        if (depth <= 0) {
            surface(id = "leaf", modifier = Modifier.width(Dimension.FillMax).height(16f.px)) {}
            return
        }
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax),
        ) {
            column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.weight(1f).height(Dimension.FillMax),
            ) {
                buildSpaceBetweenWeightedChain(depth - 1)
            }
        }
    }
}
