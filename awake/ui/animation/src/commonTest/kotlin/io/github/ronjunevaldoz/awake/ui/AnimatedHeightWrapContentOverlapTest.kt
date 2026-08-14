// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.graphics.animation.animatedHeight
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.math.ceil
import kotlin.test.Test

/**
 * Reproduces the ui-showcase accordion demo's "expanded item covers its neighbour" report.
 *
 * Real difference between the sidebar (fine) and the showcase demo (broken): the demo's
 * `animatedHeight` sits under a `Dimension.WrapContent`-height `surface()` (shadcnSurface's
 * Preview/Code panel), which re-executes its content through a *trial* measurement pass every
 * frame to size itself -- the sidebar's collapsible has no such ancestor. `content`'s wrap
 * width here stands in for real word-wrap: fewer lines at a wider measured width, exactly
 * mirroring `UiTextWrap.Word` text (the real accordion body).
 */
class AnimatedHeightWrapContentOverlapTest {
    private val lineHeight = 20f
    private val unwrappedContentWidth = 3000f

    private fun wrappedHeight(width: Float): Float =
        ceil(unwrappedContentWidth / width).coerceAtLeast(1f) * lineHeight

    @Test
    fun expandedContentUnderWrapContentSurfaceReservesTheRealWrappedHeight() {
        val ui = UiContext()
        val columnWidth = 300f
        var expanded = false
        var lastSlot: UiBounds? = null

        fun frame() {
            ui.beginFrame(400f, 800f, testSnapshot())
            ui.createColumn(x = 0f, y = 0f, width = columnWidth).apply {
                // Stand-in for shadcnSurface(modifier = Modifier.height(Dimension.WrapContent))
                // wrapping the Preview/Code panel around the real accordion demo.
                surface(id = "wrap-surface", modifier = Modifier.height(Dimension.WrapContent)) {
                    lastSlot = animatedHeight(id = "item", expanded = expanded) { slot ->
                        spacer(Modifier.height(wrappedHeight(slot.width).dp))
                    }
                }
            }
            ui.endFrame()
        }

        // Settle collapsed, then expand and run past the tween's duration (250ms default).
        repeat(10) { frame() }
        expanded = true
        repeat(30) { frame() }

        val claimedHeight = lastSlot?.height ?: 0f
        val trueHeight = wrappedHeight(columnWidth)

        check(claimedHeight >= trueHeight - 0.5f) {
            "animatedHeight reserved $claimedHeight but the real content at width=$columnWidth " +
                "needs $trueHeight -- content will spill into whatever is placed after it " +
                "(the reported accordion overlap)."
        }
    }

    /**
     * The full nesting from `UiShowcaseUi.kt`/`UiShowcaseChrome.kt`: a `row()` shell, a
     * `verticalScroll` content-viewport column (routes through `scrollPanel()`, which
     * trial-measures its content on *every* frame -- not just on a WrapContent transition),
     * then two stacked `Dimension.WrapContent`-height surfaces (the page's own panel, then the
     * Preview/Code panel) around the accordion, matching `ui-showcase-content` /
     * `ui-showcase-preview-code-{page.id}`.
     */
    @Test
    fun expandedContentUnderScrollableWrapContentChainReservesTheRealWrappedHeight() {
        val ui = UiContext()
        val rowWidth = 400f
        var expanded = false
        var lastSlot: UiBounds? = null
        var realContentWidth = 0f

        fun frame() {
            ui.beginFrame(600f, 800f, testSnapshot())
            val scrollState = ui.rememberScrollState("content-scroll")
            ui.createRow(x = 0f, y = 0f, height = 800f, width = rowWidth).column(
                id = "content-viewport",
                modifier = Modifier.verticalScroll(scrollState).width(Dimension.FillMax).height(Dimension.FillMax),
            ) {
                surface(id = "page-surface", modifier = Modifier.height(Dimension.WrapContent)) {
                    surface(id = "preview-code-surface", modifier = Modifier.height(Dimension.WrapContent)) {
                        lastSlot = animatedHeight(id = "item", expanded = expanded) { slot ->
                            realContentWidth = slot.width
                            spacer(Modifier.height(wrappedHeight(slot.width).dp))
                        }
                    }
                }
            }
            ui.endFrame()
        }

        repeat(10) { frame() }
        expanded = true
        repeat(30) { frame() }

        val claimedHeight = lastSlot?.height ?: 0f
        val trueHeight = wrappedHeight(realContentWidth)

        check(claimedHeight >= trueHeight - 0.5f) {
            "animatedHeight reserved $claimedHeight but the real content at width=$realContentWidth " +
                "needs $trueHeight -- content will spill into whatever is placed after it " +
                "(the reported accordion overlap)."
        }
    }
}
