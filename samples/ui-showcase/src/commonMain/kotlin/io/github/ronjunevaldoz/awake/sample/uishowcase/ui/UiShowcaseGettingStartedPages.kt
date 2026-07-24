// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.animateFloat
import io.github.ronjunevaldoz.awake.ui.canvas
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnPropertyDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnPropertySlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnPropertyToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.shadcnShimmer
import io.github.ronjunevaldoz.awake.ui.rememberBooleanState
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal fun ColumnScope.drawUiShowcaseOverviewPreview() {
    shadcnBadge("SHOWCASE", variant = ShadcnBadgeVariant.Secondary)
    shadcnBodyText("Dedicated sample route")
    shadcnSupportingText("This page shell exists so the design system is judged as a product surface, not just as loose demo widgets.")
    spacer(Modifier.height(8f.dp))
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
    row( horizontalArrangement = Arrangement.spacedBy(12f.dp), modifier = Modifier.copy(heightDimension = Dimension.WrapContent)) {
        surface(
            id = "ui-showcase-reference-spec",
            style = theme.components.surface then Style { shape(14f.dp) }
        , modifier = Modifier.copy(widthDimension = Dimension.Fixed(cardWidth.dp), heightDimension = Dimension.Fixed(284f.dp))) {
            shadcnSectionTitle("Official cues")
            shadcnSupportingText("The reference we keep checking against.")
            spacer(Modifier.height(8f.dp))
            supportingLines(
                listOf(
                    "Controls feel closer to 36px than 44px.",
                    "Dropdown content is a popover, not a bare button stack.",
                    "Cards sit close to the page background with restrained contrast."
                )
            )
            spacer(Modifier.height(8f.dp))
            shadcnBadge("TARGET", variant = ShadcnBadgeVariant.Outline)
        }
        surface(
            id = "ui-showcase-reference-awake",
            style = theme.components.surface then Style { shape(14f.dp) }
        , modifier = Modifier.copy(widthDimension = Dimension.Fixed(cardWidth.dp), heightDimension = Dimension.Fixed(284f.dp))) {
            shadcnSectionTitle("Awake now")
            shadcnSupportingText("Our current implementation after the sizing and popover pass.")
            spacer(Modifier.height(8f.dp))
            shadcnSupportingText(
                "Typography is tighter, menu surfaces are contained, and the gray slab effect is reduced.",
                maxLines = 4
            )
            spacer(Modifier.height(8f.dp))
            row( horizontalArrangement = Arrangement.spacedBy(8f.dp), modifier = Modifier.copy(heightDimension = 36f.dp.toDimension())) {
                shadcnButton(
                    "reference-primary",
                    "Primary",
                    modifier = Modifier.width(100f.dp).height(36f.dp),
                    variant = ShadcnButtonVariant.Primary
                )
                shadcnButton(
                    "reference-outline",
                    "Outline",
                    modifier = Modifier.width(96f.dp).height(36f.dp),
                    variant = ShadcnButtonVariant.Outline
                )
            }
            spacer(Modifier.height(8f.dp))
            shadcnBadge("AWAKE", variant = ShadcnBadgeVariant.Primary)
        }
    }
}

internal fun ColumnScope.drawUiShowcaseControlsPreview(state: UiShowcaseRuntimeState) {
    shadcnSupportingText("This page proves that the Awake theme factory can re-skin the entire component library live, including custom canvas chrome.")
    spacer(Modifier.height(16f.dp))

    row( horizontalArrangement = Arrangement.spacedBy(24f.dp), modifier = Modifier.copy(heightDimension = Dimension.WrapContent)) {
        // --- Settings Column ---
        surface(
            id = "showcase-theme-settings",
            style = theme.components.surface then Style { shape(12f.dp) }
        , modifier = Modifier.copy(widthDimension = Dimension.Fixed(320f.dp), heightDimension = Dimension.WrapContent)) {
            shadcnSectionTitle("Theme Settings")
            shadcnSupportingText("Configure the look and feel.")
            spacer(Modifier.height(12f.dp))

            shadcnPropertyDropdown(
                id = "showcase-style-preset",
                label = "Style",
                options = ShowcaseStyleOptions,
                selectedIndex = state.showcaseStylePresetIndex,
                labelWidth = 64f.dp
            )?.let { state.showcaseStylePresetIndex = it }

            shadcnPropertyDropdown(
                id = "showcase-base-color",
                label = "Base",
                options = ShowcaseBaseColorOptions,
                selectedIndex = state.showcaseBaseColorIndex,
                labelWidth = 64f.dp
            )?.let { state.showcaseBaseColorIndex = it }

            shadcnPropertyDropdown(
                id = "showcase-theme-mode",
                label = "Mode",
                options = ShowcaseThemeModeOptions,
                selectedIndex = state.showcaseThemeModeIndex,
                labelWidth = 64f.dp
            )?.let { state.showcaseThemeModeIndex = it }

            shadcnPropertyDropdown(
                id = "showcase-accent",
                label = "Accent",
                options = ShowcaseAccentOptions,
                selectedIndex = state.showcaseAccentIndex,
                labelWidth = 64f.dp
            )?.let { state.showcaseAccentIndex = it }

            spacer(Modifier.height(8f.dp))
            shadcnSupportingText(
                "Mode auto-resolves to ${if (state.showcaseResolvedDarkMode()) "dark" else "light"} on this platform.",
                maxLines = 2
            )
            spacer(Modifier.height(12f.dp))

            shadcnPropertyToggle(
                id = "showcase-live",
                label = "Live animation",
                checked = state.showcaseLiveBadge
            ).let { if (it != state.showcaseLiveBadge) state.showcaseLiveBadge = it }

            shadcnPropertyToggle(
                id = "showcase-danger-mode",
                label = "Danger treatment",
                checked = state.showcaseDangerMode
            ).let { if (it != state.showcaseDangerMode) state.showcaseDangerMode = it }
        }

        // --- Preview Column ---
        column(
            verticalArrangement = Arrangement.spacedBy(16f.dp)
        , modifier = Modifier.copy(widthDimension = Dimension.Fixed(420f.dp), heightDimension = Dimension.WrapContent)) {
            shadcnBadge("LIVE PREVIEW", variant = ShadcnBadgeVariant.Secondary)

            val previewLift = animateFloat(
                id = "showcase-preview-lift",
                target = if (state.showcaseDangerMode) 8f else 0f,
                responsiveness = 10f
            )

            shadcnSurface(
                id = "showcase-preview",
                modifier = (Modifier.offset(y = (-previewLift).dp)).copy(heightDimension = Dimension.WrapContent),
                variant = ShadcnSurfaceVariant.Muted,
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

                row( horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.copy(heightDimension = 24f.dp.toDimension())) {
                    shadcnBadge(
                        if (state.showcaseLiveBadge) "LIVE" else "PAUSED",
                        variant = if (state.showcaseLiveBadge) ShadcnBadgeVariant.Primary else ShadcnBadgeVariant.Outline
                    )
                    if (state.showcaseDangerMode) {
                        shadcnBadge("DANGER", variant = ShadcnBadgeVariant.Danger)
                    }
                }

                spacer(Modifier.height(8f.dp))
                val cardTitle = "Showcase Preview Card"
                shadcnBodyText(
                    cardTitle,
                    modifier = if (state.showcaseLiveBadge) Modifier.shadcnShimmer() else Modifier
                )

                shadcnSupportingText(
                    if (state.showcaseDangerMode) "DANGER MODE: Thematic variant proof for destructive/alert states."
                    else "LIVE PROOF: Animation state proof using conditional canvas shimmer."
                )

                spacer(Modifier.height(12f.dp))
                row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.copy(heightDimension = 36f.dp.toDimension())) {
                    if (
                        shadcnButton(
                            id = "preview-primary-action",
                            label = "Inspect",
                            modifier = Modifier.width(100f.dp).height(36f.dp),
                            variant = ShadcnButtonVariant.Primary
                        )
                    ) {
                        state.showcasePrimaryClicks += 1
                    }
                    shadcnButton(
                        id = "preview-secondary-action",
                        label = if (state.showcaseDangerMode) "Rollback" else "Publish",
                        modifier = Modifier.width(100f.dp).height(36f.dp),
                        variant = if (state.showcaseDangerMode) ShadcnButtonVariant.Danger else ShadcnButtonVariant.Outline
                    )
                }

                spacer(Modifier.height(8f.dp))
                shadcnSupportingText("Interaction proof: ${state.showcasePrimaryClicks} clicks")
            }

            surface(
                id = "showcase-theme-radius-config",
                style = theme.components.surface then Style { shape(12f.dp) }
            , modifier = Modifier.copy(widthDimension = Dimension.FillMax, heightDimension = Dimension.WrapContent)) {
                state.showcaseSurfaceRadius = shadcnPropertySlider(
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
