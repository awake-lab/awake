// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

import io.github.ronjunevaldoz.awake.core.math.Aabb

/** Opt-in occluder for [RenderSystem][io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem]'s
 * occlusion culling -- [localBounds] is that entity's bounds in its own local (pre-transform)
 * space, same "build from `MeshGeometry.vertices` via `Aabb.fromPositions`" convention
 * [MeshBounds] uses. A scene author decides which entities occlude by attaching this component;
 * it is independent of [MeshBounds] -- an occluder does not need to be cullable itself (it can
 * carry both), and a cullable entity does not need to be an occluder. An entity with no
 * `Occluder` in the world never occludes anything (occlusion culling never runs at all when
 * zero entities carry this component), same "system reads whatever's currently set, entity
 * opts in" shape [MeshBounds]/[SkinnedPose]/[PbrMaterial] already use. */
data class Occluder(
    val localBounds: Aabb,
)
