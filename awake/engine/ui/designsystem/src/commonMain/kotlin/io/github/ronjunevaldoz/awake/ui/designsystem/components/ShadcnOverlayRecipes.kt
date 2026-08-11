// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MagicNumber")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.ShadcnDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.DialogProperties
import io.github.ronjunevaldoz.awake.ui.headless.PanelEdge
import io.github.ronjunevaldoz.awake.ui.headless.SlidePanelProperties
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.contextMenuTrigger
import io.github.ronjunevaldoz.awake.ui.headless.dialog
import io.github.ronjunevaldoz.awake.ui.headless.slidePanel

/** Public, skin-level menu entries. They intentionally contain no Core style or layout types. */
sealed interface ShadcnMenuEntry
data class ShadcnMenuItem(val label: String, val enabled: Boolean = true) : ShadcnMenuEntry
data object ShadcnMenuSeparator : ShadcnMenuEntry

fun UiScope.shadcnContextMenu(
    id: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<ShadcnMenuEntry>,
    target: UiScope.() -> UiBounds,
): UiBounds {
    val bounds = target()
    val trigger = contextMenuTrigger(id, expanded, bounds)
    if (trigger.shouldOpen) onExpandedChange(true)
    if (expanded) {
        val entries = items.map { item ->
            when (item) {
                is ShadcnMenuItem -> ShadcnDropdownMenuItem(item.label, enabled = item.enabled)
                ShadcnMenuSeparator -> ShadcnDropdownMenuSeparator
            }
        }
        val result = shadcnDropdownMenu(
            id = "$id.menu",
            anchorSlot = trigger.anchor,
            expanded = true,
            items = entries,
        )
        if (result.dismissed || result.selectedIndex != null) onExpandedChange(false)
    }
    return bounds
}

enum class ShadcnDrawerPosition { Bottom, Top, Left, Right }

fun UiScope.shadcnDrawer(
    id: String,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    position: ShadcnDrawerPosition = ShadcnDrawerPosition.Bottom,
    size: Dp = 320f.dp,
    content: ColumnScope.(UiBounds) -> Unit,
): UiPopupResult {
    val result = slidePanel(
        id = id,
        expanded = expanded,
        edge = position.toPanelEdge(),
        size = size,
        properties = SlidePanelProperties(
            scrimColor = Color.Black.withAlpha(0.48f),
            surface = SurfaceStyle(
                background = themeValues.colors.background,
                foreground = themeValues.colors.foreground,
                border = SurfaceBorder(1f.dp, themeValues.colors.border),
                cornerRadius = if (position == ShadcnDrawerPosition.Top || position == ShadcnDrawerPosition.Bottom) {
                    themeValues.shapes.xl
                } else {
                    null
                },
                contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(16f.dp),
            ),
        ),
        content = content,
    )
    if (result.dismissed) onDismissRequest()
    return result
}

private fun ShadcnDrawerPosition.toPanelEdge(): PanelEdge = when (this) {
    ShadcnDrawerPosition.Bottom -> PanelEdge.Bottom
    ShadcnDrawerPosition.Top -> PanelEdge.Top
    ShadcnDrawerPosition.Left -> PanelEdge.Left
    ShadcnDrawerPosition.Right -> PanelEdge.Right
}

fun UiScope.shadcnDialog(
    id: String,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    content: ColumnScope.(UiBounds) -> Unit,
): UiPopupResult = dialog(
    id = id,
    expanded = expanded,
    width = width,
    height = height,
    properties = DialogProperties(
        dismissOnClickOutside = true,
        showScrim = true,
        surface = SurfaceStyle(
            background = themeValues.colors.card,
            foreground = themeValues.colors.cardForeground,
            border = SurfaceBorder(1f.dp, themeValues.colors.border),
            cornerRadius = themeValues.shapes.lg,
            contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(24f.dp),
        ),
    ),
    content = content,
)
