// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.gettingstarted

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTextLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.headless.width

internal fun ColumnScope.drawUiShowcaseOverviewPreview() {
    shadcnBadge(id = "showcase-badge-showcase", label = "SHOWCASE", variant = ShadcnBadgeVariant.Secondary)
    shadcnText("Dedicated sample route")
    shadcnMuted("This page shell exists so the design system is judged as a product surface, not just as loose demo widgets.")
    spacer(Modifier.height(8f.dp))
    shadcnTextLines(
        listOf(
            "Stable chrome on top, grouped navigation on the left, one detail page in the content pane.",
            "The starter sample stays a starter sample; docs and polish move here.",
            "This is now the right home for future design-system tutorials and regression proofs.",
        ),
    )
}

internal fun ColumnScope.drawUiShowcaseReferenceComparisonPreview() {
    row(horizontalArrangement = Arrangement.spacedBy(12f.dp)) {
        shadcnCard(id = "ui-showcase-reference-spec", modifier = Modifier.width(280f.dp).height(180f.dp)) {
            shadcnSectionTitle("Reference cues")
            shadcnMuted("Compact controls and restrained surfaces.")
        }
        shadcnCard(id = "ui-showcase-reference-awake", modifier = Modifier.width(280f.dp).height(180f.dp)) {
            shadcnSectionTitle("Awake")
            shadcnMuted("The same structure rendered through the public Headless boundary.")
        }
    }
}

internal fun ColumnScope.drawUiShowcaseControlsPreview(state: UiShowcaseRuntimeState) {
    shadcnMuted("This page proves that the Awake theme factory can re-skin the entire component library live, including custom canvas chrome.")
    spacer(Modifier.height(16f.dp))

    row(horizontalArrangement = Arrangement.spacedBy(24f.dp)) {
        shadcnSurface(
            id = "showcase-theme-settings",
            modifier = Modifier.width(320f.dp),
        ) {
            shadcnSectionTitle("Theme Settings")
            shadcnMuted("Configure the look and feel.")
            spacer(Modifier.height(12f.dp))

            themeSelectRow("Style", "showcase-style-preset", ShowcaseStyleOptions, state.showcaseStylePresetIndex) {
                state.showcaseStylePresetIndex = it
            }
            themeSelectRow("Base", "showcase-base-color", ShowcaseBaseColorOptions, state.showcaseBaseColorIndex) {
                state.showcaseBaseColorIndex = it
            }
            themeSelectRow("Mode", "showcase-theme-mode", ShowcaseThemeModeOptions, state.showcaseThemeModeIndex) {
                state.showcaseThemeModeIndex = it
            }
            themeSelectRow("Accent", "showcase-accent", ShowcaseAccentOptions, state.showcaseAccentIndex) {
                state.showcaseAccentIndex = it
            }

            spacer(Modifier.height(8f.dp))
            shadcnMuted(
                "Mode auto-resolves to ${if (state.showcaseResolvedDarkMode()) "dark" else "light"} on this platform.",
                maxLines = 2,
            )
            spacer(Modifier.height(12f.dp))
            themeSwitchRow("Live animation", "showcase-live", state.showcaseLiveBadge) {
                state.showcaseLiveBadge = it
            }
            themeSwitchRow("Danger treatment", "showcase-danger-mode", state.showcaseDangerMode) {
                state.showcaseDangerMode = it
            }
        }

        columnPreview(state)
    }
}

private fun ColumnScope.themeSelectRow(
    label: String,
    id: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = UiAlignment.Vertical.Center,
    ) {
        themeLabel(label, 72f.dp)
        shadcnSelect(id = id, options = options, selectedIndex = selectedIndex, modifier = Modifier.width(180f.dp))
            ?.let(onSelected)
    }
}

private fun ColumnScope.themeSwitchRow(
    label: String,
    id: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = UiAlignment.Vertical.Center,
    ) {
        themeLabel(label, 132f.dp)
        onChecked(uiScope().shadcnSwitch(id = id, checked = checked))
    }
}

private fun RowScope.themeLabel(label: String, width: io.github.ronjunevaldoz.awake.ui.api.Dp) {
    text(
        label = label,
        modifier = Modifier.width(width),
        visuals = SurfaceStyle(
            foreground = themeValues.colors.foreground,
            textSize = themeValues.typography.label,
        ),
    )
}

private fun RowScope.columnPreview(state: UiShowcaseRuntimeState) {
    // Keep the two-column structure from the reference page: the preview has a fixed width,
    // while the settings panel owns the controls and their state.
    column(
        verticalArrangement = Arrangement.spacedBy(16f.dp),
        modifier = Modifier.width(420f.dp),
    ) {
        shadcnBadge(id = "showcase-live-preview-label", label = "LIVE PREVIEW", variant = ShadcnBadgeVariant.Secondary)
        shadcnSurface(
            id = "showcase-preview",
            modifier = Modifier.width(420f.dp),
            variant = ShadcnSurfaceVariant.Muted,
        ) {
            row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = UiAlignment.Vertical.Center,
            ) {
                shadcnBadge(
                    id = "showcase-preview-status",
                    label = if (state.showcaseLiveBadge) "LIVE" else "PAUSED",
                    variant = if (state.showcaseLiveBadge) ShadcnBadgeVariant.Primary else ShadcnBadgeVariant.Outline,
                )
                if (state.showcaseDangerMode) {
                    shadcnBadge(id = "showcase-preview-danger", label = "DANGER", variant = ShadcnBadgeVariant.Danger)
                }
            }
            spacer(Modifier.height(8f.dp))
            shadcnText("Showcase Preview Card")
            shadcnMuted(
                if (state.showcaseDangerMode) {
                    "DANGER MODE: Thematic variant proof for destructive/alert states."
                } else {
                    "LIVE PROOF: Animation state proof using conditional canvas shimmer."
                },
            )
            spacer(Modifier.height(12f.dp))
            row(horizontalArrangement = Arrangement.spacedBy(10f.dp)) {
                shadcnButton(
                    id = "preview-primary-action",
                    label = "Inspect",
                    modifier = Modifier.width(100f.dp),
                    variant = ShadcnButtonVariant.Primary,
                    onClick = { state.showcasePrimaryClicks += 1 },
                )
                shadcnButton(
                    id = "preview-secondary-action",
                    label = if (state.showcaseDangerMode) "Rollback" else "Publish",
                    modifier = Modifier.width(100f.dp),
                    variant = if (state.showcaseDangerMode) ShadcnButtonVariant.Danger else ShadcnButtonVariant.Outline,
                )
            }
            spacer(Modifier.height(8f.dp))
            shadcnMuted("Interaction proof: ${state.showcasePrimaryClicks} clicks")
        }

        shadcnSurface(id = "showcase-theme-radius-config", modifier = Modifier.width(420f.dp)) {
            row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = UiAlignment.Vertical.Center,
            ) {
                themeLabel("Corner Radius", 120f.dp)
                state.showcaseSurfaceRadius = uiScope().shadcnSlider(
                    id = "showcase-radius",
                    min = 0f,
                    max = 32f,
                    value = state.showcaseSurfaceRadius,
                    modifier = Modifier.width(240f.dp),
                )
            }
        }
    }
}

internal val ShowcaseStyleOptions =
    io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnStylePreset.entries.map { it.label }
internal val ShowcaseBaseColorOptions =
    io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnBaseColor.entries.map { it.label }
internal val ShowcaseAccentOptions =
    io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnAccent.entries.map { it.label }
internal val ShowcaseThemeModeOptions =
    io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseThemeMode.entries.map { it.label }
