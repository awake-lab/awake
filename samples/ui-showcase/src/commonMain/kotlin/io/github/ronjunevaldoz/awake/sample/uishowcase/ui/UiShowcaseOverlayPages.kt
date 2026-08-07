// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseCounterContract
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnContextMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDrawer
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiAlertDialogAction
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnAlertDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnTooltip
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnPopover
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.destructiveStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private val ShowcaseActionMenuItems = listOf(
    UiDropdownMenuItem(
        label = "Pinned action",
        enabled = false,
        shadcnSupportingText = "Disabled actions stay visible without becoming clickable."
    ),
    UiDropdownMenuSeparator,
    UiDropdownMenuItem(
        label = "Duplicate panel",
        trailingLabel = "Cmd+D",
        shadcnSupportingText = "Example of a richer menu row with trailing metadata."
    ),
    UiDropdownMenuItem(
        label = "Delete scene",
        destructive = true,
        trailingLabel = "Del",
        shadcnSupportingText = "Routes into the alert dialog flow instead of doing anything immediately."
    )
)

internal fun ColumnScope.drawUiShowcaseCounterPreview(state: UiShowcaseRuntimeState) {
    state.counterStore.drainEffects()
        .lastOrNull()
        ?.let { effect -> state.showcaseCounterEffectMessage = effect.toDebugLabel() }

    val counterState = state.counterStore.state.value
    shadcnBadge("MVI", variant = ShadcnBadgeVariant.Primary)
    shadcnBodyText("Count: ${counterState.count}")
    shadcnSupportingText("Last effect: ${state.showcaseCounterEffectMessage ?: "None"}")
    spacer(Modifier.height(6f.dp))
    row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.height(36f.dp.toDimension())) {
        if (
            shadcnButton(
                id = "counter-decrement",
                label = "Decrement",
                modifier = Modifier.width(112f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Outline
            )
        ) {
            state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Decrement)
        }
        if (
            shadcnButton(
                id = "counter-increment",
                label = "Increment",
                modifier = Modifier.width(112f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Primary
            )
        ) {
            state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Increment)
        }
    }
    row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.height(36f.dp.toDimension())) {
        if (
            shadcnButton(
                id = "counter-reset",
                label = "Reset",
                modifier = Modifier.width(112f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Ghost
            )
        ) {
            state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Reset)
        }
        shadcnBadge(
            label = if (counterState.count >= 0) "FLOW" else "NEGATIVE",
            variant = if (counterState.count >= 0) ShadcnBadgeVariant.Secondary else ShadcnBadgeVariant.Danger
        )
    }
}

internal fun ColumnScope.drawUiShowcasePopupPreview() {
    val actionMenuState = context.rememberPopupState("ui-showcase-action-menu")
    val deleteDialogState = context.rememberPopupState("ui-showcase-delete-dialog")
    val infoDialogState = context.rememberPopupState("ui-showcase-info-dialog")
    var feedbackMessage by context.rememberStateValue("ui-showcase-popup-feedback") {
        "Try the action menu and dialog to inspect the popup layer."
    }

    shadcnBadge("OVERLAY", variant = ShadcnBadgeVariant.Outline)
    shadcnSupportingText("The action menu anchors to the trigger and opens inside a contained popover surface.")
    spacer(Modifier.height(6f.dp))
    row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.height(36f.dp.toDimension())) {
        val menuTrigger = buttonSlot(
            id = "ui-showcase-menu-trigger",
            label = "Actions",
            modifier = Modifier.width(112f.dp).height(36f.dp),
            style = theme.components.button,
            variant = UiButtonVariant.Filled
        )
        if (menuTrigger.clicked) {
            actionMenuState.toggle()
        }
        val menuResult = shadcnDropdownMenu(
            id = "ui-showcase-action-menu",
            anchorSlot = menuTrigger.slot,
            expanded = actionMenuState.expanded,
            items = ShowcaseActionMenuItems,
            // Default width mirrors the anchor (112dp trigger) -- too narrow for these items'
            // trailing shortcuts/supporting text, which truncated labels down to near nothing.
            // shadcn's own richer menus (with icons/shortcuts) use a fixed w-56 (224px)-class
            // width instead of matching the trigger; do the same here.
            width = Dimension.Fixed(224f.dp),
            style = Style { contentPadding(4f.dp) }
        )
        when (menuResult.selectedIndex) {
            1 -> {
                feedbackMessage = "Duplicate panel queued from the dropdown menu."
                actionMenuState.close()
            }
            2 -> {
                feedbackMessage = "Delete requested from the dropdown menu."
                actionMenuState.close()
                deleteDialogState.open()
            }
        }
        if (menuResult.dismissed) {
            actionMenuState.close()
        }
        if (
            shadcnButton(
                id = "ui-showcase-delete-trigger",
                label = "Delete Scene",
                modifier = Modifier.width(128f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Outline
            )
        ) {
            deleteDialogState.open()
        }
        if (
            shadcnButton(
                id = "ui-showcase-info-trigger",
                label = "Open Info Dialog",
                modifier = Modifier.width(150f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Secondary
            )
        ) {
            infoDialogState.open()
        }
        val drawerState = context.rememberPopupState("ui-showcase-drawer")
        val contextMenuState = context.rememberPopupState("ui-showcase-context-menu")

        if (
            shadcnButton(
                id = "ui-showcase-drawer-trigger",
                label = "Open Drawer",
                modifier = Modifier.width(120f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Outline
            )
        ) {
            drawerState.open()
        }

        shadcnContextMenu(
            id = "ui-showcase-context-menu-demo",
            expanded = contextMenuState.expanded,
            onExpandedChange = { if (it) contextMenuState.open() else contextMenuState.close() },
            items = ShowcaseActionMenuItems
        ) {
            shadcnBadge("Right Click Me", variant = ShadcnBadgeVariant.Secondary)
        }

        shadcnDrawer(
            id = "ui-showcase-drawer",
            expanded = drawerState.expanded,
            onDismissRequest = { drawerState.close() },
            header = { text("Slideover Drawer") }
        ) {
            shadcnBodyText("This is a bottom slideover drawer modal panel.")
            if (
                shadcnButton(
                    id = "ui-showcase-drawer-close",
                    label = "Close Drawer",
                    modifier = Modifier.width(100f.dp).height(32f.dp),
                    variant = ShadcnButtonVariant.Outline
                )
            ) {
                drawerState.close()
            }
        }
    }
    spacer(Modifier.height(4f.dp))
    shadcnSupportingText(feedbackMessage)

    // The plain shadcnDialog: freeform header/content/actions slots, distinct from
    // shadcnAlertDialog's fixed title+message+confirm/dismiss composition below.
    val infoDialogPopup = shadcnDialog(
        id = "ui-showcase-info-dialog",
        expanded = infoDialogState.expanded,
        width = Dimension.Fixed(320f.dp),
        header = {
            text("Scene info", style = Style { textSize(theme.typography.title) }, wrap = UiTextWrap.Word)
        },
        actions = {
            if (
                shadcnButton(
                    id = "ui-showcase-info-dialog-close",
                    label = "Close",
                    modifier = Modifier.width(88f.dp).height(32f.dp),
                    variant = ShadcnButtonVariant.Outline
                )
            ) {
                infoDialogState.close()
            }
        }
    ) { _ ->
        shadcnBodyText("shadcnDialog is the plain content-based dialog -- shadcnAlertDialog is composed from it with a fixed title/message/confirm-cancel shape.")
    }
    if (infoDialogPopup.dismissed) {
        infoDialogState.close()
    }

    val dialogResult = shadcnAlertDialog(
        id = "ui-showcase-delete-dialog",
        expanded = deleteDialogState.expanded,
        title = "Delete showcase card?",
        message = "This sample does not really delete anything. It exists to prove the alert dialog composition and confirm or dismiss flow.",
        confirmLabel = "Delete",
        confirmVariant = ShadcnButtonVariant.Danger,
        confirmStyle = theme.colors.destructiveStyle()
    )
    when (dialogResult.action) {
        UiAlertDialogAction.Confirm -> {
            feedbackMessage = "Confirmed from the alert dialog."
            deleteDialogState.close()
        }
        UiAlertDialogAction.Dismiss -> {
            feedbackMessage = "Dismissed from the alert dialog."
            deleteDialogState.close()
        }
        null -> {
            if (dialogResult.popup.dismissed) {
                feedbackMessage = "Dismissed by clicking outside the alert dialog."
                deleteDialogState.close()
            }
        }
    }
}

internal fun ColumnScope.drawUiShowcaseTooltipPreview() {
    shadcnSupportingText("Tooltips stay small and contextual: anchored to a trigger, wrapped inside a surfaced popup, and dismissible without changing the surrounding layout.")
    spacer(Modifier.height(8f.dp))
    row( horizontalArrangement = Arrangement.spacedBy(12f.dp), modifier = Modifier.height(36f.dp.toDimension())) {
        val trigger = buttonSlot(
            id = "showcase-tooltip-trigger",
            label = "Scene info",
            modifier = Modifier.width(132f.dp).height(36f.dp),
            style = theme.components.button
        )
        val visibility by rememberStateValue("showcase-tooltip-visible") { true }
        shadcnTooltip(
            anchorSlot = trigger.slot,
            visible = visibility,
            width = Dimension.Fixed(260f.dp),
            positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp)
        ) {
            text(
                label = "Frame pacing, draw calls, and scene counters can live in a tooltip without forcing a dedicated panel.",
                wrap = UiTextWrap.Word,
                overflow = UiTextOverflow.Ellipsis,
                maxLines = 3
            )
        }
        shadcnButton(
            id = "showcase-tooltip-reference",
            label = "Reference",
            modifier = Modifier.width(120f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Secondary
        )
    }
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("The preview suite keeps an open-state proof so tooltip width, wrap, and anchoring stay reviewable without hover automation.")
}

internal fun ColumnScope.drawUiShowcasePopoverPreview() {
    val popoverState = context.rememberPopupState("ui-showcase-popover")

    shadcnSupportingText("Popover owns anchored positioning, dismiss, and panel chrome; the caller renders its own trigger and expanded state, same split as the dropdown menu.")
    spacer(Modifier.height(8f.dp))
    row(horizontalArrangement = Arrangement.spacedBy(12f.dp), modifier = Modifier.height(36f.dp.toDimension())) {
        val trigger = buttonSlot(
            id = "ui-showcase-popover-trigger",
            label = "Share",
            modifier = Modifier.width(120f.dp).height(36f.dp),
            style = theme.components.button,
            variant = UiButtonVariant.Filled
        )
        if (trigger.clicked) {
            popoverState.toggle()
        }
        val popoverResult = shadcnPopover(
            id = "ui-showcase-popover",
            anchorSlot = trigger.slot,
            expanded = popoverState.expanded,
            width = Dimension.Fixed(280f.dp)
        ) {
            text(
                label = "Share scene",
                wrap = UiTextWrap.Word,
                overflow = UiTextOverflow.Ellipsis,
                maxLines = 1,
                style = Style {
                    foreground(theme.colors.foreground)
                    textSize(theme.typography.body)
                }
            )
            shadcnSupportingText("Anyone with the link can view this scene until you revoke it.")
        }
        if (popoverResult.dismissed) {
            popoverState.close()
        }
        shadcnButton(
            id = "ui-showcase-popover-reference",
            label = "Reference",
            modifier = Modifier.width(120f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Secondary
        )
    }
}

private fun UiShowcaseCounterContract.Effect.toDebugLabel(): String = when (this) {
    is UiShowcaseCounterContract.Effect.MilestoneReached -> "Milestone reached at $count"
    UiShowcaseCounterContract.Effect.ResetCompleted -> "Counter reset"
}
