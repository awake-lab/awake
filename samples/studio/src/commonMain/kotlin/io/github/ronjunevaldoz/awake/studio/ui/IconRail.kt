// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.headless.components.icon
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.paddingEnd
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.HeroIcons

private val RailButtonSize = ShadcnButtonSize.Icon.heightDp.dp
private val RailPadding = UiSpacing.xs
private val RailWidth = RailButtonSize + RailPadding * 2f

// HeroIcons directly, not ShadcnIcons: the registry only carries glyphs shadcn* components
// themselves draw, and this rail is sample-local composition.
private val RailTools = listOf(
    StudioContract.Tool.Layers to HeroIcons.Solid20Mini.square3Stack3d,
    StudioContract.Tool.Grid to HeroIcons.Solid20Mini.tableCells,
    StudioContract.Tool.Environment to HeroIcons.Solid20Mini.globeAlt,
    StudioContract.Tool.History to HeroIcons.Solid20Mini.clock,
    StudioContract.Tool.Panels to HeroIcons.Solid20Mini.squares2x2,
)

/** Floating tool rail (Modly-style): a rounded card hugging its icon stack, vertically
 * centered with a margin from the window edge rather than a full-height docked strip.
 * Top group selects a tool (state in [StudioContract.ToolRailState]); bottom group holds
 * actions -- reset reloads the active example, camera is not implemented yet. */
internal fun RowScope.drawIconRail(
    activeTool: StudioContract.Tool,
    onSelectTool: (StudioContract.Tool) -> Unit,
    onResetExample: () -> Unit,
) {
    column(
        id = "studio-icon-rail",
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.height(Dimension.FillMax).paddingEnd(UiSpacing.sm),
    ) {
        shadcnCard(
            id = "studio-tool-rail",
            modifier = Modifier.width(RailWidth),
            style = Style { contentPadding(RailPadding) },
        ) {
            RailTools.forEach { (tool, glyph) ->
                railButton(
                    id = "studio-tool-${tool.name.lowercase()}",
                    glyph = glyph,
                    active = tool == activeTool,
                    onClick = { onSelectTool(tool) },
                )
            }
            shadcnSeparator()
            railButton(
                id = "studio-tool-reset",
                glyph = HeroIcons.Solid20Mini.arrowPath,
                active = false,
                onClick = onResetExample,
            )
            railButton(
                id = "studio-tool-camera",
                glyph = HeroIcons.Solid20Mini.camera,
                active = false,
                onClick = null,
            )
        }
    }
}

/** Primary filled square when [active], ghost (transparent, accent on hover) otherwise. */
private fun UiScope.railButton(
    id: String,
    glyph: UiImageVector,
    active: Boolean,
    onClick: (() -> Unit)?,
) {
    shadcnButton(
        id = id,
        modifier = Modifier.width(RailButtonSize),
        variant = if (active) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
        size = ShadcnButtonSize.Icon,
        onClick = onClick,
    ) {
        icon(glyph, tint = if (active) theme.colors.primaryForeground else theme.colors.foreground)
    }
}
