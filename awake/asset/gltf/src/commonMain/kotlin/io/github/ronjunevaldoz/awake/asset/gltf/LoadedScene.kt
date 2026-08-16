// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.gltf

import io.github.ronjunevaldoz.awake.core.math.Mat4

/**
 * A single decoded glTF primitive, ready to hand to a backend's vertex/index buffer upload --
 * see [GltfParser.parseScene]. [vertices] is interleaved pos3+color3+uv2 (8 floats/vertex),
 * the same layout [GltfMesh.toInterleavedPositionColorUv] already produces.
 */
data class LoadedPrimitive(
    val vertices: FloatArray,
    val indices: IntArray,
    val localTransform: Mat4,
)

data class LoadedMesh(
    val name: String,
    val primitives: List<LoadedPrimitive>,
)

data class LoadedScene(
    val meshes: List<LoadedMesh>,
)
