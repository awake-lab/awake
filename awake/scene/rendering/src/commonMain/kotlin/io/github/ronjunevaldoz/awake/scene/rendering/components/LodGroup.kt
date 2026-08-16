// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh

/** One distance band's worth of an [LodGroup] -- [mesh] draws while the entity is at most
 * [maxDistance] from the camera eye. Typically a decimated mesh per level (see
 * `awake:core:geometry`'s `MeshSimplifier`/`awake:asset:mesh-optimizer`'s CLI for baking
 * these offline), highest detail first. */
data class LodLevel(
    val mesh: Mesh,
    val material: Material,
    val maxDistance: Float,
)

/**
 * A [MeshRenderer]-alternative for an entity with multiple detail levels --
 * [RenderSystem][io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem] picks
 * one [LodLevel] per frame by the entity's distance to the primary camera's eye, same "opt-in
 * sibling component, entity picks one shape or the other" relationship [InstancedMeshRenderer]
 * has to [MeshRenderer] -- an entity carries an [LodGroup] instead of a [MeshRenderer], not
 * both.
 *
 * [levels] must be sorted ascending by [LodLevel.maxDistance] -- the first level whose
 * [LodLevel.maxDistance] is at least the entity's actual distance draws; if the entity is
 * farther than every level's threshold, the last (coarsest) level still draws rather than the
 * entity vanishing -- LOD selects detail, it doesn't cull (see [MeshBounds]/frustum culling
 * for that, which composes with this independently).
 */
data class LodGroup(
    val levels: List<LodLevel>,
) {
    init {
        require(levels.isNotEmpty()) { "LodGroup needs at least one LodLevel." }
    }
}
