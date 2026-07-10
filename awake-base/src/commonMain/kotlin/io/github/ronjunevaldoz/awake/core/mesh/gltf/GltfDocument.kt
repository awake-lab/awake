/*
 * Awake
 * Awake.awake-core.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    val meshes: List<GltfMeshDef> = emptyList()
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
