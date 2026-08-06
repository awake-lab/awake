// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuEntry
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.layout.*

/**
 * Real shadcn's `ContextMenu`: a context menu trigger that listens for secondary clicks (right click)
 * over [target] and opens a floating [shadcnDropdownMenu] popup at the cursor position.
 */
fun UiScope.shadcnContextMenu(
    id: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<UiDropdownMenuEntry>,
    modifier: UiModifier = Modifier,
    target: UiScope.() -> UiBounds
): UiBounds {
    val bounds = target()
    val input = context.inputState
    val state = widgetState(id)
    val isHovered = input.pointerX >= bounds.x && input.pointerX <= bounds.x + bounds.width &&
            input.pointerY >= bounds.y && input.pointerY <= bounds.y + bounds.height

    if (isHovered && input.secondaryPointerDown) {
        state.set("clickX", input.pointerX)
        state.set("clickY", input.pointerY)
        if (!expanded) {
            onExpandedChange(true)
        }
    }

    if (expanded) {
        val clickX = state.get("clickX", input.pointerX)
        val clickY = state.get("clickY", input.pointerY)
        val result = shadcnDropdownMenu(
            id = "$id.menu",
            anchorSlot = UiBounds(clickX, clickY, 0f, 0f),
            expanded = true,
            items = items
        )
        if (result.dismissed || result.selectedIndex != null) {
            onExpandedChange(false)
        }
    }

    return bounds
}
