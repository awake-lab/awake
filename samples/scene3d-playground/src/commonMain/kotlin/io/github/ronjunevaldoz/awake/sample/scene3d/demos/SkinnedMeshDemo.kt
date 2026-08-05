// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser
import io.github.ronjunevaldoz.awake.core.mesh.gltf.LoadedAnimation
import io.github.ronjunevaldoz.awake.core.mesh.gltf.LoadedSkin
import io.github.ronjunevaldoz.awake.core.mesh.gltf.LoadedSkinnedScene
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.Camera as SceneCamera
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.math.sqrt

/**
 * Real GPU-skinned glTF viewer -- loads Khronos's `CesiumMan.gltf` reference sample (`assets/
 * models/CesiumMan.gltf`, downloaded from `KhronosGroup/glTF-Sample-Assets`, see that file's
 * sibling `CesiumMan-LICENSE.md`) via [GltfParser.parseSkinned]. 19 joints, one walk-cycle
 * animation, no `COLOR_0` (renders flat white, same convention [io.github.ronjunevaldoz.awake
 * .core.mesh.gltf.GltfMesh.toInterleavedPositionNormalColor] already uses) and no texture
 * (out of scope -- see the skinning implementation plan's Context section).
 *
 * Doesn't go through the ordinary [io.github.ronjunevaldoz.awake.scene.components.MeshRenderer]
 * ECS component / [io.github.ronjunevaldoz.awake.scene.systems.RenderSystem] path any other
 * demo's mesh does -- a skinned mesh's vertex layout ([VertexFormat.PositionNormalColorSkin])
 * doesn't match this sample's one fixed-format main 3D pipeline (`VertexFormat
 * .PositionNormalColor`, see `Scene3DPlaygroundVulkanBootstrap.kt`), so it draws through
 * [SceneGameRuntime.renderer]'s own [io.github.ronjunevaldoz.awake.render.renderer.Renderer
 * .drawSkinnedMesh] instead -- called directly from [onUpdate] every frame, the same "demo
 * calls the renderer directly" pattern [RotatingCubeDemo] already uses for
 * `renderer.drawDebugLines`. Still spawns a [Transform] entity (unused by rendering, kept so
 * this demo's world-space placement is inspectable/extensible the same way every other demo's
 * is) and a primary camera entity, same as every other demo.
 */
internal object SkinnedMeshDemo {
    private val camera = OrbitCameraController()

    private var scene: LoadedSkinnedScene? = null
    private var player: SkinnedAnimationPlayer? = null
    private var skin: LoadedSkin? = null
    private var animation: LoadedAnimation? = null
    private var meshVertices: FloatArray? = null
    private var meshIndices: IntArray? = null
    private var modelRadius = 1f

    private val timeController = ManualTimeController()
    private var displayGroupExpanded = false

    private var placementEntity: Entity? = null
    private var cameraEntity: Entity? = null
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

    private fun boundingRadius(positions: FloatArray): Float {
        var maxDistanceSquared = 0f
        var i = 0
        while (i < positions.size) {
            val x = positions[i]
            val y = positions[i + 1]
            val z = positions[i + 2]
            val distanceSquared = x * x + y * y + z * z
            if (distanceSquared > maxDistanceSquared) maxDistanceSquared = distanceSquared
            i += 3
        }
        return if (maxDistanceSquared > 0f) sqrt(maxDistanceSquared) else 1f
    }

    val entry = Scene3DDemo(
        id = "skinned-mesh",
        title = "Skinned mesh",
        renderViewport = {
            // Real geometry is drawn via Renderer.drawSkinnedMesh in onUpdate, not this
            // viewport column -- see this object's own doc comment.
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
            placementEntity?.let { world.destroy(it) }
            placementEntity = null
            cameraEntity?.let { world.destroy(it) }
            cameraEntity = null
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
            val currentMesh = mesh
            val currentMaterial = material
            if (currentMesh != null && currentMaterial != null && currentSkin != null && currentPlayer != null) {
                renderer.drawSkinnedMesh(currentMesh, currentMaterial, Mat4(), currentPlayer.jointPalette(currentSkin))
            }
            cameraEntity?.let { entity -> world.add(entity, SceneCamera(camera.computeCamera(MODEL_CENTER), isPrimary = true)) }
        }
    )

    private fun ensureSpawned(runtime: SceneGameRuntime) {
        if (placementEntity != null) return
        val vertices = meshVertices ?: return
        val indices = meshIndices ?: return
        mesh = runtime.renderer.createMesh(
            MeshGeometry(vertices, indices, format = VertexFormat.PositionNormalColorSkin)
        )
        material = runtime.renderer.createMaterial(uniformFloatCount = SKINNED_UNIFORM_FLOAT_COUNT)
        val entity = runtime.world.create()
        runtime.world.add(entity, Transform())
        placementEntity = entity
        val cameraEnt = runtime.world.create()
        runtime.world.add(cameraEnt, SceneCamera(camera.computeCamera(MODEL_CENTER), isPrimary = true))
        cameraEntity = cameraEnt
    }

    private val MODEL_CENTER = Vec3(0f, 0f, 0f)

    /** `16` (MVP) + `64` joints * `16` floats/matrix -- matches `skinned.wgsl`'s fixed
     * `MAX_JOINTS` uniform-array size (WGSL/SPIR-V uniform blocks can't be dynamically sized,
     * so the buffer must be allocated for the shader's declared max regardless of how many
     * joints [skin] actually has -- CesiumMan's 19 leaves the rest of the array simply unread). */
    private const val SKINNED_UNIFORM_FLOAT_COUNT = 16 + 64 * 16
}
