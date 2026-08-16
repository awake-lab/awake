// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.tan

/**
 * The 8 world-space corner points of a [Camera]'s view frustum, computed analytically from
 * its eye/center/up/fovY/near/far -- no matrix inversion needed (`Mat4` has none). Corners
 * are ordered near-then-far, each quad counter-clockwise starting bottom-left as seen from
 * [Camera.eye]: `[nearBL, nearBR, nearTR, nearTL, farBL, farBR, farTR, farTL]`.
 */
object Frustum {
    fun corners(camera: Camera, aspect: Float): List<Vec3> {
        val forward = (camera.center - camera.eye).normalized()
        val right = forward.cross(camera.up).normalized()
        val up = right.cross(forward)

        val nearHalfHeight = tan(camera.fovYRadians / 2f) * camera.near
        val nearHalfWidth = nearHalfHeight * aspect
        val farHalfHeight = tan(camera.fovYRadians / 2f) * camera.far
        val farHalfWidth = farHalfHeight * aspect

        val nearCenter = camera.eye + forward * camera.near
        val farCenter = camera.eye + forward * camera.far

        return listOf(
            nearCenter - right * nearHalfWidth - up * nearHalfHeight,
            nearCenter + right * nearHalfWidth - up * nearHalfHeight,
            nearCenter + right * nearHalfWidth + up * nearHalfHeight,
            nearCenter - right * nearHalfWidth + up * nearHalfHeight,
            farCenter - right * farHalfWidth - up * farHalfHeight,
            farCenter + right * farHalfWidth - up * farHalfHeight,
            farCenter + right * farHalfWidth + up * farHalfHeight,
            farCenter - right * farHalfWidth + up * farHalfHeight,
        )
    }

    /** The 12 edges of the frustum box (4 near-quad + 4 far-quad + 4 connecting), each as a
     * (start, end) corner-index pair into [corners]'s 8-element result -- a debug-line
     * renderer turns these into world-space line segments. */
    val EDGES: List<Pair<Int, Int>> = listOf(
        // near quad
        0 to 1, 1 to 2, 2 to 3, 3 to 0,
        // far quad
        4 to 5, 5 to 6, 6 to 7, 7 to 4,
        // connecting edges
        0 to 4, 1 to 5, 2 to 6, 3 to 7,
    )
}

/**
 * Whether [box] is at least partly inside the frustum [camera]/[aspect] describe -- the culling
 * test the corner list alone could not answer.
 *
 * Conservative: a box that is outside every plane individually is rejected, but a large box
 * straddling a corner can be reported as visible when it is not. That direction is the safe one
 * (drawing something invisible costs a frame's work; skipping something visible is a bug), and
 * it is what every "cheap AABB vs frustum" test does.
 */
fun Frustum.intersects(camera: Camera, aspect: Float, box: Aabb): Boolean {
    val corners = corners(camera, aspect)
    val near = corners.take(NEAR_CORNER_COUNT)
    val far = corners.drop(NEAR_CORNER_COUNT)
    // Each plane is spanned by three corners, wound so its normal points INTO the frustum --
    // verified by hand against Camera's identity-view test setup (eye at origin, forward -Z):
    // every plane below passes through the eye (as a symmetric frustum's side/top/bottom
    // planes must) and gives a positive signedDistanceTo a point on the forward axis.
    val planes = listOf(
        planeThrough(near[BOTTOM_LEFT], near[BOTTOM_RIGHT], far[BOTTOM_RIGHT]), // bottom
        planeThrough(near[TOP_RIGHT], near[TOP_LEFT], far[TOP_RIGHT]), // top
        planeThrough(near[BOTTOM_LEFT], far[BOTTOM_LEFT], near[TOP_LEFT]), // left
        planeThrough(near[BOTTOM_RIGHT], near[TOP_RIGHT], far[TOP_RIGHT]), // right
        planeThrough(near[BOTTOM_LEFT], near[TOP_LEFT], near[TOP_RIGHT]), // near
        planeThrough(far[BOTTOM_LEFT], far[TOP_RIGHT], far[TOP_LEFT]), // far
    )
    return planes.filterNotNull().none { plane -> box.isFullyBehind(plane) }
}

/** The box is outside when its most-positive corner along the plane normal still sits behind it. */
private fun Aabb.isFullyBehind(plane: Plane): Boolean {
    val positiveX = if (plane.normal.x >= 0f) max.x else min.x
    val positiveY = if (plane.normal.y >= 0f) max.y else min.y
    val positiveZ = if (plane.normal.z >= 0f) max.z else min.z
    return plane.signedDistanceTo(Vec3(positiveX, positiveY, positiveZ)) < 0f
}

/** `null` for three collinear points, which describe no plane. */
private fun planeThrough(a: Vec3, b: Vec3, c: Vec3): Plane? {
    val normal = (b - a).cross(c - a)
    if (normal.length3() < DEGENERATE_NORMAL) return null
    return Plane.through(a, normal)
}

private const val NEAR_CORNER_COUNT = 4
// Matches Frustum.corners()'s own documented order: [nearBL, nearBR, nearTR, nearTL, ...].
private const val BOTTOM_LEFT = 0
private const val BOTTOM_RIGHT = 1
private const val TOP_RIGHT = 2
private const val TOP_LEFT = 3
private const val DEGENERATE_NORMAL = 1e-6f
