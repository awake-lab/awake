// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

/**
 * A square reference/floor grid on the XZ plane at a fixed height -- same "pure geometry
 * helper, zero GPU dependency, unit-tested" role [Frustum] already plays for the debug-line
 * wireframe feature.
 *
 * Returns `(start, end)` point pairs rather than
 * [io.github.ronjunevaldoz.awake.render.renderer.LineSegment] directly, mirroring
 * [Frustum.corners]/[Frustum.EDGES]'s own split (points here, the caller assembles the
 * renderer-facing line type with its own color) -- this module (`awake-base`) doesn't depend
 * on `awake-engine-render-api` (where `LineSegment` lives), so returning `LineSegment` here
 * isn't possible without inverting that dependency; a caller in a module that already depends
 * on both (e.g. `sample-hello-cube`'s demos) does `Grid.lines(...).map { (a, b) ->
 * LineSegment(a, b, color) }`, same as those demos already do for [Frustum.EDGES].
 */
object Grid {
    /**
     * Produces `2 * (divisions + 1)` line segments: `divisions + 1` lines parallel to X
     * (each at a fixed Z) and `divisions + 1` lines parallel to Z (each at a fixed X),
     * spanning `[-size / 2, size / 2]` on both axes at height [y] -- a `divisions`-by-
     * `divisions` cell grid, lines crossing at right angles.
     */
    fun lines(size: Float, divisions: Int, y: Float = 0f): List<Pair<Vec3, Vec3>> {
        require(divisions >= 1) { "divisions must be >= 1, was $divisions" }
        val half = size / 2f
        val step = size / divisions
        val result = ArrayList<Pair<Vec3, Vec3>>((divisions + 1) * 2)

        // Lines parallel to X, one per Z coordinate.
        for (i in 0..divisions) {
            val z = -half + i * step
            result += Vec3(-half, y, z) to Vec3(half, y, z)
        }
        // Lines parallel to Z, one per X coordinate.
        for (i in 0..divisions) {
            val x = -half + i * step
            result += Vec3(x, y, -half) to Vec3(x, y, half)
        }

        return result
    }
}
