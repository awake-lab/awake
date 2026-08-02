// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import io.github.ronjunevaldoz.awake.ui.layout.*

/**
 * Real-measurement probe for the live report: "the parent [WrapContent] container is expanding
 * on initial scroll, then fully wrap when scrolled down" on
 * `samples/ui-showcase`'s scroll-panel preview (`showcase-scroll-panel-page` wrapping
 * `scroll-container`). Reproduces the exact shape -- a `Dimension.WrapContent`-height outer
 * `surface` wrapping a fixed-height (176px) `verticalScroll` inner `column` -- and measures the
 * outer's real committed [UiBounds.height] at several `UiScrollState.offsetY` values.
 */
class WrapContentScrollLeakProbeTest {

    private fun content(): io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope.(UiBounds) -> Unit = {
        column(
            id = "scroll-container",
            modifier = Modifier.width(Dimension.FillMax).height(176f.px).verticalScroll(scrollState)
        ) {
            repeat(10) { index ->
                surface(
                    id = "row-$index",
                    modifier = Modifier.width(Dimension.FillMax).height(32f.px)
                ) { }
            }
        }
    }

    private lateinit var scrollState: UiScrollState

    @Test
    fun outerWrapContentHeightStaysStableAcrossScrollOffsets() {
        scrollState = UiScrollState()
        val ui = UiContext()

        fun renderFrame(): UiBounds {
            ui.beginFrame(920f, 620f, testSnapshot())
            var outerSlot: UiBounds? = null
            ui.createBox(x = 0f, y = 0f, width = 920f, height = 620f).column(
                id = "page",
                modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax)
            ) {
                outerSlot = surface(
                    id = "showcase-scroll-panel-page",
                    modifier = Modifier.width(Dimension.Fixed(420f.px)).height(Dimension.WrapContent),
                    content = content()
                )
            }
            ui.endFrame()
            return requireNotNull(outerSlot)
        }

        // 2 frames at offset 0 (rest), catches a trial-vs-real single-frame divergence too.
        val restFrame1 = renderFrame()
        val restFrame2 = renderFrame()

        // Scroll to a mid-way offset (content is ~10*32=320px tall vs a 176px viewport, so
        // maxOffsetY should land around 144).
        scrollState.scrollTo(offsetY = 60f)
        val midFrame = renderFrame()

        // Scroll all the way to the bottom.
        scrollState.scrollTo(offsetY = 10_000f)
        val bottomFrame1 = renderFrame()
        val bottomFrame2 = renderFrame()

        val report = buildString {
            appendLine("outer WrapContent surface height across scroll offsets/frames:")
            appendLine("  rest (offset=0)   frame1=${restFrame1.height} frame2=${restFrame2.height}")
            appendLine("  mid  (offset=60)  height=${midFrame.height} realOffsetY=${scrollState.offsetY}")
            appendLine("  bottom (offset=max) frame1=${bottomFrame1.height} frame2=${bottomFrame2.height} realOffsetY=${scrollState.offsetY}")
        }
        println(report)

        val heights = listOf(restFrame1.height, restFrame2.height, midFrame.height, bottomFrame1.height, bottomFrame2.height)
        check(heights.max() - heights.min() < 0.5f) {
            "outer WrapContent surface height must stay stable regardless of inner scroll offset -- $report"
        }
    }
}
