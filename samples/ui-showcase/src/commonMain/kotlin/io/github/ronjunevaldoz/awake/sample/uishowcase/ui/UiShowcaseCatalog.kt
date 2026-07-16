// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseCounterContract
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlertDialogAction
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.UiDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.alertDialog
import io.github.ronjunevaldoz.awake.ui.animateFloat
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertyDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertySlider
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertyToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.dropdownMenu
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.offset
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.supportingLines

private val ShowcaseBadgeOptions = listOf("Primary", "Secondary", "Outline", "Danger")
private val ShowcaseActionMenuItems = listOf(
    UiDropdownMenuItem(
        label = "Pinned action",
        enabled = false,
        supportingText = "Disabled actions stay visible without becoming clickable."
    ),
    UiDropdownMenuSeparator,
    UiDropdownMenuItem(
        label = "Duplicate panel",
        trailingLabel = "Cmd+D",
        supportingText = "Example of a richer menu row with trailing metadata."
    ),
    UiDropdownMenuItem(
        label = "Delete scene",
        destructive = true,
        trailingLabel = "Del",
        supportingText = "Routes into the alert dialog flow instead of doing anything immediately."
    )
)

private enum class ShowcaseSection(
    val id: String,
    val navLabel: String,
    val title: String,
    val description: String
) {
    Overview(
        id = "overview",
        navLabel = "Overview",
        title = "Awake Shadcn Catalog",
        description = "A dedicated UI sample for owned Awake components, layout patterns, and state examples."
    ),
    Buttons(
        id = "buttons",
        navLabel = "Buttons",
        title = "Buttons And Badges",
        description = "Primary actions, muted actions, destructive affordances, and compact status chips."
    ),
    Controls(
        id = "controls",
        navLabel = "Controls",
        title = "Controls And Preview",
        description = "Property controls feeding a live themed preview so the token layer is visible immediately."
    ),
    Counter(
        id = "counter",
        navLabel = "MVI Counter",
        title = "State Container Sample",
        description = "A tiny MVI sample using the showcase counter store, reducer-driven state, and one-shot effects."
    ),
    Popups(
        id = "popups",
        navLabel = "Popups",
        title = "Popup Layers",
        description = "Dropdowns and alert dialogs wired through the shared popup state hooks and DSL layer."
    ),
    Notes(
        id = "notes",
        navLabel = "Notes",
        title = "Why This Sample Exists",
        description = "A clean place to prove the design system without forcing starter-game to behave like docs."
    );

    companion object {
        fun fromId(id: String): ShowcaseSection = entries.firstOrNull { it.id == id } ?: Overview
    }
}

internal fun UiColumnDslScope.drawUiShowcaseSidebar(compact: Boolean) {
    awakeShadcnBadge("SHADCN", variant = AwakeShadcnBadgeVariant.Primary)
    text("Awake UI Showcase")
    awakeShadcnSupportingText(
        if (compact) {
            "A focused component catalog with section navigation and live proofs."
        } else {
            "A dedicated sample module for the Awake shadcn-inspired theme, component recipes, and interactive examples."
        }
    )
    spacer(12f)
    drawUiShowcaseMenu()
    spacer(12f)
    supportingLines(
        listOf(
            "Dedicated module instead of a route inside starter-game.",
            "Sidebar and content panes use authored surfaces so the theme reads clearly.",
            "Use this sample for visual polish and future component proofs."
        )
    )
}

internal fun UiColumnDslScope.drawUiShowcasePageContent(
    state: UiShowcaseRuntimeState,
    showInlineMenu: Boolean
) {
    val selectedSection = context.rememberStateValue("ui-showcase-page", "section") {
        ShowcaseSection.Overview.id
    }
    val section = ShowcaseSection.fromId(selectedSection.value)

    awakeShadcnSectionHeader(
        title = section.title,
        description = section.description
    )
    spacer(8f)
    if (showInlineMenu) {
        drawUiShowcaseMenu(
            compact = true,
            selected = section,
            onSelect = { selectedSection.value = it.id }
        )
        spacer(12f)
    }
    when (section) {
        ShowcaseSection.Overview -> drawUiShowcaseOverview()
        ShowcaseSection.Buttons -> drawUiShowcaseButtonsAndBadges()
        ShowcaseSection.Controls -> drawUiShowcaseControlsAndPreview(state)
        ShowcaseSection.Counter -> drawUiShowcaseCounterMviSample(state)
        ShowcaseSection.Popups -> drawUiShowcasePopupSample()
        ShowcaseSection.Notes -> drawUiShowcaseNotes()
    }
}

private fun UiColumnDslScope.drawUiShowcaseMenu() {
    val selectedSection = context.rememberStateValue("ui-showcase-page", "section") {
        ShowcaseSection.Overview.id
    }
    drawUiShowcaseMenu(
        compact = false,
        selected = ShowcaseSection.fromId(selectedSection.value),
        onSelect = { selectedSection.value = it.id }
    )
}

private fun UiColumnDslScope.drawUiShowcaseMenu(
    compact: Boolean,
    selected: ShowcaseSection,
    onSelect: (ShowcaseSection) -> Unit
) {
    if (!compact) {
        awakeShadcnSectionHeader(
            title = "Components",
            description = "Navigate the catalog one section at a time."
        )
    }
    ShowcaseSection.entries.forEach { section ->
        if (
            awakeShadcnButton(
                id = "ui-showcase-section-${section.id}",
                width = 0f,
                height = 34f,
                label = section.navLabel,
                modifier = UiModifier(width = Dimension.FillMax),
                variant = if (section == selected) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Ghost
            )
        ) {
            onSelect(section)
        }
    }
}

private fun UiColumnDslScope.drawUiShowcaseOverview() {
    awakeShadcnSurface(
        id = "ui-showcase-overview",
        height = Dimension.WrapContent,
        variant = AwakeShadcnSurfaceVariant.Muted,
        style = Style { shape(14f.dp) }
    ) {
        awakeShadcnBadge("SHOWCASE", variant = AwakeShadcnBadgeVariant.Secondary)
        text("Dedicated sample route")
        awakeShadcnSupportingText("This sample exists so the design system can be judged on its own page shell rather than through starter-game chrome.")
        spacer(8f)
        supportingLines(
            listOf(
                "The starter sample goes back to being a starter sample.",
                "The dedicated showcase owns layout experiments, visual regression targets, and future component tutorials.",
                "Sidebar and content surfaces intentionally use different treatment so the page does not collapse into one flat gray slab."
            )
        )
    }
}

private fun UiColumnDslScope.drawUiShowcaseButtonsAndBadges() {
    awakeShadcnSurface(
        id = "ui-showcase-buttons",
        height = Dimension.WrapContent,
        variant = AwakeShadcnSurfaceVariant.Muted,
        style = Style { shape(14f.dp) }
    ) {
        awakeShadcnSectionTitle("Buttons")
        row(height = 36f, gap = 8f) {
            awakeShadcnButton("showcase-primary", 120f, 36f, "Primary", variant = AwakeShadcnButtonVariant.Primary)
            awakeShadcnButton("showcase-secondary", 120f, 36f, "Secondary", variant = AwakeShadcnButtonVariant.Secondary)
        }
        row(height = 36f, gap = 8f) {
            awakeShadcnButton("showcase-outline", 110f, 36f, "Outline", variant = AwakeShadcnButtonVariant.Outline)
            awakeShadcnButton("showcase-ghost", 98f, 36f, "Ghost", variant = AwakeShadcnButtonVariant.Ghost)
            awakeShadcnButton("showcase-danger", 102f, 36f, "Danger", variant = AwakeShadcnButtonVariant.Danger)
        }
        spacer(10f)
        awakeShadcnSectionTitle("Badges")
        row(height = 30f, gap = 8f) {
            awakeShadcnBadge("LIVE", variant = AwakeShadcnBadgeVariant.Primary)
            awakeShadcnBadge("SCENE", variant = AwakeShadcnBadgeVariant.Secondary)
            awakeShadcnBadge("BETA", variant = AwakeShadcnBadgeVariant.Outline)
            awakeShadcnBadge("RISK", variant = AwakeShadcnBadgeVariant.Danger)
        }
    }
}

private fun UiColumnDslScope.drawUiShowcaseControlsAndPreview(state: UiShowcaseRuntimeState) {
    awakeShadcnSurface(
        id = "ui-showcase-controls",
        height = Dimension.WrapContent,
        variant = AwakeShadcnSurfaceVariant.Muted,
        style = Style { shape(14f.dp) }
    ) {
        awakeShadcnSectionTitle("Controls")
        val nextLive = awakeShadcnPropertyToggle(
            id = "showcase-live",
            label = "Live badge",
            checked = state.showcaseLiveBadge
        )
        if (nextLive != state.showcaseLiveBadge) state.showcaseLiveBadge = nextLive

        val nextDanger = awakeShadcnPropertyToggle(
            id = "showcase-danger-mode",
            label = "Danger mode",
            checked = state.showcaseDangerMode
        )
        if (nextDanger != state.showcaseDangerMode) state.showcaseDangerMode = nextDanger

        awakeShadcnPropertyDropdown(
            id = "showcase-badge-variant",
            label = "Badge",
            options = ShowcaseBadgeOptions,
            selectedIndex = state.showcaseBadgeVariantIndex
        )?.let { state.showcaseBadgeVariantIndex = it }

        state.showcaseSurfaceRadius = awakeShadcnPropertySlider(
            id = "showcase-radius",
            label = "Radius",
            min = 8f,
            max = 24f,
            value = state.showcaseSurfaceRadius
        )

        spacer(10f)
        awakeShadcnSectionTitle("Preview")
        val previewLift = context.animateFloat(
            id = "showcase-preview-lift",
            target = if (state.showcaseDangerMode) 10f else 0f,
            responsiveness = 10f
        )
        awakeShadcnSurface(
            id = "showcase-preview",
            height = Dimension.WrapContent,
            modifier = UiModifier().offset(y = (-previewLift).dp),
            style = Style { shape(state.showcaseSurfaceRadius.dp) }
        ) {
            val badgeVariant = state.showcaseBadgeVariant()
            awakeShadcnBadge(if (state.showcaseLiveBadge) "LIVE" else "PAUSED", variant = badgeVariant)
            text("Showcase preview card")
            awakeShadcnSupportingText("Controls, buttons, and content all sit on the same public Awake UI layer.")
            spacer(6f)
            row(height = 36f, gap = 8f) {
                if (
                    awakeShadcnButton(
                        id = "preview-primary-action",
                        width = 112f,
                        height = 36f,
                        label = "Inspect",
                        variant = AwakeShadcnButtonVariant.Primary
                    )
                ) {
                    state.showcasePrimaryClicks += 1
                }
                awakeShadcnButton(
                    id = "preview-secondary-action",
                    width = 118f,
                    height = 36f,
                    label = if (state.showcaseDangerMode) "Rollback" else "Publish",
                    variant = if (state.showcaseDangerMode) AwakeShadcnButtonVariant.Danger else AwakeShadcnButtonVariant.Outline
                )
            }
            text("Primary clicks: ${state.showcasePrimaryClicks}")
        }
    }
}

private fun UiColumnDslScope.drawUiShowcaseCounterMviSample(state: UiShowcaseRuntimeState) {
    state.counterStore.drainEffects()
        .lastOrNull()
        ?.let { effect -> state.showcaseCounterEffectMessage = effect.toDebugLabel() }

    val counterState = state.counterStore.state.value

    awakeShadcnSurface(
        id = "ui-showcase-counter",
        height = Dimension.WrapContent,
        variant = AwakeShadcnSurfaceVariant.Muted,
        style = Style { shape(14f.dp) }
    ) {
        awakeShadcnSectionHeader(
            title = "MVI Counter",
            description = "A tiny Awake-native example: sealed intents drive a reducer-backed state flow, and one-shot effects stay off persistent state."
        )
        awakeShadcnBadge("MVI", variant = AwakeShadcnBadgeVariant.Primary)
        text("Counter Contract")
        awakeShadcnSupportingText("A good default pattern once demos need async actions, screen state, and effect channels.")
        spacer(4f)
        text("Count: ${counterState.count}")
        awakeShadcnSupportingText("Last effect: ${state.showcaseCounterEffectMessage ?: "None"}")
        spacer(6f)
        row(height = 36f, gap = 8f) {
            if (
                awakeShadcnButton(
                    id = "counter-decrement",
                    width = 108f,
                    height = 36f,
                    label = "Decrement",
                    variant = AwakeShadcnButtonVariant.Outline
                )
            ) {
                state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Decrement)
            }
            if (
                awakeShadcnButton(
                    id = "counter-increment",
                    width = 108f,
                    height = 36f,
                    label = "Increment",
                    variant = AwakeShadcnButtonVariant.Primary
                )
            ) {
                state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Increment)
            }
        }
        row(height = 36f, gap = 8f) {
            if (
                awakeShadcnButton(
                    id = "counter-reset",
                    width = 108f,
                    height = 36f,
                    label = "Reset",
                    variant = AwakeShadcnButtonVariant.Ghost
                )
            ) {
                state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Reset)
            }
            awakeShadcnBadge(
                label = if (counterState.count >= 0) "FLOW" else "NEGATIVE",
                variant = if (counterState.count >= 0) AwakeShadcnBadgeVariant.Secondary else AwakeShadcnBadgeVariant.Danger
            )
        }
    }
}

private fun UiColumnDslScope.drawUiShowcasePopupSample() {
    val actionMenuState = context.rememberPopupState("ui-showcase-action-menu")
    val deleteDialogState = context.rememberPopupState("ui-showcase-delete-dialog")
    val feedbackMessage = context.rememberStateValue("ui-showcase-popup-feedback") {
        "Try the action menu and the dialog to inspect the popup layer."
    }

    awakeShadcnSurface(
        id = "ui-showcase-popups",
        height = Dimension.WrapContent,
        variant = AwakeShadcnSurfaceVariant.Muted,
        style = Style { shape(14f.dp) }
    ) {
        awakeShadcnSectionHeader(
            title = "Popup Components",
            description = "Menu and dialog proofs running through the shared DSL surface."
        )
        awakeShadcnBadge("OVERLAY", variant = AwakeShadcnBadgeVariant.Outline)
        text("Menu + Dialog")
        awakeShadcnSupportingText("The button below anchors a richer dropdown menu with separators, supporting copy, and destructive actions.")
        spacer(6f)
        row(height = 36f, gap = 8f) {
            val menuTrigger = buttonSlot(
                id = "ui-showcase-menu-trigger",
                label = "Actions",
                width = 112f,
                height = 36f,
                style = AwakeShadcnStyles.button(AwakeShadcnButtonVariant.Secondary),
                variant = io.github.ronjunevaldoz.awake.ui.UiButtonVariant.Filled
            )
            if (menuTrigger.clicked) {
                actionMenuState.toggle()
            }
            val menuResult = dropdownMenu(
                id = "ui-showcase-action-menu",
                anchorSlot = menuTrigger.slot,
                expanded = actionMenuState.expanded,
                items = ShowcaseActionMenuItems,
                style = Style { contentPadding(0f.dp) }
            )
            when (menuResult.selectedIndex) {
                1 -> {
                    feedbackMessage.value = "Duplicate panel queued from the dropdown menu."
                    actionMenuState.close()
                }
                2 -> {
                    feedbackMessage.value = "Delete requested from the dropdown menu."
                    actionMenuState.close()
                    deleteDialogState.open()
                }
            }
            if (menuResult.dismissed) {
                actionMenuState.close()
            }
            if (
                awakeShadcnButton(
                    id = "ui-showcase-delete-trigger",
                    width = 132f,
                    height = 36f,
                    label = "Open Dialog",
                    variant = AwakeShadcnButtonVariant.Outline
                )
            ) {
                deleteDialogState.open()
            }
        }
        spacer(4f)
        awakeShadcnSupportingText(feedbackMessage.value)
    }

    val dialogResult = alertDialog(
        id = "ui-showcase-delete-dialog",
        expanded = deleteDialogState.expanded,
        title = "Delete showcase card?",
        message = "This sample does not really delete anything. It exists to prove the alert dialog composition and confirm or dismiss flow."
    )
    when (dialogResult.action) {
        UiAlertDialogAction.Confirm -> {
            feedbackMessage.value = "Confirmed from the alert dialog."
            deleteDialogState.close()
        }
        UiAlertDialogAction.Dismiss -> {
            feedbackMessage.value = "Dismissed from the alert dialog."
            deleteDialogState.close()
        }
        null -> {
            if (dialogResult.popup.dismissed) {
                feedbackMessage.value = "Dismissed by clicking outside the alert dialog."
                deleteDialogState.close()
            }
        }
    }
}

private fun UiColumnDslScope.drawUiShowcaseNotes() {
    awakeShadcnSurface(
        id = "ui-showcase-notes",
        height = Dimension.WrapContent,
        variant = AwakeShadcnSurfaceVariant.Muted,
        style = Style { shape(14f.dp) }
    ) {
        supportingLines(
            listOf(
                "The design system lives in ui-designsystem; this sample is only authored usage.",
                "The old starter-game route was functional, but it mixed starter scaffolding with showcase concerns.",
                "This dedicated module is now the right place for future component docs, screenshots, and visual regression targets."
            )
        )
    }
}

private fun UiShowcaseRuntimeState.showcaseBadgeVariant(): AwakeShadcnBadgeVariant = when (showcaseBadgeVariantIndex) {
    0 -> AwakeShadcnBadgeVariant.Primary
    1 -> AwakeShadcnBadgeVariant.Secondary
    2 -> AwakeShadcnBadgeVariant.Outline
    else -> AwakeShadcnBadgeVariant.Danger
}

private fun UiShowcaseCounterContract.Effect.toDebugLabel(): String = when (this) {
    is UiShowcaseCounterContract.Effect.MilestoneReached -> "Milestone reached at $count"
    UiShowcaseCounterContract.Effect.ResetCompleted -> "Counter reset"
}
