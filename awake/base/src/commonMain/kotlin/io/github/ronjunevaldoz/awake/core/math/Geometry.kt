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

private const val POSITION_COMPONENTS = 3
