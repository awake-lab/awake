// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.graphics.animation

import io.github.ronjunevaldoz.awake.ui.EaseOut
import io.github.ronjunevaldoz.awake.ui.Easing
import io.github.ronjunevaldoz.awake.ui.animateFloatTween
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.scope.isMeasuring
import io.github.ronjunevaldoz.awake.ui.scope.measureColumnContent

/**
 * Clips its content to an animated height.
 */
fun ColumnScope.animatedHeight(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    durationMs: Float = 250f,
    easing: Easing = EaseOut,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds? {
    // 1. Measure the content to know the target expanded height -- only on the frame this
    // collapses into an expanded state, not every frame it stays expanded (that used to run a
    // full extra layout pass forever for a collapsible left open, e.g. a sidebar group).
    //
    // A WrapContent/weighted-child trial-measurement pass re-executes this whole content lambda
    // against a scratch UiContext that shares this real, persisted `state` (see
    // UiContextMeasureState.createMeasureContext) -- so an unguarded `state.set("wasExpanded",
    // expanded)` below would let the trial pass "consume" this frame's false->true transition
    // before the real pass ever sees it: the real pass would then read wasExpanded already
    // flipped true, skip the one-time remeasure, and keep rendering the *previous* expansion's
    // stale cachedHeight even though this frame's content actually changed size. Guard both
    // writes like every other side-effecting UiContext operation (see
    // ResizablePanelGroup.handle()/UiContext.animateFloat's identical isMeasuring() gate) --
    // reads (and the trial's own local remeasure, needed to size this call's own WrapContent
    // result) stay unconditional.
    val state = widgetState(id)
    var cachedHeight: Float = state.get("measuredHeight", 0f)
    val wasExpanded = state.get("wasExpanded", false)

    if (expanded && (!wasExpanded || cachedHeight <= 0f)) {
        val measured = measureColumnContent(
            width = fillWidthOrNull() ?: 4096f,
            gap = gap,
            content = content,
        )
        cachedHeight = measured.height
        if (!isMeasuring()) {
            state.set("measuredHeight", cachedHeight)
        }
    }
    if (!isMeasuring()) {
        state.set("wasExpanded", expanded)
    }

    // 2. Animate current height toward 0 (collapsed) or measured.height (expanded). A fixed-
    // duration animateFloatTween, not the spring-style animateFloat this used to call: that one
    // chases its target with exponential decay that never truly reaches it, so collapsing relied
    // on an epsilon "snapDistance" to decide when to give up and call it 0 -- decay is fastest
    // early and slows to an imperceptible crawl near the target, so in practice content looked
    // fully collapsed within ~15-20 frames while the container kept an invisible sliver of
    // leftover height for dozens more frames, then hard-snapped to exactly 0 once it finally
    // crossed the epsilon (reported live as "slowly hidden, then snap" -- a real, reproducible
    // symptom, not a one-off). Worse, since a wrap-sized parent (e.g. a card) measures this
    // child's height fresh every frame, the parent's own shrink desynced from the child's the
    // instant the child's slot went from "still claiming a sub-2px sliver" to "gone entirely" --
    // the parent had no equivalent snap of its own to stay in lockstep with (reported separately
    // as "the parent didn't have same animation"). animateFloatTween has none of this: it reaches
    // the EXACT target in a known, bounded number of frames, so the slot legitimately becomes
    // exactly 0 (not epsilon-close) right as the tween completes, and a wrap-sized parent measuring
    // it every frame tracks that exact value with no separate snap step to fall out of sync.
    val targetHeight = if (expanded) cachedHeight else 0f
    val animatedHeight =
        animateFloatTween(id, targetHeight, durationMs = durationMs, easing = easing)

    // 3. Render a clipped container with the animated height
    return if (animatedHeight > 0.01f) {
        val requestedWidth = modifier.widthDimension ?: Dimension.FillMax
        val slot = claimSlot(requestedWidth, Dimension.Fixed(animatedHeight.px))
        clip(slot) {
            // Preserve this scope's own real gap on the fresh clipped column -- createColumn no
            // longer takes a raw gap, so reconstruct it as an equivalent SpacedBy arrangement.
            context.createColumn(
                slot,
                verticalArrangement = Arrangement.spacedBy(this@animatedHeight.gap.px),
            )
                .content(slot)
        }
        slot
    } else {
        null
    }
}
