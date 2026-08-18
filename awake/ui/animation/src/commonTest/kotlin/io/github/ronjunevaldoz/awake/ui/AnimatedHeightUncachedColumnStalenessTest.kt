// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.graphics.animation.animatedHeight
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput

/**
 * Regression for [io.github.ronjunevaldoz.awake.ui.graphics.animation.animatedHeight]'s own
 * `wasExpanded`/`measuredHeight` bookkeeping getting corrupted by `column()`'s unconditional
 * hasWeightedChild trial pass (see `UiPrimitiveScope.column()`'s doc comment, and `ResizablePanelGroup
 * .handle()`'s identical fix) -- proof this widget is only safe when driven through the same
 * plain, UNCACHED `column()` a real caller (e.g. `shadcnCollapsible`) always uses, not the raw
 * `ui.createColumn()` scope the existing `AnimatedHeightCollapseProbeTest`/
 * `AnimatedHeightWrapContentOverlapTest` drive directly.
 *
 * The trial pass re-executes `animatedHeight`'s content lambda every frame against a scratch
 * context that shares the real, persisted `WidgetState`. Before the fix, its unconditional
 * `state.set("wasExpanded", expanded)` ran during that trial too -- on the very frame a
 * collapsed panel re-expands, the trial (which always runs *before* the real pass within the
 * same frame) flips `wasExpanded` to `true` first, so the real pass then reads it already
 * flipped and skips its one-time remeasure, keeping whatever `measuredHeight` was cached from
 * the *previous* expansion even if this frame's content is now a different size.
 */
class AnimatedHeightUncachedColumnStalenessTest {

    @Test
    fun reExpandingRemeasuresGrownContentEvenWhenNestedInAnUncachedColumn() {
        val ui = UiContext()
        var expanded = true
        var contentHeightDp = 100f

        fun frame(): UiBounds? {
            ui.beginFrame(UiFrameInput(viewportWidth = 400f, viewportHeight = 800f, input = testSnapshot()))
            var slot: UiBounds? = null
            ui.createAbsolute(x = 0f, y = 0f).column(
                id = "shell",
                modifier = Modifier.width(300f.px).height(400f.px),
            ) {
                slot = animatedHeight(id = "panel", expanded = expanded) {
                    spacer(Modifier.height(contentHeightDp.dp))
                }
            }
            ui.finishFrame().primitives
            return slot
        }

        // Expand and let the tween fully settle at the first content height.
        var settled = 0f
        repeat(60) { settled = frame()?.height ?: 0f }
        check(settled > 90f) { "expected settled height near 100, got $settled" }

        // Collapse fully.
        expanded = false
        repeat(60) { frame() }

        // Content grows while collapsed (e.g. a list backing this panel gained rows), then
        // re-expand -- the cached measured height must be recomputed on this transition frame,
        // not silently reused from before.
        contentHeightDp = 200f
        expanded = true
        var reSettled = 0f
        repeat(60) { reSettled = frame()?.height ?: 0f }

        assertTrue(
            reSettled > 190f,
            "re-expand must remeasure the now-taller content even when animatedHeight() sits " +
                "inside a plain, uncached column() (settled at $reSettled, expected ~200)",
        )
    }
}
