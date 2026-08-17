// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

/** Surface parameters for a [MeshRenderer] entity drawn by the primary lit pipeline or by the
 * textured/glTF PBR pipeline (`VertexFormat.PositionNormalColorUv`). All `var` so a demo or
 * gameplay code can drive them from a slider.
 *
 * [metallic]/[roughness] mean two different things depending on which pipeline reads them: the
 * primary lit pipeline (no textures) uses them directly as the surface's only metallic/
 * roughness values; the textured pipeline multiplies them into its metallic-roughness texture
 * sample as glTF's own `metallicFactor`/`roughnessFactor` do. [baseColorFactor]/[emissiveFactor]
 * are read only by the textured pipeline, same glTF-factor role -- the primary pipeline has no
 * base-color/emissive texture to multiply them into.
 *
 * Mutually exclusive with [SkinnedPose]: both feed the same `DrawCall.extraUniformFloats`, but
 * they apply to different vertex formats, so no entity carries both. */
data class PbrMaterial(
    var metallic: Float = 0f,
    var roughness: Float = 0.5f,
    var baseColorFactor: FloatArray = floatArrayOf(1f, 1f, 1f, 1f),
    var emissiveFactor: FloatArray = floatArrayOf(0f, 0f, 0f),
)
