// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.animateFloat
import io.github.ronjunevaldoz.awake.ui.canvas
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.shadcnSupportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberBooleanState
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.toSlot
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme

internal fun ColumnScope.drawUiShowcaseOverviewPreview() {
    shadcnBadge("SHOWCASE", variant = ShadcnBadgeVariant.Secondary)
    shadcnBodyText("Dedicated sample route")
    shadcnSupportingText("This page shell exists so the design system is judged as a product surface, not just as loose demo widgets.")
    spacer(Modifier.height(8f.dp))
    shadcnSupportingLines(
        listOf(
            "Stable chrome on top, grouped navigation on the left, one detail page in the content pane.",
            "The starter sample stays a starter sample; docs and polish move here.",
            "This is now the right home for future design-system tutorials and regression proofs."
        )
    )
}

internal fun ColumnScope.drawUiShowcaseReferenceComparisonPreview() {
    row(
        horizontalArrangement = Arrangement.spacedBy(12f.dp),
        modifier = Modifier.height(Dimension.WrapContent)
    ) {
        shadcnCard(
            id = "ui-showcase-reference-spec",
            modifier = Modifier.width(Dimension.WrapContent).height(Dimension.Fixed(284f.dp))
        ) {
            shadcnSectionTitle("Official cues")
            shadcnSupportingText("The reference we keep checking against.")
            spacer(Modifier.height(8f.dp))
            shadcnSupportingLines(
                listOf(
                    "Controls feel closer to 36px than 44px.",
                    "Dropdown content is a popover, not a bare button stack.",
                    "Cards sit close to the page background with restrained contrast."
                )
            )
            spacer(Modifier.height(8f.dp))
            shadcnBadge("TARGET", variant = ShadcnBadgeVariant.Outline)
        }
        shadcnCard(
            id = "ui-showcase-reference-awake",
            modifier = Modifier.width(Dimension.WrapContent)
                .height(Dimension.Fixed(284f.dp))
        ) {
            shadcnSectionTitle("Awake now")
            shadcnSupportingText("Our current implementation after the sizing and popover pass.")
            spacer(Modifier.height(8f.dp))
            shadcnSupportingText(
                "Typography is tighter, menu surfaces are contained, and the gray slab effect is reduced.",
                maxLines = 4
            )
            spacer(Modifier.height(8f.dp))
            row(
                horizontalArrangement = Arrangement.spacedBy(8f.dp),
                modifier = Modifier.height(36f.dp.toDimension())
            ) {
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

    row(
        horizontalArrangement = Arrangement.spacedBy(24f.dp),
        modifier = Modifier.height(Dimension.WrapContent)
    ) {
        // --- Settings Column ---
        surface(
            id = "showcase-theme-settings",
            style = theme.components.surface then Style { shape(12f.dp) },
            modifier = Modifier.width(Dimension.Fixed(320f.dp)).height(Dimension.WrapContent)
        ) {
            shadcnSectionTitle("Theme Settings")
            shadcnSupportingText("Configure the look and feel.")
            spacer(Modifier.height(12f.dp))

            // Shared fixed label width across all rows in this settings panel -- sized to fit
            // the longest label ("Accent") so dropdowns align to the same x position instead of
            // each row's label claiming only its own intrinsic width.
            val settingsLabelWidth = 72f.dp

            shadcnFieldDropdown(
                id = "showcase-style-preset",
                options = ShowcaseStyleOptions,
                selectedIndex = state.showcaseStylePresetIndex
            ) {
                shadcnFieldLabel("Style", modifier = Modifier.width(settingsLabelWidth))
            }?.let { state.showcaseStylePresetIndex = it }

            shadcnFieldDropdown(
                id = "showcase-base-color",
                options = ShowcaseBaseColorOptions,
                selectedIndex = state.showcaseBaseColorIndex
            ) {
                shadcnFieldLabel("Base", modifier = Modifier.width(settingsLabelWidth))
            }?.let { state.showcaseBaseColorIndex = it }

            shadcnFieldDropdown(
                id = "showcase-theme-mode",
                options = ShowcaseThemeModeOptions,
                selectedIndex = state.showcaseThemeModeIndex
            ) {
                shadcnFieldLabel("Mode", modifier = Modifier.width(settingsLabelWidth))
            }?.let { state.showcaseThemeModeIndex = it }

            shadcnFieldDropdown(
                id = "showcase-accent",
                options = ShowcaseAccentOptions,
                selectedIndex = state.showcaseAccentIndex
            ) {
                shadcnFieldLabel("Accent", modifier = Modifier.width(settingsLabelWidth))
            }?.let { state.showcaseAccentIndex = it }

            spacer(Modifier.height(8f.dp))
            shadcnSupportingText(
                "Mode auto-resolves to ${if (state.showcaseResolvedDarkMode()) "dark" else "light"} on this platform.",
                maxLines = 2
            )
            spacer(Modifier.height(12f.dp))

            shadcnFieldSwitch(
                id = "showcase-live",
                label = "Live animation",
                checked = state.showcaseLiveBadge
            ).let { if (it != state.showcaseLiveBadge) state.showcaseLiveBadge = it }

            shadcnFieldSwitch(
                id = "showcase-danger-mode",
                label = "Danger treatment",
                checked = state.showcaseDangerMode
            ).let { if (it != state.showcaseDangerMode) state.showcaseDangerMode = it }
        }

        // --- Preview Column ---
        column(
            verticalArrangement = Arrangement.spacedBy(16f.dp),
            modifier = Modifier.width(Dimension.Fixed(420f.dp)).height(Dimension.WrapContent)
        ) {
            shadcnBadge("LIVE PREVIEW", variant = ShadcnBadgeVariant.Secondary)

            val previewLift = animateFloat(
                id = "showcase-preview-lift",
                target = if (state.showcaseDangerMode) 8f else 0f,
                responsiveness = 10f
            )

            shadcnSurface(
                id = "showcase-preview",
                modifier = (Modifier.offset(y = (-previewLift).dp)).height(Dimension.WrapContent),
                variant = ShadcnSurfaceVariant.Muted,
                style = Style {
                    shape(state.showcaseSurfaceRadius.dp)
                    contentPadding(16f.dp)
                    borderWidth(0f.dp) // Proves we can override variant defaults
                }
            ) { previewSlot ->
                var shimmerForward by rememberBooleanState(
                    "showcase-preview-shimmer-direction",
                    initial = true
                )
                val shimmerTarget = when {
                    !state.showcaseLiveBadge -> 0.15f // Static position when paused
                    shimmerForward -> 1f
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
                    if (shimmerForward && shimmerPhase >= 0.99f) shimmerForward = false
                    if (!shimmerForward && shimmerPhase <= 0.01f) shimmerForward = true
                }

                // Draw chrome BEFORE content so it stays behind widgets
                drawShowcaseGradientChrome(
                    slot = previewSlot,
                    shimmerPhase = shimmerPhase,
                    dangerMode = state.showcaseDangerMode
                )

                // Height left as WrapContent (not a hardcoded 24dp guess): shadcnBadge's actual
                // rendered height is glyph height + its own content padding, which is taller
                // than 24dp -- a fixed 24dp row doesn't clip the badge's paint to fit, so the
                // badge visually overflowed past the row and overlapped the title text below it.
                row(horizontalArrangement = Arrangement.SpaceBetween) {
                    shadcnBadge(
                        if (state.showcaseLiveBadge) "LIVE" else "PAUSED",
                        variant = if (state.showcaseLiveBadge) ShadcnBadgeVariant.Primary else ShadcnBadgeVariant.Outline
                    )
                    if (state.showcaseDangerMode) {
                        shadcnBadge("DANGER", variant = ShadcnBadgeVariant.Danger)
                    }
                }

                spacer(Modifier.height(8f.dp))
                // No shimmer here (shimmer already has its own dedicated showcase page): a
                // single static frame never gets to animate, so it renders at the sweep's
                // *initial* phase every time, which lands the highlight band on the title's
                // leading glyphs and paints them near-white -- reads as corrupted/missing text
                // in a still image/docs snapshot, not a real problem the user would ever see in
                // motion, but a real problem for every static capture of this page.
                shadcnBodyText("Showcase Preview Card")

                shadcnSupportingText(
                    if (state.showcaseDangerMode) "DANGER MODE: Thematic variant proof for destructive/alert states."
                    else "LIVE PROOF: Animation state proof using conditional canvas shimmer."
                )

                spacer(Modifier.height(12f.dp))
                row(
                    horizontalArrangement = Arrangement.spacedBy(10f.dp),
                    modifier = Modifier.height(36f.dp.toDimension())
                ) {
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
                style = theme.components.surface then Style { shape(12f.dp) },
                modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)
            ) {
                state.showcaseSurfaceRadius = shadcnFieldSlider(
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
    slot: UiBounds,
    shimmerPhase: Float,
    dangerMode: Boolean,
) {
    val tokens = theme.colors
    val headerHeight = 40f

    val themeGradient = UiLinearGradient.horizontal(
        start = tokens.primary.withAlpha(0.08f),
        end = tokens.accent.withAlpha(0.12f)
    )

    canvas(slot.toSlot()) {
        // Inherit shape from surface stack if available, otherwise fallback to sharp corners
        val clipSpec = context.currentShapeSpec
            ?: io.github.ronjunevaldoz.awake.ui.UiShapeSpec.RoundedRectangle(0f.dp)

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
