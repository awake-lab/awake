// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.scene

/**
 * A tiny synthetic GLB (1x1 quad, position-only attributes) built in-memory with the same
 * technique `GltfParserSceneTest` uses (real GLB header + JSON chunk + BIN chunk, hand-encoded
 * -- no external asset/bundling pipeline exists yet for wasmJs samples, so this proves the real
 * `GltfParser.parseScene` -> `LoadedScene` -> `MeshGeometry` path end-to-end without needing one).
 * Distinct topology from [sampleCubeGeometry]/[sampleGridGeometry] on purpose, so a passing
 * render is evidence the parser's output -- not a coincidentally-identical hardcoded mesh --
 * is what's on screen.
 */
fun gltfSampleQuadGlb(): ByteArray {
    val positions = floatArrayOf(
        0f, 0f, 0f,
        1f, 0f, 0f,
        1f, 0f, 1f,
        0f, 0f, 1f
    )
    val indices = shortArrayOf(0, 1, 2, 2, 3, 0)

    val bin = ByteArray(positions.size * 4 + indices.size * 2)
    var offset = 0
    for (value in positions) {
        val bits = value.toRawBits()
        bin[offset] = (bits and 0xFF).toByte()
        bin[offset + 1] = ((bits ushr 8) and 0xFF).toByte()
        bin[offset + 2] = ((bits ushr 16) and 0xFF).toByte()
        bin[offset + 3] = ((bits ushr 24) and 0xFF).toByte()
        offset += 4
    }
    for (value in indices) {
        bin[offset] = (value.toInt() and 0xFF).toByte()
        bin[offset + 1] = ((value.toInt() ushr 8) and 0xFF).toByte()
        offset += 2
    }

    val json = """
        {
          "bufferViews": [
            { "buffer": 0, "byteOffset": 0, "byteLength": ${positions.size * 4} },
            { "buffer": 0, "byteOffset": ${positions.size * 4}, "byteLength": ${indices.size * 2} }
          ],
          "accessors": [
            { "bufferView": 0, "componentType": 5126, "count": 4, "type": "VEC3" },
            { "bufferView": 1, "componentType": 5123, "count": 6, "type": "SCALAR" }
          ],
          "meshes": [
            { "name": "quad", "primitives": [ { "attributes": { "POSITION": 0 }, "indices": 1 } ] }
          ],
          "buffers": [ { "byteLength": ${bin.size} } ],
          "nodes": [ { "mesh": 0 } ],
          "scenes": [ { "nodes": [0] } ],
          "scene": 0
        }
    """.trimIndent()

    return encodeGlb(json, bin)
}

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
