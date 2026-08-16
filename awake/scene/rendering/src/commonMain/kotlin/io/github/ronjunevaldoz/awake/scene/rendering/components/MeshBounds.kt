// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

import io.github.ronjunevaldoz.awake.core.math.Aabb

/** Optional add-on for a [MeshRenderer] entity -- [localBounds] is that mesh's bounds in its
 * own local (pre-transform) space, typically built from the same `MeshGeometry.vertices`
 * passed to [io.github.ronjunevaldoz.awake.render.renderer.Renderer.createMesh] via
 * `Aabb.fromPositions`. [RenderSystem][io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem]
 * reads it into that entity's [Transform][io.github.ronjunevaldoz.awake.scene.core.components.Transform]
 * to frustum-cull the entity when it can't possibly be visible -- an entity with no `MeshBounds`
 * is never culled (always drawn, same as before this component existed), same "system reads
 * whatever's currently set, entity opts in by adding the component" shape [SkinnedPose]/
 * [PbrMaterial] already use. */
data class MeshBounds(
    val localBounds: Aabb,
)
