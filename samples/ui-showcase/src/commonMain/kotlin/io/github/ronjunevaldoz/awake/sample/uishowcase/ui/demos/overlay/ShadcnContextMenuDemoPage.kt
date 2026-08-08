// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.overlay

import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnContextMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

private val ContextMenuItems = listOf(
    UiDropdownMenuItem(label = "Back", trailingLabel = "Alt+Left"),
    UiDropdownMenuItem(label = "Forward", trailingLabel = "Alt+Right", enabled = false),
    UiDropdownMenuItem(label = "Reload", trailingLabel = "Cmd+R"),
    UiDropdownMenuSeparator,
    UiDropdownMenuItem(label = "Inspect Element", trailingLabel = "Cmd+Opt+I"),
    UiDropdownMenuItem(label = "Delete Item", destructive = true),
)

internal fun ColumnScope.drawShadcnContextMenuDemoPreview() {
    val contextMenuState = context.rememberPopupState("showcase-ctx-menu")
    var lastAction by context.rememberStateValue("showcase-ctx-menu", "action") { "None" }

    shadcnBadge("CONTEXT MENU", variant = ShadcnBadgeVariant.Secondary)
    shadcnSupportingText("Right-click over the designated area to trigger floating action context items.")
    spacer(Modifier.height(8f.dp))

    shadcnContextMenu(
        id = "showcase-ctx-menu-demo",
        expanded = contextMenuState.expanded,
        onExpandedChange = { if (it) contextMenuState.open() else contextMenuState.close() },
        items = ContextMenuItems,
    ) {
        shadcnCard("ctx-card", modifier = Modifier.fillMaxWidth().height(100f.dp)) {
            text(
                "Right click anywhere inside this card area to open Context Menu",
                verticallyCentered = true,
            )
        }
    }
}
