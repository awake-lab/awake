// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Frustum
import io.github.ronjunevaldoz.awake.core.math.Mat4

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
