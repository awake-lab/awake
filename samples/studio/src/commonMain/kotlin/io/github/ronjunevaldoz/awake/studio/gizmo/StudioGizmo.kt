// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.gizmo

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Vec2
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshRenderer

/** Which transform a drag edits -- studio's `Tool`, kept out of this file's imports so the gizmo
 * stays independent of the store. */
enum class GizmoTool { Select, Move, Rotate, Scale }

/**
 * What a frame's pointer did to the selection.
 *
 * A type rather than a nullable entity id because "no click happened this frame" and "the click
 * landed on empty space" are different answers that both want to be `null` somewhere: conflating
 * them meant every frame of a held drag reported "nothing picked" and cleared the selection one
 * frame after making it.
 */
sealed interface GizmoPick {
    /** A press resolved to [entityId] -- `null` when it landed on empty space. */
    data class Selected(val entityId: Int?) : GizmoPick

    /** No press this frame: a held drag, a release, or the pointer outside the viewport. */
    data object None : GizmoPick
}

/**
 * Viewport pointer state for one frame, in the viewport's own pixels.
 *
 * `null` [position] means the pointer is outside the viewport -- not the same as "at 0,0", which
 * is a corner the user can legitimately click.
 */
data class GizmoPointer(val position: Vec2?, val down: Boolean)

/** Everything about one frame the gizmo needs: where the viewport is looking, what is selected,
 * where the pointer is, and what a drag currently means. Bundled because they always arrive
 * together and are always read together. */
data class GizmoFrame(
    val projection: ViewportProjection,
    val selectedEntityId: Int?,
    val pointer: GizmoPointer,
    val tool: GizmoTool,
)

/**
 * The translate gizmo's whole state machine: what is selected, what is being dragged, and where
 * the pointer was last frame.
 *
 * A class rather than loose remembered values because a drag is inherently stateful -- it starts
 * on one frame, continues across several, and must survive the selection changing underneath it.
 */
class StudioGizmo {
    private var draggingAxis: GizmoAxis? = null
    private var lastPointer: Vec2? = null
    private var wasDown = false

    /**
     * Advances one frame and reports what the pointer did to the selection.
     *
     * Handles win over picking: a click that lands on a handle starts a drag instead of selecting
     * whatever is behind it, which is what makes a gizmo usable over a crowded scene.
     */
    @Suppress("ReturnCount") // One exit per state the frame can be in: released, press, drag.
    fun update(world: World, frame: GizmoFrame, boundsOf: (entityId: Int) -> Aabb?): GizmoPick {
        val projection = frame.projection
        val selectedEntityId = frame.selectedEntityId
        val pointer = frame.pointer
        val tool = frame.tool
        val position = pointer.position
        val pressedThisFrame = pointer.down && !wasDown
        val released = !pointer.down
        wasDown = pointer.down

        if (released || position == null) {
            draggingAxis = null
            lastPointer = position
            return GizmoPick.None
        }

        val selectedTransform = selectedEntityId?.let { world.transformOf(it) }
        val handleLength = HANDLE_LENGTH

        if (pressedThisFrame) {
            // Select has no handles, so every press is a pick -- the tool for getting at an
            // object that a handle would otherwise cover.
            val origin = selectedTransform?.position?.takeIf { tool != GizmoTool.Select }
            draggingAxis = origin?.let { projection.hitTestHandle(it, handleLength, position) }
            lastPointer = position
            // Grabbing a handle is not a selection change: the thing being dragged stays selected.
            return if (draggingAxis == null) {
                GizmoPick.Selected(projection.pickEntityAt(position, world.candidates(boundsOf)))
            } else {
                GizmoPick.None
            }
        }

        val axis = draggingAxis
        val previous = lastPointer
        lastPointer = position
        if (axis == null || previous == null || selectedTransform == null) return GizmoPick.None

        val moved = projection.dragAlongAxis(
            origin = selectedTransform.position,
            axis = axis,
            handleLength = handleLength,
            dragX = position.x - previous.x,
            dragY = position.y - previous.y,
        )
        // Written straight into the live component, so the inspector's own fields follow it.
        // Component-wise rather than `+ axis.direction * moved`: this runs every drag frame, and
        // the operator form would allocate a Vec3 per frame (see Vec3's naming contract).
        applyDrag(tool, selectedTransform, axis, moved)
        return GizmoPick.None
    }

    /** What a drag of [moved] world units along [axis] means for the current [tool]. */
    private fun applyDrag(tool: GizmoTool, transform: Transform, axis: GizmoAxis, moved: Float) {
        when (tool) {
            GizmoTool.Select -> Unit

            // Component-wise rather than `+ axis.direction * moved`: this runs every drag frame,
            // and the operator form would allocate a Vec3 per frame (see Vec3's naming contract).
            GizmoTool.Move -> {
                transform.position.x += axis.direction.x * moved
                transform.position.y += axis.direction.y * moved
                transform.position.z += axis.direction.z * moved
            }

            // Distance along the handle reads as an angle. Transform.rotation is Euler radians and
            // each axis handle maps to its own component, so a single-axis drag needs no
            // quaternion round trip.
            GizmoTool.Rotate -> {
                val radians = moved * RADIANS_PER_HANDLE_LENGTH
                transform.rotation.x += axis.direction.x * radians
                transform.rotation.y += axis.direction.y * radians
                transform.rotation.z += axis.direction.z * radians
            }

            // Clamped above zero: a scale through zero flips the object inside out and cannot be
            // dragged back, because every later delta is multiplied by nothing.
            GizmoTool.Scale -> {
                transform.scale.x = (transform.scale.x + axis.direction.x * moved).coerceAtLeast(MIN_SCALE)
                transform.scale.y = (transform.scale.y + axis.direction.y * moved).coerceAtLeast(MIN_SCALE)
                transform.scale.z = (transform.scale.z + axis.direction.z * moved).coerceAtLeast(MIN_SCALE)
            }
        }
    }

    /**
     * The handle lines for [selectedEntityId], or empty when nothing is selected.
     *
     * World-space [LineSegment]s rather than UI primitives: they belong in the 3D pass, where
     * they depth-test against the scene the way a real editor's handles do.
     */
    fun handleLines(world: World, selectedEntityId: Int?, tool: GizmoTool): List<LineSegment> {
        val transform = selectedEntityId
            ?.takeIf { tool != GizmoTool.Select }
            ?.let { world.transformOf(it) }
            ?: return emptyList()
        val origin = transform.position
        return GizmoAxis.entries.map { axis ->
            LineSegment(
                start = Vec3(origin.x, origin.y, origin.z),
                end = Vec3(
                    origin.x + axis.direction.x * HANDLE_LENGTH,
                    origin.y + axis.direction.y * HANDLE_LENGTH,
                    origin.z + axis.direction.z * HANDLE_LENGTH,
                ),
                color = if (axis == draggingAxis) DRAG_COLOR else axis.color,
            )
        }
    }

    private fun World.transformOf(entityId: Int): Transform? {
        var found: Transform? = null
        queryEach<Transform> { entity, transform -> if (entity.id == entityId) found = transform }
        return found
    }

    /** Every renderable entity with resolvable bounds, in world space. */
    private fun World.candidates(boundsOf: (Int) -> Aabb?): List<GizmoCandidate> = buildList {
        queryEach(Transform::class, MeshRenderer::class) { entity, transform, _ ->
            val local = boundsOf(entity.id) ?: return@queryEach
            add(GizmoCandidate(entity.id, local.transformed(transform.worldMatrix)))
        }
    }

    private companion object {
        /** Fixed world length, so a handle shrinks with distance like the object it belongs to.
         * A screen-constant size is the usual editor behaviour and wants the camera distance;
         * this is the smaller thing that works. */
        const val HANDLE_LENGTH = 1.5f

        /** A drag the full length of a handle turns the object a quarter turn -- fast enough to
         * be useful, slow enough to aim. */
        const val RADIANS_PER_HANDLE_LENGTH = 1.0f

        /** Scale may approach zero but not reach it: at zero every later delta multiplies by
         * nothing and the object can never be dragged back. */
        const val MIN_SCALE = 0.01f
        val DRAG_COLOR = floatArrayOf(1f, 0.9f, 0.3f, 1f)
    }
}
