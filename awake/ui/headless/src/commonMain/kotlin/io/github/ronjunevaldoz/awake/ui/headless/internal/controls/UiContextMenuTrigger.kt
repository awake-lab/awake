// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.interact
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.UiInteraction
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.withIntrinsicLabelWidth

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/**
 * [shouldOpen] is true exactly on the frame a fresh secondary-click lands over the trigger's
 * target while not already expanded -- the caller reacts to it once (e.g. `onExpandedChange
 * (true)`), not every frame. [anchor] is the click position to open a popup at, remembered
 * across frames via [UiPrimitiveScope.widgetState] so it stays stable while the pointer moves after the
 * click that opened it.
 */
data class UiContextMenuTrigger(
    val shouldOpen: Boolean,
    val anchor: UiBounds,
)

/**
 * Detect-secondary-click-over-target, remember-the-click-point, signal-open-once -- any
 * differently-skinned right-click menu needs the same interaction, independent of what
 * actually opens. Uses [UiPrimitiveScope.hitTest] rather than a manual point-in-rect comparison.
 */
fun UiPrimitiveScope.contextMenuTrigger(id: String, expanded: Boolean, target: UiBounds): UiContextMenuTrigger {
    val input = context.inputState
    val state = widgetState(id)
    var shouldOpen = false

    if (hitTest(target) && input.secondaryPointerDown) {
        state.set("clickX", input.pointerX)
        state.set("clickY", input.pointerY)
        if (!expanded) shouldOpen = true
    }

    val clickX = state.get("clickX", input.pointerX)
    val clickY = state.get("clickY", input.pointerY)
    return UiContextMenuTrigger(shouldOpen = shouldOpen, anchor = UiBounds(clickX, clickY, 0f, 0f))
}
