// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.animateFloat
import io.github.ronjunevaldoz.awake.ui.core.graphics.gradientBorder
import io.github.ronjunevaldoz.awake.ui.core.graphics.gradientRect
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnAccent
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.awakeShadcnPropertyDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.awakeShadcnPropertySlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.awakeShadcnPropertyToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.offset
import io.github.ronjunevaldoz.awake.ui.rememberBooleanState
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.width

internal fun ColumnScope.drawUiShowcaseOverviewPreview() {
    awakeShadcnBadge("SHOWCASE", variant = AwakeShadcnBadgeVariant.Secondary)
    awakeShadcnBodyText("Dedicated sample route")
    awakeShadcnSupportingText("This page shell exists so the design system is judged as a product surface, not just as loose demo widgets.")
    spacer(UiModifier().height(8f.dp))
    supportingLines(
        listOf(
            "Stable chrome on top, grouped navigation on the left, one detail page in the content pane.",
            "The starter sample stays a starter sample; docs and polish move here.",
            "This is now the right home for future design-system tutorials and regression proofs."
        )
    )
}

internal fun ColumnScope.drawUiShowcaseReferenceComparisonPreview() {
    val cardWidth = 220f
    row(height = Dimension.WrapContent, horizontalArrangement = Arrangement.spacedBy(12f.dp)) {
        surface(
            id = "ui-showcase-reference-spec",
            width = Dimension.Fixed(cardWidth.dp),
            height = Dimension.Fixed(284f.dp),
            style = theme.components.surface then Style { shape(14f.dp) }
        ) {
            awakeShadcnSectionTitle("Official cues")
            awakeShadcnSupportingText("The reference we keep checking against.")
            spacer(UiModifier().height(8f.dp))
            supportingLines(
                listOf(
                    "Controls feel closer to 36px than 44px.",
                    "Dropdown content is a popover, not a bare button stack.",
                    "Cards sit close to the page background with restrained contrast."
                )
            )
            spacer(UiModifier().height(8f.dp))
            awakeShadcnBadge("TARGET", variant = AwakeShadcnBadgeVariant.Outline)
        }
        surface(
            id = "ui-showcase-reference-awake",
            width = Dimension.Fixed(cardWidth.dp),
            height = Dimension.Fixed(284f.dp),
            style = theme.components.surface then Style { shape(14f.dp) }
        ) {
            awakeShadcnSectionTitle("Awake now")
            awakeShadcnSupportingText("Our current implementation after the sizing and popover pass.")
            spacer(UiModifier().height(8f.dp))
            awakeShadcnSupportingText(
                "Typography is tighter, menu surfaces are contained, and the gray slab effect is reduced.",
                maxLines = 4
            )
            spacer(UiModifier().height(8f.dp))
            row(height = 36f.dp, horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
                awakeShadcnButton(
                    "reference-primary",
                    "Primary",
                    modifier = UiModifier().width(100f.dp).height(36f.dp),
                    variant = AwakeShadcnButtonVariant.Primary
                )
                awakeShadcnButton(
                    "reference-outline",
                    "Outline",
                    modifier = UiModifier().width(96f.dp).height(36f.dp),
                    variant = AwakeShadcnButtonVariant.Outline
                )
            }
            spacer(UiModifier().height(8f.dp))
            awakeShadcnBadge("AWAKE", variant = AwakeShadcnBadgeVariant.Primary)
        }
    }
}

internal fun ColumnScope.drawUiShowcaseControlsPreview(state: UiShowcaseRuntimeState) {
    awakeShadcnPropertyDropdown(
        id = "showcase-style-preset",
        label = "Style",
        options = ShowcaseStyleOptions,
        selectedIndex = state.showcaseStylePresetIndex,
        labelWidth = 72f.dp
    )?.let { state.showcaseStylePresetIndex = it }
    awakeShadcnPropertyDropdown(
        id = "showcase-base-color",
        label = "Base",
        options = ShowcaseBaseColorOptions,
        selectedIndex = state.showcaseBaseColorIndex,
        labelWidth = 72f.dp
    )?.let { state.showcaseBaseColorIndex = it }
    awakeShadcnPropertyDropdown(
        id = "showcase-theme-mode",
        label = "Theme",
        options = ShowcaseThemeModeOptions,
        selectedIndex = state.showcaseThemeModeIndex,
        labelWidth = 72f.dp
    )?.let { state.showcaseThemeModeIndex = it }
    awakeShadcnPropertyDropdown(
        id = "showcase-accent",
        label = "Accent",
        options = ShowcaseAccentOptions,
        selectedIndex = state.showcaseAccentIndex,
        labelWidth = 72f.dp
    )?.let { state.showcaseAccentIndex = it }
    awakeShadcnSupportingText(
        "Auto resolves to ${if (state.showcaseResolvedDarkMode()) "dark" else "light"} on this platform.",
        maxLines = 2
    )

    spacer(UiModifier().height(10f.dp))
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

    state.showcaseNotifyChecked = awakeShadcnCheckbox(
        id = "showcase-notify",
        checked = state.showcaseNotifyChecked,
        label = "Notify on publish"
    )

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

    spacer(UiModifier().height(10f.dp))
    val previewLift = animateFloat(
        id = "showcase-preview-lift",
        target = if (state.showcaseDangerMode) 10f else 0f,
        responsiveness = 10f
    )
    awakeShadcnSurface(
        id = "showcase-preview",
        height = Dimension.WrapContent,
        modifier = UiModifier().offset(y = (-previewLift).dp),
        variant = AwakeShadcnSurfaceVariant.Muted,
        style = Style { shape(state.showcaseSurfaceRadius.dp) }
    ) { previewSlot ->
        val shimmerForward = rememberBooleanState("showcase-preview-shimmer-direction", initial = true)
        val shimmerTarget = when {
            !state.showcaseLiveBadge -> 0f
            shimmerForward.value -> 1f
            else -> 0f
        }
        val shimmerPhase = animateFloat(
            id = "showcase-preview-shimmer",
            target = shimmerTarget,
            initial = 0f,
            responsiveness = 2.5f,
            snapDistance = 0.015f
        )
        if (state.showcaseLiveBadge) {
            if (shimmerForward.value && shimmerPhase >= 0.98f) shimmerForward.value = false
            if (!shimmerForward.value && shimmerPhase <= 0.02f) shimmerForward.value = true
        } else {
            shimmerForward.value = true
        }
        drawShowcaseGradientChrome(
            slot = previewSlot,
            shimmerPhase = shimmerPhase,
            dangerMode = state.showcaseDangerMode
        )
        val badgeVariant = state.showcaseBadgeVariant()
        awakeShadcnBadge(if (state.showcaseLiveBadge) "LIVE" else "PAUSED", variant = badgeVariant)
        row(height = 28f.dp, horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
            awakeShadcnBadge(
                label = state.showcaseStylePreset().label.uppercase(),
                variant = AwakeShadcnBadgeVariant.Outline
            )
            awakeShadcnBadge(
                label = state.showcaseBaseColor().label.uppercase(),
                variant = AwakeShadcnBadgeVariant.Secondary
            )
            awakeShadcnBadge(
                label = state.showcaseAccent().label.uppercase(),
                variant = if (state.showcaseAccent() == AwakeShadcnAccent.Base) {
                    AwakeShadcnBadgeVariant.Outline
                } else {
                    AwakeShadcnBadgeVariant.Primary
                }
            )
        }
        awakeShadcnBodyText("Showcase preview card")
        awakeShadcnSupportingText("Light is the default mood now, Auto follows the platform, and the sample chrome can carry gradients and shimmer without hardcoding per-demo paint.")
        spacer(UiModifier().height(6f.dp))
        row(height = 36f.dp, horizontalArrangement = Arrangement.spacedBy(10f.dp)) {
            if (
                awakeShadcnButton(
                    id = "preview-primary-action",
                    label = "Inspect",
                    modifier = UiModifier().width(112f.dp).height(36f.dp),
                    variant = AwakeShadcnButtonVariant.Primary
                )
            ) {
                state.showcasePrimaryClicks += 1
            }
            awakeShadcnButton(
                id = "preview-secondary-action",
                label = if (state.showcaseDangerMode) "Rollback" else "Publish",
                modifier = UiModifier().width(120f.dp).height(36f.dp),
                variant = if (state.showcaseDangerMode) AwakeShadcnButtonVariant.Danger else AwakeShadcnButtonVariant.Outline
            )
        }
        awakeShadcnBodyText("Primary clicks: ${state.showcasePrimaryClicks}")
    }
}

private fun ColumnScope.drawShowcaseGradientChrome(
    slot: UiSlot,
    shimmerPhase: Float,
    dangerMode: Boolean,
) {
    val themeGradient = UiLinearGradient.horizontal(
        start = lerpColor(theme.tokens.primary.withAlpha(0.12f), theme.tokens.accent.withAlpha(0.18f), shimmerPhase),
        end = lerpColor(theme.tokens.accent.withAlpha(0.22f), theme.tokens.secondary.withAlpha(0.12f), shimmerPhase)
    )
    val borderGradient = UiLinearGradient.horizontal(
        start = if (dangerMode) theme.tokens.destructive.withAlpha(0.92f) else theme.tokens.primary.withAlpha(0.64f),
        end = if (dangerMode) theme.tokens.accent.withAlpha(0.82f) else theme.tokens.accent.withAlpha(0.84f)
    )
    val shimmerWidth = (slot.width * 0.28f).coerceAtLeast(52f)
    val shimmerX = slot.x + (slot.width - shimmerWidth) * shimmerPhase.coerceIn(0f, 1f)
    context.createAbsolute(slot.x, slot.y, overlayOnly = true).apply {
        gradientBorder(slot, width = 1f.dp, gradient = borderGradient, overlay = true)
        gradientRect(
            UiSlot(slot.x, slot.y, slot.width, 44f.coerceAtMost(slot.height)),
            gradient = themeGradient,
            overlay = true
        )
        gradientRect(
            UiSlot(shimmerX, slot.y + 1f, shimmerWidth, (slot.height - 2f).coerceAtLeast(0f)),
            gradient = UiLinearGradient.horizontal(
                start = Color.Transparent,
                end = theme.tokens.foreground.withAlpha(if (dangerMode) 0.08f else 0.12f)
            ),
            overlay = true
        )
    }
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color = Color(
    r = start.r + (end.r - start.r) * fraction,
    g = start.g + (end.g - start.g) * fraction,
    b = start.b + (end.b - start.b) * fraction,
    a = start.a + (end.a - start.a) * fraction
)

private fun UiShowcaseRuntimeState.showcaseBadgeVariant() = when (showcaseBadgeVariantIndex) {
    0 -> AwakeShadcnBadgeVariant.Primary
    1 -> AwakeShadcnBadgeVariant.Secondary
    2 -> AwakeShadcnBadgeVariant.Outline
    else -> AwakeShadcnBadgeVariant.Danger
}
