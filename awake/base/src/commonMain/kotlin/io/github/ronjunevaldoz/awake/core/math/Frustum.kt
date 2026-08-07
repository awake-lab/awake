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
            farCenter - right * farHalfWidth + up * farHalfHeight
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
        0 to 4, 1 to 5, 2 to 6, 3 to 7
    )
}
