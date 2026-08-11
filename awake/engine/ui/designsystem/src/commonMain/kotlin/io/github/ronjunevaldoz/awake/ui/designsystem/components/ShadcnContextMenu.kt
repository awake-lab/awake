// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuEntry
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.unstyled.components.contextMenuTrigger

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
    target: UiScope.() -> UiBounds,
): UiBounds {
    val bounds = target()
    val trigger = contextMenuTrigger(id, expanded, bounds)
    if (trigger.shouldOpen) {
        onExpandedChange(true)
    }

    if (expanded) {
        val result = shadcnDropdownMenu(
            id = "$id.menu",
            anchorSlot = trigger.anchor,
            expanded = true,
            items = items,
            // A context menu's anchor is the cursor point, not a real control -- it has zero
            // width. shadcnDropdownMenu's own default width (anchorSlot.width) would resolve
            // to a zero-width panel, so items paint with no background behind them (reads as
            // "transparent"). Size to content instead, same as any other point-anchored popup,
            // unless the caller pinned a width on [modifier].
            width = modifier.widthDimension ?: Dimension.WrapContent,
        )
        if (result.dismissed || result.selectedIndex != null) {
            onExpandedChange(false)
        }
    }

    return bounds
}
