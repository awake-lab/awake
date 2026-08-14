// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * A `FillMax`-height child inside a scroll viewport must resolve against the VIEWPORT, not
 * against `measureColumnContent`'s unbounded-measure sentinel.
 *
 * `scrollPanel` measured its content with no `height` argument, so the parameter fell back to the
 * unbounded sentinel. A child asking for `Dimension.FillMax` resolved to that sentinel during the
 * trial pass, `contentHeight` inherited it, and `canScrollY`/`maxOffsetY` were computed off a
 * number nobody can scroll to -- a scrollbar on a sidebar whose content fits, and scrolling that
 * never reaches an end.
 *
 * Only `fillMaxHeight` reaches this. `weight(1f)` divides the committed slot at layout time and
 * measured correctly throughout, so a weighted-spacer version of this test passes with or without
 * the fix and is not kept here.
 *
 * The existing scroll tests all use fixed-height children, which cannot reach this path -- a
 * fixed child measures the same under any available height.
 */
class ScrollableFillMaxChildMeasureTest {

    private fun renderScrollPanel(
        state: UiScrollState,
        contentBuilder: io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope.() -> Unit,
    ) {
        val ui = UiContext()
        ui.beginFrame(VIEWPORT_WIDTH, FRAME_HEIGHT, testSnapshot())
        ui.createBox(x = 0f, y = 0f, width = VIEWPORT_WIDTH, height = FRAME_HEIGHT).column(
            id = "page",
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax),
        ) {
            column(
                id = "scroll-container",
                modifier = Modifier
                    .width(Dimension.FillMax)
                    .height(VIEWPORT_HEIGHT.px)
                    .verticalScroll(state),
            ) { contentBuilder() }
        }
        ui.endFrame()
    }

    @Test
    fun aFillMaxHeightChildDoesNotInventScrollableContent() {
        val state = UiScrollState()
        renderScrollPanel(state) {
            surface(id = "stretchy", modifier = Modifier.width(Dimension.FillMax).fillMaxHeight()) { }
        }

        assertFalse(
            state.canScrollY,
            "a single FillMax-height child exactly fills its viewport, so there is nothing to " +
                "scroll -- got contentHeight=${state.contentHeight} against " +
                "viewportHeight=${state.viewportHeight}.",
        )
    }

    private companion object {
        const val VIEWPORT_WIDTH = 300f
        const val VIEWPORT_HEIGHT = 200f
        const val FRAME_HEIGHT = 600f
    }
}
