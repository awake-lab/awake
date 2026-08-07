// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.boundingCenter
import io.github.ronjunevaldoz.awake.core.math.boundingRadius
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser
import io.github.ronjunevaldoz.awake.core.mesh.gltf.LoadedAnimation
import io.github.ronjunevaldoz.awake.core.mesh.gltf.LoadedSkin
import io.github.ronjunevaldoz.awake.core.mesh.gltf.LoadedSkinnedScene
import io.github.ronjunevaldoz.awake.core.mesh.gltf.SkinnedAnimationPlayer
import io.github.ronjunevaldoz.awake.core.utils.ManualTimeController
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.components.SkinnedPose
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.camera
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.meshRenderer
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.scene
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.transform
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/**
 * Real GPU-skinned glTF viewer.
 */
internal object SkinnedMeshDemo {
    private var cameraEntity: Entity? = null
    private var skinnedEntity: Entity? = null

    private var scene: LoadedSkinnedScene? = null
    private var player: SkinnedAnimationPlayer? = null
    private var skin: LoadedSkin? = null
    private var animation: LoadedAnimation? = null
    private var meshVertices: FloatArray? = null
    private var meshIndices: IntArray? = null
    private var modelRadius = 1f
    private var modelCenter: Vec3 = Vec3(0f, 0f, 0f)

    private var followTargetEntity: Entity? = null
    private var panningEntity: Entity? = null
    private var showAimMarkers = false

    private val timeController = ManualTimeController()
    private var displayGroupExpanded = false

    private var spawned = false
    private var mesh: Mesh? = null
    private var material: Material? = null

    suspend fun preload() {
        if (scene != null) return
        val bytes = readResourceBytes("assets/models/CesiumMan.gltf")
        val loaded = GltfParser.parseSkinned(bytes.decodeToString())
        val skinnedNodeIndex = loaded.nodes.indexOfFirst { it.mesh != null && it.skin != null }
        require(skinnedNodeIndex >= 0)
        val node = loaded.nodes[skinnedNodeIndex]
        val gltfMesh = loaded.meshes[node.mesh!!]

        scene = loaded
        skin = loaded.skins[node.skin!!]
        animation = loaded.animations.firstOrNull()
        player = SkinnedAnimationPlayer(loaded)
        meshVertices = gltfMesh.toInterleavedSkinned()
        meshIndices = gltfMesh.indices
        modelRadius = boundingRadius(gltfMesh.positions)

        modelCenter = boundingCenter(gltfMesh.positions)
    }

    val entry = Scene3DDemo(
        id = "skinned-mesh",
        title = "Skinned mesh",
        renderViewport = { },
        renderControls = { scope ->
            val config = cameraEntity?.let { world.get<CameraComponent>(it) }
            if (config != null) {
                scope.renderCameraModeToggle(config.mode) { config.mode = it }
            }
            cameraEntity?.let { scope.renderProjectionControls(world, it, idPrefix = "skinned") }
            showAimMarkers = scope.shadcnSwitch(id = "skinned-show-aim-markers", checked = showAimMarkers, label = "Show aim markers")
            scope.shadcnCollapsibleCard(
                id = "skinned-controls-display",
                expanded = displayGroupExpanded,
                onExpandedChange = { displayGroupExpanded = it },
                header = { text("Display", verticallyCentered = true) },
            ) {
                timeController.autoPlay = shadcnSwitch(id = "skinned-auto-play", checked = timeController.autoPlay, label = "Auto-play")
                timeController.hours = shadcnFieldSliderWithValue(
                    id = "skinned-time",
                    label = "Time",
                    min = 0f,
                    max = ManualTimeController.HOURS_PER_CYCLE,
                    value = timeController.hours,
                    enabled = !timeController.autoPlay,
                )
                text(label = "Turn off Auto-play to scrub the walk-cycle clip by hand.")
            }
        },
        onActivate = { ensureSpawned(this) },
        onDeactivate = { world ->
            cameraEntity?.let { world.destroy(it) }
            cameraEntity = null
            skinnedEntity?.let { world.destroy(it) }
            skinnedEntity = null
            followTargetEntity?.let { world.destroy(it) }
            followTargetEntity = null
            panningEntity?.let { world.destroy(it) }
            panningEntity = null
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
                skinnedEntity?.let { world.get<SkinnedPose>(it)?.jointPalette = currentPlayer.jointPalette(currentSkin) }
            }

            renderer.drawReferenceGrid()

            val fte = followTargetEntity ?: world.create().also {
                followTargetEntity = it
                world.add(it, Transform().apply { position.set(modelCenter) })
            }

            updateDemoCamera(
                world = world,
                cameraEntity = cameraEntity!!,
                targetEntity = fte,
                panningEntity = panningEntity,
                onPanningEntityCreated = { panningEntity = it },
            )

            if (showAimMarkers) {
                val markers = mutableListOf<LineSegment>()
                val mc = modelCenter
                markers += LineSegment(mc, mc + Vec3(0.1f, 0f, 0f), floatArrayOf(0.9f, 0.15f, 0.15f, 1f))
                markers += LineSegment(mc, mc + Vec3(0f, 0.1f, 0f), floatArrayOf(0.15f, 0.75f, 0.15f, 1f))
                markers += LineSegment(mc, mc + Vec3(0f, 0f, 0.1f), floatArrayOf(0.15f, 0.35f, 0.9f, 1f))
                val ft = world.get<Transform>(fte)?.position ?: modelCenter
                markers += LineSegment(ft, ft + Vec3(0.1f, 0f, 0f), floatArrayOf(1f, 1f, 0f, 1f))
                markers += LineSegment(ft, ft + Vec3(0f, 0.1f, 0f), floatArrayOf(1f, 1f, 0f, 1f))
                markers += LineSegment(ft, ft + Vec3(0f, 0f, 0.1f), floatArrayOf(1f, 1f, 0f, 1f))
                renderer.drawDebugLines(markers)
            }
        },
    )

    private fun ensureSpawned(runtime: SceneGameRuntime) {
        if (spawned) return
        val vertices = meshVertices ?: return
        val indices = meshIndices ?: return
        val currentSkin = requireNotNull(skin)
        val currentPlayer = requireNotNull(player)
        mesh = runtime.renderer.createMesh(
            MeshGeometry(vertices, indices, format = VertexFormat.PositionNormalColorSkin),
        )
        material = runtime.renderer.createMaterial(uniformFloatCount = SKINNED_UNIFORM_FLOAT_COUNT)

        runtime.world.scene {
            cameraEntity = entity(
                "Camera",
                Modifier().camera(
                    target = null,
                    lens = CoreCamera.perspective(eye = Vec3(0f, 5f, 10f), center = modelCenter),
                ),
            )

            skinnedEntity = entity("SkinnedMesh", Modifier().transform().meshRenderer(mesh!!, material!!))
            runtime.world.add(skinnedEntity!!, SkinnedPose(currentPlayer.jointPalette(currentSkin)))
        }

        spawned = true
    }

    private const val SKINNED_UNIFORM_FLOAT_COUNT = 16 + 64 * 16
}
