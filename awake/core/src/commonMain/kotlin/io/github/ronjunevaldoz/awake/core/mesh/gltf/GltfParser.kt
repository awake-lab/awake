// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.mesh.gltf

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Quat
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser.decodeBuffer
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser.parse
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser.parseScene
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser.parseSkinned
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser.readBaseColorImageBytes
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
 * - **No materials or textures beyond a base color texture** -- [parse] reads only the first
 *   mesh's first primitive's `POSITION`/`NORMAL`/`COLOR_0`/`TEXCOORD_0`/`JOINTS_0`/`WEIGHTS_0`
 *   attributes, its index accessor, and (if present) its material's embedded `baseColorTexture`
 *   image bytes -- see [readBaseColorImageBytes]. No other PBR channels (metallic/roughness,
 *   normal, emissive, occlusion maps) are read. [parseScene] walks the full node hierarchy for
 *   static meshes; [parseSkinned] walks it for skeletal skinning/animation (skins + animation
 *   clips too).
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

    private fun readPrimitive(
        document: GltfDocument,
        buffers: List<ByteArray>,
        primitive: GltfPrimitive,
    ): GltfMesh {
        val positionAccessor = primitive.attributes.position
            ?: error("glTF primitive is missing a POSITION attribute.")
        val positions =
            readFloatAccessor(document, buffers, positionAccessor, componentsPerElement = 3)
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
        val jointIndices =
            primitive.attributes.joints0?.let { readJointAccessor(document, buffers, it) }
        val jointWeights = primitive.attributes.weights0?.let {
            readFloatAccessor(document, buffers, it, componentsPerElement = 4)
        }
        val baseColorImageBytes = readBaseColorImageBytes(document, primitive)

        return GltfMesh(
            positions,
            normals,
            colors,
            uvs,
            indices,
            jointIndices,
            jointWeights,
            baseColorImageBytes,
        )
    }

    /** Resolves [primitive]'s material -> `pbrMetallicRoughness.baseColorTexture` -> texture ->
     * image, returning that image's still-encoded bytes (decoded from its base64 data URI) --
     * `null` at any missing link in that chain (no material, no base color texture, an
     * external/non-embedded image URI). */
    private fun readBaseColorImageBytes(
        document: GltfDocument,
        primitive: GltfPrimitive,
    ): ByteArray? {
        val uri = primitive.material
            ?.let { document.materials.getOrNull(it) }
            ?.pbrMetallicRoughness
            ?.baseColorTexture
            ?.let { document.textures.getOrNull(it.index) }
            ?.source
            ?.let { document.images.getOrNull(it) }
            ?.uri
            ?: return null
        return if (uri.startsWith("data:")) decodeBase64DataUri(uri) else null // External image files aren't supported.
    }

    /**
     * Parses a plain-JSON embedded-buffer glTF (same input shape as [parse] -- NOT a GLB
     * container, see [parse]'s own doc comment) into every mesh/node/skin/animation the document
     * has, for skeletal skinning/animation playback. Unlike [parse] (first mesh/primitive only)
     * this reads the whole node hierarchy -- a skin's joints are node indices, so the demo layer
     * needs the real scene graph, not just one primitive's raw attributes.
     */
    fun parseSkinned(json: String): LoadedSkinnedScene {
        val document = GltfJson.decodeFromString(GltfDocument.serializer(), json)
        val buffers = document.buffers.map(::decodeBuffer)

        val nodes = document.nodes.map { node ->
            val explicitMatrix = node.matrix?.let { m ->
                require(m.size == 16) { "glTF node matrix must have 16 elements, got ${m.size}." }
                Mat4().apply { for (i in 0 until 16) data[i] = m[i] }
            }
            val t = node.translation ?: listOf(0f, 0f, 0f)
            val r = node.rotation ?: listOf(0f, 0f, 0f, 1f)
            val s = node.scale ?: listOf(1f, 1f, 1f)
            LoadedNode(
                translation = Vec3(t[0], t[1], t[2]),
                rotation = Quat(r[0], r[1], r[2], r[3]),
                scale = Vec3(s[0], s[1], s[2]),
                matrix = explicitMatrix,
                children = node.children,
                mesh = node.mesh,
                skin = node.skin,
            )
        }
        val meshes = document.meshes.map { meshDef ->
            val primitive = meshDef.primitives.firstOrNull()
                ?: error("glTF mesh '${meshDef.name}' has no primitives.")
            readPrimitive(document, buffers, primitive)
        }
        val skins = document.skins.indices.map { parseSkin(document, buffers, it) }
        val animations = document.animations.indices.map { parseAnimation(document, buffers, it) }
        val sceneIndex = document.scene ?: 0
        val rootNodes = document.scenes.getOrNull(sceneIndex)?.nodes ?: emptyList()

        return LoadedSkinnedScene(nodes, meshes, skins, animations, rootNodes)
    }

    private fun parseSkin(
        document: GltfDocument,
        buffers: List<ByteArray>,
        skinIndex: Int,
    ): LoadedSkin {
        val skin = document.skins[skinIndex]
        val inverseBindFloats = skin.inverseBindMatrices?.let {
            readFloatAccessor(document, buffers, it, componentsPerElement = 16)
        }
        val inverseBindMatrices = skin.joints.indices.map { jointIndex ->
            Mat4().apply {
                if (inverseBindFloats != null) {
                    for (component in 0 until 16) {
                        data[component] =
                            inverseBindFloats[jointIndex * 16 + component]
                    }
                }
                // else: no inverseBindMatrices accessor -- glTF spec default is identity per joint.
            }
        }
        return LoadedSkin(skin.joints, inverseBindMatrices)
    }

    private fun parseAnimation(
        document: GltfDocument,
        buffers: List<ByteArray>,
        animationIndex: Int,
    ): LoadedAnimation {
        val animation = document.animations[animationIndex]
        val channels = animation.channels.mapNotNull { channel ->
            val targetNode = channel.target.node ?: return@mapNotNull null
            val sampler = animation.samplers.getOrElse(channel.sampler) {
                error("glTF animation $animationIndex channel references sampler ${channel.sampler}, out of range.")
            }
            val componentsPerKeyframe = when (channel.target.path) {
                "translation", "scale" -> 3
                "rotation" -> 4
                else -> return@mapNotNull null // "weights" (morph targets) -- not supported.
            }
            val times =
                readFloatAccessor(document, buffers, sampler.input, componentsPerElement = 1)
            val values = readFloatAccessor(
                document,
                buffers,
                sampler.output,
                componentsPerElement = componentsPerKeyframe,
            )
            LoadedAnimationChannel(
                targetNode = targetNode,
                targetPath = channel.target.path,
                sampler = LoadedAnimationSampler(times, values, componentsPerKeyframe),
            )
        }
        return LoadedAnimation(channels)
    }

    /** Parses the 12-byte GLB header and its chunks, returning the JSON chunk's text and the
     * BIN chunk's bytes (`null` if the container has no BIN chunk -- valid when every buffer
     * uses its own base64 URI instead). */
    private fun readGlbContainer(bytes: ByteArray): Pair<String, ByteArray?> {
        require(bytes.size >= GLB_HEADER_SIZE) { "glTF binary (GLB) buffer is too small to contain a header." }
        val magic = readUIntLe(bytes, 0)
        require(magic == GLB_MAGIC) {
            "Not a valid glTF binary (GLB) file -- expected magic 0x46546C67, got 0x${
                magic.toString(
                    16,
                )
            }."
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
                    "this parser (only base64 data URIs).",
            )
        return decodeBase64DataUri(uri)
    }

    /** Like [decodeBuffer], but a buffer with no `uri` resolves to [glbBin] (the GLB
     * container's BIN chunk) instead of erroring -- per the glTF 2.0 spec's "GLB-stored
     * Buffer" convention. */
    private fun decodeBufferOrGlbBin(buffer: GltfBuffer, glbBin: ByteArray?): ByteArray {
        val uri = buffer.uri ?: return glbBin
            ?: error("glTF buffer has no uri and the GLB container has no BIN chunk.")
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
        return Mat4.fromTrs(
            translation = Vec3(t[0], t[1], t[2]),
            rotation = Quat(r[0], r[1], r[2], r[3]),
            scale = Vec3(s[0], s[1], s[2]),
        )
    }

    /** `a * b` in the standard row/col sense (applies [b] first, then [a]) -- deliberately
     * not [Mat4]'s own `times` operator; see [Mat4.multiplyColumnMajor]'s doc comment. */
    private fun multiply(a: Mat4, b: Mat4): Mat4 = Mat4.multiplyColumnMajor(a, b)

    private fun accessorAt(document: GltfDocument, index: Int): GltfAccessor =
        document.accessors.getOrElse(index) {
            error("glTF accessor index $index is out of range (${document.accessors.size} accessors).")
        }

    private fun bufferViewFor(
        document: GltfDocument,
        accessor: GltfAccessor,
        accessorIndex: Int,
    ): GltfBufferView {
        val bufferViewIndex = accessor.bufferView
            ?: error("glTF accessor $accessorIndex has no bufferView -- sparse accessors are not supported.")
        return document.bufferViews.getOrElse(bufferViewIndex) {
            error("glTF bufferView index $bufferViewIndex is out of range.")
        }
    }

    private fun componentSize(componentType: Int): Int = when (componentType) {
        COMPONENT_TYPE_UNSIGNED_BYTE -> 1
        COMPONENT_TYPE_UNSIGNED_SHORT -> 2
        COMPONENT_TYPE_UNSIGNED_INT, COMPONENT_TYPE_FLOAT -> BYTES_PER_FLOAT
        else -> error("Unsupported glTF accessor componentType: $componentType")
    }

    private fun typeComponentCount(type: String): Int = when (type) {
        "SCALAR" -> 1
        "VEC2" -> 2
        "VEC3" -> 3
        "VEC4" -> 4
        "MAT4" -> 16
        else -> error("Unsupported glTF accessor type: $type")
    }

    private fun readFloatAccessor(
        document: GltfDocument,
        buffers: List<ByteArray>,
        accessorIndex: Int,
        componentsPerElement: Int,
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
        accessorIndex: Int,
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

    /** Reads a `JOINTS_0`-shaped accessor (`ubyte4` or `ushort4` per the glTF 2.0 spec) into
     * `IntArray(count * 4)`, one entry per component -- widened to `Int` regardless of the
     * source byte width, matching [io.github.ronjunevaldoz.awake.render.mesh.VertexAttributeFormat.UInt4]'s
     * own "widen everything to 4 bytes/component" convention. */
    private fun readJointAccessor(
        document: GltfDocument,
        buffers: List<ByteArray>,
        accessorIndex: Int,
    ): IntArray {
        val accessor = accessorAt(document, accessorIndex)
        require(typeComponentCount(accessor.type) == 4) {
            "glTF JOINTS_0 accessor $accessorIndex has type ${accessor.type}, expected VEC4."
        }
        val bufferView = bufferViewFor(document, accessor, accessorIndex)
        val bytes = buffers[bufferView.buffer]
        val componentSize = componentSize(accessor.componentType)
        val elementByteSize = 4 * componentSize
        val stride = bufferView.byteStride ?: elementByteSize
        val base = bufferView.byteOffset + accessor.byteOffset

        return IntArray(accessor.count * 4) { i ->
            val elementIndex = i / 4
            val component = i % 4
            val offset = base + elementIndex * stride + component * componentSize
            when (accessor.componentType) {
                COMPONENT_TYPE_UNSIGNED_BYTE -> bytes[offset].toInt() and 0xFF
                COMPONENT_TYPE_UNSIGNED_SHORT -> readUShortLe(bytes, offset)
                else -> error("Unsupported glTF JOINTS_0 componentType: ${accessor.componentType}")
            }
        }
    }

    private fun readFloatLe(bytes: ByteArray, offset: Int): Float =
        Float.fromBits(readUIntLe(bytes, offset))

    private fun readUShortLe(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)

    private fun readUIntLe(bytes: ByteArray, offset: Int): Int = (bytes[offset].toInt() and 0xFF) or
        ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
        ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
        ((bytes[offset + 3].toInt() and 0xFF) shl 24)
}
