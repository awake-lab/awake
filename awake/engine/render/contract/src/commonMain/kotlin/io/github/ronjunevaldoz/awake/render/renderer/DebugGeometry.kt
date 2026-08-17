// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Frustum
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3

/**
 * Pure geometry -- turns a [Camera]'s frustum or a world-space [Aabb] into [LineSegment]s for
 * [Renderer.drawDebugLines]. No `World`/`Entity` dependency: a caller (e.g. a
 * `DebugVisualizationSystem`) decides *when* and *for which entities* to call these, this file
 * only decides *what the lines look like*.
 */

/** One line per [Frustum.EDGES] entry, camera/[aspect]'s frustum -- same corner order
 * [Frustum.intersects] already uses. */
fun frustumDebugLines(camera: Camera, aspect: Float, color: FloatArray): List<LineSegment> {
    val corners = Frustum.corners(camera, aspect)
    return Frustum.EDGES.map { (a, b) -> LineSegment(corners[a], corners[b], color) }
}

/** One line per [Aabb.EDGES] entry, [bounds] transformed into world space by [worldMatrix] --
 * mirrors [frustumDebugLines]'s shape for a box instead of a frustum. */
fun boundsDebugLines(bounds: Aabb, worldMatrix: Mat4, color: FloatArray): List<LineSegment> {
    val corners = bounds.transformed(worldMatrix).corners()
    return Aabb.EDGES.map { (a, b) -> LineSegment(corners[a], corners[b], color) }
}

/** A directional-light gizmo at [origin]: one line pointing along [direction] (fixed visual
 * length, not to scale with the scene) plus a small cross of 4 perpendicular segments at
 * [origin] -- reads as a light icon even from an angle where the direction line foreshortens
 * to a point. */
fun lightGizmoLines(origin: Vec3, direction: Vec3, color: FloatArray): List<LineSegment> {
    val d = direction.normalized()
    val arrowEnd = origin + d * LIGHT_GIZMO_ARROW_LENGTH
    val reference = if (kotlin.math.abs(d.y) > 0.99f) Vec3(1f, 0f, 0f) else Vec3(0f, 1f, 0f)
    val right = d.cross(reference).normalized()
    val up = right.cross(d).normalized()
    val armLength = LIGHT_GIZMO_CROSS_ARM_LENGTH
    return listOf(
        LineSegment(origin, arrowEnd, color),
        LineSegment(origin - right * armLength, origin + right * armLength, color),
        LineSegment(origin - up * armLength, origin + up * armLength, color),
    )
}

private const val LIGHT_GIZMO_ARROW_LENGTH = 3f
private const val LIGHT_GIZMO_CROSS_ARM_LENGTH = 0.5f
