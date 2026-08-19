// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.systems

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Vec2
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.scene.rendering.components.WorldDebugSettings
import io.github.ronjunevaldoz.awake.scene.rendering.systems.debugVisualizationLines
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAppLifecycleRuntime
import io.github.ronjunevaldoz.awake.studio.gizmo.GizmoFrame
import io.github.ronjunevaldoz.awake.studio.gizmo.GizmoPick
import io.github.ronjunevaldoz.awake.studio.gizmo.GizmoPointer
import io.github.ronjunevaldoz.awake.studio.gizmo.GizmoTool
import io.github.ronjunevaldoz.awake.studio.gizmo.StudioGizmo
import io.github.ronjunevaldoz.awake.studio.gizmo.StudioViewportRect
import io.github.ronjunevaldoz.awake.studio.gizmo.ViewportProjection
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.ui.context.UiInputOwnership

/**
 * Drives the translate gizmo once per frame.
 *
 * A system rather than something hooked into the overlay: the UI layout callback it used to run
 * from fires several times per frame (the resizable group measures before it places), which
 * advanced a drag's state machine multiple times per frame and against intermediate rects.
 *
 * The viewport rect comes from [StudioViewportRect], written by the shell during layout -- the
 * same rect the scene is rendered into, so a pick can never disagree with what the user sees.
 */
internal class StudioGizmoSystem(
    private val runtime: SceneAppLifecycleRuntime,
    private val store: StudioStore,
    private val gizmo: StudioGizmo,
    private val viewportRect: StudioViewportRect,
    private val boundsOf: (Int) -> Aabb?,
) : System {
    override fun update(world: World, delta: Float) {
        val viewport = viewportRect.bounds ?: return
        val camera = primaryCamera(world) ?: return
        val projection = ViewportProjection(
            camera = camera.camera,
            viewProjection = camera.camera.viewProjectionMatrix(
                viewport.width / viewport.height,
                runtime.renderer.clipSpace,
            ),
            width = viewport.width,
            height = viewport.height,
        )

        val input = runtime.uiContext.inputState
        val insideViewport = input.pointerX >= viewport.x &&
                input.pointerX <= viewport.x + viewport.width &&
                input.pointerY >= viewport.y &&
                input.pointerY <= viewport.y + viewport.height
        // null rather than a clamped edge position: a drag must not keep running against the
        // panel border after the cursor leaves it.
        val local = if (insideViewport) Vec2(
            input.pointerX - viewport.x,
            input.pointerY - viewport.y
        ) else null

        val selected = store.state.value.inspector.selectedEntityId
        val tool = store.state.value.tools.active.toGizmoTool()
        // Edit mode only: dragging while gameplay systems own the transform would fight them, and
        // the result is discarded on stop anyway.
        if (store.state.value.mode == StudioContract.Mode.Edit) {
            val pick = gizmo.update(
                world,
                GizmoFrame(projection, selected, GizmoPointer(local, input.pointerDown), tool),
                boundsOf,
            )
            // Only a frame that actually resolved a press changes the selection. Dispatching on
            // every frame the button is held cleared it one frame after making it.
            if (pick is GizmoPick.Selected && pick.entityId != selected) {
                store.dispatch(StudioContract.Intent.SelectEntity(pick.entityId))
            }
        }
        // Renderer.drawDebugLines REPLACES the whole line buffer rather than appending, and this
        // system runs after DebugVisualizationSystem (see infrastructureSystems' own comment).
        // A call here with ONLY the gizmo's own handle lines silently wiped whatever
        // DebugVisualizationSystem had just drawn (frustum/bounds/light/shadow lines) every
        // frame something was also selected -- the two calls never combined, the second one just
        // replaced the first. Recomputing DebugVisualizationSystem's own lines here and unioning
        // them into ONE call (rather than, say, appending from the other side) keeps this the
        // single place that decides what the frame's line buffer actually contains.
        val handleLines = gizmo.handleLines(world, selected, tool)
        val debugSettings = world.family<WorldDebugSettings>().components().firstOrNull()
        val debugLines = debugSettings?.let { debugVisualizationLines(world, runtime.renderer, it) }
            ?: emptyList()
        val lines = debugLines + handleLines
        if (lines.isNotEmpty()) runtime.renderer.drawDebugLines(lines)
    }

    /** Mapped rather than shared: the gizmo does not depend on studio's store, and a UI enum
     * gaining a case that has no drag behaviour should fail to compile here. */
    private fun StudioContract.Tool.toGizmoTool(): GizmoTool = when (this) {
        StudioContract.Tool.Select -> GizmoTool.Select
        StudioContract.Tool.Move -> GizmoTool.Move
        StudioContract.Tool.Rotate -> GizmoTool.Rotate
        StudioContract.Tool.Scale -> GizmoTool.Scale
    }

    private fun primaryCamera(world: World): Camera? {
        var found: Camera? = null
        world.family<Camera>()
            .forEach { _, camera -> if (found == null && camera.isPrimary) found = camera }
        return found
    }
}

/** ORs a handle drag into `isCaptured` -- [io.github.ronjunevaldoz.awake.scene.controls.systems
 * .CameraSystem]/[io.github.ronjunevaldoz.awake.scene.controls.systems.CameraInputSystem]
 * already treat `isCaptured` as "some other input consumer owns this drag/keypress" (a real UI
 * widget); a gizmo handle drag is exactly that too, it just isn't a UI widget. Without this,
 * dragging a handle ALSO orbited the camera underneath it -- both systems read the same raw
 * pointer drag with no mutual exclusion. */
internal fun UiInputOwnership.gizmoCapturedOwnership(gizmo: StudioGizmo): UiInputOwnership =
    if (gizmo.isDraggingHandle) copy(isCaptured = true) else this
