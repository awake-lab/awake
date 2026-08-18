// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroupSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnIcon
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnPopover
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
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
    val environment: Boolean,
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
    val onEnvironmentChange: (Boolean) -> Unit,
    val onDebugFrustumChange: (Boolean) -> Unit,
    val onDebugBoundsChange: (Boolean) -> Unit,
    val onDebugOcclusionChange: (Boolean) -> Unit,
    val onDebugLightsChange: (Boolean) -> Unit,
    val onDebugShadowFrustumChange: (Boolean) -> Unit,
)

/** Whether the debug popover (below) is open -- module-level, not [ViewPillState], since it's
 * purely this pill's own transient UI state, not something the rest of Studio ever reads. */
private var debugMenuExpanded = false

/**
 * Three groups, not one long row: view (camera mode/projection), render style (wireframe/
 * shadows/sky -- toggled often while editing, stays always-visible), and debug visualization
 * (frustum/bounds/occlusion/light/shadow-frustum -- dev-only, rarely touched, collapsed behind
 * one "Debug" popover instead of five extra always-on buttons).
 */
internal fun UiScope.drawViewPill(state: ViewPillState, actions: ViewPillActions) {
    row(horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
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
        }

        shadcnButtonGroup(id = "studio-style-pill") {
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
            shadcnButton(
                id = "studio-view-environment",
                label = "Sky",
                variant = if (state.environment) ShadcnButtonVariant.Secondary else ShadcnButtonVariant.Ghost,
                size = ShadcnButtonSize.Xs,
                onClick = { actions.onEnvironmentChange(!state.environment) },
            )
        }

        shadcnButtonGroup(id = "studio-debug-pill") {
            shadcnButton(
                id = "studio-view-debug-menu",
                // Explicit width, not WrapContent: a WrapContent trigger's content lambda runs
                // multiple times per frame for layout trial passes, and interactive controls
                // nested inside it (the checkboxes below) see those trial invocations as real
                // clicks -- see PopoverPage.kt's own trigger for the same explicit-width
                // precedent every other popover-with-a-trigger call site already follows.
                modifier = Modifier.width(64f.dp),
                variant = if (debugMenuExpanded) ShadcnButtonVariant.Secondary else ShadcnButtonVariant.Ghost,
                size = ShadcnButtonSize.Xs,
                onClick = { debugMenuExpanded = !debugMenuExpanded },
            ) { trigger ->
                shadcnText("Debug")
                val popup = uiScope().shadcnPopover(
                    id = "studio-view-debug-menu-popover",
                    anchorSlot = trigger,
                    expanded = debugMenuExpanded,
                    width = Dimension.Fixed(170f.dp),
                ) {
                    debugCheckbox("studio-debug-frustum", "Frustum", state.debugFrustum, actions.onDebugFrustumChange)
                    debugCheckbox("studio-debug-bounds", "Bounds", state.debugBounds, actions.onDebugBoundsChange)
                    debugCheckbox(
                        "studio-debug-occlusion",
                        "Occlusion",
                        state.debugOcclusion,
                        actions.onDebugOcclusionChange,
                    )
                    debugCheckbox("studio-debug-lights", "Light gizmo", state.debugLights, actions.onDebugLightsChange)
                    debugCheckbox(
                        "studio-debug-shadow-frustum",
                        "Shadow frustum",
                        state.debugShadowFrustum,
                        actions.onDebugShadowFrustumChange,
                    )
                }
                if (popup.dismissed) debugMenuExpanded = false
            }
        }
    }
}

private fun ColumnScope.debugCheckbox(id: String, label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    if (shadcnCheckbox(id = id, checked = checked, label = label)) onChange(!checked)
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
