// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Clips its content to an animated height.
 */
fun ColumnScope.animatedHeight(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = UiModifier(),
    responsiveness: Float = 12f,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot? {
    // 1. Measure the content to know the target expanded height
    val state = widgetState(id)
    var cachedHeight: Float = state.get("measuredHeight", 0f)

    if (expanded) {
        val measured = context.measureDslColumnContent(
            width = fillWidthOrNull() ?: 4096f,
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale,
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
        val requestedWidth = modifier.width ?: Dimension.FillMax
        val slot = claimSlot(requestedWidth, Dimension.Fixed(animatedHeight.px))
        clip(slot) {
            context.ui(slot, font, theme, this@animatedHeight.gap, textScale) {
                this.content(slot)
            }
        }
        slot
    } else {
        null
    }
}
