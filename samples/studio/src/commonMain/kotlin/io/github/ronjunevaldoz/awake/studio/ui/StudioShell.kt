// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.frame
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizableHandle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizablePanel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizablePanelGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnToggleGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.ResizableDirection

private val StudioTheme = shadcnTheme(dark = true)

// Dark neutral gray, not pure black -- Renderer.clearColor otherwise defaults to stark black,
// which reads as "nothing rendered" rather than a real viewport background. Same reasoning
// scene3d-playground's own VIEWPORT_CLEAR_COLOR documents.
@Suppress("MagicNumber") // The literals define the constant; naming each channel adds nothing.
private val ViewportClearColor = floatArrayOf(0.14f, 0.14f, 0.16f, 1f)

internal fun SceneGameRuntime.drawStudioShell(store: StudioStore, viewportWidth: Float, viewportHeight: Float) {
    renderer.clearColor = ViewportClearColor
    uiContext.pushTheme(StudioTheme)
    frame(viewportWidth, viewportHeight) {
        drawStudioShellBody(store, world, renderer)
    }
}

// Sum to exactly 1f -- shadcnResizablePanelGroup lays each panel out independently at
// fraction * availableWidth (see ResizablePanelGroupScope.panel), not "last panel fills the
// remainder", so a sum under/over 1 leaves a gap or overlap on first mount.
private const val SIDEBAR_FRACTION = 0.18f
private const val VIEWPORT_FRACTION = 0.62f
private const val INSPECTOR_FRACTION = 0.20f

// Same default shadcnSeparator() itself uses -- passed explicitly (not left to the default) so
// this file's own height math below stays in sync with what actually gets drawn.
private val SEPARATOR_THICKNESS = 1f.dp

/**
 * The docked shell chrome: a full-width top bar, a 3-way resizable split (Scene Browser |
 * viewport | Inspector), and a full-width status bar -- flush to every frame edge, no outer
 * margin, so it reads as a docked IDE rather than the floating light-theme cards with margins
 * around a dark viewport this replaced. The only separators are hairlines
 * ([io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator]) and the resizable
 * group's own draggable handles.
 *
 * Plain [store]/[world]/[renderer] params instead of a [SceneGameRuntime] receiver -- letting a
 * bare [io.github.ronjunevaldoz.awake.ui.context.UiContext] drive this directly is what makes it
 * testable without a full game/runtime bootstrap (see `StudioShellLayoutTest`).
 *
 * The panels group's height is computed explicitly (shell height minus the top bar, the status
 * bar, and both hairlines) rather than `Modifier.weight(1f)` -- a weight fill here left a gap
 * exactly the status bar's own height tall between the panels and the status bar, since
 * `shadcnResizablePanelGroup` already runs its own dry-count-then-real pass over its content
 * independently of the outer column's weight-distribution trial; computing the height directly
 * sidesteps that interaction instead of depending on it.
 */
internal fun UiScope.drawStudioShellBody(store: StudioStore, world: World, renderer: Renderer) {
    column(
        id = "studio-shell",
        verticalArrangement = Arrangement.spacedBy(0f.dp),
        modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax),
    ) { shellSlot ->
        drawStudioTopBar(
            // "Play" replays the active example -- re-selecting it queues LoadExample, which
            // tears the scene down and re-instantiates it, same reset semantics the icon rail's
            // own reset button already uses.
            onPlay = { store.dispatch(StudioContract.Intent.SelectExample(store.state.value.examples.activeExampleId)) },
        )
        shadcnSeparator(thickness = SEPARATOR_THICKNESS)
        val panelsHeightPx = (
            shellSlot.height - TOP_BAR_HEIGHT.toPx() - STATUS_BAR_HEIGHT.toPx() -
                SEPARATOR_THICKNESS.toPx() * 2f
            ).coerceAtLeast(0f)
        shadcnResizablePanelGroup(
            id = "studio-panels-group",
            direction = ResizableDirection.Horizontal,
            modifier = Modifier.width(Dimension.FillMax).height(panelsHeightPx.px),
        ) {
            shadcnResizablePanel(id = "studio-panel-sidebar", defaultSize = SIDEBAR_FRACTION, minSize = 0.12f, maxSize = 0.32f) {
                drawExampleRail(
                    activeExampleId = store.state.value.examples.activeExampleId,
                    onSelectExample = { store.dispatch(StudioContract.Intent.SelectExample(it)) },
                )
            }
            shadcnResizableHandle(id = "studio-panel-handle-left", withHandle = true)
            shadcnResizablePanel(id = "studio-panel-viewport", defaultSize = VIEWPORT_FRACTION, minSize = 0.3f) {
                drawStudioViewportPanel(store, renderer)
            }
            shadcnResizableHandle(id = "studio-panel-handle-right", withHandle = true)
            shadcnResizablePanel(id = "studio-panel-inspector", defaultSize = INSPECTOR_FRACTION, minSize = 0.14f, maxSize = 0.32f) {
                drawInspectorPanel(world)
            }
        }
        shadcnSeparator(thickness = SEPARATOR_THICKNESS)
        drawStudioStatusBar()
    }
}

/** The viewport panel's own content: the floating icon rail hugging its left edge, the 3D
 * viewport region (the toolbar used to float here too -- it lives in the top bar now), and the
 * right-click camera menu/orbit-drag hooked to that same region's bounds. */
private fun UiScope.drawStudioViewportPanel(store: StudioStore, renderer: Renderer) {
    column(modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax)) {
        drawStudioViewportHeader(store)
        row(
            id = "studio-viewport-row",
            modifier = Modifier.width(Dimension.FillMax).weight(1f),
        ) {
        drawIconRail(
            activeTool = store.state.value.toolRail.activeTool,
            onSelectTool = { store.dispatch(StudioContract.Intent.SelectTool(it)) },
            onResetExample = { store.dispatch(StudioContract.Intent.SelectExample(store.state.value.examples.activeExampleId)) },
            onSelectCameraMode = { store.dispatch(StudioContract.Intent.SetCameraMode(it)) },
            onSelectCameraProjection = { store.dispatch(StudioContract.Intent.SetProjection(it)) },
        )
        val viewportBounds = column(
            id = "studio-viewport-column",
            modifier = Modifier.weight(1f).height(Dimension.FillMax).padding(8f.dp),
        ) { }
        drawDisplayRail(
            wireframe = renderer.wireframe,
            shadows = renderer.shadowsEnabled,
            onWireframeChange = { renderer.wireframe = it },
            onShadowsChange = { renderer.shadowsEnabled = it },
        )
        viewportCameraMenu(
            id = "studio-viewport-camera-menu",
            bounds = viewportBounds,
            onPick = { index ->
                dispatchCameraMenuPick(
                    index,
                    onSelectMode = { store.dispatch(StudioContract.Intent.SetCameraMode(it)) },
                    onSelectProjection = { store.dispatch(StudioContract.Intent.SetProjection(it)) },
                )
            },
        )
            driveViewportOrbitDrag(store, viewportBounds)
        }
    }
}

private fun UiScope.drawStudioViewportHeader(store: StudioStore) {
    val camera = store.state.value.camera
    val modes = StudioContract.CameraPresetMode.entries
    val projections = StudioContract.Projection.entries
    row(
        id = "studio-viewport-header",
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.width(Dimension.FillMax).height(40f.dp),
    ) {
        shadcnSelect(
            id = "studio-viewport-camera-mode",
            options = modes.map { it.name },
            selectedIndex = modes.indexOf(camera.mode),
            modifier = Modifier.width(156f.dp),
        )?.let { index ->
            modes.getOrNull(index)?.let { store.dispatch(StudioContract.Intent.SetCameraMode(it)) }
        }
        shadcnToggleGroup(
            id = "studio-viewport-projection",
            options = listOf("Perspective", "Ortho"),
            selectedIndex = projections.indexOf(camera.projection),
            modifier = Modifier.width(176f.dp).height(36f.dp),
        ) { index ->
            projections.getOrNull(index)?.let { store.dispatch(StudioContract.Intent.SetProjection(it)) }
        }
    }
}

// Same sensitivity as `awake:scene:controls`' CameraSystem.LOOK_SENSITIVITY -- consistent drag
// feel with the engine's own gameplay camera.
private const val ORBIT_DRAG_SENSITIVITY = 0.005f

/** Primary-drag orbits the camera while [StudioContract.CameraPresetMode.Orbit] is active --
 * Front/Top ignore drag input entirely (core-math skill Rule 5: a mode that ignores an axis
 * must not accumulate it). */
private fun UiScope.driveViewportOrbitDrag(store: StudioStore, bounds: UiBounds) {
    if (store.state.value.camera.mode != StudioContract.CameraPresetMode.Orbit) return
    val input = context.inputState
    val isHovered = input.pointerX >= bounds.x &&
        input.pointerX <= bounds.x + bounds.width &&
        input.pointerY >= bounds.y &&
        input.pointerY <= bounds.y + bounds.height
    val dragging = input.pointerDown && isHovered
    val state = widgetState("studio-viewport-orbit-drag")
    val wasDragging = state.get("wasDragging", false)
    if (dragging && wasDragging) {
        val dx = input.pointerX - state.get("lastX", input.pointerX)
        val dy = input.pointerY - state.get("lastY", input.pointerY)
        if (dx != 0f || dy != 0f) {
            store.dispatch(StudioContract.Intent.OrbitBy(dx * ORBIT_DRAG_SENSITIVITY, -dy * ORBIT_DRAG_SENSITIVITY))
        }
    }
    state.set("lastX", input.pointerX)
    state.set("lastY", input.pointerY)
    state.set("wasDragging", dragging)
}
