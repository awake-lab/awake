// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

/** Metallic-roughness surface parameters for a [MeshRenderer] entity drawn by the primary lit
 * pipeline. Both are `var` so a demo or gameplay code can drive them from a slider.
 *
 * Mutually exclusive with [SkinnedPose]: both feed the same `DrawCall.extraUniformFloats`, but
 * they apply to different vertex formats, so no entity carries both. */
data class PbrMaterial(
    var metallic: Float = 0f,
    var roughness: Float = 0.5f,
)
