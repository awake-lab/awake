// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.components

/** Optional add-on for a [MeshRenderer] entity whose mesh uses a GPU-skinned vertex format --
 * [RenderSystem][io.github.ronjunevaldoz.awake.scene.systems.RenderSystem] reads
 * [jointPalette] into that entity's `DrawCall.extraUniformFloats` every frame. A joint
 * palette is animated per-frame state (not static entity data), so [jointPalette] is a `var`
 * gameplay code mutates in place each frame -- same "system reads whatever's currently set"
 * shape [io.github.ronjunevaldoz.awake.scene.components.SpinControl.radians] already uses,
 * not a value this component computes itself. */
data class SkinnedPose(
    var jointPalette: FloatArray,
)
