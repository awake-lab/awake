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

import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val COMPONENT_TYPE_UNSIGNED_BYTE = 5121
private const val COMPONENT_TYPE_UNSIGNED_SHORT = 5123
private const val COMPONENT_TYPE_UNSIGNED_INT = 5125
private const val COMPONENT_TYPE_FLOAT = 5126

private const val BYTES_PER_FLOAT = 4
private const val BASE64_DATA_URI_MARKER = ";base64,"

private val GltfJson = Json { ignoreUnknownKeys = true }

/**
 * Minimal glTF 2.0 mesh importer -- JSON structure + embedded (base64 data-URI) buffers
 * only. Deliberately narrow scope for this MVP phase (per docs/MVP_PLAN.md's Phase 4
 * checklist: "cube can be hardcoded, but the first real model needs this. Full glTF
 * (skinning, animation) is post-MVP"):
 * - **No external `.bin` buffer files** -- only base64 data URIs are decoded. A real asset
 *   pipeline would resolve an external `uri` relative to the `.gltf`'s own resource path
 *   via `readResourceBytes`; deferred since a self-contained (embedded-buffer) `.gltf` is
 *   sufficient to prove the accessor/bufferView decoding this parser exists for.
 * - **No scene graph, materials, textures, animations, or skinning** -- only the first
 *   mesh's first primitive's `POSITION`/`NORMAL`/`COLOR_0`/`TEXCOORD_0` attributes and its
 *   index accessor are read.
 * - **Only `FLOAT` vertex attributes and `UNSIGNED_BYTE`/`UNSIGNED_SHORT`/`UNSIGNED_INT`
 *   indices** are decoded -- glTF also allows signed `BYTE`/`SHORT` and normalized integer
 *   attributes, neither of which real exporters emit for `POSITION`/`NORMAL`/`COLOR_0` in
 *   practice.
 */
object GltfParser {
    fun parse(json: String): GltfMesh {
        val document = GltfJson.decodeFromString(GltfDocument.serializer(), json)
        val mesh = document.meshes.firstOrNull()
            ?: error("glTF document has no meshes.")
        val primitive = mesh.primitives.firstOrNull()
            ?: error("glTF mesh '${mesh.name}' has no primitives.")

        val buffers = document.buffers.map(::decodeBuffer)

        val positionAccessor = primitive.attributes.position
            ?: error("glTF primitive is missing a POSITION attribute.")
        val positions = readFloatAccessor(document, buffers, positionAccessor, componentsPerElement = 3)
        val normals = primitive.attributes.normal?.let {
            readFloatAccessor(document, buffers, it, componentsPerElement = 3)
        }
        val colors = primitive.attributes.color0?.let {
            readFloatAccessor(document, buffers, it, componentsPerElement = 3)
        }
        val uvs = primitive.attributes.texCoord0?.let {
            readFloatAccessor(document, buffers, it, componentsPerElement = 2)
        }
        val indices = primitive.indices?.let { readIndexAccessor(document, buffers, it) }
            ?: IntArray(positions.size / 3) { it }

        return GltfMesh(positions, normals, colors, uvs, indices)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeBuffer(buffer: GltfBuffer): ByteArray {
        val uri = buffer.uri
            ?: error(
                "glTF buffer has no uri -- external .bin buffers are not supported by " +
                    "this parser (only base64 data URIs)."
            )
        val markerIndex = uri.indexOf(BASE64_DATA_URI_MARKER)
        require(uri.startsWith("data:") && markerIndex >= 0) {
            "glTF buffer uri is not a base64 data URI -- external .bin file references " +
                "are not supported by this parser: $uri"
        }
        return Base64.decode(uri.substring(markerIndex + BASE64_DATA_URI_MARKER.length))
    }

    private fun accessorAt(document: GltfDocument, index: Int): GltfAccessor {
        return document.accessors.getOrElse(index) {
            error("glTF accessor index $index is out of range (${document.accessors.size} accessors).")
        }
    }

    private fun bufferViewFor(document: GltfDocument, accessor: GltfAccessor, accessorIndex: Int): GltfBufferView {
        val bufferViewIndex = accessor.bufferView
            ?: error("glTF accessor $accessorIndex has no bufferView -- sparse accessors are not supported.")
        return document.bufferViews.getOrElse(bufferViewIndex) {
            error("glTF bufferView index $bufferViewIndex is out of range.")
        }
    }

    private fun componentSize(componentType: Int): Int {
        return when (componentType) {
            COMPONENT_TYPE_UNSIGNED_BYTE -> 1
            COMPONENT_TYPE_UNSIGNED_SHORT -> 2
            COMPONENT_TYPE_UNSIGNED_INT, COMPONENT_TYPE_FLOAT -> BYTES_PER_FLOAT
            else -> error("Unsupported glTF accessor componentType: $componentType")
        }
    }

    private fun typeComponentCount(type: String): Int {
        return when (type) {
            "SCALAR" -> 1
            "VEC2" -> 2
            "VEC3" -> 3
            "VEC4" -> 4
            else -> error("Unsupported glTF accessor type: $type")
        }
    }

    private fun readFloatAccessor(
        document: GltfDocument,
        buffers: List<ByteArray>,
        accessorIndex: Int,
        componentsPerElement: Int
    ): FloatArray {
        val accessor = accessorAt(document, accessorIndex)
        require(accessor.componentType == COMPONENT_TYPE_FLOAT) {
            "glTF accessor $accessorIndex has componentType ${accessor.componentType}, expected FLOAT (5126)."
        }
        require(typeComponentCount(accessor.type) == componentsPerElement) {
            "glTF accessor $accessorIndex has type ${accessor.type}, expected " +
                "$componentsPerElement-component elements."
        }
        val bufferView = bufferViewFor(document, accessor, accessorIndex)
        val bytes = buffers[bufferView.buffer]
        val elementByteSize = componentsPerElement * BYTES_PER_FLOAT
        val stride = bufferView.byteStride ?: elementByteSize
        val base = bufferView.byteOffset + accessor.byteOffset

        val result = FloatArray(accessor.count * componentsPerElement)
        for (elementIndex in 0 until accessor.count) {
            val elementBase = base + elementIndex * stride
            for (component in 0 until componentsPerElement) {
                result[elementIndex * componentsPerElement + component] =
                    readFloatLe(bytes, elementBase + component * BYTES_PER_FLOAT)
            }
        }
        return result
    }

    private fun readIndexAccessor(
        document: GltfDocument,
        buffers: List<ByteArray>,
        accessorIndex: Int
    ): IntArray {
        val accessor = accessorAt(document, accessorIndex)
        require(typeComponentCount(accessor.type) == 1) {
            "glTF index accessor $accessorIndex has type ${accessor.type}, expected SCALAR."
        }
        val bufferView = bufferViewFor(document, accessor, accessorIndex)
        val bytes = buffers[bufferView.buffer]
        val stride = bufferView.byteStride ?: componentSize(accessor.componentType)
        val base = bufferView.byteOffset + accessor.byteOffset

        return IntArray(accessor.count) { elementIndex ->
            val offset = base + elementIndex * stride
            when (accessor.componentType) {
                COMPONENT_TYPE_UNSIGNED_BYTE -> bytes[offset].toInt() and 0xFF
                COMPONENT_TYPE_UNSIGNED_SHORT -> readUShortLe(bytes, offset)
                COMPONENT_TYPE_UNSIGNED_INT -> readUIntLe(bytes, offset)
                else -> error("Unsupported glTF index componentType: ${accessor.componentType}")
            }
        }
    }

    private fun readFloatLe(bytes: ByteArray, offset: Int): Float {
        return Float.fromBits(readUIntLe(bytes, offset))
    }

    private fun readUShortLe(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)
    }

    private fun readUIntLe(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }
}
