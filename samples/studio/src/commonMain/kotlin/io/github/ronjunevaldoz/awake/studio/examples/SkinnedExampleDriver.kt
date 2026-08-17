// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.asset.gltf.GltfParser
import io.github.ronjunevaldoz.awake.asset.gltf.firstSkinnedAsset
import io.github.ronjunevaldoz.awake.core.animation.AnimationClip
import io.github.ronjunevaldoz.awake.core.animation.AnimationPose
import io.github.ronjunevaldoz.awake.core.animation.Skin
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.scene.rendering.components.SkinnedPose
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneInstance

private const val SKINNED_UNIFORM_FLOAT_COUNT = 16 + 64 * 16

/** CesiumMan.gltf, parsed once and driven each frame while the skinned-mesh example is
 * active. The one example whose content isn't pure data -- joint-palette sampling from a
 * playback clock is real per-frame simulation, not fakeable as a scene document. */
internal object SkinnedExampleDriver {
    private var vertices: FloatArray? = null
    private var indices: IntArray? = null
    private var skin: Skin? = null
    private var clip: AnimationClip? = null
    private var pose: AnimationPose? = null
    private var elapsedSeconds = 0f

    suspend fun preload() {
        if (skin != null) return
        val bytes = readResourceBytes("assets/models/CesiumMan.gltf")
        val loaded = GltfParser.parseSkinned(bytes.decodeToString())
        val asset = requireNotNull(loaded.firstSkinnedAsset()) { "CesiumMan.gltf has no skinned node." }
        vertices = asset.mesh.toInterleavedSkinned()
        indices = asset.mesh.indices
        skin = asset.skin
        clip = asset.clip
        pose = AnimationPose(asset.skeleton)
    }

    fun createMesh(runtime: SceneGameRuntime): Mesh = runtime.renderer.createMesh(
        MeshGeometry(
            requireNotNull(vertices),
            requireNotNull(indices),
            format = VertexFormat.PositionNormalColorSkin,
        ),
    )

    fun createMaterial(runtime: SceneGameRuntime): Material =
        runtime.renderer.createMaterial(uniformFloatCount = SKINNED_UNIFORM_FLOAT_COUNT)

    /** The joint palette's initial value is sized/valued from the parsed skin, which no static
     * scene document can author -- called once right after instantiate, same shape the
     * original demo used (`world.add(skinnedEntity, SkinnedPose(currentPose.jointPalette(...)))`. */
    fun attachPose(instance: SceneInstance, runtime: SceneGameRuntime) {
        val currentPose = requireNotNull(pose)
        val currentSkin = requireNotNull(skin)
        val node = instance.roots.find { it.name == "skinned-mesh" } ?: return
        runtime.world.add(node.entity, SkinnedPose(currentPose.jointPalette(currentSkin)))
    }

    fun advance(runtime: SceneGameRuntime, delta: Float) {
        val currentPose = pose ?: return
        val currentSkin = skin ?: return
        val currentClip = clip
        if (currentClip != null) {
            elapsedSeconds += delta
            val duration = currentClip.duration
            val timeSeconds = if (duration > 0f) elapsedSeconds % duration else 0f
            currentPose.sample(currentClip, timeSeconds)
        }
        // Family query, not a cached Entity handle -- exactly one entity carries SkinnedPose
        // while this example is active, and this is the sanctioned zero-allocation pattern
        // every other System in this codebase uses, not a per-frame world.get(entity) lookup.
        runtime.world.queryEach<SkinnedPose> { _, skinnedPose ->
            skinnedPose.jointPalette = currentPose.jointPalette(currentSkin)
        }
    }
}
