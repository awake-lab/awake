// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.mesh.gltf

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [GltfParser.parseScene] (GLB container + `scenes`/`nodes` traversal) tests -- builds real
 * GLB bytes programmatically (header + JSON chunk + BIN chunk), same spirit as
 * [GltfParserTest]'s `triangleBufferBase64()` helper.
 */
class GltfParserSceneTest {
    /** A single flat triangle: v0=(0,0,0), v1=(1,0,0), v2=(0,1,0), indices=[0,1,2] -- 9 floats
     * (positions) followed by 3 unsigned shorts (indices), matching the bufferView/accessor
     * offsets declared in [meshJsonFragment]. */
    private fun triangleBinBytes(): ByteArray {
        val positions = floatArrayOf(0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f)
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
        return bytes
    }

    private fun meshesAndAccessorsJson(): String = """
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
            "primitives": [ { "attributes": { "POSITION": 0 }, "indices": 1 } ]
          }
        ],
        "buffers": [ { "byteLength": 42 } ]
    """.trimIndent()

    private fun buildGlb(nodesAndScenesJson: String, binChunk: ByteArray): ByteArray {
        val json = """
            {
              ${meshesAndAccessorsJson()},
              $nodesAndScenesJson
            }
        """.trimIndent()
        return encodeGlb(json, binChunk)
    }

    /** Writes a minimal but spec-valid GLB container: 12-byte header, JSON chunk (padded to a
     * 4-byte boundary with spaces), BIN chunk (padded with zero bytes). */
    private fun encodeGlb(json: String, bin: ByteArray): ByteArray {
        val jsonBytes = json.encodeToByteArray()
        val jsonPadded = jsonBytes + ByteArray((4 - jsonBytes.size % 4) % 4) { ' '.code.toByte() }
        val binPadded = bin + ByteArray((4 - bin.size % 4) % 4)

        val totalLength = 12 + 8 + jsonPadded.size + 8 + binPadded.size
        val out = ByteArray(totalLength)

        writeUIntLe(out, 0, 0x46546C67) // magic "glTF"
        writeUIntLe(out, 4, 2) // version
        writeUIntLe(out, 8, totalLength)

        var offset = 12
        writeUIntLe(out, offset, jsonPadded.size)
        writeUIntLe(out, offset + 4, 0x4E4F534A) // "JSON"
        jsonPadded.copyInto(out, offset + 8)
        offset += 8 + jsonPadded.size

        writeUIntLe(out, offset, binPadded.size)
        writeUIntLe(out, offset + 4, 0x004E4942) // "BIN\0"
        binPadded.copyInto(out, offset + 8)

        return out
    }

    private fun writeUIntLe(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    @Test
    fun parsesSingleNodeWithIdentityTransform() {
        val glb = buildGlb(
            """
            "nodes": [ { "mesh": 0 } ],
            "scenes": [ { "nodes": [0] } ],
            "scene": 0
            """.trimIndent(),
            triangleBinBytes(),
        )

        val scene = GltfParser.parseScene(glb)

        assertEquals(1, scene.meshes.size)
        val mesh = scene.meshes.single()
        assertEquals("triangle", mesh.name)
        assertEquals(1, mesh.primitives.size)
        val primitive = mesh.primitives.single()
        assertEquals(3 * 8, primitive.vertices.size)
        assertEquals(listOf(0, 1, 2), primitive.indices.toList())
        assertEquals(identityData(), primitive.localTransform.data.toList())
    }

    @Test
    fun parsesNodeWithExplicitMatrix() {
        // Column-major translate(5, 6, 7).
        val matrix = listOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            5f, 6f, 7f, 1f,
        )
        val glb = buildGlb(
            """
            "nodes": [ { "mesh": 0, "matrix": [${matrix.joinToString(",")}] } ],
            "scenes": [ { "nodes": [0] } ]
            """.trimIndent(),
            triangleBinBytes(),
        )

        val scene = GltfParser.parseScene(glb)

        assertEquals(matrix, scene.meshes.single().primitives.single().localTransform.data.toList())
    }

    @Test
    fun parsesNodeWithTranslationRotationScale() {
        val glb = buildGlb(
            """
            "nodes": [
              { "mesh": 0, "translation": [1, 2, 3], "rotation": [0, 0, 0, 1], "scale": [2, 2, 2] }
            ],
            "scenes": [ { "nodes": [0] } ]
            """.trimIndent(),
            triangleBinBytes(),
        )

        val scene = GltfParser.parseScene(glb)

        // Identity rotation + scale 2 + translation (1,2,3) -> column-major TRS matrix.
        val expected = listOf(
            2f, 0f, 0f, 0f,
            0f, 2f, 0f, 0f,
            0f, 0f, 2f, 0f,
            1f, 2f, 3f, 1f,
        )
        assertEquals(expected, scene.meshes.single().primitives.single().localTransform.data.toList())
    }

    @Test
    fun composesParentAndChildTransformsAcrossHierarchy() {
        // root (translate 10,0,0) -> child (translate 0,5,0) -> grandchild (mesh 0, translate 0,0,1).
        val glb = buildGlb(
            """
            "nodes": [
              { "children": [1], "translation": [10, 0, 0] },
              { "children": [2], "translation": [0, 5, 0] },
              { "mesh": 0, "translation": [0, 0, 1] }
            ],
            "scenes": [ { "nodes": [0] } ]
            """.trimIndent(),
            triangleBinBytes(),
        )

        val scene = GltfParser.parseScene(glb)

        val world = scene.meshes.single().primitives.single().localTransform
        // Pure translations compose additively regardless of multiplication convention.
        assertEquals(10f, world.m03)
        assertEquals(5f, world.m13)
        assertEquals(1f, world.m23)
    }

    private fun identityData(): List<Float> = listOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f,
    )
}
