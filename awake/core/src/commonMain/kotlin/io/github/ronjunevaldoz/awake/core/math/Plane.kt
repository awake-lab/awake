// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.sqrt

/**
 * An infinite plane, stored as a unit [normal] and the signed [distance] from the origin along
 * it (`dot(normal, point) + distance == 0` on the surface).
 *
 * The constraint surface a plane-locked gizmo drag, a ground snap, or a frustum side needs.
 * [normal] is normalized on construction for the same reason [Ray]'s direction is: an
 * unnormalized normal silently scales every distance this plane reports.
 */
data class Plane(val normal: Vec3, val distance: Float) {

    init {
        val length = sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z)
        require(length > EPSILON) { "A plane needs a normal, got $normal." }
        normal.x /= length
        normal.y /= length
        normal.z /= length
    }

    /** Positive in front of the plane (the side [normal] points to), negative behind, zero on it. */
    fun signedDistanceTo(point: Vec3): Float =
        normal.x * point.x + normal.y * point.y + normal.z * point.z + distance

    fun project(point: Vec3): Vec3 {
        val signed = signedDistanceTo(point)
        return Vec3(
            point.x - normal.x * signed,
            point.y - normal.y * signed,
            point.z - normal.z * signed,
        )
    }

    companion object {
        private const val EPSILON = 1e-6f

        /** The plane with [normal] passing through [point] -- how a caller with a surface and a
         * direction actually thinks about it, rather than solving for the origin distance. */
        fun through(point: Vec3, normal: Vec3): Plane {
            val length = sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z)
            require(length > EPSILON) { "A plane needs a normal, got $normal." }
            val unit = Vec3(normal.x / length, normal.y / length, normal.z / length)
            return Plane(unit, -(unit.x * point.x + unit.y * point.y + unit.z * point.z))
        }
    }
}
