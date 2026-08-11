// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.navigation.shadcnTabs
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnScrollArea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberStateValue

private val BOTTOM_DOCK_TABS = listOf("Console", "Timeline", "Assets")
private val BOTTOM_DOCK_INSET = 8f.dp

/** Docked workspace panel. Its tabs intentionally use the same remembered-index pattern as the
 * ui-showcase Tabs preview, so selection survives immediate-mode frames without adding another
 * Studio store concern for temporary chrome state. */
internal fun UiScope.drawStudioBottomDock() {
    var selectedTab by context.rememberStateValue("studio-bottom-dock", "selected-tab") { 0 }
    column(
        id = "studio-bottom-dock-content",
        verticalArrangement = Arrangement.spacedBy(8f.dp),
        modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax).padding(BOTTOM_DOCK_INSET),
    ) {
        selectedTab = shadcnTabs(
            id = "studio-bottom-dock",
            tabs = BOTTOM_DOCK_TABS,
            selectedIndex = selectedTab,
        )
        when (selectedTab) {
            0 -> drawStudioConsole()
            1 -> drawStudioTimelinePlaceholder()
            2 -> drawStudioAssetsPlaceholder()
        }
    }
}

private fun ColumnScope.drawStudioConsole() {
    row(
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        modifier = Modifier.width(Dimension.FillMax),
    ) {
        shadcnBadge("0 errors", variant = ShadcnBadgeVariant.Outline)
        shadcnBadge("0 warnings", variant = ShadcnBadgeVariant.Outline)
    }
    shadcnScrollArea(
        id = "studio-console-log",
        modifier = Modifier.width(Dimension.FillMax).weight(1f),
    ) {
        shadcnText("Studio ready", muted = true)
    }
}

private fun ColumnScope.drawStudioTimelinePlaceholder() {
    column(modifier = Modifier.width(Dimension.FillMax).weight(1f)) {
        shadcnText("Timeline", muted = true)
        shadcnText("Animation clips will appear here.", muted = true)
    }
}

private fun ColumnScope.drawStudioAssetsPlaceholder() {
    column(modifier = Modifier.width(Dimension.FillMax).weight(1f)) {
        shadcnText("Assets", muted = true)
        shadcnText("Project assets will appear here.", muted = true)
    }
}
