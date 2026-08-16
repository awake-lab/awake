// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh

/**
 * One entity, N copies of [mesh]/[material] drawn in a single GPU instanced draw call --
 * each of [transforms] is a full world matrix (not composed with the entity's own
 * [io.github.ronjunevaldoz.awake.scene.core.components.Transform], which this component
 * doesn't require at all). A dedicated component rather than a batching pass over ordinary
 * [MeshRenderer] entities: grouping arbitrary entities by shared mesh identity every frame is
 * its own perf-sensitive design problem (sort/regroup cost, cache invalidation) -- this is the
 * opt-in "I already know these N copies belong together" case (procedural scattering: grass,
 * rocks, repeated props), not an automatic optimization applied to every entity.
 *
 * Static geometry only -- there's no `extraUniformFloats`-equivalent slot here, so a
 * GPU-skinned [mesh] (which needs its own per-draw joint palette) can't be instanced this way;
 * see [io.github.ronjunevaldoz.awake.render.renderer.DrawCall.instanceModels]'s doc comment
 * for why animated instancing needs a materially different technique (a storage-buffer joint
 * palette indexed by instance ID) this engine doesn't have yet.
 */
data class InstancedMeshRenderer(
    val mesh: Mesh,
    val material: Material,
    val transforms: List<Mat4>,
)
