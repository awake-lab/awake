// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.frame
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width

private val StudioTheme = shadcnTheme(dark = false)

// Dark neutral gray, not pure black -- Renderer.clearColor otherwise defaults to stark black,
// which reads as "nothing rendered" rather than a real viewport background. Same reasoning
// scene3d-playground's own VIEWPORT_CLEAR_COLOR documents.
@Suppress("MagicNumber") // The literals define the constant; naming each channel adds nothing.
private val ViewportClearColor = floatArrayOf(0.14f, 0.14f, 0.16f, 1f)

internal fun SceneGameRuntime.drawStudioShell(store: StudioStore, viewportWidth: Float, viewportHeight: Float) {
    renderer.clearColor = ViewportClearColor
    uiContext.pushTheme(StudioTheme)
    frame(viewportWidth, viewportHeight) {
        row(
            id = "studio-shell",
            horizontalArrangement = Arrangement.spacedBy(0f.dp),
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax).padding(8.dp),
        ) {
            drawIconRail(
                activeTool = store.state.value.toolRail.activeTool,
                onSelectTool = { store.dispatch(StudioContract.Intent.SelectTool(it)) },
                // Re-selecting the active example queues LoadExample, which tears the scene
                // down and re-instantiates it -- reset without a dedicated intent.
                onResetExample = { store.dispatch(StudioContract.Intent.SelectExample(store.state.value.examples.activeExampleId)) },
                onSelectCameraMode = { store.dispatch(StudioContract.Intent.SetCameraMode(it)) },
                onSelectCameraProjection = { store.dispatch(StudioContract.Intent.SetProjection(it)) },
            )
            drawExampleRail(
                activeExampleId = store.state.value.examples.activeExampleId,
                onSelectExample = { store.dispatch(StudioContract.Intent.SelectExample(it)) },
            )
            val viewportBounds = column(
                id = "studio-viewport-column",
                verticalArrangement = Arrangement.spacedBy(8f.dp),
                modifier = Modifier.weight(1f).height(Dimension.FillMax).padding(8f.dp),
            ) {
                drawStudioToolbar(renderer)
            }
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
            drawInspectorPanel(world)
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
    val isHovered = input.pointerX >= bounds.x && input.pointerX <= bounds.x + bounds.width &&
        input.pointerY >= bounds.y && input.pointerY <= bounds.y + bounds.height
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
