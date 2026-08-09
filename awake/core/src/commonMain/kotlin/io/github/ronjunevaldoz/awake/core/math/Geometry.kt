// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.sqrt

/** Largest distance from the origin across every vertex position in [positions] (a flat
 * x/y/z-per-vertex array, e.g. [io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfMesh
 * .positions]) -- a cheap bounding-sphere radius, good enough to pick a camera zoom that frames
 * a whole model without computing a real AABB. Returns `1f` for an empty/degenerate
 * (all-zero) [positions], never `0f`, so a caller can safely divide by this to normalize scale. */
fun boundingRadius(positions: FloatArray): Float {
    var maxDistanceSquared = 0f
    var i = 0
    while (i < positions.size) {
        val x = positions[i]
        val y = positions[i + 1]
        val z = positions[i + 2]
        val distanceSquared = x * x + y * y + z * z
        if (distanceSquared > maxDistanceSquared) maxDistanceSquared = distanceSquared
        i += POSITION_COMPONENTS
    }
    return if (maxDistanceSquared > 0f) sqrt(maxDistanceSquared) else 1f
}

/**
 * Midpoint of the axis-aligned bounding box around [positions] (a flat `x, y, z, x, y, z...`
 * array). Returns the origin for an empty array.
 *
 * This is the point a camera should orbit or aim at: unlike averaging the vertices, it is not
 * pulled toward whichever region of the model happens to be most densely tessellated.
 */
fun boundingCenter(positions: FloatArray): Vec3 {
    if (positions.size < POSITION_COMPONENTS) return Vec3(0f, 0f, 0f)
    var minX = Float.POSITIVE_INFINITY
    var minY = Float.POSITIVE_INFINITY
    var minZ = Float.POSITIVE_INFINITY
    var maxX = Float.NEGATIVE_INFINITY
    var maxY = Float.NEGATIVE_INFINITY
    var maxZ = Float.NEGATIVE_INFINITY
    var i = 0
    while (i + 2 < positions.size) {
        val x = positions[i]
        val y = positions[i + 1]
        val z = positions[i + 2]
        if (x < minX) minX = x
        if (y < minY) minY = y
        if (z < minZ) minZ = z
        if (x > maxX) maxX = x
        if (y > maxY) maxY = y
        if (z > maxZ) maxZ = z
        i += POSITION_COMPONENTS
    }
    return Vec3((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f)
}

private const val POSITION_COMPONENTS = 3
