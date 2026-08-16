// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.gltf

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Covers [GltfParser]'s material/texture/image chain -- `primitive.material` ->
 * `pbrMetallicRoughness.baseColorTexture` -> `textures[].source` -> `images[].uri` -> decoded
 * bytes (see [GltfParser.readMaterialImageBytes]'s own doc comment) -- mirroring
 * [GltfParserTest]'s synthetic-JSON-with-base64-buffer pattern rather than depending on a real
 * downloaded asset.
 */
@OptIn(ExperimentalEncodingApi::class)
class GltfParserMaterialTest {
    private val triangleAccessorsAndBuffer = """
        "buffers": [
          { "uri": "data:application/octet-stream;base64,${triangleBufferBase64()}", "byteLength": 36 }
        ],
        "bufferViews": [
          { "buffer": 0, "byteOffset": 0, "byteLength": 36 }
        ],
        "accessors": [
          { "bufferView": 0, "componentType": 5126, "count": 3, "type": "VEC3" }
        ]
    """.trimIndent()

    private fun triangleBufferBase64(): String {
        val positions = floatArrayOf(0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f)
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
        return Base64.encode(bytes)
    }

    @Test
    fun resolvesBaseColorImageBytesThroughMaterialTextureImageChain() {
        val imageBytes = byteArrayOf(1, 2, 3, 4, 5)
        val imageBase64 = Base64.encode(imageBytes)
        val json = """
            {
              $triangleAccessorsAndBuffer,
              "materials": [
                { "pbrMetallicRoughness": { "baseColorTexture": { "index": 0 } } }
              ],
              "textures": [ { "source": 0 } ],
              "images": [ { "uri": "data:image/png;base64,$imageBase64" } ],
              "meshes": [
                {
                  "primitives": [
                    { "attributes": { "POSITION": 0 }, "material": 0 }
                  ]
                }
              ]
            }
        """.trimIndent()

        val mesh = GltfParser.parse(json)

        assertTrue(imageBytes.contentEquals(mesh.baseColorImageBytes), "decoded image bytes must match the embedded base64 payload")
    }

    @Test
    fun baseColorImageBytesIsNullWhenPrimitiveHasNoMaterial() {
        val json = """
            {
              $triangleAccessorsAndBuffer,
              "meshes": [
                { "primitives": [ { "attributes": { "POSITION": 0 } } ] }
              ]
            }
        """.trimIndent()

        val mesh = GltfParser.parse(json)

        assertNull(mesh.baseColorImageBytes)
    }

    @Test
    fun baseColorImageBytesIsNullWhenMaterialHasNoBaseColorTexture() {
        val json = """
            {
              $triangleAccessorsAndBuffer,
              "materials": [ { "pbrMetallicRoughness": {} } ],
              "meshes": [
                { "primitives": [ { "attributes": { "POSITION": 0 }, "material": 0 } ] }
              ]
            }
        """.trimIndent()

        val mesh = GltfParser.parse(json)

        assertNull(mesh.baseColorImageBytes)
    }

    @Test
    fun resolvesEveryPbrChannelThroughItsOwnTextureImageChain() {
        val baseColor = byteArrayOf(1)
        val metallicRoughness = byteArrayOf(2)
        val normal = byteArrayOf(3)
        val occlusion = byteArrayOf(4)
        val emissive = byteArrayOf(5)
        fun dataUri(bytes: ByteArray) = "data:image/png;base64,${Base64.encode(bytes)}"
        val json = """
            {
              $triangleAccessorsAndBuffer,
              "materials": [
                {
                  "pbrMetallicRoughness": {
                    "baseColorTexture": { "index": 0 },
                    "metallicRoughnessTexture": { "index": 1 }
                  },
                  "normalTexture": { "index": 2 },
                  "occlusionTexture": { "index": 3 },
                  "emissiveTexture": { "index": 4 }
                }
              ],
              "textures": [
                { "source": 0 }, { "source": 1 }, { "source": 2 }, { "source": 3 }, { "source": 4 }
              ],
              "images": [
                { "uri": "${dataUri(baseColor)}" },
                { "uri": "${dataUri(metallicRoughness)}" },
                { "uri": "${dataUri(normal)}" },
                { "uri": "${dataUri(occlusion)}" },
                { "uri": "${dataUri(emissive)}" }
              ],
              "meshes": [
                {
                  "primitives": [
                    { "attributes": { "POSITION": 0 }, "material": 0 }
                  ]
                }
              ]
            }
        """.trimIndent()

        val mesh = GltfParser.parse(json)

        assertTrue(baseColor.contentEquals(mesh.baseColorImageBytes))
        assertTrue(metallicRoughness.contentEquals(mesh.metallicRoughnessImageBytes))
        assertTrue(normal.contentEquals(mesh.normalImageBytes))
        assertTrue(occlusion.contentEquals(mesh.occlusionImageBytes))
        assertTrue(emissive.contentEquals(mesh.emissiveImageBytes))
    }

    @Test
    fun pbrChannelsAreNullWhenPrimitiveHasNoMaterial() {
        val json = """
            {
              $triangleAccessorsAndBuffer,
              "meshes": [
                { "primitives": [ { "attributes": { "POSITION": 0 } } ] }
              ]
            }
        """.trimIndent()

        val mesh = GltfParser.parse(json)

        assertNull(mesh.metallicRoughnessImageBytes)
        assertNull(mesh.normalImageBytes)
        assertNull(mesh.occlusionImageBytes)
        assertNull(mesh.emissiveImageBytes)
    }

    @Test
    fun baseColorImageBytesIsNullForExternalImageUri() {
        val json = """
            {
              $triangleAccessorsAndBuffer,
              "materials": [
                { "pbrMetallicRoughness": { "baseColorTexture": { "index": 0 } } }
              ],
              "textures": [ { "source": 0 } ],
              "images": [ { "uri": "duck.png" } ],
              "meshes": [
                {
                  "primitives": [
                    { "attributes": { "POSITION": 0 }, "material": 0 }
                  ]
                }
              ]
            }
        """.trimIndent()

        val mesh = GltfParser.parse(json)

        assertNull(mesh.baseColorImageBytes)
    }
}
