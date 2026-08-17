// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.asset.gltf.GltfParser
import io.github.ronjunevaldoz.awake.core.graphics.createBitmap
import io.github.ronjunevaldoz.awake.core.graphics.toRgba8Bytes
import io.github.ronjunevaldoz.awake.core.math.boundingRadius
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.texture.PbrTextureSet
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.rendering.components.PbrMaterial
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneInstance

private const val TEXTURED_VERTEX_STRIDE_COMPONENTS = 11

/** mvp(16) + lightDirection(4) + lightColor(4) + model(16) + cameraPosition(4) + material(4) +
 * baseColorFactor(4) + emissiveFactor(4). Must match `textured.wgsl`'s Uniforms field order. */
private const val TEXTURED_UNIFORM_FLOAT_COUNT = 56

/** Duck.gltf, parsed once and exposed to the `assets { }` DSL as named mesh/material
 * factories -- the same real parsing `GltfViewerDemo.preload()` does, repackaged so the scene
 * file can reference "duck"/"duck-material" by name instead of a demo building the entities
 * itself.
 *
 * Camera is fixed in v1 (no runtime centering), but the mesh is still scaled by
 * `1/modelRadius` at load time -- an authored camera has no way to know Duck.gltf's real,
 * unpredictable extents in advance, so the asset itself is normalized to a known unit scale
 * instead, and the scene file's fixed eye/center are authored against that known scale. */
internal object GltfViewerAssets {
    private var interleaved: FloatArray? = null
    private var indices: IntArray? = null
    private var texture: TextureAsset? = null
    private var pbrTextures: PbrTextureSet? = null
    private var pbrMaterial: PbrMaterial? = null

    suspend fun preload() {
        if (interleaved != null) return
        val bytes = readResourceBytes("assets/models/Duck.gltf")
        val gltfMesh = GltfParser.parse(bytes.decodeToString())
        val modelRadius = boundingRadius(gltfMesh.positions)
        interleaved = scalePositions(gltfMesh.toInterleavedPositionNormalColorUv(), 1f / modelRadius)
        indices = gltfMesh.indices
        val imageBytes = requireNotNull(gltfMesh.baseColorImageBytes)
        val bitmap = createBitmap(imageBytes)
        texture = TextureAsset(bitmap.toRgba8Bytes(), bitmap.width, bitmap.height)
        pbrTextures = PbrTextureSet(
            metallicRoughness = gltfMesh.metallicRoughnessImageBytes?.let { decodeTexture(it) },
            normal = gltfMesh.normalImageBytes?.let { decodeTexture(it) },
            occlusion = gltfMesh.occlusionImageBytes?.let { decodeTexture(it) },
            emissive = gltfMesh.emissiveImageBytes?.let { decodeTexture(it) },
        )
        pbrMaterial = PbrMaterial(
            metallic = gltfMesh.metallicFactor,
            roughness = gltfMesh.roughnessFactor,
            baseColorFactor = gltfMesh.baseColorFactor,
            emissiveFactor = gltfMesh.emissiveFactor,
        )
    }

    private suspend fun decodeTexture(imageBytes: ByteArray): TextureAsset {
        val bitmap = createBitmap(imageBytes)
        return TextureAsset(bitmap.toRgba8Bytes(), bitmap.width, bitmap.height)
    }

    /** The joint palette's PbrMaterial equivalent: `GltfViewerAssets.createMesh`/
     * `.createMaterial` feed the `assets { }` DSL, but a component this specific to one loaded
     * model isn't authorable in a scene document either, same reasoning
     * [SkinnedExampleDriver.attachPose] documents. */
    fun attach(instance: SceneInstance, runtime: SceneGameRuntime) {
        val node = instance.roots.find { it.name == "duck" } ?: return
        runtime.world.add(node.entity, requireNotNull(pbrMaterial))
    }

    private fun scalePositions(source: FloatArray, factor: Float): FloatArray {
        val result = source.copyOf()
        var i = 0
        while (i < result.size) {
            result[i] *= factor
            result[i + 1] *= factor
            result[i + 2] *= factor
            i += TEXTURED_VERTEX_STRIDE_COMPONENTS
        }
        return result
    }

    fun createMesh(runtime: SceneGameRuntime): Mesh = runtime.renderer.createMesh(
        MeshGeometry(
            requireNotNull(interleaved),
            requireNotNull(indices),
            format = VertexFormat.PositionNormalColorUv,
        ),
    )

    fun createMaterial(runtime: SceneGameRuntime): Material =
        runtime.renderer.createMaterial(texture = requireNotNull(texture))
}
