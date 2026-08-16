// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.gizmo

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Ray
import io.github.ronjunevaldoz.awake.core.math.Vec2
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.projectToViewport
import io.github.ronjunevaldoz.awake.core.math.rayThroughViewport

/** The three translate axes, in the red/green/blue every editor uses for X/Y/Z. Rotate and scale
 * handles reuse the same hit-testing once they exist. */
enum class GizmoAxis(val direction: Vec3, val color: FloatArray) {
    X(Vec3(1f, 0f, 0f), floatArrayOf(AXIS_BRIGHT, AXIS_DIM, AXIS_DIM, 1f)),
    Y(Vec3(0f, 1f, 0f), floatArrayOf(AXIS_DIM, AXIS_BRIGHT, AXIS_DIM, 1f)),
    Z(Vec3(0f, 0f, 1f), floatArrayOf(AXIS_DIM, AXIS_MID, AXIS_BRIGHT, 1f)),
}

private const val AXIS_BRIGHT = 1f
private const val AXIS_MID = 0.4f
private const val AXIS_DIM = 0.2f

/** One pickable thing: the entity, and its bounds already in world space. */
data class GizmoCandidate(val entityId: Int, val bounds: Aabb)

/**
 * One viewport's worth of projection context: the camera, its matrix, and the pixel size of the
 * region it draws into.
 *
 * These four always travel together -- passing them separately to every hit-test and drag meant
 * five parameters per call and five chances to pass a stale one. Build it once per frame from the
 * primary camera and the viewport panel's bounds.
 */
data class ViewportProjection(
    val camera: Camera,
    val viewProjection: Mat4,
    val width: Float,
    val height: Float,
) {
    /** A world point in this viewport's pixels, or `null` behind the camera. */
    fun toScreen(world: Vec3): Vec2? = camera.projectToViewport(world, viewProjection, width, height)

    /** The world ray through a viewport pixel, or `null` when the matrix cannot be inverted. */
    fun rayThrough(pointer: Vec2): Ray? =
        camera.rayThroughViewport(pointer.x, pointer.y, viewProjection, width, height)
}

/**
 * The axis handle under [pointer], or `null` when the pointer is not on one.
 *
 * Hit-tested in screen space against the drawn segment: the handle is a line the user is aiming
 * at, so the distance that matters is the one they can see.
 */
fun ViewportProjection.hitTestHandle(
    origin: Vec3,
    handleLength: Float,
    pointer: Vec2,
    tolerance: Float = HANDLE_TOLERANCE_PX,
): GizmoAxis? {
    val originScreen = toScreen(origin) ?: return null
    var best: GizmoAxis? = null
    var bestDistance = tolerance
    GizmoAxis.entries.forEach { axis ->
        val tipScreen = toScreen(axis.tipFrom(origin, handleLength)) ?: return@forEach
        val distance = pointer.distanceToSegment(originScreen, tipScreen)
        if (distance < bestDistance) {
            bestDistance = distance
            best = axis
        }
    }
    return best
}

/**
 * How far to move along [axis] for a pointer that moved [dragX]/[dragY] pixels.
 *
 * The axis is projected to screen and the drag projected onto that direction, so dragging along
 * the handle moves the object and dragging across it does nothing. Returns `0` when the axis
 * points nearly at the camera: its screen projection collapses to a point, and dividing by that
 * length would send the object to infinity for a one-pixel drag.
 */
fun ViewportProjection.dragAlongAxis(
    origin: Vec3,
    axis: GizmoAxis,
    handleLength: Float,
    dragX: Float,
    dragY: Float,
): Float {
    val originScreen = toScreen(origin)
    val tipScreen = toScreen(axis.tipFrom(origin, handleLength))
    if (originScreen == null || tipScreen == null) return 0f
    val axisX = tipScreen.x - originScreen.x
    val axisY = tipScreen.y - originScreen.y
    val lengthSquared = axisX * axisX + axisY * axisY
    // Fraction of the handle's own screen length the drag covered, scaled back to world units.
    return if (lengthSquared < MIN_AXIS_SCREEN_LENGTH_SQUARED) {
        0f
    } else {
        ((dragX * axisX + dragY * axisY) / lengthSquared) * handleLength
    }
}

/**
 * The nearest entity whose bounds the ray through [pointer] enters, or `null` for empty space.
 *
 * A real ray against real bounds, not a screen-distance approximation: the approximation picked
 * by an object's ORIGIN, so a ground plane was only selectable near its centre and a small object
 * in front of a large one could lose. This needs both [Mat4.inverse] and per-entity bounds, which
 * is why it was not possible when the gizmo was first written.
 */
fun ViewportProjection.pickEntityAt(pointer: Vec2, candidates: List<GizmoCandidate>): Int? {
    val ray = rayThrough(pointer) ?: return null
    var best: Int? = null
    var bestDistance = Float.MAX_VALUE
    candidates.forEach { candidate ->
        val distance = ray.intersectAabb(candidate.bounds) ?: return@forEach
        if (distance < bestDistance) {
            bestDistance = distance
            best = candidate.entityId
        }
    }
    return best
}

private fun GizmoAxis.tipFrom(origin: Vec3, handleLength: Float): Vec3 = Vec3(
    origin.x + direction.x * handleLength,
    origin.y + direction.y * handleLength,
    origin.z + direction.z * handleLength,
)

/** Generous: a 1px line is unhittable with a mouse, and these are the only draggable things in
 * the viewport, so over-claiming costs nothing. */
private const val HANDLE_TOLERANCE_PX = 10f

/** Below this the axis is pointing at the camera and its screen direction is noise. */
private const val MIN_AXIS_SCREEN_LENGTH_SQUARED = 4f
