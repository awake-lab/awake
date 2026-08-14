// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTabs
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.weight

private val BOTTOM_DOCK_TABS = listOf("Console", "Timeline", "Assets")
private val BOTTOM_DOCK_INSET = 8f.dp

/** Docked workspace panel. Its tabs intentionally use the same remembered-index pattern as the
 * ui-showcase Tabs preview, so selection survives immediate-mode frames without adding another
 * Studio store concern for temporary chrome state. */
internal fun UiScope.drawStudioBottomDock(store: StudioStore) {
    val selectedState = rememberStateValue("studio-bottom-dock", "selected-tab") { 0 }
    var selectedTab by selectedState::value
    // A tab click is resolved while this column is rendering. Keep this frame's content tied to
    // the selection that its measurement pass saw, then apply the requested tab for next frame.
    // Switching immediately makes the render pass claim a different number of child slots than
    // the measured plan and crashes ColumnScope.claimSlot().
    val renderedTab = selectedTab
    column(
        verticalArrangement = Arrangement.spacedBy(8f.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(BOTTOM_DOCK_INSET),
    ) {
        val requestedTab = shadcnTabs(
            id = "studio-bottom-dock",
            tabs = BOTTOM_DOCK_TABS,
            selectedIndex = renderedTab,
        )
        when (renderedTab) {
            0 -> drawStudioConsole(store)
            1 -> drawStudioTimelinePlaceholder()
            2 -> drawStudioAssetsPlaceholder()
        }
        selectedTab = requestedTab
    }
}

private fun ColumnScope.drawStudioConsole(store: StudioStore) {
    val entries = store.state.value.console.entries
    val errors = entries.count { it.level == StudioContract.ConsoleLevel.Error }
    val warnings = entries.count { it.level == StudioContract.ConsoleLevel.Warning }
    row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        row(horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
            shadcnBadge(id = "studio-console-errors", label = "$errors errors", variant = ShadcnBadgeVariant.Outline)
            shadcnBadge(id = "studio-console-warnings", label = "$warnings warnings", variant = ShadcnBadgeVariant.Outline)
        }
        shadcnButton(
            id = "studio-console-clear",
            label = "Clear",
            size = ShadcnButtonSize.Xs,
            variant = ShadcnButtonVariant.Ghost,
            onClick = { store.dispatch(StudioContract.Intent.ClearConsole) },
        )
    }
    // The vertical resizable dock measures its direct children before placing them. A variable
    // number of console rows can change between those passes on the first frame, so compose the
    // bounded log as one stable direct child until ui-core's measurement contract is repaired.
    val log = entries.takeLast(MAX_VISIBLE_CONSOLE_ENTRIES)
        .joinToString(separator = "\n") { it.message }
        .ifEmpty { "No console output." }
    shadcnText(log, muted = entries.all { it.level == StudioContract.ConsoleLevel.Info })
}

private const val MAX_VISIBLE_CONSOLE_ENTRIES = 4

private fun ColumnScope.drawStudioTimelinePlaceholder() {
    column(modifier = Modifier.fillMaxWidth().weight(1f)) {
        shadcnText("Timeline", muted = true)
        shadcnText("Animation clips will appear here.", muted = true)
    }
}

private fun ColumnScope.drawStudioAssetsPlaceholder() {
    column(modifier = Modifier.fillMaxWidth().weight(1f)) {
        shadcnText("Assets", muted = true)
        shadcnText("Project assets will appear here.", muted = true)
    }
}
