// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.mesh.gltf

import io.github.ronjunevaldoz.awake.core.math.Mat4
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val COMPONENT_TYPE_UNSIGNED_BYTE = 5121
private const val COMPONENT_TYPE_UNSIGNED_SHORT = 5123
private const val COMPONENT_TYPE_UNSIGNED_INT = 5125
private const val COMPONENT_TYPE_FLOAT = 5126

private const val BYTES_PER_FLOAT = 4
private const val BASE64_DATA_URI_MARKER = ";base64,"

// glTF Binary (GLB) container -- see https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html#glb-file-format
private const val GLB_MAGIC = 0x46546C67
private const val GLB_HEADER_SIZE = 12
private const val GLB_CHUNK_TYPE_JSON = 0x4E4F534A
private const val GLB_CHUNK_TYPE_BIN = 0x004E4942

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
        return readPrimitive(document, buffers, primitive)
    }

    /**
     * Parses a glTF Binary (GLB) container -- header + JSON chunk + optional BIN chunk --
     * walks its `scenes`/`nodes` graph, and returns every mesh reachable from the default
     * scene with each primitive's owning node's composed world transform attached. Unlike
     * [parse], this reads every mesh/primitive in the document (not just the first), and
     * resolves buffers with no `uri` (i.e. glTF's "use the GLB BIN chunk" convention) in
     * addition to base64 data URIs.
     */
    fun parseScene(bytes: ByteArray): LoadedScene {
        val (json, glbBin) = readGlbContainer(bytes)
        val document = GltfJson.decodeFromString(GltfDocument.serializer(), json)
        val buffers = document.buffers.map { decodeBufferOrGlbBin(it, glbBin) }

        val loadedMeshes = mutableListOf<LoadedMesh>()

        fun visit(nodeIndex: Int, parentWorld: Mat4) {
            val node = document.nodes.getOrElse(nodeIndex) {
                error("glTF node index $nodeIndex is out of range (${document.nodes.size} nodes).")
            }
            val world = multiply(parentWorld, nodeLocalTransform(node))
            node.mesh?.let { meshIndex ->
                val meshDef = document.meshes.getOrElse(meshIndex) {
                    error("glTF mesh index $meshIndex is out of range (${document.meshes.size} meshes).")
                }
                val primitives = meshDef.primitives.map { primitive ->
                    val raw = readPrimitive(document, buffers, primitive)
                    LoadedPrimitive(raw.toInterleavedPositionColorUv(), raw.indices, world)
                }
                loadedMeshes += LoadedMesh(meshDef.name ?: "", primitives)
            }
            node.children.forEach { visit(it, world) }
        }

        val sceneIndex = document.scene ?: 0
        val rootNodes = document.scenes.getOrNull(sceneIndex)?.nodes ?: emptyList()
        rootNodes.forEach { visit(it, Mat4()) }

        return LoadedScene(loadedMeshes)
    }

    private fun readPrimitive(document: GltfDocument, buffers: List<ByteArray>, primitive: GltfPrimitive): GltfMesh {
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

    /** Parses the 12-byte GLB header and its chunks, returning the JSON chunk's text and the
     * BIN chunk's bytes (`null` if the container has no BIN chunk -- valid when every buffer
     * uses its own base64 URI instead). */
    private fun readGlbContainer(bytes: ByteArray): Pair<String, ByteArray?> {
        require(bytes.size >= GLB_HEADER_SIZE) { "glTF binary (GLB) buffer is too small to contain a header." }
        val magic = readUIntLe(bytes, 0)
        require(magic == GLB_MAGIC) {
            "Not a valid glTF binary (GLB) file -- expected magic 0x46546C67, got 0x${magic.toString(16)}."
        }
        val totalLength = readUIntLe(bytes, 8)
        require(totalLength <= bytes.size) {
            "glTF binary (GLB) declared length $totalLength exceeds actual buffer size ${bytes.size}."
        }

        var offset = GLB_HEADER_SIZE
        var json: String? = null
        var bin: ByteArray? = null
        while (offset < totalLength) {
            val chunkLength = readUIntLe(bytes, offset)
            val chunkType = readUIntLe(bytes, offset + 4)
            val chunkDataStart = offset + 8
            val chunkDataEnd = chunkDataStart + chunkLength
            when (chunkType) {
                GLB_CHUNK_TYPE_JSON -> json = bytes.decodeToString(chunkDataStart, chunkDataEnd)
                GLB_CHUNK_TYPE_BIN -> bin = bytes.copyOfRange(chunkDataStart, chunkDataEnd)
            }
            offset = chunkDataEnd
        }
        return (json ?: error("glTF binary (GLB) file has no JSON chunk.")) to bin
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeBuffer(buffer: GltfBuffer): ByteArray {
        val uri = buffer.uri
            ?: error(
                "glTF buffer has no uri -- external .bin buffers are not supported by " +
                    "this parser (only base64 data URIs)."
            )
        return decodeBase64DataUri(uri)
    }

    /** Like [decodeBuffer], but a buffer with no `uri` resolves to [glbBin] (the GLB
     * container's BIN chunk) instead of erroring -- per the glTF 2.0 spec's "GLB-stored
     * Buffer" convention. */
    private fun decodeBufferOrGlbBin(buffer: GltfBuffer, glbBin: ByteArray?): ByteArray {
        val uri = buffer.uri
        if (uri == null) {
            return glbBin ?: error("glTF buffer has no uri and the GLB container has no BIN chunk.")
        }
        return decodeBase64DataUri(uri)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeBase64DataUri(uri: String): ByteArray {
        val markerIndex = uri.indexOf(BASE64_DATA_URI_MARKER)
        require(uri.startsWith("data:") && markerIndex >= 0) {
            "glTF buffer uri is not a base64 data URI -- external .bin file references " +
                "are not supported by this parser: $uri"
        }
        return Base64.decode(uri.substring(markerIndex + BASE64_DATA_URI_MARKER.length))
    }

    /** This node's local transform -- [GltfNode.matrix] verbatim (glTF matrices are already
     * column-major 16 floats, matching [Mat4.data]'s own layout) when present, otherwise
     * composed from TRS (missing components default to identity translation/rotation/scale
     * per the glTF 2.0 spec). */
    private fun nodeLocalTransform(node: GltfNode): Mat4 {
        node.matrix?.let { m ->
            require(m.size == 16) { "glTF node matrix must have 16 elements, got ${m.size}." }
            return Mat4().apply { for (i in 0 until 16) data[i] = m[i] }
        }
        val t = node.translation ?: listOf(0f, 0f, 0f)
        val r = node.rotation ?: listOf(0f, 0f, 0f, 1f)
        val s = node.scale ?: listOf(1f, 1f, 1f)
        return trsMatrix(
            tx = t[0], ty = t[1], tz = t[2],
            qx = r[0], qy = r[1], qz = r[2], qw = r[3],
            sx = s[0], sy = s[1], sz = s[2]
        )
    }

    /** Standard T * R * S composition, written directly into [Mat4]'s column-major storage
     * (avoids [Mat4]'s own `translate`/`rotate`/`scale`/`times` helpers, whose chained-matrix
     * convention doesn't match the row/col semantics of the `mRC` accessors used here). */
    private fun trsMatrix(
        tx: Float, ty: Float, tz: Float,
        qx: Float, qy: Float, qz: Float, qw: Float,
        sx: Float, sy: Float, sz: Float
    ): Mat4 {
        val xx = qx * qx
        val yy = qy * qy
        val zz = qz * qz
        val xy = qx * qy
        val xz = qx * qz
        val yz = qy * qz
        val wx = qw * qx
        val wy = qw * qy
        val wz = qw * qz

        val r00 = 1f - 2f * (yy + zz)
        val r01 = 2f * (xy - wz)
        val r02 = 2f * (xz + wy)
        val r10 = 2f * (xy + wz)
        val r11 = 1f - 2f * (xx + zz)
        val r12 = 2f * (yz - wx)
        val r20 = 2f * (xz - wy)
        val r21 = 2f * (yz + wx)
        val r22 = 1f - 2f * (xx + yy)

        return Mat4().apply {
            m00 = r00 * sx; m10 = r10 * sx; m20 = r20 * sx; m30 = 0f
            m01 = r01 * sy; m11 = r11 * sy; m21 = r21 * sy; m31 = 0f
            m02 = r02 * sz; m12 = r12 * sz; m22 = r22 * sz; m32 = 0f
            m03 = tx; m13 = ty; m23 = tz; m33 = 1f
        }
    }

    /** `a * b` in the standard row/col sense (applies [b] first, then [a]) -- deliberately
     * not [Mat4]'s own `times` operator; see [trsMatrix]'s doc comment. */
    private fun multiply(a: Mat4, b: Mat4): Mat4 {
        val result = Mat4()
        for (col in 0 until 4) {
            for (row in 0 until 4) {
                var sum = 0f
                for (k in 0 until 4) {
                    sum += a.data[k * 4 + row] * b.data[col * 4 + k]
                }
                result.data[col * 4 + row] = sum
            }
        }
        return result
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
