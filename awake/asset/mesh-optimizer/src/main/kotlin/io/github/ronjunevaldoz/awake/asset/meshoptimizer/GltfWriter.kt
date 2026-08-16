// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.meshoptimizer

import io.github.ronjunevaldoz.awake.asset.gltf.GltfAccessor
import io.github.ronjunevaldoz.awake.asset.gltf.GltfBuffer
import io.github.ronjunevaldoz.awake.asset.gltf.GltfBufferView
import io.github.ronjunevaldoz.awake.asset.gltf.GltfDocument
import io.github.ronjunevaldoz.awake.asset.gltf.GltfMeshDef
import io.github.ronjunevaldoz.awake.asset.gltf.GltfNode
import io.github.ronjunevaldoz.awake.asset.gltf.GltfPrimitive
import io.github.ronjunevaldoz.awake.asset.gltf.GltfPrimitiveAttributes
import io.github.ronjunevaldoz.awake.asset.gltf.GltfScene
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val COMPONENT_TYPE_FLOAT = 5126
private const val COMPONENT_TYPE_UNSIGNED_INT = 5125
private const val BYTES_PER_FLOAT = 4
private const val BYTES_PER_UINT = 4
private const val POSITION_COMPONENTS = 3

/**
 * The minimal counterpart to [io.github.ronjunevaldoz.awake.asset.gltf.GltfParser.parse]'s
 * "first mesh, first primitive" reading -- writes a single-primitive, position-only (plus
 * indices) plain-JSON `.gltf` with one embedded base64 buffer, round-trippable by that same
 * `parse()` call. Not a general glTF exporter: no materials/normals/UVs/skinning, matching
 * [io.github.ronjunevaldoz.awake.core.geometry.MeshSimplifier]'s own "position-only in v1"
 * scope -- this only exists to prove the decimate-and-re-import pipeline round-trips.
 */
@OptIn(ExperimentalEncodingApi::class)
object GltfWriter {
    private val json = Json { prettyPrint = false }

    fun writePositionOnlyMesh(positions: FloatArray, indices: IntArray): String {
        val vertexCount = positions.size / POSITION_COMPONENTS
        val positionBytes = vertexCount * POSITION_COMPONENTS * BYTES_PER_FLOAT
        val indexBytes = indices.size * BYTES_PER_UINT
        val buffer = ByteArray(positionBytes + indexBytes)

        var offset = 0
        for (value in positions) {
            writeFloatLe(buffer, offset, value)
            offset += BYTES_PER_FLOAT
        }
        for (value in indices) {
            writeUIntLe(buffer, offset, value)
            offset += BYTES_PER_UINT
        }

        val document = GltfDocument(
            buffers = listOf(
                GltfBuffer(
                    uri = "data:application/octet-stream;base64,${Base64.encode(buffer)}",
                    byteLength = buffer.size,
                ),
            ),
            bufferViews = listOf(
                GltfBufferView(buffer = 0, byteOffset = 0, byteLength = positionBytes),
                GltfBufferView(buffer = 0, byteOffset = positionBytes, byteLength = indexBytes),
            ),
            accessors = listOf(
                GltfAccessor(bufferView = 0, componentType = COMPONENT_TYPE_FLOAT, count = vertexCount, type = "VEC3"),
                GltfAccessor(bufferView = 1, componentType = COMPONENT_TYPE_UNSIGNED_INT, count = indices.size, type = "SCALAR"),
            ),
            meshes = listOf(
                GltfMeshDef(
                    name = "decimated",
                    primitives = listOf(
                        GltfPrimitive(attributes = GltfPrimitiveAttributes(position = 0), indices = 1),
                    ),
                ),
            ),
            nodes = listOf(GltfNode(mesh = 0)),
            scenes = listOf(GltfScene(nodes = listOf(0))),
            scene = 0,
        )
        return json.encodeToString(GltfDocument.serializer(), document)
    }

    private fun writeFloatLe(bytes: ByteArray, offset: Int, value: Float) = writeUIntLe(bytes, offset, value.toRawBits())

    private fun writeUIntLe(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }
}
