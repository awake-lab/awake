// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.mesh.gltf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The subset of the glTF 2.0 JSON schema (https://registry.khronos.org/glTF/specs/2.0/
 * glTF-2.0.html) this parser actually reads: enough to pull a single mesh's vertex
 * attributes and indices out of a `.gltf` file with embedded (base64 data-URI) buffers.
 * No scenes/nodes/materials/textures/animations/skinning -- see [GltfParser]'s doc comment
 * for why this is deliberately minimal, not an oversight.
 */
@Serializable
data class GltfDocument(
    val buffers: List<GltfBuffer> = emptyList(),
    val bufferViews: List<GltfBufferView> = emptyList(),
    val accessors: List<GltfAccessor> = emptyList(),
    val meshes: List<GltfMeshDef> = emptyList(),
    /** Index into [scenes] of the scene to load when none is explicitly requested -- glTF
     * default is `0` when the document has any scenes at all. */
    val scene: Int? = null,
    val scenes: List<GltfScene> = emptyList(),
    val nodes: List<GltfNode> = emptyList()
)

@Serializable
data class GltfScene(
    val nodes: List<Int> = emptyList()
)

@Serializable
data class GltfNode(
    val name: String? = null,
    val mesh: Int? = null,
    /** 16 column-major floats -- mutually exclusive with [translation]/[rotation]/[scale]
     * per the glTF 2.0 spec; when absent, TRS is composed instead. */
    val matrix: List<Float>? = null,
    val translation: List<Float>? = null,
    /** Quaternion `[x, y, z, w]`. */
    val rotation: List<Float>? = null,
    val scale: List<Float>? = null,
    val children: List<Int> = emptyList()
)

@Serializable
data class GltfBuffer(
    /** A `data:application/octet-stream;base64,...` (or similar) data URI -- external
     * file URIs (a relative path to a sibling `.bin`) are not supported by this parser;
     * see [GltfParser]'s doc comment. */
    val uri: String? = null,
    val byteLength: Int = 0
)

@Serializable
data class GltfBufferView(
    val buffer: Int,
    val byteOffset: Int = 0,
    val byteLength: Int = 0,
    val byteStride: Int? = null
)

@Serializable
data class GltfAccessor(
    val bufferView: Int? = null,
    val byteOffset: Int = 0,
    /** glTF's numeric component type constants -- `5120` BYTE, `5121` UNSIGNED_BYTE,
     * `5122` SHORT, `5123` UNSIGNED_SHORT, `5125` UNSIGNED_INT, `5126` FLOAT. Only
     * FLOAT (vertex attributes) and the three unsigned integer types (indices) are
     * actually handled -- see [GltfComponentType]. */
    val componentType: Int,
    val count: Int,
    /** `"SCALAR"`, `"VEC2"`, `"VEC3"`, or `"VEC4"` -- determines how many [componentType]
     * values make up one element. */
    val type: String
)

@Serializable
data class GltfPrimitiveAttributes(
    @SerialName("POSITION") val position: Int? = null,
    @SerialName("NORMAL") val normal: Int? = null,
    @SerialName("COLOR_0") val color0: Int? = null,
    @SerialName("TEXCOORD_0") val texCoord0: Int? = null
)

@Serializable
data class GltfPrimitive(
    val attributes: GltfPrimitiveAttributes,
    val indices: Int? = null
)

@Serializable
data class GltfMeshDef(
    val name: String? = null,
    val primitives: List<GltfPrimitive> = emptyList()
)
