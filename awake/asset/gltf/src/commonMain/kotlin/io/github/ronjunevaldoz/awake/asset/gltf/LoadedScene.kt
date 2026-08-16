// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.gltf

import io.github.ronjunevaldoz.awake.core.math.Mat4

/**
 * A single decoded glTF primitive, ready to hand to a backend's vertex/index buffer upload --
 * see [GltfParser.parseScene]. [vertices] is interleaved pos3+color3+uv2 (8 floats/vertex),
 * the same layout [GltfMesh.toInterleavedPositionColorUv] already produces. [baseColorImageBytes]
 * is this primitive's own material's base color texture, still encoded -- `null` unless this
 * primitive has a material with one -- same "per-primitive, not per-mesh" scoping [GltfMesh]
 * already uses, since sibling primitives of one mesh (e.g. skin vs. clothing) commonly use
 * different materials. [metallicRoughnessImageBytes]/[normalImageBytes]/[occlusionImageBytes]/
 * [emissiveImageBytes] are the same per-primitive scoping for the rest of [GltfMesh]'s PBR
 * texture channels.
 */
data class LoadedPrimitive(
    val vertices: FloatArray,
    val indices: IntArray,
    val localTransform: Mat4,
    val baseColorImageBytes: ByteArray? = null,
    val metallicRoughnessImageBytes: ByteArray? = null,
    val normalImageBytes: ByteArray? = null,
    val occlusionImageBytes: ByteArray? = null,
    val emissiveImageBytes: ByteArray? = null,
)

data class LoadedMesh(
    val name: String,
    val primitives: List<LoadedPrimitive>,
)

data class LoadedScene(
    val meshes: List<LoadedMesh>,
)
