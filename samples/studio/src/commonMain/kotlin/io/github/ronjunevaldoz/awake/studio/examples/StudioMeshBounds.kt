// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry

/**
 * Local-space bounds for the meshes studio registers, keyed by the same asset ID a scene
 * document uses.
 *
 * Computed from the geometry rather than stored alongside it because `MeshGeometry` reaches the
 * GPU and is not kept: this is the last point where the vertices are still on the CPU. The glTF
 * meshes are absent until their geometry is loaded -- picking simply skips an entity with no
 * entry rather than guessing a size.
 */
internal object StudioMeshBounds {
    private val bounds = mutableMapOf<String, Aabb>()

    operator fun get(meshId: String): Aabb? = bounds[meshId]

    fun register(meshId: String, geometry: MeshGeometry) {
        geometry.bounds?.let { bounds[meshId] = it }
    }

    fun register(meshId: String, positions: FloatArray, stride: Int) {
        Aabb.fromPositions(positions, stride)?.let { bounds[meshId] = it }
    }
}
