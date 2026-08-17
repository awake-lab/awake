// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroupSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnIcon
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.ui.heroicons.icon.HeroIcons

/** The group's own height: one button plus the hairline border it now owns. */
private val PillHeight = 32f.dp

private val outline = HeroIcons.Outline24

/**
 * The transform tools, floating at the viewport's top-left.
 *
 * Modal -- one tool at a time, and it decides what a drag does. The view controls are a separate
 * pill because they are independent toggles; mixing the two interaction models in one strip is
 * what made the old rail unreadable.
 *
 * Horizontal, not the vertical rail the design called for -- a label does not render in a pill
 * sized for icons, it truncated to "S..." at every width tried when this was still text.
 */
internal fun UiScope.drawToolPill(
    activeTool: StudioContract.Tool,
    onSelectTool: (StudioContract.Tool) -> Unit,
) {
    shadcnButtonGroup(id = "studio-tool-pill", modifier = Modifier.height(PillHeight)) {
        StudioContract.Tool.entries.forEachIndexed { index, tool ->
            if (index > 0) shadcnButtonGroupSeparator()
            val glyph = tool.glyph()
            if (glyph == null) {
                shadcnButton(
                    id = "studio-tool-${tool.name.lowercase()}",
                    label = tool.name,
                    variant = if (tool == activeTool) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Sm,
                    onClick = { onSelectTool(tool) },

                    )
            } else {
                rowIconButton(
                    id = "studio-tool-${tool.name.lowercase()}",
                    glyph = glyph,
                    active = tool == activeTool,
                    onClick = { onSelectTool(tool) },
                )
            }
        }
    }
}

/** `null` where heroicons has no honest equivalent -- that tool falls back to its own name. */
private fun StudioContract.Tool.glyph(): UiImageVector? = when (this) {
    StudioContract.Tool.Select -> outline.cursorArrowRays
    StudioContract.Tool.Move -> outline.arrowsPointingOut
    StudioContract.Tool.Rotate -> outline.arrowPath
    StudioContract.Tool.Scale -> outline.arrowsPointingIn
}

private fun CameraMode.glyph(): UiImageVector = when (this) {
    CameraMode.FirstPerson -> outline.eye
    CameraMode.ThirdPerson -> outline.userCircle
    CameraMode.Cinematic -> outline.film
    CameraMode.TopDown -> outline.map
}

/**
 * View state, floating along the viewport's top edge: which camera, which projection, and the
 * display toggles.
 *
 * Everything here is what THIS viewport shows, not what the document contains -- the distinction
 * that keeps it out of the window's own top bar.
 */
internal data class ViewPillState(
    val mode: CameraMode,
    val projection: StudioContract.Projection,
    val wireframe: Boolean,
    val shadows: Boolean,
    val debugFrustum: Boolean,
    val debugBounds: Boolean,
    val debugOcclusion: Boolean,
    val debugLights: Boolean,
    val debugShadowFrustum: Boolean,
)

internal data class ViewPillActions(
    val onCycleMode: () -> Unit,
    val onToggleProjection: () -> Unit,
    val onWireframeChange: (Boolean) -> Unit,
    val onShadowsChange: (Boolean) -> Unit,
    val onDebugFrustumChange: (Boolean) -> Unit,
    val onDebugBoundsChange: (Boolean) -> Unit,
    val onDebugOcclusionChange: (Boolean) -> Unit,
    val onDebugLightsChange: (Boolean) -> Unit,
    val onDebugShadowFrustumChange: (Boolean) -> Unit,
)

internal fun UiScope.drawViewPill(state: ViewPillState, actions: ViewPillActions) {
    shadcnButtonGroup(id = "studio-view-pill") {
        rowIconButton(
            id = "studio-view-mode",
            glyph = state.mode.glyph(),
            active = false,
            onClick = actions.onCycleMode,
        )

        shadcnButtonGroupSeparator()
        shadcnButton(
            id = "studio-view-projection",
            label = if (state.projection == StudioContract.Projection.Perspective) "Persp" else "Ortho",
            variant = ShadcnButtonVariant.Ghost,
            size = ShadcnButtonSize.Xs,
            onClick = actions.onToggleProjection,
        )
        shadcnButtonGroupSeparator()
        rowIconButton(
            id = "studio-view-wireframe",
            glyph = outline.squares2x2,
            active = state.wireframe,
            onClick = { actions.onWireframeChange(!state.wireframe) },
        )
        rowIconButton(
            id = "studio-view-shadows",
            glyph = outline.sun,
            active = state.shadows,
            onClick = { actions.onShadowsChange(!state.shadows) },
        )
        shadcnButtonGroupSeparator()
        rowIconButton(
            id = "studio-view-debug-frustum",
            glyph = outline.videoCamera,
            active = state.debugFrustum,
            onClick = { actions.onDebugFrustumChange(!state.debugFrustum) },
        )
        rowIconButton(
            id = "studio-view-debug-bounds",
            glyph = outline.cube,
            active = state.debugBounds,
            onClick = { actions.onDebugBoundsChange(!state.debugBounds) },
        )
        rowIconButton(
            id = "studio-view-debug-occlusion",
            glyph = outline.eyeSlash,
            active = state.debugOcclusion,
            onClick = { actions.onDebugOcclusionChange(!state.debugOcclusion) },
        )
        shadcnButtonGroupSeparator()
        shadcnButton(
            id = "studio-view-debug-lights",
            label = "Light",
            variant = if (state.debugLights) ShadcnButtonVariant.Secondary else ShadcnButtonVariant.Ghost,
            size = ShadcnButtonSize.Xs,
            onClick = { actions.onDebugLightsChange(!state.debugLights) },
        )
        shadcnButton(
            id = "studio-view-debug-shadow-frustum",
            label = "Shadow",
            variant = if (state.debugShadowFrustum) ShadcnButtonVariant.Secondary else ShadcnButtonVariant.Ghost,
            size = ShadcnButtonSize.Xs,
            onClick = { actions.onDebugShadowFrustumChange(!state.debugShadowFrustum) },
        )
    }
}

/** The collapsed console strip: entity count and backend, with the dock a click away. */
internal fun UiScope.drawStatusStrip(
    expanded: Boolean,
    mode: String,
    backend: String,
    entityCount: Int,
    onToggle: () -> Unit,
) {
    row(
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.height(STATUS_BAR_HEIGHT),
    ) {
        rowIconButton(
            id = "studio-console-toggle",
            glyph = outline.commandLine,
            active = false,
            onClick = onToggle,
        )
        shadcnText("$mode  -  $entityCount entities  -  $backend")
    }
}

private fun UiScope.rowIconButton(
    id: String,
    glyph: UiImageVector,
    active: Boolean,
    onClick: () -> Unit,
) {
    shadcnButton(
        id = id,
        variant = if (active) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
        size = ShadcnButtonSize.Icon,
        onClick = onClick,
    ) { shadcnIcon(glyph) }
}
