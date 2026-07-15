// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.ui

import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.propertyDropdown
import io.github.ronjunevaldoz.awake.ui.propertySlider
import io.github.ronjunevaldoz.awake.ui.propertyToggle
import io.github.ronjunevaldoz.awake.ui.sectionTitle
import io.github.ronjunevaldoz.awake.ui.shellPane
import io.github.ronjunevaldoz.awake.ui.textLines
import io.github.ronjunevaldoz.awake.ui.toDimension

private val ShowcaseBadgeOptions = listOf("Primary", "Secondary", "Outline", "Danger")

internal fun GameUiRuntime.drawStarterShadcnShowcase(slot: UiSlot, state: StarterGameRuntimeState) {
    shellPane(
        slot = slot,
        id = "starter-shadcn-showcase",
        theme = AwakeShadcnTheme,
        gap = 12f,
        insets = UiInsets(16f.dp),
        radius = 12f.dp,
        borderWidth = 1f.dp,
        style = Style {
            shape(12f.dp)
        }
    ) {
        text("Awake Shadcn Showcase")
        text("Owned, Awake-native styling layered on top of the same core widgets and layout primitives.")

        spacer(6f)
        sectionTitle("Buttons")
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
        sectionTitle("Badges")
        row(height = 30f, gap = 8f) {
            awakeShadcnBadge("LIVE", variant = AwakeShadcnBadgeVariant.Primary)
            awakeShadcnBadge("SCENE", variant = AwakeShadcnBadgeVariant.Secondary)
            awakeShadcnBadge("BETA", variant = AwakeShadcnBadgeVariant.Outline)
            awakeShadcnBadge("RISK", variant = AwakeShadcnBadgeVariant.Danger)
        }

        spacer(2f)
        sectionTitle("Controls")
        val nextLive = propertyToggle(
            id = "showcase-live",
            label = "Live badge",
            checked = state.showcaseLiveBadge,
            height = 28f,
            style = Style {
                background(AwakeShadcnTheme.tokens.secondary)
                borderWidth(1f.dp)
                borderColor(AwakeShadcnTheme.tokens.border)
                shape(999f.dp)
            }
        )
        if (nextLive != state.showcaseLiveBadge) state.showcaseLiveBadge = nextLive

        val nextDanger = propertyToggle(
            id = "showcase-danger-mode",
            label = "Danger mode",
            checked = state.showcaseDangerMode,
            height = 28f,
            style = Style {
                background(AwakeShadcnTheme.tokens.secondary)
                borderWidth(1f.dp)
                borderColor(AwakeShadcnTheme.tokens.border)
                shape(999f.dp)
            }
        )
        if (nextDanger != state.showcaseDangerMode) state.showcaseDangerMode = nextDanger

        propertyDropdown(
            id = "showcase-badge-variant",
            label = "Badge",
            options = ShowcaseBadgeOptions,
            selectedIndex = state.showcaseBadgeVariantIndex,
            style = Style {
                background(AwakeShadcnTheme.tokens.secondary)
                foreground(AwakeShadcnTheme.tokens.foreground)
                borderWidth(1f.dp)
                borderColor(AwakeShadcnTheme.tokens.border)
                shape(6f.dp)
            }
        )?.let { state.showcaseBadgeVariantIndex = it }

        state.showcaseSurfaceRadius = propertySlider(
            id = "showcase-radius",
            label = "Radius",
            min = 8f,
            max = 24f,
            value = state.showcaseSurfaceRadius,
            style = Style {
                background(AwakeShadcnTheme.tokens.muted)
                foreground(AwakeShadcnTheme.tokens.foreground)
            }
        )

        spacer(2f)
        sectionTitle("Preview")
        awakeShadcnSurface(
            id = "showcase-preview",
            height = 166f.toDimension(),
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
            text("Buttons, badges, dropdowns, and sliders can all share the same branded token layer.")
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
        sectionTitle("Why It Matters")
        textLines(
            listOf(
                "The sample still uses the shared scene router and platform-neutral game shell.",
                "The branded layer stays outside ui-core, but it composes on the same public widget stack.",
                "This scene is the living reference for future design-system polish."
            )
        )
    }
}

private fun StarterGameRuntimeState.showcaseBadgeVariant(): AwakeShadcnBadgeVariant = when (showcaseBadgeVariantIndex) {
    0 -> AwakeShadcnBadgeVariant.Primary
    1 -> AwakeShadcnBadgeVariant.Secondary
    2 -> AwakeShadcnBadgeVariant.Outline
    else -> AwakeShadcnBadgeVariant.Danger
}
