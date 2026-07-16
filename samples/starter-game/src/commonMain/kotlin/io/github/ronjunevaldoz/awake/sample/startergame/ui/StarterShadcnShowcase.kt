// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.ui

import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterCounterContract
import io.github.ronjunevaldoz.awake.ui.UiAlertDialogAction
import io.github.ronjunevaldoz.awake.ui.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.UiDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.alertDialog
import io.github.ronjunevaldoz.awake.ui.animateFloat
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnButtonVariant
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
import io.github.ronjunevaldoz.awake.ui.shellPane
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

internal fun GameUiRuntime.drawStarterShadcnShowcase(slot: UiSlot, state: StarterGameRuntimeState) {
    shellPane(
        slot = slot,
        id = "starter-shadcn-showcase",
        gap = 12f,
        insets = UiInsets(16f.dp),
        radius = 12f.dp,
        borderWidth = 1f.dp,
        style = Style {
            shape(12f.dp)
        }
    ) {
        drawStarterShadcnShowcaseContent(state)
    }
}

internal fun UiColumnDslScope.drawStarterShadcnShowcaseContent(state: StarterGameRuntimeState) {
    text("Awake Component Showcase")
    awakeShadcnSupportingText("Owned, Awake-native styling layered on top of the same core widgets, now including popup menus and dialog composition.")

    spacer(6f)
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

    spacer(2f)
    awakeShadcnSectionTitle("Badges")
    row(height = 30f, gap = 8f) {
        awakeShadcnBadge("LIVE", variant = AwakeShadcnBadgeVariant.Primary)
        awakeShadcnBadge("SCENE", variant = AwakeShadcnBadgeVariant.Secondary)
        awakeShadcnBadge("BETA", variant = AwakeShadcnBadgeVariant.Outline)
        awakeShadcnBadge("RISK", variant = AwakeShadcnBadgeVariant.Danger)
    }

    spacer(2f)
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

    spacer(2f)
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
        style = Style {
            shape(state.showcaseSurfaceRadius.dp)
        }
    ) {
        val badgeVariant = state.showcaseBadgeVariant()
        awakeShadcnBadge(
            if (state.showcaseLiveBadge) "LIVE" else "PAUSED",
            variant = badgeVariant
        )
        text("Starter shell card")
        awakeShadcnSupportingText("Buttons, badges, dropdowns, and sliders can all share the same branded token layer.")
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

    spacer(2f)
    drawStarterCounterMviSample(state)

    spacer(2f)
    drawStarterPopupComponentShowcase()

    spacer(2f)
    awakeShadcnSectionTitle("Why It Matters")
    supportingLines(
        listOf(
            "The sample still uses the shared scene router and platform-neutral game shell.",
            "The branded layer stays outside ui-core, but it composes on the same public widget stack.",
            "The counter sample shows MVI with sealed intents, StateFlow state, and one-shot effects.",
            "Popup menus and dialogs are now exercised in the showcase scene instead of living only in tests."
        )
    )
}

private fun UiColumnDslScope.drawStarterCounterMviSample(state: StarterGameRuntimeState) {
    state.counterStore.drainEffects()
        .lastOrNull()
        ?.let { effect -> state.showcaseCounterEffectMessage = effect.toDebugLabel() }

    val counterState = state.counterStore.state.value

    awakeShadcnSectionHeader(
        title = "MVI Counter",
        description = "A tiny Awake-native example: sealed intents drive a reducer-backed StateFlow, and one-shot effects stay off the persistent state."
    )

    awakeShadcnSurface(
        id = "showcase-counter-mvi",
        height = Dimension.WrapContent,
        style = Style {
            shape(10f.dp)
        }
    ) {
        awakeShadcnBadge("MVI", variant = AwakeShadcnBadgeVariant.Primary)
        text("Counter Contract")
        awakeShadcnSupportingText("Good fit for upcoming demos once the UI has async actions, screen state, and one-shot events.")
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
                state.counterStore.dispatch(StarterCounterContract.Intent.Decrement)
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
                state.counterStore.dispatch(StarterCounterContract.Intent.Increment)
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
                state.counterStore.dispatch(StarterCounterContract.Intent.Reset)
            }
            awakeShadcnBadge(
                label = if (counterState.count >= 0) "FLOW" else "NEGATIVE",
                variant = if (counterState.count >= 0) AwakeShadcnBadgeVariant.Secondary else AwakeShadcnBadgeVariant.Danger
            )
        }
    }
}

private fun UiColumnDslScope.drawStarterPopupComponentShowcase() {
    val actionMenuState = context.rememberPopupState("showcase-action-menu")
    val deleteDialogState = context.rememberPopupState("showcase-delete-dialog")
    val feedbackMessage = context.rememberStateValue("showcase-popup-feedback") {
        "Try the action menu and the dialog to inspect the new popup layer."
    }

    awakeShadcnSectionHeader(
        title = "Popup Components",
        description = "Dedicated sample wiring for the new popup/menu/dialog layer, all driven through the shared DSL surface."
    )

    awakeShadcnSurface(
        id = "showcase-popup-components",
        height = Dimension.WrapContent,
        style = Style {
            shape(10f.dp)
        }
    ) {
        awakeShadcnBadge("OVERLAY", variant = AwakeShadcnBadgeVariant.Outline)
        text("Menu + Dialog")
        awakeShadcnSupportingText("The button below anchors a richer dropdown menu with separators, supporting copy, and destructive actions.")
        spacer(6f)
        row(height = 36f, gap = 8f) {
            val menuTrigger = buttonSlot(
                id = "showcase-menu-trigger",
                label = "Actions",
                width = 112f,
                height = 36f,
                style = Style.Empty,
                variant = AwakeShadcnButtonVariant.Secondary.toUiButtonVariant()
            )
            if (menuTrigger.clicked) {
                actionMenuState.toggle()
            }
            val menuResult = dropdownMenu(
                id = "showcase-action-menu",
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
                    id = "showcase-delete-trigger",
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
        id = "showcase-delete-dialog",
        expanded = deleteDialogState.expanded,
        title = "Delete showcase scene?",
        message = "This sample does not really delete anything. It exists to prove the alert dialog composition and confirm/dismiss flow."
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

private fun StarterGameRuntimeState.showcaseBadgeVariant(): AwakeShadcnBadgeVariant = when (showcaseBadgeVariantIndex) {
    0 -> AwakeShadcnBadgeVariant.Primary
    1 -> AwakeShadcnBadgeVariant.Secondary
    2 -> AwakeShadcnBadgeVariant.Outline
    else -> AwakeShadcnBadgeVariant.Danger
}

private fun AwakeShadcnButtonVariant.toUiButtonVariant() = when (this) {
    AwakeShadcnButtonVariant.Primary,
    AwakeShadcnButtonVariant.Secondary,
    AwakeShadcnButtonVariant.Danger -> io.github.ronjunevaldoz.awake.ui.UiButtonVariant.Filled
    AwakeShadcnButtonVariant.Outline -> io.github.ronjunevaldoz.awake.ui.UiButtonVariant.Outline
    AwakeShadcnButtonVariant.Ghost -> io.github.ronjunevaldoz.awake.ui.UiButtonVariant.Ghost
}

private fun StarterCounterContract.Effect.toDebugLabel(): String = when (this) {
    is StarterCounterContract.Effect.MilestoneReached -> "Milestone reached at $count"
    StarterCounterContract.Effect.ResetCompleted -> "Counter reset"
}
