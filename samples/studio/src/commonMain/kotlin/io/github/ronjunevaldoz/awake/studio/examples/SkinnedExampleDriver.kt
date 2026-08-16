// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.asset.gltf.GltfParser
import io.github.ronjunevaldoz.awake.asset.gltf.LoadedAnimation
import io.github.ronjunevaldoz.awake.asset.gltf.LoadedSkin
import io.github.ronjunevaldoz.awake.asset.gltf.SkinnedAnimationPlayer
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.ecs.World
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
    private var skin: LoadedSkin? = null
    private var animation: LoadedAnimation? = null
    private var player: SkinnedAnimationPlayer? = null
    private var elapsedSeconds = 0f

    suspend fun preload() {
        if (skin != null) return
        val bytes = readResourceBytes("assets/models/CesiumMan.gltf")
        val loaded = GltfParser.parseSkinned(bytes.decodeToString())
        val skinnedNodeIndex = loaded.nodes.indexOfFirst { it.mesh != null && it.skin != null }
        require(skinnedNodeIndex >= 0) { "CesiumMan.gltf has no skinned node." }
        val node = loaded.nodes[skinnedNodeIndex]
        val gltfMesh = loaded.meshes[node.mesh!!]
        vertices = gltfMesh.toInterleavedSkinned()
        indices = gltfMesh.indices
        skin = loaded.skins[node.skin!!]
        animation = loaded.animations.firstOrNull()
        player = SkinnedAnimationPlayer(loaded)
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
     * original demo used (`world.add(skinnedEntity, SkinnedPose(currentPlayer.jointPalette(...)))`. */
    fun attachPose(instance: SceneInstance, world: World) {
        val currentPlayer = requireNotNull(player)
        val currentSkin = requireNotNull(skin)
        val node = instance.roots.find { it.name == "skinned-mesh" } ?: return
        world.add(node.entity, SkinnedPose(currentPlayer.jointPalette(currentSkin)))
    }

    fun advance(runtime: SceneGameRuntime, delta: Float) {
        val currentPlayer = player ?: return
        val currentSkin = skin ?: return
        val currentAnimation = animation
        if (currentAnimation != null) {
            elapsedSeconds += delta
            val duration = currentPlayer.duration(currentAnimation)
            val timeSeconds = if (duration > 0f) elapsedSeconds % duration else 0f
            currentPlayer.sample(currentAnimation, timeSeconds)
        }
        // Family query, not a cached Entity handle -- exactly one entity carries SkinnedPose
        // while this example is active, and this is the sanctioned zero-allocation pattern
        // every other System in this codebase uses, not a per-frame world.get(entity) lookup.
        runtime.world.queryEach<SkinnedPose> { _, pose ->
            pose.jointPalette = currentPlayer.jointPalette(currentSkin)
        }
    }
}
