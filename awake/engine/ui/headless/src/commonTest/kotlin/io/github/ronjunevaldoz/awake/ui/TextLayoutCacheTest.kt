// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression coverage for docs/tasks/2026-08-03-text-layout-measure-cache.md's
 * `layoutBitmapText` memoization -- verifies the *skip*, not just the value, since the value was
 * never in doubt for a pure function (see that doc's "Verification of correctness" section).
 */
class TextLayoutCacheTest {

    /** Delegates every call to [BitmapFont] but counts [advanceFor] calls, the operation the
     * cache is meant to skip on a hit. */
    private class CountingFont(private val delegate: UiFont = BitmapFont()) : UiFont by delegate {
        var advanceForCalls: Int = 0
            private set

        override fun advanceFor(char: Char, glyphPx: Float): Float {
            advanceForCalls++
            return delegate.advanceFor(char, glyphPx)
        }
    }

    private fun renderOnce(ui: UiContext, font: UiFont, label: String, slotWidth: Float = 200f) {
        ui.pushFont(font)
        val scope = ui.createAbsolute(x = 0f, y = 0f)
        scope.text(label = label, slot = UiBounds(0f, 0f, slotWidth, 12f), font = font)
    }

    @Test
    fun identicalTextTwoFramesInARowSkipsTheSecondLayoutWalk() {
        val font = CountingFont()
        val ui = UiContext()
        val label = "the quick brown fox"
        // emitLinesInternal's draw pass calls advanceFor once per character every frame,
        // cache or no cache -- that's the one part of renderTextBlock this design deliberately
        // does not (and should not) skip, see the design doc's "not a cache target" note. A
        // cache hit therefore doesn't drive the per-frame delta to zero, it drives it down to
        // exactly this floor.
        val expectedDrawPassCallsPerFrame = label.length

        ui.beginFrame(400f, 200f, testSnapshot())
        renderOnce(ui, font, label)
        ui.endFrame()
        val callsAfterFirstFrame = font.advanceForCalls
        assertTrue(
            callsAfterFirstFrame > expectedDrawPassCallsPerFrame,
            "first frame must pay both the layout walk and the draw pass, not just the draw floor",
        )

        ui.beginFrame(400f, 200f, testSnapshot())
        renderOnce(ui, font, label)
        ui.endFrame()

        assertEquals(
            expectedDrawPassCallsPerFrame,
            font.advanceForCalls - callsAfterFirstFrame,
            "second frame with identical label/glyphPx/width/font must hit the layout cache: " +
                "its advanceFor delta over frame 1 should be exactly the unavoidable draw-pass " +
                "cost, with zero added for layoutBitmapText's wrap/measure walk",
        )
    }

    @Test
    fun changedLabelIsNotServedFromTheStaleCacheEntry() {
        val font = CountingFont()
        val ui = UiContext()

        ui.beginFrame(400f, 200f, testSnapshot())
        renderOnce(ui, font, "short")
        ui.endFrame()
        val callsAfterFirstFrame = font.advanceForCalls

        ui.beginFrame(400f, 200f, testSnapshot())
        renderOnce(ui, font, "a much longer label that is clearly not the same string")
        ui.endFrame()

        assertTrue(
            font.advanceForCalls > callsAfterFirstFrame,
            "a different label must miss the cache and re-walk the string, not silently reuse " +
                "the previous label's cached layout",
        )
    }
}
