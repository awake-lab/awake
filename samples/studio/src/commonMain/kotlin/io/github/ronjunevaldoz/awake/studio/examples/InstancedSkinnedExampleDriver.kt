// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.asset.gltf.GltfParser
import io.github.ronjunevaldoz.awake.asset.gltf.firstSkinnedAsset
import io.github.ronjunevaldoz.awake.core.animation.AnimationClip
import io.github.ronjunevaldoz.awake.core.animation.AnimationPose
import io.github.ronjunevaldoz.awake.core.animation.Skin
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.scene.rendering.components.InstancedSkinnedMeshRenderer
import io.github.ronjunevaldoz.awake.scene.rendering.components.SkinnedInstance
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.Scene

private const val GRID_SIDE = 3
private const val SPACING = 1.2f
private const val INSTANCED_UNIFORM_FLOAT_COUNT = 24

/** CesiumMan.gltf, parsed once, resampled per instance each frame -- same shape
 * [SkinnedExampleDriver] uses for the single-instance case, but one [SkinnedInstance] per grid
 * cell instead of one [io.github.ronjunevaldoz.awake.scene.rendering.components.SkinnedPose].
 * Each instance's clock is phase-offset so their walk cycles visibly drift apart, proving the
 * per-instance POSE varies, not just the per-instance transform. `InstancedSkinnedMeshRenderer`
 * isn't an authorable scene component yet, same reason [InstancedCubesExampleDriver] attaches
 * its component post-instantiate instead of via the scene document. */
internal object InstancedSkinnedExampleDriver {
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
        MeshGeometry(requireNotNull(vertices), requireNotNull(indices), format = VertexFormat.PositionNormalColorSkin),
    )

    fun createMaterial(runtime: SceneGameRuntime): Material =
        runtime.renderer.createMaterial(uniformFloatCount = INSTANCED_UNIFORM_FLOAT_COUNT)

    fun attach(instance: Scene, runtime: SceneGameRuntime) {
        val node = instance.roots.find { it.name == "instanced-skinned" } ?: return
        val mesh = runtime.requireMesh("instanced-skinned-mesh")
        val material = runtime.requireMaterial("instanced-skinned-material")
        runtime.world.add(node.entity, InstancedSkinnedMeshRenderer(mesh, material, sampleInstances()))
    }

    fun advance(runtime: SceneGameRuntime, delta: Float) {
        elapsedSeconds += delta
        runtime.world.queryEach<InstancedSkinnedMeshRenderer> { entity, existing ->
            runtime.world.add(entity, existing.copy(instances = sampleInstances()))
        }
    }

    private fun sampleInstances(): List<SkinnedInstance> {
        val currentPose = pose
        val currentSkin = skin
        if (currentPose == null || currentSkin == null) return emptyList()
        val currentClip = clip
        val duration = currentClip?.duration ?: 0f
        val total = GRID_SIDE * GRID_SIDE
        val instances = ArrayList<SkinnedInstance>(total)
        for (index in 0 until total) {
            if (currentClip != null && duration > 0f) {
                val phase = (elapsedSeconds + index * duration / total) % duration
                currentPose.sample(currentClip, phase)
            }
            instances += SkinnedInstance(
                transform = gridTransforms[index],
                jointPalette = currentPose.jointPalette(currentSkin),
            )
        }
        return instances
    }

    private val gridTransforms: List<Mat4> = buildList {
        val offset = (GRID_SIDE - 1) * SPACING / 2f
        for (index in 0 until GRID_SIDE * GRID_SIDE) {
            add(
                Mat4().translate(
                    (index % GRID_SIDE) * SPACING - offset,
                    0f,
                    (index / GRID_SIDE) * SPACING - offset,
                ),
            )
        }
    }
}
