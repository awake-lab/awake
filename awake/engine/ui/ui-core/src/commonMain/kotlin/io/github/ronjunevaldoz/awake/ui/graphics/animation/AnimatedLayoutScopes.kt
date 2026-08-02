// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.graphics.animation

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.animateFloat
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.measureColumnContent
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.layout.*

/**
 * Clips its content to an animated height.
 */
fun ColumnScope.animatedHeight(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    responsiveness: Float = 12f,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds? {
    // 1. Measure the content to know the target expanded height -- only on the frame this
    // collapses into an expanded state, not every frame it stays expanded (that used to run a
    // full extra layout pass forever for a collapsible left open, e.g. a sidebar group).
    val state = widgetState(id)
    var cachedHeight: Float = state.get("measuredHeight", 0f)
    val wasExpanded = state.get("wasExpanded", false)

    if (expanded && !wasExpanded) {
        val measured = measureColumnContent(
            width = fillWidthOrNull() ?: 4096f,
            gap = gap,
            content = content
        )
        cachedHeight = measured.height
        state.set("measuredHeight", cachedHeight)
    }
    state.set("wasExpanded", expanded)

    // 2. Animate current height toward 0 (collapsed) or measured.height (expanded)
    val targetHeight = if (expanded) cachedHeight else 0f
    val animatedHeight = animateFloat(id, targetHeight, responsiveness = responsiveness)

    // 3. Render a clipped container with the animated height
    return if (animatedHeight > 0.01f) {
        val requestedWidth = modifier.widthDimension ?: Dimension.FillMax
        val slot = claimSlot(requestedWidth, Dimension.Fixed(animatedHeight.px))
        clip(slot) {
            // Preserve this scope's own real gap on the fresh clipped column -- createColumn no
            // longer takes a raw gap, so reconstruct it as an equivalent SpacedBy arrangement.
            context.createColumn(slot, verticalArrangement = Arrangement.spacedBy(this@animatedHeight.gap.px))
                .content(slot)
        }
        slot
    } else {
        null
    }
}
