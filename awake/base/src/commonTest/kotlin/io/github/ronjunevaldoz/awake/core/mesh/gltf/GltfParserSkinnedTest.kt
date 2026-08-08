// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.mesh.gltf

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals

/** [GltfParser.parseSkinned] tests -- a minimal synthetic 2-joint skin + 1-channel rotation
 * animation, built the same "construct real bytes, base64-encode" way [GltfParserTest] does. */
@OptIn(ExperimentalEncodingApi::class)
class GltfParserSkinnedTest {
    private fun floatBytes(values: FloatArray): ByteArray {
        val bytes = ByteArray(values.size * 4)
        var offset = 0
        for (value in values) {
            val bits = value.toRawBits()
            bytes[offset] = (bits and 0xFF).toByte()
            bytes[offset + 1] = ((bits ushr 8) and 0xFF).toByte()
            bytes[offset + 2] = ((bits ushr 16) and 0xFF).toByte()
            bytes[offset + 3] = ((bits ushr 24) and 0xFF).toByte()
            offset += 4
        }
        return bytes
    }

    private fun identityMat4(): FloatArray = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f,
    )

    private val inverseBindMatrices = identityMat4() + identityMat4() // 2 joints, both identity.
    private val times = floatArrayOf(0f, 1f)
    private val rotations = floatArrayOf(
        0f,
        0f,
        0f,
        1f, // keyframe 0: identity rotation.
        0f,
        0f,
        0.7071f,
        0.7071f, // keyframe 1: ~90 degrees about Z.
    )

    private fun skinnedGltfJson(): String {
        val bytes = floatBytes(inverseBindMatrices) + floatBytes(times) + floatBytes(rotations)
        val bufferBase64 = Base64.encode(bytes)

        val invBindByteLength = inverseBindMatrices.size * 4
        val timesByteLength = times.size * 4
        val rotationsByteLength = rotations.size * 4

        return """
            {
              "buffers": [
                { "uri": "data:application/octet-stream;base64,$bufferBase64", "byteLength": ${bytes.size} }
              ],
              "bufferViews": [
                { "buffer": 0, "byteOffset": 0, "byteLength": $invBindByteLength },
                { "buffer": 0, "byteOffset": $invBindByteLength, "byteLength": $timesByteLength },
                { "buffer": 0, "byteOffset": ${invBindByteLength + timesByteLength}, "byteLength": $rotationsByteLength }
              ],
              "accessors": [
                { "bufferView": 0, "componentType": 5126, "count": 2, "type": "MAT4" },
                { "bufferView": 1, "componentType": 5126, "count": 2, "type": "SCALAR" },
                { "bufferView": 2, "componentType": 5126, "count": 2, "type": "VEC4" }
              ],
              "nodes": [
                { "translation": [0, 0, 0] },
                { "translation": [0, 1, 0] }
              ],
              "scenes": [ { "nodes": [0] } ],
              "scene": 0,
              "skins": [ { "inverseBindMatrices": 0, "joints": [0, 1] } ],
              "animations": [
                {
                  "channels": [ { "sampler": 0, "target": { "node": 1, "path": "rotation" } } ],
                  "samplers": [ { "input": 1, "output": 2, "interpolation": "LINEAR" } ]
                }
              ]
            }
        """.trimIndent()
    }

    @Test
    fun parsesSkinAndAnimationFromEmbeddedBuffers() {
        val scene = GltfParser.parseSkinned(skinnedGltfJson())

        assertEquals(2, scene.nodes.size)
        assertEquals(1, scene.skins.size)
        val skin = scene.skins.single()
        assertEquals(listOf(0, 1), skin.joints)
        assertEquals(2, skin.inverseBindMatrices.size)
        assertEquals(identityMat4().toList(), skin.inverseBindMatrices[0].data.toList())
        assertEquals(identityMat4().toList(), skin.inverseBindMatrices[1].data.toList())

        assertEquals(1, scene.animations.size)
        val channel = scene.animations.single().channels.single()
        assertEquals(1, channel.targetNode)
        assertEquals("rotation", channel.targetPath)
        assertEquals(4, channel.sampler.componentsPerKeyframe)
        assertEquals(times.toList(), channel.sampler.times.toList())
        assertEquals(rotations.toList(), channel.sampler.values.toList())
    }
}
