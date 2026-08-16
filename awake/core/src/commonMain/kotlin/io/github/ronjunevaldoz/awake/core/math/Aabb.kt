// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.max
import kotlin.math.min

/**
 * An axis-aligned bounding box, the shape picking and culling actually want.
 *
 * [boundingRadius]/[boundingCenter] already give a sphere, which is enough to frame a whole model
 * but far too generous for hit-testing: a ground plane's sphere swallows everything standing on
 * it. Same "pure geometry, zero GPU dependency, unit-tested" role [Frustum] and [Grid] play.
 *
 * Immutable, unlike [Vec3]: a box is derived data (from a mesh, or from another box), so the
 * in-place mutation that keeps per-frame vector math allocation-free buys nothing here.
 */
data class Aabb(val min: Vec3, val max: Vec3) {

    val center: Vec3 get() = Vec3((min.x + max.x) * HALF, (min.y + max.y) * HALF, (min.z + max.z) * HALF)

    /** Half the size on each axis -- what a caller scaling a box or building a sphere from it
     * needs, and always the quantity people write `(max - min) / 2` for by hand. */
    val extents: Vec3 get() = Vec3((max.x - min.x) * HALF, (max.y - min.y) * HALF, (max.z - min.z) * HALF)

    operator fun contains(point: Vec3): Boolean =
        point.x in min.x..max.x && point.y in min.y..max.y && point.z in min.z..max.z

    fun union(other: Aabb): Aabb = Aabb(
        min = Vec3(min(min.x, other.min.x), min(min.y, other.min.y), min(min.z, other.min.z)),
        max = Vec3(max(max.x, other.max.x), max(max.y, other.max.y), max(max.z, other.max.z)),
    )

    /**
     * This box transformed by [matrix], re-fitted to the axes.
     *
     * All eight corners are transformed, not just min and max: under rotation those two corners
     * do not stay the box's extremes, and transforming only them produces a box that is too
     * small in exactly the diagonal directions where geometry sticks out. The result is a bound
     * on the transformed box, so it grows under repeated rotation -- rebuild from the source
     * geometry rather than chaining this.
     */
    fun transformed(matrix: Mat4): Aabb {
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var minZ = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        var maxZ = -Float.MAX_VALUE
        for (corner in corners()) {
            val transformed = matrix.transformPosition(Vec4(corner.x, corner.y, corner.z, 1f))
            minX = min(minX, transformed.x)
            minY = min(minY, transformed.y)
            minZ = min(minZ, transformed.z)
            maxX = max(maxX, transformed.x)
            maxY = max(maxY, transformed.y)
            maxZ = max(maxZ, transformed.z)
        }
        return Aabb(Vec3(minX, minY, minZ), Vec3(maxX, maxY, maxZ))
    }

    /** This box's 8 corners, one bit per axis (every min/max combination) -- same order
     * [EDGES] indexes into, for a debug-line renderer turning a box into a wireframe the same
     * way [Frustum.EDGES] already does for a frustum. */
    fun corners(): List<Vec3> = List(CORNER_COUNT) { corner ->
        Vec3(
            if (corner and X_BIT == 0) min.x else max.x,
            if (corner and Y_BIT == 0) min.y else max.y,
            if (corner and Z_BIT == 0) min.z else max.z,
        )
    }

    companion object {
        private const val HALF = 0.5f
        private const val CORNER_COUNT = 8
        private const val X_BIT = 1
        private const val Y_BIT = 2
        private const val Z_BIT = 4
        private const val COMPONENTS_PER_POSITION = 3

        /** The 12 edges of a box, each a (start, end) index pair into [corners]'s 8-element
         * result -- same "index pairs, not duplicated points" shape [Frustum.EDGES] uses. */
        val EDGES: List<Pair<Int, Int>> = listOf(
            // bottom face (z bit 0): x varies at y=0, then y varies at x=1, etc.
            0 to 1, 1 to 3, 3 to 2, 2 to 0,
            // top face (z bit 1)
            4 to 5, 5 to 7, 7 to 6, 6 to 4,
            // vertical edges connecting bottom to top
            0 to 4, 1 to 5, 2 to 6, 3 to 7,
        )

        /**
         * The tight box around packed `x, y, z` triples, or `null` for empty/degenerate input --
         * the same `positions` layout [boundingRadius] takes.
         *
         * `null` rather than a zero-size box at the origin: "this mesh has no vertices" and "this
         * mesh is a point at the origin" are different facts, and a caller that picks against the
         * second one silently hit-tests a thing that isn't there.
         */
        fun fromPositions(positions: FloatArray, stride: Int = COMPONENTS_PER_POSITION): Aabb? {
            if (stride < COMPONENTS_PER_POSITION || positions.size < stride) return null
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var minZ = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE
            var maxY = -Float.MAX_VALUE
            var maxZ = -Float.MAX_VALUE
            var index = 0
            while (index + 2 < positions.size) {
                minX = min(minX, positions[index])
                minY = min(minY, positions[index + 1])
                minZ = min(minZ, positions[index + 2])
                maxX = max(maxX, positions[index])
                maxY = max(maxY, positions[index + 1])
                maxZ = max(maxZ, positions[index + 2])
                index += stride
            }
            return Aabb(Vec3(minX, minY, minZ), Vec3(maxX, maxY, maxZ))
        }
    }
}
