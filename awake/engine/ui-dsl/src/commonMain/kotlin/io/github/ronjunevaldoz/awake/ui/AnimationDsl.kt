// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Clips its content to an animated height.
 */
fun UiColumnDslScope.animatedHeight(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = UiModifier(),
    responsiveness: Float = 12f,
    content: UiColumnDslScope.(slot: UiSlot) -> Unit
) {
    // 1. Measure the content to know the target expanded height
    val state = scope.widgetState(id)
    var cachedHeight: Float = state.get("measuredHeight", 0f)

    // Peek at the current animation value to decide if we still need to measure/layout
    // while collapsing.
    val animState = scope.widgetState("__animation__$id")
    val currentAnimHeight: Float = animState.get("value", if (expanded) 1000f else 0f)

    if (expanded || currentAnimHeight > 0.01f) {
        val measured = context.measureColumnContent(
            width = fillWidthOrNull() ?: 1000f,
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
    val animatedHeight = animateFloat(id, targetHeight, initial = if (expanded) targetHeight else 0f, responsiveness = responsiveness)

    // 3. Render a clipped container with the animated height
    if (animatedHeight > 0.01f) {
        val requestedWidth = modifier.width ?: Dimension.FillMax
        val slot = scope.claimSlot(requestedWidth, Dimension.Fixed(animatedHeight.px))
        scope.clip(slot) {
            UiColumnDslScope(childColumn(slot, gap = gap)).content(slot)
        }
    }
}
