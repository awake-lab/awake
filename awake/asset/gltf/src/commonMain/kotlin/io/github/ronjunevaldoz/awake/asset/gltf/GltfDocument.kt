// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.gltf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The subset of the glTF 2.0 JSON schema (https://registry.khronos.org/glTF/specs/2.0/
 * glTF-2.0.html) this parser actually reads: enough to pull mesh vertex attributes/indices,
 * a node hierarchy, skeletal skinning/animation, and base color/metallic-roughness/normal/
 * occlusion/emissive textures out of a `.gltf` file -- see [GltfParser]'s doc comment for
 * exactly which parts of [materials]/[textures]/[images] are read: still-encoded image bytes
 * per channel, plus [GltfPbrMetallicRoughness]'s `baseColorFactor`/`metallicFactor`/
 * `roughnessFactor` and [GltfMaterial.emissiveFactor] -- the scalar/vector factors a
 * texture-free (or partially textured) material tints or replaces its maps with. No other
 * material fields (normal `scale`, occlusion `strength`) are read.
 */
@Serializable
data class GltfDocument(
    val buffers: List<GltfBuffer> = emptyList(),
    val bufferViews: List<GltfBufferView> = emptyList(),
    val accessors: List<GltfAccessor> = emptyList(),
    val meshes: List<GltfMeshDef> = emptyList(),
    /** Index into [scenes] of the scene to load when none is explicitly requested -- glTF
     * default is `0` when the document has any scenes at all. */
    val scene: Int? = null,
    val scenes: List<GltfScene> = emptyList(),
    val nodes: List<GltfNode> = emptyList(),
    val skins: List<GltfSkin> = emptyList(),
    val animations: List<GltfAnimation> = emptyList(),
    val materials: List<GltfMaterial> = emptyList(),
    val textures: List<GltfTexture> = emptyList(),
    val images: List<GltfImage> = emptyList(),
)

@Serializable
data class GltfMaterial(
    val name: String? = null,
    val pbrMetallicRoughness: GltfPbrMetallicRoughness? = null,
    val normalTexture: GltfTextureRef? = null,
    val occlusionTexture: GltfTextureRef? = null,
    val emissiveTexture: GltfTextureRef? = null,
    /** Linear RGB `[r, g, b]`, multiplied into [emissiveTexture]'s sample (or standing alone
     * when there's no emissive texture) -- glTF 2.0 spec default `[0, 0, 0]` (no emission). */
    val emissiveFactor: List<Float>? = null,
)

@Serializable
data class GltfPbrMetallicRoughness(
    val baseColorTexture: GltfTextureRef? = null,
    /** Linear RGBA `[r, g, b, a]`, multiplied into [baseColorTexture]'s sample (or standing
     * alone when there's no base color texture) -- glTF 2.0 spec default `[1, 1, 1, 1]`
     * (opaque white, i.e. the texture's own color passes through unmodified). */
    val baseColorFactor: List<Float>? = null,
    /** Green channel = roughness, blue channel = metalness (glTF 2.0 spec) -- read as one
     * still-encoded image, same "backend decides how to sample its channels" scope
     * [readBaseColorImageBytes]'s sibling readers already have. */
    val metallicRoughnessTexture: GltfTextureRef? = null,
    /** Multiplies [metallicRoughnessTexture]'s blue channel (or stands alone when there's no
     * texture) -- glTF 2.0 spec default `1.0`. */
    val metallicFactor: Float? = null,
    /** Multiplies [metallicRoughnessTexture]'s green channel (or stands alone when there's no
     * texture) -- glTF 2.0 spec default `1.0`. */
    val roughnessFactor: Float? = null,
)

@Serializable
data class GltfTextureRef(
    val index: Int,
    @SerialName("texCoord") val texCoordSet: Int = 0,
)

@Serializable
data class GltfTexture(
    val source: Int? = null,
    val sampler: Int? = null,
)

@Serializable
data class GltfImage(
    /** A `data:image/png;base64,...` (or `image/jpeg`) data URI -- external image file
     * references are not supported, same "embedded buffers only" scope [GltfBuffer] already
     * has. */
    val uri: String? = null,
    val mimeType: String? = null,
)

@Serializable
data class GltfSkin(
    val inverseBindMatrices: Int? = null,
    val joints: List<Int> = emptyList(),
    val skeleton: Int? = null,
)

@Serializable
data class GltfAnimation(
    val channels: List<GltfAnimationChannel> = emptyList(),
    val samplers: List<GltfAnimationSampler> = emptyList(),
    val name: String? = null,
)

@Serializable
data class GltfAnimationChannel(
    val sampler: Int,
    val target: GltfAnimationTarget,
)

@Serializable
data class GltfAnimationTarget(
    val node: Int? = null,
    /** `"translation"`, `"rotation"`, `"scale"`, or `"weights"` -- only the first three are
     * handled ([GltfParser]'s skinning path has no morph-target support). */
    val path: String,
)

@Serializable
data class GltfAnimationSampler(
    val input: Int,
    val output: Int,
    /** `"LINEAR"`, `"STEP"`, or `"CUBICSPLINE"` -- only `LINEAR` (nlerp/lerp) is sampled;
     * every channel in the reference CesiumMan clip this parser targets is `LINEAR`. */
    val interpolation: String = "LINEAR",
)

@Serializable
data class GltfScene(
    val nodes: List<Int> = emptyList(),
)

@Serializable
data class GltfNode(
    val name: String? = null,
    val mesh: Int? = null,
    /** Index into [GltfDocument.skins] -- present on the node that carries a skinned mesh. */
    val skin: Int? = null,
    /** 16 column-major floats -- mutually exclusive with [translation]/[rotation]/[scale]
     * per the glTF 2.0 spec; when absent, TRS is composed instead. */
    val matrix: List<Float>? = null,
    val translation: List<Float>? = null,
    /** Quaternion `[x, y, z, w]`. */
    val rotation: List<Float>? = null,
    val scale: List<Float>? = null,
    val children: List<Int> = emptyList(),
)

@Serializable
data class GltfBuffer(
    /** A `data:application/octet-stream;base64,...` (or similar) data URI -- external
     * file URIs (a relative path to a sibling `.bin`) are not supported by this parser;
     * see [GltfParser]'s doc comment. */
    val uri: String? = null,
    val byteLength: Int = 0,
)

@Serializable
data class GltfBufferView(
    val buffer: Int,
    val byteOffset: Int = 0,
    val byteLength: Int = 0,
    val byteStride: Int? = null,
)

@Serializable
data class GltfAccessor(
    val bufferView: Int? = null,
    val byteOffset: Int = 0,
    /** glTF's numeric component type constants -- `5120` BYTE, `5121` UNSIGNED_BYTE,
     * `5122` SHORT, `5123` UNSIGNED_SHORT, `5125` UNSIGNED_INT, `5126` FLOAT. FLOAT and the
     * three unsigned integer types are used as-is; `BYTE`/`SHORT`/`UNSIGNED_BYTE`/
     * `UNSIGNED_SHORT` vertex attributes with [normalized] set are decoded through
     * [io.github.ronjunevaldoz.awake.core.geometry.NormalizedInt] (a quantized export's
     * vertex data). */
    val componentType: Int,
    val count: Int,
    /** `"SCALAR"`, `"VEC2"`, `"VEC3"`, or `"VEC4"` -- determines how many [componentType]
     * values make up one element. */
    val type: String,
    /** glTF 2.0 spec: whether an integer [componentType] vertex attribute packs a normalized
     * float (`[-1, 1]` signed, `[0, 1]` unsigned) rather than a raw integer value -- ignored
     * (and meaningless per spec) for index accessors and `FLOAT`. Default `false` matches
     * every accessor this parser read before this field existed. */
    val normalized: Boolean = false,
)

@Serializable
data class GltfPrimitiveAttributes(
    @SerialName("POSITION") val position: Int? = null,
    @SerialName("NORMAL") val normal: Int? = null,
    @SerialName("COLOR_0") val color0: Int? = null,
    @SerialName("TEXCOORD_0") val texCoord0: Int? = null,
    @SerialName("JOINTS_0") val joints0: Int? = null,
    @SerialName("WEIGHTS_0") val weights0: Int? = null,
)

@Serializable
data class GltfPrimitive(
    val attributes: GltfPrimitiveAttributes,
    val indices: Int? = null,
    val material: Int? = null,
)

@Serializable
data class GltfMeshDef(
    val name: String? = null,
    val primitives: List<GltfPrimitive> = emptyList(),
)
