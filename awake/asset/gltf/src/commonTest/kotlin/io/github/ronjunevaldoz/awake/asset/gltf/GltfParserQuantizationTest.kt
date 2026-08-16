// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.gltf

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** [GltfParser]'s decode of normalized integer (quantized) vertex-attribute accessors --
 * `BYTE`/`UNSIGNED_BYTE`/`SHORT`/`UNSIGNED_SHORT` with `normalized: true`, the shape a
 * `gltfpack`/meshoptimizer-quantized export uses instead of raw `FLOAT` positions. Mirrors
 * [GltfParserTest]'s synthetic-JSON-with-base64-buffer pattern. */
@OptIn(ExperimentalEncodingApi::class)
class GltfParserQuantizationTest {
    private fun shortLeBytes(values: List<Int>): ByteArray {
        val bytes = ByteArray(values.size * 2)
        values.forEachIndexed { i, value ->
            bytes[i * 2] = (value and 0xFF).toByte()
            bytes[i * 2 + 1] = ((value ushr 8) and 0xFF).toByte()
        }
        return bytes
    }

    private fun singlePositionJson(componentType: Int, normalized: Boolean, bytes: ByteArray): String {
        val bufferBase64 = Base64.encode(bytes)
        return """
            {
              "buffers": [ { "uri": "data:application/octet-stream;base64,$bufferBase64", "byteLength": ${bytes.size} } ],
              "bufferViews": [ { "buffer": 0, "byteOffset": 0, "byteLength": ${bytes.size} } ],
              "accessors": [
                { "bufferView": 0, "componentType": $componentType, "count": 1, "type": "VEC3", "normalized": $normalized }
              ],
              "meshes": [ { "primitives": [ { "attributes": { "POSITION": 0 } } ] } ]
            }
        """.trimIndent()
    }

    @Test
    fun decodesNormalizedSignedShortPositionsToTheGltfSpecFormula() {
        // 32767 -> 1.0, -32768 -> -1.0 (clamped per spec), 0 -> 0.0.
        val bytes = shortLeBytes(listOf(32767, -32768, 0))
        val json = singlePositionJson(componentType = 5122, normalized = true, bytes = bytes)

        val mesh = GltfParser.parse(json)

        assertEquals(listOf(1f, -1f, 0f), mesh.positions.toList())
    }

    @Test
    fun decodesNormalizedUnsignedShortPositionsToTheGltfSpecFormula() {
        val bytes = shortLeBytes(listOf(65535, 0, 32768))
        val json = singlePositionJson(componentType = 5123, normalized = true, bytes = bytes)

        val mesh = GltfParser.parse(json)

        assertEquals(65535f / 65535f, mesh.positions[0])
        assertEquals(0f, mesh.positions[1])
        assertEquals(32768f / 65535f, mesh.positions[2])
    }

    @Test
    fun rejectsNonNormalizedIntegerVertexAttribute() {
        val bytes = shortLeBytes(listOf(1, 2, 3))
        val json = singlePositionJson(componentType = 5122, normalized = false, bytes = bytes)

        assertFailsWith<IllegalArgumentException> { GltfParser.parse(json) }
    }
}
