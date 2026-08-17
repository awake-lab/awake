// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.boundingCenter
import io.github.ronjunevaldoz.awake.core.math.boundingRadius
import io.github.ronjunevaldoz.awake.asset.gltf.GltfParser
import io.github.ronjunevaldoz.awake.asset.gltf.LoadedSkinnedScene
import io.github.ronjunevaldoz.awake.asset.gltf.firstSkinnedAsset
import io.github.ronjunevaldoz.awake.core.animation.AnimationClip
import io.github.ronjunevaldoz.awake.core.animation.AnimationCrossfade
import io.github.ronjunevaldoz.awake.core.animation.AnimationPose
import io.github.ronjunevaldoz.awake.core.animation.Skin
import io.github.ronjunevaldoz.awake.core.utils.ManualTimeController
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.camera
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.meshRenderer
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.scene
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.transform
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.SkinnedPose
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/**
 * Real GPU-skinned glTF viewer.
 */
internal object SkinnedMeshDemo {
    private var cameraEntity: Entity? = null
    private var skinnedEntity: Entity? = null

    private var scene: LoadedSkinnedScene? = null
    private var pose: AnimationPose? = null
    private var skin: Skin? = null
    private var clip: AnimationClip? = null

    // Crossfade blending proof-of-concept: CesiumMan.gltf has exactly one real clip, so this
    // doesn't demo a semantically-real walk/run transition -- it proves the blend MATH by
    // crossfading the SAME clip's timeline back to its own start (a hard cut a viewer would see
    // as a pop otherwise), not a second real animated asset (none is vendored in this repo).
    // `.copy()` on the clip each press gives AnimationCrossfade.play() a distinct reference, so
    // its `clip === currentClip` same-clip no-op guard doesn't swallow the restart.
    private var crossfadeEnabled = false
    private var crossfade: AnimationCrossfade? = null
    private var meshVertices: FloatArray? = null
    private var meshIndices: IntArray? = null
    private var modelRadius = 1f
    private var modelCenter: Vec3 = Vec3(0f, 0f, 0f)

    private var followTargetEntity: Entity? = null
    private var panningEntity: Entity? = null
    private var showAimMarkers = false

    private val timeController = ManualTimeController()
    private var displayGroupExpanded = false
    private var crossfadeGroupExpanded = false

    private var spawned = false
    private var mesh: Mesh? = null
    private var material: Material? = null

    suspend fun preload() {
        if (scene != null) return
        val bytes = readResourceBytes("assets/models/CesiumMan.gltf")
        val loaded = GltfParser.parseSkinned(bytes.decodeToString())
        val asset = requireNotNull(loaded.firstSkinnedAsset())

        scene = loaded
        skin = asset.skin
        clip = asset.clip
        pose = AnimationPose(asset.skeleton)
        crossfade = AnimationCrossfade(asset.skeleton)
        asset.clip?.let { crossfade?.play(it, blendSeconds = 0f) }
        meshVertices = asset.mesh.toInterleavedSkinned()
        meshIndices = asset.mesh.indices
        modelRadius = boundingRadius(asset.mesh.positions)

        modelCenter = boundingCenter(asset.mesh.positions)
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
            showAimMarkers = scope.shadcnSwitch(
                id = "skinned-show-aim-markers",
                checked = showAimMarkers,
                label = "Show aim markers",
            )
            scope.shadcnCollapsibleCard(
                id = "skinned-controls-display",
                expanded = displayGroupExpanded,
                onExpandedChange = { displayGroupExpanded = it },
                header = { _, _ -> shadcnText("Display") },
            ) {
                timeController.autoPlay = shadcnSwitch(
                    id = "skinned-auto-play",
                    checked = timeController.autoPlay,
                    label = "Auto-play",
                )
                timeController.hours = shadcnFieldSliderWithValue(
                    id = "skinned-time",
                    label = "Time",
                    min = 0f,
                    max = ManualTimeController.HOURS_PER_CYCLE,
                    value = timeController.hours,
                    enabled = !timeController.autoPlay,
                )
                shadcnText(label = "Turn off Auto-play to scrub the walk-cycle clip by hand.")
            }
            scope.shadcnCollapsibleCard(
                id = "skinned-controls-crossfade",
                expanded = crossfadeGroupExpanded,
                onExpandedChange = { crossfadeGroupExpanded = it },
                header = { _, _ -> shadcnText("Crossfade blending") },
            ) {
                crossfadeEnabled = shadcnSwitch(
                    id = "skinned-crossfade-enabled",
                    checked = crossfadeEnabled,
                    label = "Drive playback via AnimationCrossfade",
                )
                shadcnButton(
                    id = "skinned-crossfade-restart",
                    label = "Crossfade to clip start",
                    enabled = crossfadeEnabled,
                    onClick = {
                        clip?.let { crossfade?.play(it.copy(), blendSeconds = CROSSFADE_BLEND_SECONDS) }
                    },
                )
                shadcnText(
                    label = "Proves the blend math (this demo has only one real clip to play " +
                        "with) -- restarting mid-cycle eases back to the start over half a " +
                        "second instead of popping.",
                )
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
            val currentSkin = skin
            if (crossfadeEnabled) {
                val currentCrossfade = crossfade
                if (currentCrossfade != null && currentSkin != null) {
                    val palette = currentCrossfade.advance(delta, currentSkin)
                    skinnedEntity?.let { world.get<SkinnedPose>(it)?.jointPalette = palette }
                }
            } else {
                timeController.advance(delta)
                val currentClip = clip
                val currentPose = pose
                if (currentClip != null && currentPose != null) {
                    val duration = currentClip.duration
                    val timeSeconds =
                        if (duration > 0f) (timeController.hours / ManualTimeController.HOURS_PER_CYCLE) * duration else 0f
                    currentPose.sample(currentClip, timeSeconds)
                }
                if (currentSkin != null && currentPose != null) {
                    skinnedEntity?.let {
                        world.get<SkinnedPose>(it)?.jointPalette =
                            currentPose.jointPalette(currentSkin)
                    }
                }
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
                markers += LineSegment(
                    mc,
                    mc + Vec3(0.1f, 0f, 0f),
                    floatArrayOf(0.9f, 0.15f, 0.15f, 1f),
                )
                markers += LineSegment(
                    mc,
                    mc + Vec3(0f, 0.1f, 0f),
                    floatArrayOf(0.15f, 0.75f, 0.15f, 1f),
                )
                markers += LineSegment(
                    mc,
                    mc + Vec3(0f, 0f, 0.1f),
                    floatArrayOf(0.15f, 0.35f, 0.9f, 1f),
                )
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
        val currentPose = requireNotNull(pose)
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

            skinnedEntity =
                entity("SkinnedMesh", Modifier().transform().meshRenderer(mesh!!, material!!))
            runtime.world.add(skinnedEntity!!, SkinnedPose(currentPose.jointPalette(currentSkin)))
        }

        spawned = true
    }

    private const val SKINNED_UNIFORM_FLOAT_COUNT = 16 + 64 * 16
    private const val CROSSFADE_BLEND_SECONDS = 0.5f
}
