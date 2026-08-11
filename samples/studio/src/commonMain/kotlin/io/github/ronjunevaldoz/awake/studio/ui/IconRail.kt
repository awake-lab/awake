// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.api.dp

private val RailButtonSize = ShadcnButtonSize.Icon.heightDp
private val RailPadding = 4f.dp
private val RailWidth = RailButtonSize + RailPadding * 2f

private val RailTools = StudioContract.Tool.entries

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
                    label = tool.name.lowercase().replaceFirstChar(Char::uppercase),
                    active = tool == activeTool,
                    onClick = { onSelectTool(tool) },
                )
            }
            shadcnSeparator()
            railButton(
                id = "studio-tool-reset",
                label = "Reset",
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
    shadcnButton(
        id = "studio-tool-camera",
        label = "Camera",
        modifier = Modifier.width(RailButtonSize),
        variant = ShadcnButtonVariant.Ghost,
        size = ShadcnButtonSize.Icon,
        onClick = {},
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
                label = "Wireframe",
                active = wireframe,
                onClick = { onWireframeChange(!wireframe) },
            )
            railButton(
                id = "studio-display-shadows",
                label = "Shadows",
                active = shadows,
                onClick = { onShadowsChange(!shadows) },
            )
        }
    }
}

private fun ColumnScope.railButton(
    id: String,
    label: String,
    active: Boolean,
    onClick: (() -> Unit)?,
) {
    shadcnButton(
        id = id,
        label = label,
        modifier = Modifier.width(RailButtonSize),
        variant = if (active) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
        size = ShadcnButtonSize.Icon,
        onClick = onClick,
    )
}
