// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.mesh.gltf

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@OptIn(ExperimentalEncodingApi::class)
class GltfParserTest {
    /** A single flat triangle: v0=(0,0,0), v1=(1,0,0), v2=(0,1,0), indices=[0,1,2]. Bytes
     * are built (not hand-encoded) so the test can't have the same base64 typo the parser
     * might: 9 floats (positions) followed by 3 unsigned shorts (indices), matching the
     * bufferView/accessor offsets declared in [gltfJson]. */
    private fun triangleBufferBase64(): String {
        val positions = floatArrayOf(
            0f, 0f, 0f,
            1f, 0f, 0f,
            0f, 1f, 0f,
        )
        val indices = shortArrayOf(0, 1, 2)
        val bytes = ByteArray(positions.size * 4 + indices.size * 2)
        var offset = 0
        for (value in positions) {
            val bits = value.toRawBits()
            bytes[offset] = (bits and 0xFF).toByte()
            bytes[offset + 1] = ((bits ushr 8) and 0xFF).toByte()
            bytes[offset + 2] = ((bits ushr 16) and 0xFF).toByte()
            bytes[offset + 3] = ((bits ushr 24) and 0xFF).toByte()
            offset += 4
        }
        for (value in indices) {
            bytes[offset] = (value.toInt() and 0xFF).toByte()
            bytes[offset + 1] = ((value.toInt() ushr 8) and 0xFF).toByte()
            offset += 2
        }
        return Base64.encode(bytes)
    }

    private fun gltfJson(bufferBase64: String): String = """
        {
          "buffers": [
            { "uri": "data:application/octet-stream;base64,$bufferBase64", "byteLength": 42 }
          ],
          "bufferViews": [
            { "buffer": 0, "byteOffset": 0, "byteLength": 36 },
            { "buffer": 0, "byteOffset": 36, "byteLength": 6 }
          ],
          "accessors": [
            { "bufferView": 0, "componentType": 5126, "count": 3, "type": "VEC3" },
            { "bufferView": 1, "componentType": 5123, "count": 3, "type": "SCALAR" }
          ],
          "meshes": [
            {
              "name": "triangle",
              "primitives": [
                { "attributes": { "POSITION": 0 }, "indices": 1 }
              ]
            }
          ]
        }
    """.trimIndent()

    @Test
    fun parsesPositionsAndIndicesFromEmbeddedBuffer() {
        val mesh = GltfParser.parse(gltfJson(triangleBufferBase64()))

        assertEquals(3, mesh.vertexCount)
        assertEquals(
            listOf(0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f),
            mesh.positions.toList(),
        )
        assertEquals(listOf(0, 1, 2), mesh.indices.toList())
        assertNull(mesh.normals)
        assertNull(mesh.colors)
        assertNull(mesh.uvs)
    }

    @Test
    fun interleavesWithDefaultWhiteColorAndZeroUvWhenAbsent() {
        val mesh = GltfParser.parse(gltfJson(triangleBufferBase64()))

        val interleaved = mesh.toInterleavedPositionColorUv()

        assertEquals(3 * 8, interleaved.size)
        // Vertex 1 (index 1): position (1,0,0), default color (1,1,1), default uv (0,0)
        val vertex1 = interleaved.toList().subList(8, 16)
        assertEquals(listOf(1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f), vertex1)
    }

    @Test
    fun rejectsExternalBufferUrisWithoutProvidedBytes() {
        val json = """
            {
              "buffers": [ { "uri": "mesh.bin", "byteLength": 42 } ],
              "bufferViews": [ { "buffer": 0, "byteOffset": 0, "byteLength": 36 } ],
              "accessors": [ { "bufferView": 0, "componentType": 5126, "count": 3, "type": "VEC3" } ],
              "meshes": [ { "primitives": [ { "attributes": { "POSITION": 0 } } ] } ]
            }
        """.trimIndent()

        assertFailsWith<IllegalStateException> { GltfParser.parse(json) }
    }

    @Test
    fun resolvesExternalBufferUriFromProvidedExternalResources() {
        val positions = floatArrayOf(
            0f, 0f, 0f,
            1f, 0f, 0f,
            0f, 1f, 0f,
        )
        val bytes = ByteArray(positions.size * 4)
        var offset = 0
        for (value in positions) {
            val bits = value.toRawBits()
            bytes[offset] = (bits and 0xFF).toByte()
            bytes[offset + 1] = ((bits ushr 8) and 0xFF).toByte()
            bytes[offset + 2] = ((bits ushr 16) and 0xFF).toByte()
            bytes[offset + 3] = ((bits ushr 24) and 0xFF).toByte()
            offset += 4
        }
        val json = """
            {
              "buffers": [ { "uri": "mesh.bin", "byteLength": ${bytes.size} } ],
              "bufferViews": [ { "buffer": 0, "byteOffset": 0, "byteLength": ${bytes.size} } ],
              "accessors": [ { "bufferView": 0, "componentType": 5126, "count": 3, "type": "VEC3" } ],
              "meshes": [ { "primitives": [ { "attributes": { "POSITION": 0 } } ] } ]
            }
        """.trimIndent()

        assertEquals(listOf("mesh.bin"), GltfParser.externalUris(json))
        val mesh = GltfParser.parse(json, externalResources = mapOf("mesh.bin" to bytes))
        assertEquals(positions.toList(), mesh.positions.toList())
    }
}
