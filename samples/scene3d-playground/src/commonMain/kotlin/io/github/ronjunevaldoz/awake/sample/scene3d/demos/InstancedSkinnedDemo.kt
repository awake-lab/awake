// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.asset.gltf.GltfParser
import io.github.ronjunevaldoz.awake.asset.gltf.LoadedAnimation
import io.github.ronjunevaldoz.awake.asset.gltf.LoadedSkin
import io.github.ronjunevaldoz.awake.asset.gltf.SkinnedAnimationPlayer
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.camera
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.scene
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.InstancedSkinnedMeshRenderer
import io.github.ronjunevaldoz.awake.scene.rendering.components.SkinnedInstance
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/**
 * A grid of INDEPENDENTLY ANIMATED CesiumMan copies in one GPU draw call -- the only demo that
 * exercises [InstancedSkinnedMeshRenderer] (and therefore both backends' skinned-instanced
 * pipeline + joint-palette storage buffer).
 *
 * Each copy's animation clock is phase-offset by its grid index, so they visibly walk out of
 * sync: that desync is the actual proof the per-instance POSE varies, not just the per-instance
 * transform (which static instancing already does).
 *
 * Needs the `skinned_instanced` shader set wired into the bootstrap (see
 * `Scene3DPlaygroundVulkanBootstrap`/`...WebGpuBootstrap`); without it the backend has no
 * skinned-instanced pipeline for this format and the whole draw call is skipped.
 */
internal object InstancedSkinnedDemo {
    private var cameraEntity: Entity? = null
    private var crowdEntity: Entity? = null
    private var anchorEntity: Entity? = null
    private var panningEntity: Entity? = null

    private var player: SkinnedAnimationPlayer? = null
    private var skin: LoadedSkin? = null
    private var animation: LoadedAnimation? = null
    private var meshVertices: FloatArray? = null
    private var meshIndices: IntArray? = null

    private var mesh: Mesh? = null
    private var material: Material? = null

    private var elapsedSeconds = 0f

    /** Reparsed here rather than shared with [SkinnedMeshDemo]: that demo mutates its own
     * player's sampled pose every frame, and this one re-samples the same player once per
     * instance, so sharing one would leave whichever ran last holding the other's pose. */
    suspend fun preload() {
        if (player != null) return
        val loaded = GltfParser.parseSkinned(
            readResourceBytes("assets/models/CesiumMan.gltf").decodeToString(),
        )
        val node = loaded.nodes.first { it.mesh != null && it.skin != null }
        val gltfMesh = loaded.meshes[node.mesh!!]
        skin = loaded.skins[node.skin!!]
        animation = loaded.animations.firstOrNull()
        player = SkinnedAnimationPlayer(loaded)
        meshVertices = gltfMesh.toInterleavedSkinned()
        meshIndices = gltfMesh.indices
    }

    val entry = Scene3DDemo(
        id = "instanced-skinned",
        title = "Instanced skinned",
        renderViewport = { },
        renderControls = { scope ->
            val config = cameraEntity?.let { world.get<CameraComponent>(it) }
            if (config != null) {
                scope.renderCameraModeToggle(config.mode) { config.mode = it }
            }
            cameraEntity?.let {
                scope.renderProjectionControls(world, it, idPrefix = "instanced-skinned")
            }
            scope.shadcnText(
                label = "${GRID_SIDE * GRID_SIDE} animated copies, one draw call. Each copy's " +
                    "clock is phase-offset, so their walk cycles drift apart.",
            )
        },
        onActivate = { ensureSpawned(this) },
        onDeactivate = { world ->
            listOfNotNull(cameraEntity, crowdEntity, anchorEntity, panningEntity)
                .forEach { world.destroy(it) }
            cameraEntity = null
            crowdEntity = null
            anchorEntity = null
            panningEntity = null
            elapsedSeconds = 0f
        },
        onUpdate = { delta ->
            ensureSpawned(this)
            elapsedSeconds += delta
            renderer.drawReferenceGrid()

            crowdEntity?.let { entity ->
                world.get<InstancedSkinnedMeshRenderer>(entity)?.let { existing ->
                    world.add(entity, existing.copy(instances = sampleInstances()))
                }
            }

            updateDemoCamera(
                world = world,
                cameraEntity = cameraEntity!!,
                targetEntity = anchorEntity!!,
                panningEntity = panningEntity,
                onPanningEntityCreated = { panningEntity = it },
            )
        },
    )

    /** One [SkinnedInstance] per grid cell, each sampled at its OWN clock. Re-sampled every
     * frame through the single shared player (sample -> read palette -> next instance), which
     * is why the palettes have to be copied out one at a time rather than in a batch. */
    private fun sampleInstances(): List<SkinnedInstance> {
        val currentPlayer = player
        val currentSkin = skin
        if (currentPlayer == null || currentSkin == null) return emptyList()
        val currentAnimation = animation
        val duration = currentAnimation?.let { currentPlayer.duration(it) } ?: 0f
        val total = GRID_SIDE * GRID_SIDE
        val instances = ArrayList<SkinnedInstance>(total)
        for (index in 0 until total) {
            if (currentAnimation != null && duration > 0f) {
                val phase = (elapsedSeconds + index * duration / total) % duration
                currentPlayer.sample(currentAnimation, phase)
            }
            instances += SkinnedInstance(
                transform = gridTransforms[index],
                jointPalette = currentPlayer.jointPalette(currentSkin),
            )
        }
        return instances
    }

    /** A GRID_SIDE x GRID_SIDE lattice on the ground plane, built once -- only the poses change
     * per frame, so rebuilding these Mat4s every frame would allocate for nothing. */
    private val gridTransforms: List<Mat4> = buildList {
        val offset = (GRID_SIDE - 1) * SPACING / 2f
        for (index in 0 until GRID_SIDE * GRID_SIDE) {
            add(
                Mat4().apply {
                    m03 = (index % GRID_SIDE) * SPACING - offset
                    m23 = (index / GRID_SIDE) * SPACING - offset
                },
            )
        }
    }

    private fun ensureSpawned(runtime: SceneGameRuntime) {
        val vertices = meshVertices
        val indices = meshIndices
        if (crowdEntity != null || vertices == null || indices == null) return
        val crowdMesh = mesh ?: runtime.renderer.createMesh(
            MeshGeometry(vertices, indices, format = VertexFormat.PositionNormalColorSkin),
        ).also { mesh = it }
        val crowdMaterial = material
            ?: runtime.renderer.createMaterial(uniformFloatCount = INSTANCED_UNIFORM_FLOAT_COUNT)
                .also { material = it }

        runtime.world.scene {
            cameraEntity = entity(
                "Camera",
                Modifier().camera(
                    target = null,
                    lens = CoreCamera.perspective(
                        eye = Vec3(0f, CAMERA_HEIGHT, CAMERA_DISTANCE),
                        center = Vec3.ZERO,
                    ),
                ),
            )
        }
        anchorEntity = runtime.world.create().also { entity ->
            runtime.world.add(entity, Transform())
        }
        crowdEntity = runtime.world.create().also { entity ->
            runtime.world.add(
                entity,
                InstancedSkinnedMeshRenderer(crowdMesh, crowdMaterial, sampleInstances()),
            )
        }
    }

    /** `skinned_instanced.wgsl`'s Uniforms -- same 24 floats as `instanced.wgsl`. The joint
     * palettes are NOT here; they ride the storage buffer instead. */
    private const val INSTANCED_UNIFORM_FLOAT_COUNT = 24

    private const val GRID_SIDE = 3
    private const val SPACING = 1.2f
    private const val CAMERA_HEIGHT = 2.5f
    private const val CAMERA_DISTANCE = 6f
}
