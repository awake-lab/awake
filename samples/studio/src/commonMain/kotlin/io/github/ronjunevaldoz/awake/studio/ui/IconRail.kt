// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnIcons
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.width

private val RailButtonSize = ShadcnButtonSize.Icon.heightDp
private val RailPadding = 4f.dp
private val RailWidth = RailButtonSize + RailPadding * 2f

private val RailTools = StudioContract.Tool.entries

/** No literal icon exists yet for every [StudioContract.Tool] -- these are the closest semantic
 * match in the registered [ShadcnIcons] set, not a 1:1 upstream glyph. */
private fun StudioContract.Tool.icon(): UiImageVector = when (this) {
    StudioContract.Tool.Layers -> ShadcnIcons.square3Stack3d
    StudioContract.Tool.Grid -> ShadcnIcons.squares2x2
    StudioContract.Tool.Environment -> ShadcnIcons.sun
    StudioContract.Tool.History -> ShadcnIcons.clock
    StudioContract.Tool.Panels -> ShadcnIcons.puzzlePiece
}

/** Floating tool rail (Modly-style): a rounded card hugging its icon stack, vertically
 * centered with a margin from the window edge rather than a full-height docked strip.
 * Top group selects a tool (state in [StudioContract.ToolRailState]); bottom group holds
 * actions -- reset reloads the active example, camera opens the mode/projection menu. */
internal fun RowScope.drawIconRail(
    activeTool: StudioContract.Tool,
    onSelectTool: (StudioContract.Tool) -> Unit,
    onResetExample: () -> Unit,
    onSelectCameraMode: (CameraMode) -> Unit,
    onSelectCameraProjection: (StudioContract.Projection) -> Unit,
) {
    column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight(),
    ) {
        shadcnSurface(
            id = "studio-tool-rail",
            modifier = Modifier.width(RailWidth),
        ) {
            RailTools.forEach { tool ->
                railButton(
                    id = "studio-tool-${tool.name.lowercase()}",
                    glyph = tool.icon(),
                    active = tool == activeTool,
                    onClick = { onSelectTool(tool) },
                )
            }
            shadcnSeparator()
            railButton(
                id = "studio-tool-reset",
                glyph = ShadcnIcons.arrowPath,
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
private fun ColumnScope.cameraRailButton(
    onSelectMode: (CameraMode) -> Unit,
    onSelectProjection: (StudioContract.Projection) -> Unit,
) {
    var requestOpen = false
    // Wrapped so the menu has a slot to anchor to -- shadcnButton reports whether it was clicked,
    // not where it landed.
    val bounds = column(modifier = Modifier.width(RailButtonSize)) {
        shadcnButton(
            id = "studio-tool-camera",
            modifier = Modifier.width(RailButtonSize),
            variant = ShadcnButtonVariant.Ghost,
            size = ShadcnButtonSize.Icon,
            onClick = { requestOpen = true },
        ) { icon(ShadcnIcons.camera) }
    }
    // The button carried an empty onClick, so the rail's camera menu never existed -- the id the
    // test looks for was produced by no code at all. Anchored to the button's own bounds so the
    // menu opens beside the rail rather than at the pointer.
    railCameraMenu(
        id = "studio-tool-camera-menu",
        anchor = bounds,
        requestOpen = requestOpen,
        onPick = { index ->
            dispatchCameraMenuPick(
                index,
                onSelectMode = onSelectMode,
                onSelectProjection = onSelectProjection,
            )
        },
    )
}

/** Primary filled square when [active], ghost (transparent, accent on hover) otherwise. */
/**
 * Display toggles, floating at the viewport's RIGHT edge opposite the tool rail.
 *
 * Separate from the tool rail on purpose: that rail is modal (one tool at a time), these are
 * independent booleans, and mixing the two interaction models in one strip serves neither.
 * Blender splits the same way -- tools in the `T` toolbar, shading and overlay toggles floating
 * at the viewport's top right.
 *
 * They lived in the top bar before, which was a scoping error: wireframe and shadows govern how
 * THIS VIEWPORT draws, not the document. Grid and gizmo visibility belong here too when they
 * exist, which is the other reason they are not crammed into the header.
 */
internal fun RowScope.drawDisplayRail(
    wireframe: Boolean,
    shadows: Boolean,
    onWireframeChange: (Boolean) -> Unit,
    onShadowsChange: (Boolean) -> Unit,
) {
    column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight(),
    ) {
        shadcnSurface(
            id = "studio-display-rail-card",
            modifier = Modifier.width(RailWidth),
        ) {
            railButton(
                id = "studio-display-wireframe",
                glyph = ShadcnIcons.squares2x2,
                active = wireframe,
                onClick = { onWireframeChange(!wireframe) },
            )
            railButton(
                id = "studio-display-shadows",
                glyph = ShadcnIcons.eye,
                active = shadows,
                onClick = { onShadowsChange(!shadows) },
            )
        }
    }
}

private fun ColumnScope.railButton(
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
    ) { icon(glyph) }
}
