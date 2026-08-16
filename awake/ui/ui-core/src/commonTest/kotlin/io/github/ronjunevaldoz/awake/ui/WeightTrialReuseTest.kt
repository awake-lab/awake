// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Gate against the throwaway `hasWeightedChild` trial coming back (see
 * docs/tasks/2026-08-02-trial-measure-double-execution.md). Weighted-ness is structural and the
 * real content pass already executes the subtree, so from a node's second frame onward the answer
 * comes from last frame's live observation and NO trial should run at all for this shape.
 *
 * Fixture is deliberately explicit-size-only (no WrapContent anywhere): WrapContent nodes pay a
 * separate SIZING trial that this change does not touch, and mixing the two would make the count
 * unreadable.
 */
class WeightTrialReuseTest {

    @Test
    fun nestedRowsAndColumnsRunNoTrialAfterTheFirstFrame() {
        val ui = UiContext()
        val firstFrame = countTrials(ui) { drawChain(depth = 4) }
        val secondFrame = countTrials(ui) { drawChain(depth = 4) }
        val thirdFrame = countTrials(ui) { drawChain(depth = 4) }

        assertTrue(
            firstFrame > 0,
            "fixture must actually exercise the weight-detection trial on its first frame, " +
                "otherwise this test proves nothing -- got $firstFrame",
        )
        assertEquals(
            0,
            secondFrame,
            "every row()/column() in an unchanged tree must reuse last frame's structural " +
                "answer -- a non-zero count means the throwaway trial is back (first=$firstFrame)",
        )
        assertEquals(0, thirdFrame, "steady state must stay trial-free")
    }

    @Test
    fun layoutIsIdenticalOnTheReusedFrame() {
        val ui = UiContext()
        val slots = mutableListOf<UiBounds>()
        repeat(3) { frame ->
            ui.beginFrame(400f, 400f, testSnapshot())
            slots += ui.createColumn(x = 0f, y = 0f, width = 400f, height = 400f).row(
                modifier = Modifier.width(Dimension.Fixed(300f.px)).height(Dimension.Fixed(40f.px)),
            ) {
                column(modifier = Modifier.weight(1f).height(Dimension.Fixed(40f.px))) { }
                column(modifier = Modifier.width(Dimension.Fixed(50f.px)).height(Dimension.Fixed(40f.px))) { }
            }
            ui.endFrame()
            if (frame > 0) {
                assertEquals(slots[0], slots[frame], "reused answer changed the layout on frame $frame")
            }
        }
    }

    /** A weight() appearing where last frame had none is a structural change: one frame of
     * unweighted layout is the accepted lag, a permanently wrong branch is not. */
    @Test
    fun aNewlyWeightedChildSelfCorrectsOnTheNextFrame() {
        val ui = UiContext()

        fun draw(useWeight: Boolean): UiBounds {
            var first = UiBounds(0f, 0f, 0f, 0f)
            ui.beginFrame(400f, 400f, testSnapshot())
            ui.createColumn(x = 0f, y = 0f, width = 400f, height = 400f).row(
                modifier = Modifier.width(Dimension.Fixed(300f.px)).height(Dimension.Fixed(40f.px)),
            ) {
                first = column(
                    modifier = if (useWeight) {
                        Modifier.weight(1f).height(Dimension.Fixed(40f.px))
                    } else {
                        Modifier.width(Dimension.Fixed(50f.px)).height(Dimension.Fixed(40f.px))
                    },
                ) { }
                column(modifier = Modifier.width(Dimension.Fixed(50f.px)).height(Dimension.Fixed(40f.px))) { }
            }
            ui.endFrame()
            return first
        }

        draw(useWeight = false)
        draw(useWeight = false)
        val lagged = draw(useWeight = true)
        val corrected = draw(useWeight = true)
        val reference = UiContext().let { fresh ->
            fresh.beginFrame(400f, 400f, testSnapshot())
            var slot = UiBounds(0f, 0f, 0f, 0f)
            fresh.createColumn(x = 0f, y = 0f, width = 400f, height = 400f).row(
                modifier = Modifier.width(Dimension.Fixed(300f.px)).height(Dimension.Fixed(40f.px)),
            ) {
                slot = column(modifier = Modifier.weight(1f).height(Dimension.Fixed(40f.px))) { }
                column(modifier = Modifier.width(Dimension.Fixed(50f.px)).height(Dimension.Fixed(40f.px))) { }
            }
            fresh.endFrame()
            slot
        }

        assertTrue(
            lagged.width != reference.width,
            "fixture must actually exercise the one-frame lag, otherwise this test proves nothing",
        )
        assertEquals(
            reference.width,
            corrected.width,
            "the frame after a weight() appeared must match a first-frame (trial-computed) " +
                "weighted layout -- lagged=${lagged.width}",
        )
    }

    private fun countTrials(ui: UiContext, content: UiPrimitiveScope.() -> Unit): Int {
        ui.beginFrame(1000f, 1000f, testSnapshot())
        UiMeasureTrialStats.reset()
        UiMeasureTrialStats.enabled = true
        try {
            ui.createColumn(x = 0f, y = 0f, width = 1000f, height = 1000f).content()
            ui.endFrame()
            return UiMeasureTrialStats.trialCount
        } finally {
            UiMeasureTrialStats.enabled = false
            UiMeasureTrialStats.reset()
        }
    }

    private fun UiPrimitiveScope.drawChain(depth: Int) {
        if (depth == 0) return
        row(modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax)) {
            column(modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax)) {
                drawChain(depth - 1)
            }
        }
    }
}
