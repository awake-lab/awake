// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.ManualTimeController
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.LitShadowUniformLayout
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.core.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshBounds
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.rendering.components.Occluder
import io.github.ronjunevaldoz.awake.scene.rendering.components.PbrMaterial
import io.github.ronjunevaldoz.awake.scene.rendering.components.WorldDebugSettings
import io.github.ronjunevaldoz.awake.scene.runtime.SceneDocument
import io.github.ronjunevaldoz.awake.scene.runtime.SceneLoader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import kotlin.math.PI

/**
 * A real cube spinning in place over a real reference ground grid, driven entirely through the
 * real ECS [io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime.world].
 */
internal object RotatingCubeDemo {
    private var cameraEntity: Entity? = null

    private var wireframe = false
    private var cubeMaterialParams: PbrMaterial? = null
    private var spinRadians = 0f

    private var panningEntity: Entity? = null
    private var showAimMarkers = false

    private val timeController = ManualTimeController()
    private var displayGroupExpanded = false

    private var cubeEntity: Entity? = null
    private var lightEntity: Entity? = null

    private var groundEntity: Entity? = null

    // Occlusion-culling proof-of-concept: a wall between the camera's default eye and the
    // spinning cube's rest position, plus a small cube tucked directly behind it along that
    // same sightline. Both are code-spawned (not authored in rotating-cube.scene.json) since
    // Occluder/MeshBounds aren't authorable SceneComponents yet, same reason
    // InstancedCubesExampleDriver/SkinnedExampleDriver attach their own components
    // post-instantiate instead. showOcclusion toggle draws the wall's actual Occluder box as
    // an orange wireframe (see DebugVisualizationSystem) -- the wall's own MeshRenderer is a
    // plain unit cube, not scaled to look wall-shaped, so the wireframe (not the rendered
    // mesh) is the visual proof of what's actually occluding.
    private var wallEntity: Entity? = null
    private var hiddenCubeEntity: Entity? = null
    private var showOcclusion = false
    private var debugSettingsEntity: Entity? = null

    private const val CUBE_REST_HEIGHT = 0.5f

    /** mvp(16) + lightDirection(4) + lightColor(4) + lightMvp(16) + model(16) +
     * cameraPosition(4) + material(4). Must match `lit_shadow.wgsl`'s Uniforms field order. */
    private var document: SceneDocument? = null

    /** The scene document's byte load is suspend, so it happens once in the scene DSL's
     * onReady rather than in the per-frame [Scene3DDemo.onActivate] hook. */
    suspend fun preload() {
        if (document == null) {
            document =
                SceneLoader.loadFromResource("assets/scenes/rotating-cube.scene.json")
        }
    }

    val entry = Scene3DDemo(
        id = "rotating-cube",
        title = "Rotating cube",
        renderViewport = { },
        renderControls = { scope ->
            val config = cameraEntity?.let { world.get<CameraComponent>(it) }
            if (config != null) {
                scope.renderCameraModeToggle(config.mode) { config.mode = it }
            }
            cameraEntity?.let { scope.renderProjectionControls(world, it, idPrefix = "cube") }
            showAimMarkers = scope.shadcnSwitch(
                id = "cube-show-aim-markers",
                checked = showAimMarkers,
                label = "Show aim markers",
            )
            scope.shadcnCollapsibleCard(
                id = "cube-controls-display",
                expanded = displayGroupExpanded,
                onExpandedChange = { displayGroupExpanded = it },
                header = { _, _ -> shadcnText("Display") },
            ) {
                wireframe =
                    shadcnSwitch(id = "cube-wireframe", checked = wireframe, label = "Wireframe")
                timeController.autoPlay = shadcnSwitch(
                    id = "cube-auto-spin",
                    checked = timeController.autoPlay,
                    label = "Auto-spin",
                )
                timeController.hours = shadcnFieldSliderWithValue(
                    id = "cube-time",
                    label = "Time",
                    min = 0f,
                    max = ManualTimeController.HOURS_PER_CYCLE,
                    value = timeController.hours,
                    enabled = !timeController.autoPlay,
                )
                shadcnText(label = "Turn off Auto-spin to freeze the cube at an exact time (0-24h = one full turn).")
                showOcclusion = shadcnSwitch(
                    id = "cube-show-occlusion",
                    checked = showOcclusion,
                    label = "Show occlusion",
                )
                shadcnText(
                    label = "A wall occludes a hidden cube from the camera's default angle -- " +
                        "orange wireframe is the wall's actual occluder box.",
                )
                cubeMaterialParams?.let { pbr ->
                    pbr.metallic = shadcnFieldSliderWithValue(
                        id = "cube-metallic",
                        label = "Metallic",
                        min = 0f,
                        max = 1f,
                        value = pbr.metallic,
                    )
                    pbr.roughness = shadcnFieldSliderWithValue(
                        id = "cube-roughness",
                        label = "Roughness",
                        min = 0f,
                        max = 1f,
                        value = pbr.roughness,
                    )
                }
            }
        },
        onActivate = {
            spinRadians = 0f
            timeController.reset()

            val instance = SceneLoader.instantiate(requireNotNull(document), world)
            val library = requireAssetLibrary()
            instance.renderableRequests.forEach { request ->
                world.add(request.entity, library.resolve(this, request))
            }

            val byName = instance.roots.associateBy { it.name }
            cubeEntity = byName.getValue("cube").entity
            groundEntity = byName.getValue("ground").entity
            cameraEntity = byName.getValue("camera").entity
            lightEntity = byName.getValue("light").entity
            // The sliders drive the component the document produced, so edits and the file
            // stay the same object rather than two copies that can disagree.
            cubeMaterialParams = world.get<PbrMaterial>(cubeEntity!!)

            val cubeMesh = requireMesh("cube")
            val litShadowMaterial = requireMaterial("lit-shadow")
            wallEntity = world.create().also { entity ->
                world.add(entity, Transform(worldMatrix = Mat4().translate(0f, 2.75f, 5f)))
                world.add(entity, MeshRenderer(cubeMesh, litShadowMaterial))
                world.add(entity, Occluder(Aabb(Vec3(-10f, -10f, -0.5f), Vec3(10f, 10f, 0.5f))))
            }
            hiddenCubeEntity = world.create().also { entity ->
                world.add(entity, Transform(worldMatrix = Mat4().translate(0f, 1.85f, 3f)))
                world.add(entity, MeshRenderer(cubeMesh, litShadowMaterial))
                world.add(entity, MeshBounds(Aabb(Vec3(-0.5f, -0.5f, -0.5f), Vec3(0.5f, 0.5f, 0.5f))))
            }
            debugSettingsEntity = world.create().also { entity ->
                world.add(entity, WorldDebugSettings(showOcclusion = showOcclusion))
            }
        },
        onDeactivate = { world ->
            cubeEntity?.let { world.destroy(it) }
            cubeEntity = null
            groundEntity?.let { world.destroy(it) }
            groundEntity = null
            lightEntity?.let { world.destroy(it) }
            lightEntity = null
            panningEntity?.let { world.destroy(it) }
            panningEntity = null
            cameraEntity?.let { world.destroy(it) }
            cameraEntity = null
            wallEntity?.let { world.destroy(it) }
            wallEntity = null
            hiddenCubeEntity?.let { world.destroy(it) }
            hiddenCubeEntity = null
            debugSettingsEntity?.let { world.destroy(it) }
            debugSettingsEntity = null
        },
        onUpdate = { delta ->
            timeController.advance(delta)
            spinRadians = timeController.hours * HOURS_TO_DEGREES * DEGREES_TO_RADIANS

            debugSettingsEntity?.let { world.get<WorldDebugSettings>(it)?.showOcclusion = showOcclusion }

            // Use the real line pipeline. A hand-built edge list would need its own copy of
            // the spin, which is how this demo previously ended up rotating the wrong way.
            renderer.wireframe = wireframe
            renderer.drawReferenceGrid()

            cubeEntity?.let { entity ->
                world.get(entity, SpinControl::class)?.radians = spinRadians
            }

            updateDemoCamera(
                world = world,
                cameraEntity = cameraEntity!!,
                targetEntity = cubeEntity!!,
                panningEntity = panningEntity,
                onPanningEntityCreated = { panningEntity = it },
            )

            if (showAimMarkers) {
                val markers = mutableListOf<LineSegment>()
                val cpos = cubeWorldPosition()
                markers += LineSegment(cpos, cpos + Vec3(0.1f, 0f, 0f), AXIS_COLOR_X)
                markers += LineSegment(cpos, cpos + Vec3(0f, 0.1f, 0f), AXIS_COLOR_Y)
                markers += LineSegment(cpos, cpos + Vec3(0f, 0f, 0.1f), AXIS_COLOR_Z)
                renderer.drawDebugLines(markers)
            }
        },
    )

    private fun cubeWorldPosition(): Vec3 = Vec3(0f, CUBE_REST_HEIGHT, 0f)

    private const val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()
    private const val HOURS_TO_DEGREES = 360f / 24f
}

/** `lit_shadow.wgsl`'s Uniforms size -- taken from the shared layout rather than re-summed
 * here, so adding a field to that shader can't leave this call site silently short. */
internal val LIT_SHADOW_UNIFORM_FLOAT_COUNT = LitShadowUniformLayout.total
