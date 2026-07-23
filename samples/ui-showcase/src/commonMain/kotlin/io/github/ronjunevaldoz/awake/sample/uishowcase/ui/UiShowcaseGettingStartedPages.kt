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
import io.github.ronjunevaldoz.awake.ui.canvas
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
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
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.offset
import io.github.ronjunevaldoz.awake.ui.rememberBooleanState
import io.github.ronjunevaldoz.awake.ui.shadcnShimmer
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
    awakeShadcnSupportingText("This page proves that the Awake theme factory can re-skin the entire component library live, including custom canvas chrome.")
    spacer(UiModifier().height(16f.dp))

    row(height = Dimension.WrapContent, horizontalArrangement = Arrangement.spacedBy(24f.dp)) {
        // --- Settings Column ---
        surface(
            id = "showcase-theme-settings",
            width = Dimension.Fixed(320f.dp),
            height = Dimension.WrapContent,
            style = theme.components.surface then Style { shape(12f.dp) }
        ) {
            awakeShadcnSectionTitle("Theme Settings")
            awakeShadcnSupportingText("Configure the look and feel.")
            spacer(UiModifier().height(12f.dp))

            awakeShadcnPropertyDropdown(
                id = "showcase-style-preset",
                label = "Style",
                options = ShowcaseStyleOptions,
                selectedIndex = state.showcaseStylePresetIndex,
                labelWidth = 64f.dp
            )?.let { state.showcaseStylePresetIndex = it }

            awakeShadcnPropertyDropdown(
                id = "showcase-base-color",
                label = "Base",
                options = ShowcaseBaseColorOptions,
                selectedIndex = state.showcaseBaseColorIndex,
                labelWidth = 64f.dp
            )?.let { state.showcaseBaseColorIndex = it }

            awakeShadcnPropertyDropdown(
                id = "showcase-theme-mode",
                label = "Mode",
                options = ShowcaseThemeModeOptions,
                selectedIndex = state.showcaseThemeModeIndex,
                labelWidth = 64f.dp
            )?.let { state.showcaseThemeModeIndex = it }

            awakeShadcnPropertyDropdown(
                id = "showcase-accent",
                label = "Accent",
                options = ShowcaseAccentOptions,
                selectedIndex = state.showcaseAccentIndex,
                labelWidth = 64f.dp
            )?.let { state.showcaseAccentIndex = it }

            spacer(UiModifier().height(8f.dp))
            awakeShadcnSupportingText(
                "Mode auto-resolves to ${if (state.showcaseResolvedDarkMode()) "dark" else "light"} on this platform.",
                maxLines = 2
            )
            spacer(UiModifier().height(12f.dp))

            awakeShadcnPropertyToggle(
                id = "showcase-live",
                label = "Live animation",
                checked = state.showcaseLiveBadge
            ).let { if (it != state.showcaseLiveBadge) state.showcaseLiveBadge = it }

            awakeShadcnPropertyToggle(
                id = "showcase-danger-mode",
                label = "Danger treatment",
                checked = state.showcaseDangerMode
            ).let { if (it != state.showcaseDangerMode) state.showcaseDangerMode = it }
        }

        // --- Preview Column ---
        column(
            width = Dimension.Fixed(420f.dp),
            height = Dimension.WrapContent,
            verticalArrangement = Arrangement.spacedBy(16f.dp)
        ) {
            awakeShadcnBadge("LIVE PREVIEW", variant = AwakeShadcnBadgeVariant.Secondary)

            val previewLift = animateFloat(
                id = "showcase-preview-lift",
                target = if (state.showcaseDangerMode) 8f else 0f,
                responsiveness = 10f
            )

            awakeShadcnSurface(
                id = "showcase-preview",
                height = Dimension.WrapContent,
                modifier = UiModifier().offset(y = (-previewLift).dp),
                variant = AwakeShadcnSurfaceVariant.Muted,
                style = Style {
                    shape(state.showcaseSurfaceRadius.dp)
                    contentPadding(16f.dp)
                    borderWidth(0f.dp) // Proves we can override variant defaults
                }
            ) { previewSlot ->
                val shimmerForward = rememberBooleanState("showcase-preview-shimmer-direction", initial = true)
                val shimmerTarget = when {
                    !state.showcaseLiveBadge -> 0.15f // Static position when paused
                    shimmerForward.value -> 1f
                    else -> 0f
                }
                val shimmerPhase = animateFloat(
                    id = "showcase-preview-shimmer",
                    target = shimmerTarget,
                    initial = 0f,
                    responsiveness = 2.0f,
                    snapDistance = 0.01f
                )

                if (state.showcaseLiveBadge) {
                    if (shimmerForward.value && shimmerPhase >= 0.99f) shimmerForward.value = false
                    if (!shimmerForward.value && shimmerPhase <= 0.01f) shimmerForward.value = true
                }

                // Draw chrome BEFORE content so it stays behind widgets
                drawShowcaseGradientChrome(
                    slot = previewSlot,
                    shimmerPhase = shimmerPhase,
                    dangerMode = state.showcaseDangerMode
                )

                row(height = 24f.dp, horizontalArrangement = Arrangement.SpaceBetween) {
                    awakeShadcnBadge(
                        if (state.showcaseLiveBadge) "LIVE" else "PAUSED",
                        variant = if (state.showcaseLiveBadge) AwakeShadcnBadgeVariant.Primary else AwakeShadcnBadgeVariant.Outline
                    )
                    if (state.showcaseDangerMode) {
                        awakeShadcnBadge("DANGER", variant = AwakeShadcnBadgeVariant.Danger)
                    }
                }

                spacer(UiModifier().height(8f.dp))
                val cardTitle = "Showcase Preview Card"
                awakeShadcnBodyText(
                    cardTitle,
                    modifier = if (state.showcaseLiveBadge) UiModifier().shadcnShimmer() else UiModifier()
                )

                awakeShadcnSupportingText(
                    if (state.showcaseDangerMode) "DANGER MODE: Thematic variant proof for destructive/alert states."
                    else "LIVE PROOF: Animation state proof using conditional canvas shimmer."
                )

                spacer(UiModifier().height(12f.dp))
                row(height = 36f.dp, horizontalArrangement = Arrangement.spacedBy(10f.dp)) {
                    if (
                        awakeShadcnButton(
                            id = "preview-primary-action",
                            label = "Inspect",
                            modifier = UiModifier().width(100f.dp).height(36f.dp),
                            variant = AwakeShadcnButtonVariant.Primary
                        )
                    ) {
                        state.showcasePrimaryClicks += 1
                    }
                    awakeShadcnButton(
                        id = "preview-secondary-action",
                        label = if (state.showcaseDangerMode) "Rollback" else "Publish",
                        modifier = UiModifier().width(100f.dp).height(36f.dp),
                        variant = if (state.showcaseDangerMode) AwakeShadcnButtonVariant.Danger else AwakeShadcnButtonVariant.Outline
                    )
                }

                spacer(UiModifier().height(8f.dp))
                awakeShadcnSupportingText("Interaction proof: ${state.showcasePrimaryClicks} clicks")
            }

            surface(
                id = "showcase-theme-radius-config",
                width = Dimension.FillMax,
                height = Dimension.WrapContent,
                style = theme.components.surface then Style { shape(12f.dp) }
            ) {
                state.showcaseSurfaceRadius = awakeShadcnPropertySlider(
                    id = "showcase-radius",
                    label = "Corner Radius",
                    min = 0f,
                    max = 32f,
                    value = state.showcaseSurfaceRadius
                )
            }
        }
    }
}

private fun ColumnScope.drawShowcaseGradientChrome(
    slot: UiSlot,
    shimmerPhase: Float,
    dangerMode: Boolean,
) {
    val tokens = theme.tokens
    val headerHeight = 40f
    
    val themeGradient = UiLinearGradient.horizontal(
        start = tokens.primary.withAlpha(0.08f),
        end = tokens.accent.withAlpha(0.12f)
    )

    canvas(slot) {
        // Inherit shape from surface stack if available, otherwise fallback to sharp corners
        val clipSpec = context.currentShapeSpec ?: io.github.ronjunevaldoz.awake.ui.UiShapeSpec.RoundedRectangle(0f.dp)
        
        // Clip all chrome to the surface's corners
        clipShape(
            shape = clipSpec,
            x = 0f,
            y = 0f,
            width = bounds.width,
            height = bounds.height
        ) {
            // --- 1. Header Surface (main pass, behind content) ---
            drawGradientRect(
                x = 0f,
                y = 0f,
                width = bounds.width,
                height = headerHeight,
                gradient = themeGradient
            )

            // --- 2. Shimmer Peak (main pass, behind content) ---
            val shimmerWidth = 160f
            val shimmerX = -shimmerWidth + (bounds.width + shimmerWidth) * shimmerPhase
            
            val shimmerColor = if (dangerMode) tokens.destructive else tokens.accent
            val highlight = shimmerColor.withAlpha(0.12f)
            
            // Left half: Transparent -> Highlight
            drawGradientRect(
                x = shimmerX,
                y = 0f,
                width = shimmerWidth / 2f,
                height = bounds.height,
                gradient = UiLinearGradient.horizontal(Color.Transparent, highlight)
            )
            // Right half: Highlight -> Transparent
            drawGradientRect(
                x = shimmerX + shimmerWidth / 2f,
                y = 0f,
                width = shimmerWidth / 2f,
                height = bounds.height,
                gradient = UiLinearGradient.horizontal(highlight, Color.Transparent)
            )
        }
    }
}
