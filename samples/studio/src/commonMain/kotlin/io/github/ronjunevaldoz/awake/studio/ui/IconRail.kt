// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.paddingEnd
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.HeroIcons
import io.github.ronjunevaldoz.awake.ui.unstyled.components.icon

private val RailButtonSize = ShadcnButtonSize.Icon.heightDp
private val RailPadding = UiSpacing.xs
private val RailWidth = RailButtonSize + RailPadding * 2f

// HeroIcons directly, not ShadcnIcons: the registry only carries glyphs shadcn* components
// themselves draw, and this rail is sample-local composition.
//
// Grid used to read `tableCells` (a literal table grid) -- `squares2x2` is the actual grid
// glyph, so it moved there, which freed `squares2x2` up from Panels; `bars3` (a stacked-lines
// layout glyph) reads as "panels/layout" better than the vacated grid glyph did. Only glyphs
// that exist in HeroIcons.Solid20Mini today -- no Solid24-only glyph (e.g. square2Stack).
private val RailTools = listOf(
    StudioContract.Tool.Layers to HeroIcons.Solid20Mini.square3Stack3d,
    StudioContract.Tool.Grid to HeroIcons.Solid20Mini.squares2x2,
    StudioContract.Tool.Environment to HeroIcons.Solid20Mini.globeAlt,
    StudioContract.Tool.History to HeroIcons.Solid20Mini.clock,
    StudioContract.Tool.Panels to HeroIcons.Solid20Mini.bars3,
)

/** Floating tool rail (Modly-style): a rounded card hugging its icon stack, vertically
 * centered with a margin from the window edge rather than a full-height docked strip.
 * Top group selects a tool (state in [StudioContract.ToolRailState]); bottom group holds
 * actions -- reset reloads the active example, camera opens the mode/projection menu. */
internal fun RowScope.drawIconRail(
    activeTool: StudioContract.Tool,
    onSelectTool: (StudioContract.Tool) -> Unit,
    onResetExample: () -> Unit,
    onSelectCameraMode: (StudioContract.CameraPresetMode) -> Unit,
    onSelectCameraProjection: (StudioContract.Projection) -> Unit,
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
            cameraRailButton(
                onSelectMode = onSelectCameraMode,
                onSelectProjection = onSelectCameraProjection,
            )
        }
    }
}

/** Opens the camera-mode/projection menu anchored to this button -- the same list the
 * viewport's right-click menu shows (see CameraMenu.kt). */
private fun UiScope.cameraRailButton(
    onSelectMode: (StudioContract.CameraPresetMode) -> Unit,
    onSelectProjection: (StudioContract.Projection) -> Unit,
) {
    val popup = rememberPopupState("studio-tool-camera-menu")
    var anchor = UiBounds(0f, 0f, 0f, 0f)
    shadcnButton(
        id = "studio-tool-camera",
        modifier = Modifier.width(RailButtonSize),
        variant = ShadcnButtonVariant.Ghost,
        size = ShadcnButtonSize.Icon,
        onClick = { popup.toggle() },
    ) { slot ->
        anchor = slot
        icon(HeroIcons.Solid20Mini.camera, tint = theme.colors.foreground)
    }
    if (popup.expanded) {
        val result = shadcnDropdownMenu(
            id = "studio-tool-camera-menu.dropdown",
            anchorSlot = anchor,
            expanded = true,
            items = CameraMenuItems,
            width = Dimension.WrapContent,
        )
        if (result.dismissed || result.selectedIndex != null) popup.close()
        result.selectedIndex?.let { index ->
            dispatchCameraMenuPick(index, onSelectMode, onSelectProjection)
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
