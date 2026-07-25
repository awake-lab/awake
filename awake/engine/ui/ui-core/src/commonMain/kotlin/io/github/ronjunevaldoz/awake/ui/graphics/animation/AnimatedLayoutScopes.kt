// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.core.graphics.animation

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.animateFloat
import io.github.ronjunevaldoz.awake.ui.core.graphics.clip
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.measureColumnContent
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Clips its content to an animated height.
 */
fun ColumnScope.animatedHeight(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    responsiveness: Float = 12f,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot? {
    // 1. Measure the content to know the target expanded height
    val state = widgetState(id)
    var cachedHeight: Float = state.get("measuredHeight", 0f)

    if (expanded) {
        val measured = measureColumnContent(
            width = fillWidthOrNull() ?: 4096f,
            gap = gap,
            content = content
        )
        cachedHeight = measured.height
        state.set("measuredHeight", cachedHeight)
    }

    // 2. Animate current height toward 0 (collapsed) or measured.height (expanded)
    val targetHeight = if (expanded) cachedHeight else 0f
    val animatedHeight = animateFloat(id, targetHeight, responsiveness = responsiveness)

    // 3. Render a clipped container with the animated height
    return if (animatedHeight > 0.01f) {
        val requestedWidth = modifier.widthDimension ?: Dimension.FillMax
        val slot = claimSlot(requestedWidth, Dimension.Fixed(animatedHeight.px))
        clip(slot) {
            context.createColumn(slot, gap = this@animatedHeight.gap)
                .content(slot)
        }
        slot
    } else {
        null
    }
}
