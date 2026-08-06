// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.OrbitCameraController
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.boundingRadius
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser
import io.github.ronjunevaldoz.awake.core.mesh.gltf.LoadedAnimation
import io.github.ronjunevaldoz.awake.core.mesh.gltf.LoadedSkin
import io.github.ronjunevaldoz.awake.core.mesh.gltf.LoadedSkinnedScene
import io.github.ronjunevaldoz.awake.core.mesh.gltf.SkinnedAnimationPlayer
import io.github.ronjunevaldoz.awake.core.utils.ManualTimeController
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.SkinnedPose
import io.github.ronjunevaldoz.awake.scene.controls.OrbitCameraDemoRig
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/**
 * Real GPU-skinned glTF viewer -- loads Khronos's `CesiumMan.gltf` reference sample (`assets/
 * models/CesiumMan.gltf`, downloaded from `KhronosGroup/glTF-Sample-Assets`, see that file's
 * sibling `CesiumMan-LICENSE.md`) via [GltfParser.parseSkinned]. 19 joints, one walk-cycle
 * animation, no `COLOR_0` (renders flat white, same convention [io.github.ronjunevaldoz.awake
 * .core.mesh.gltf.GltfMesh.toInterleavedPositionNormalColor] already uses) and no texture
 * (out of scope -- see the skinning implementation plan's Context section).
 *
 * Renders through the ordinary [MeshRenderer]/[io.github.ronjunevaldoz.awake.scene.systems
 * .RenderSystem] path, same as every other demo -- [rig]'s placement entity carries
 * [MeshRenderer] plus [SkinnedPose] (the joint palette [RenderSystem][io.github
 * .ronjunevaldoz.awake.scene.systems.RenderSystem] reads into that entity's `DrawCall
 * .extraUniformFloats` every frame), its mesh's own [VertexFormat.PositionNormalColorSkin]
 * resolving to the `skinned.wgsl` pipeline via `Renderer.pipelinesByFormat`.
 *
 * `Renderer.drawSkinnedMesh` (the bypass this demo used before `RenderSystem` supported more
 * than one vertex format) is gone now that nothing needs it.
 */
internal object SkinnedMeshDemo {
    private val rig = OrbitCameraDemoRig(OrbitCameraController())
    private val camera get() = rig.camera

    private var scene: LoadedSkinnedScene? = null
    private var player: SkinnedAnimationPlayer? = null
    private var skin: LoadedSkin? = null
    private var animation: LoadedAnimation? = null
    private var meshVertices: FloatArray? = null
    private var meshIndices: IntArray? = null
    private var modelRadius = 1f

    private val timeController = ManualTimeController()
    private var displayGroupExpanded = false

    private var spawned = false
    private var mesh: Mesh? = null
    private var material: Material? = null

    init {
        camera.orbitDegrees = 20f
    }

    suspend fun preload() {
        if (scene != null) return
        val bytes = readResourceBytes("assets/models/CesiumMan.gltf")
        val loaded = GltfParser.parseSkinned(bytes.decodeToString())
        val skinnedNodeIndex = loaded.nodes.indexOfFirst { it.mesh != null && it.skin != null }
        require(skinnedNodeIndex >= 0) { "CesiumMan.gltf has no node with both a mesh and a skin." }
        val node = loaded.nodes[skinnedNodeIndex]
        val gltfMesh = loaded.meshes[node.mesh!!]

        scene = loaded
        skin = loaded.skins[node.skin!!]
        animation = loaded.animations.firstOrNull()
        player = SkinnedAnimationPlayer(loaded)
        meshVertices = gltfMesh.toInterleavedSkinned()
        meshIndices = gltfMesh.indices
        modelRadius = boundingRadius(gltfMesh.positions)

        camera.zoomMin = modelRadius * 0.5f
        camera.zoomMax = modelRadius * 20f
        camera.zoom = modelRadius * 2.5f
        camera.panRange = modelRadius * 2f
        camera.far = camera.zoomMax * 2f
    }

    val entry = Scene3DDemo(
        id = "skinned-mesh",
        title = "Skinned mesh",
        renderViewport = {
            // Real geometry is drawn by RenderSystem via this demo's own ECS entity (see
            // onActivate/onUpdate below) -- this viewport column only needs to exist so the
            // shell lays out a center pane for it.
        },
        renderControls = {
            renderOrbitCameraControls(camera, idPrefix = "skinned", targetLabel = "model")
            shadcnCollapsibleCard(
                id = "skinned-controls-display",
                expanded = displayGroupExpanded,
                onExpandedChange = { displayGroupExpanded = it },
                header = { text("Display", verticallyCentered = true) }
            ) {
                timeController.autoPlay = shadcnSwitch(id = "skinned-auto-play", checked = timeController.autoPlay, label = "Auto-play")
                timeController.hours = shadcnFieldSliderWithValue(
                    id = "skinned-time",
                    label = "Time",
                    min = 0f,
                    max = ManualTimeController.HOURS_PER_CYCLE,
                    value = timeController.hours,
                    enabled = !timeController.autoPlay
                )
                text(label = "Turn off Auto-play to scrub the walk-cycle clip by hand.")
            }
        },
        onActivate = { ensureSpawned(this) },
        onDeactivate = { world ->
            rig.destroy(world)
            spawned = false
        },
        onUpdate = { delta ->
            ensureSpawned(this)
            timeController.advance(delta)
            val currentAnimation = animation
            val currentPlayer = player
            val currentSkin = skin
            if (currentAnimation != null && currentPlayer != null) {
                val duration = currentPlayer.duration(currentAnimation)
                val timeSeconds = if (duration > 0f) (timeController.hours / ManualTimeController.HOURS_PER_CYCLE) * duration else 0f
                currentPlayer.sample(currentAnimation, timeSeconds)
            }
            if (currentSkin != null && currentPlayer != null) {
                rig.entity?.let { world.get<SkinnedPose>(it)?.jointPalette = currentPlayer.jointPalette(currentSkin) }
            }
            rig.refresh(world, MODEL_CENTER)
        }
    )

    private fun ensureSpawned(runtime: SceneGameRuntime) {
        if (spawned) return
        val vertices = meshVertices ?: return
        val indices = meshIndices ?: return
        val currentSkin = requireNotNull(skin) { "SkinnedMeshDemo.preload() must resolve skin before ensureSpawned." }
        val currentPlayer = requireNotNull(player) {
            "SkinnedMeshDemo.preload() must resolve player before ensureSpawned."
        }
        mesh = runtime.renderer.createMesh(
            MeshGeometry(vertices, indices, format = VertexFormat.PositionNormalColorSkin)
        )
        material = runtime.renderer.createMaterial(uniformFloatCount = SKINNED_UNIFORM_FLOAT_COUNT)
        rig.spawn(runtime.world, MODEL_CENTER)
        val entity = rig.entity!!
        runtime.world.add(entity, MeshRenderer(mesh!!, material!!))
        runtime.world.add(entity, SkinnedPose(currentPlayer.jointPalette(currentSkin)))
        spawned = true
    }

    private val MODEL_CENTER = Vec3(0f, 0f, 0f)

    /** `16` (MVP) + `64` joints * `16` floats/matrix -- matches `skinned.wgsl`'s fixed
     * `MAX_JOINTS` uniform-array size (WGSL/SPIR-V uniform blocks can't be dynamically sized,
     * so the buffer must be allocated for the shader's declared max regardless of how many
     * joints [skin] actually has -- CesiumMan's 19 leaves the rest of the array simply unread). */
    private const val SKINNED_UNIFORM_FLOAT_COUNT = 16 + 64 * 16
}
