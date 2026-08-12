// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.context.UiWeightCacheConsistencyCheck
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Verifies `UiPrimitiveScope.row()`/`UiPrimitiveScope.column()`'s opt-in `id`/`cacheKey` hasWeightedChild cache --
 * see docs/tasks/2026-08-02-trial-measure-cross-frame-cache.md. Distinct failure mode from
 * [WrapContentMeasurementStateTest] (that file guards against a trial pass reading stale
 * *default* state instead of real state; this cache's own new risk is a caller-supplied
 * `cacheKey` that doesn't change when it should -- a stale-*key*, not stale-*state*, bug).
 */
class RowColumnWeightCacheTest {

    @Test
    fun cacheHitSkipsTrialAndProducesIdenticalLayout() {
        val ui = UiContext()

        fun draw(): UiBounds = ui.createColumn(x = 0f, y = 0f, width = 400f, height = 400f).column(
            id = "outer",
            modifier = Modifier.width(Dimension.Fixed(200f.px)).height(Dimension.Fixed(120f.px)),
            cacheKey = "stable",
        ) {
            row(modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(30f.px))) { }
        }

        ui.beginFrame(400f, 400f, testSnapshot())
        UiMeasureTrialStats.reset()
        UiMeasureTrialStats.enabled = true
        val firstSlot = draw()
        val firstFrameTrials = UiMeasureTrialStats.trialCount
        ui.endFrame()

        ui.beginFrame(400f, 400f, testSnapshot())
        UiMeasureTrialStats.reset()
        val secondSlot = draw()
        val secondFrameTrials = UiMeasureTrialStats.trialCount
        UiMeasureTrialStats.enabled = false
        UiMeasureTrialStats.reset()
        ui.endFrame()

        assertTrue(
            secondFrameTrials < firstFrameTrials,
            "second frame with the same id/cacheKey should skip the hasWeightedChild trial " +
                "entirely (cache hit) -- first=$firstFrameTrials second=$secondFrameTrials",
        )
        assertEquals(firstSlot, secondSlot, "cache hit must not change resulting layout")
    }

    @Test
    fun cacheKeyChangePicksUpNewWeightedChildAnswer() {
        val ui = UiContext()

        fun draw(useWeight: Boolean, cacheKey: String): List<UiBounds> {
            val slots = mutableListOf<UiBounds>()
            ui.createColumn(x = 0f, y = 0f, width = 400f, height = 400f).column(
                id = "outer",
                modifier = Modifier.width(Dimension.Fixed(300f.px)).height(Dimension.Fixed(120f.px)),
            ) {
                // The cache must live on the row() that directly owns the weight()-tagged
                // child -- weighted-ness is a DIRECT-children-only signal, so caching it on the
                // outer column() above (whose only direct child is this row, never weighted
                // itself) would never actually exercise the row's own weight() detection.
                row(
                    id = "inner-row",
                    cacheKey = cacheKey,
                    modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(30f.px)),
                ) {
                    // weight() lives on the column() wrapper, not directly on a composite
                    // surface() -- matches TrialMeasureScalingTest's fixture shape and avoids
                    // surface()'s own internal WrapContent-width-fallback interfering with the
                    // width this test is actually asserting on.
                    slots += column(
                        id = "a",
                        modifier = if (useWeight) {
                            Modifier.weight(1f).height(Dimension.Fixed(30f.px))
                        } else {
                            Modifier.width(Dimension.Fixed(50f.px)).height(Dimension.Fixed(30f.px))
                        },
                    ) { }
                    slots += column(id = "b", modifier = Modifier.width(Dimension.Fixed(50f.px)).height(Dimension.Fixed(30f.px))) { }
                }
            }
            return slots
        }

        ui.beginFrame(400f, 400f, testSnapshot())
        val unweighted = draw(useWeight = false, cacheKey = "v1")
        ui.endFrame()

        // Different cacheKey -> guaranteed cache miss -> fresh trial picks up the real,
        // structurally-changed content (a weighted first child now fills remaining width).
        ui.beginFrame(400f, 400f, testSnapshot())
        val weighted = draw(useWeight = true, cacheKey = "v2")
        ui.endFrame()

        assertTrue(
            weighted[0].width > unweighted[0].width,
            "cacheKey change must re-trial and pick up the new weighted-child answer -- " +
                "unweighted first child width=${unweighted[0].width} weighted=${weighted[0].width}",
        )
    }

    @Test
    fun staleCacheKeyIsCaughtByDebugConsistencyCheck() {
        val ui = UiContext()

        fun draw(useWeight: Boolean) {
            ui.createColumn(x = 0f, y = 0f, width = 400f, height = 400f).column(
                id = "outer",
                modifier = Modifier.width(Dimension.Fixed(300f.px)).height(Dimension.Fixed(120f.px)),
            ) {
                row(
                    id = "inner-row",
                    // Deliberately held constant across frames even though content structurally
                    // changes below -- the exact caller mistake this design's own risk section
                    // names. This must be on the row that owns the weighted child (see
                    // cacheKeyChangePicksUpNewWeightedChildAnswer's comment on why).
                    cacheKey = "same-key-both-frames",
                    modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(30f.px)),
                ) {
                    surface(
                        id = "a",
                        modifier = if (useWeight) {
                            Modifier.weight(1f).height(Dimension.Fixed(30f.px))
                        } else {
                            Modifier.width(Dimension.Fixed(50f.px)).height(Dimension.Fixed(30f.px))
                        },
                    ) { }
                }
            }
        }

        UiWeightCacheConsistencyCheck.enabled = true
        try {
            ui.beginFrame(400f, 400f, testSnapshot())
            draw(useWeight = false)
            ui.endFrame()

            // Second frame: same cacheKey, but a .weight() child was added -- this is a cache
            // *hit* by id/cacheKey, so without the debug consistency check it would silently
            // return the stale "no weighted child" answer. With the check enabled it must throw.
            ui.beginFrame(400f, 400f, testSnapshot())
            assertFailsWith<IllegalStateException> {
                draw(useWeight = true)
            }
        } finally {
            UiWeightCacheConsistencyCheck.enabled = false
        }
    }
}
