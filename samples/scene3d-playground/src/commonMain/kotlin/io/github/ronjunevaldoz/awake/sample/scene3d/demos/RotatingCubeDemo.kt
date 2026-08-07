// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.ManualTimeController
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.components.Light
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import kotlin.math.PI

/**
 * A real cube spinning in place over a real reference ground grid, driven entirely through the
 * real ECS [io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime.world].
 */
internal object RotatingCubeDemo {
    private var cameraEntity: Entity? = null

    private var wireframe = false
    private var spinRadians = 0f

    private var panningEntity: Entity? = null
    private var showAimMarkers = false

    private val timeController = ManualTimeController()
    private var displayGroupExpanded = false

    private var cubeMesh: Mesh? = null
    private var material: Material? = null
    private var cubeEntity: Entity? = null
    private var lightEntity: Entity? = null

    private var groundMesh: Mesh? = null
    private var groundEntity: Entity? = null

    private const val CUBE_REST_HEIGHT = 0.5f

    /** mvp(16) + lightDirection(4) + lightColor(4) + lightMvp(16) -- see `lit_shadow.wgsl`'s
     * own Uniforms struct doc comment for why lightMvp is appended last. Only needed by demos
     * whose entities draw through the primary (shadow-casting/receiving) pipeline -- this is
     * the only demo in this sample app that does. */
    private const val LIT_SHADOW_UNIFORM_FLOAT_COUNT = 40

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
            showAimMarkers = scope.shadcnSwitch(id = "cube-show-aim-markers", checked = showAimMarkers, label = "Show aim markers")
            scope.shadcnCollapsibleCard(
                id = "cube-controls-display",
                expanded = displayGroupExpanded,
                onExpandedChange = { displayGroupExpanded = it },
                header = { text("Display", verticallyCentered = true) }
            ) {
                wireframe = shadcnSwitch(id = "cube-wireframe", checked = wireframe, label = "Wireframe")
                timeController.autoPlay = shadcnSwitch(id = "cube-auto-spin", checked = timeController.autoPlay, label = "Auto-spin")
                timeController.hours = shadcnFieldSliderWithValue(
                    id = "cube-time",
                    label = "Time",
                    min = 0f,
                    max = ManualTimeController.HOURS_PER_CYCLE,
                    value = timeController.hours,
                    enabled = !timeController.autoPlay
                )
                text(label = "Turn off Auto-spin to freeze the cube at an exact time (0-24h = one full turn).")
            }
        },
        onActivate = {
            if (cubeMesh == null) cubeMesh = renderer.createMesh(rotatingCubeGeometry)
            if (groundMesh == null) groundMesh = renderer.createMesh(rotatingGroundPlaneGeometry)
            // Shared by both the cube and the ground plane -- Material already supports
            // multiple draw calls per frame (see its own uniformSlotsByFrame/materialUsage
            // doc comments), so one lit+shadow material covers every primary-format entity
            // this demo draws.
            if (material == null) material = renderer.createMaterial(uniformFloatCount = LIT_SHADOW_UNIFORM_FLOAT_COUNT)
            spinRadians = 0f
            timeController.reset()
            val cube = world.create()
            world.add(cube, Transform().apply { position.set(cubeWorldPosition()) })
            world.add(cube, SpinControl().apply { radians = spinRadians })
            world.add(cube, MeshRenderer(cubeMesh!!, material!!))
            cubeEntity = cube

            val ground = world.create()
            world.add(ground, Transform())
            world.add(ground, MeshRenderer(groundMesh!!, material!!))
            groundEntity = ground

            val cam = world.create()
            world.add(cam, Camera(io.github.ronjunevaldoz.awake.core.math.Camera(
                eye = Vec3(0f, 5f, 10f),
                center = cubeWorldPosition(),
                fovYRadians = 45f * (PI / 180.0).toFloat(),
                near = 0.1f,
                far = 100f
            )))
            cameraEntity = cam
            
            lightEntity = world.create().also { world.add(it, Light()) }
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
        },
        onUpdate = { delta ->
            timeController.advance(delta)
            spinRadians = timeController.hours * HOURS_TO_DEGREES * DEGREES_TO_RADIANS

            // The real VK_POLYGON_MODE_LINE pipeline, not a hand-built edge list. This demo used
            // to swap the cube's MeshRenderer for its own drawDebugLines() call, which meant the
            // wireframe cube got its own second implementation of the spin -- with the opposite
            // sign convention to SpinSystem's, so it visibly rotated the wrong way and sat in a
            // slightly different place than the solid cube it was standing in for.
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
                onPanningEntityCreated = { panningEntity = it }
            )

            if (showAimMarkers) {
                val markers = mutableListOf<LineSegment>()
                val cpos = cubeWorldPosition()
                markers += LineSegment(cpos, cpos + Vec3(0.1f, 0f, 0f), AXIS_COLOR_X)
                markers += LineSegment(cpos, cpos + Vec3(0f, 0.1f, 0f), AXIS_COLOR_Y)
                markers += LineSegment(cpos, cpos + Vec3(0f, 0f, 0.1f), AXIS_COLOR_Z)
                renderer.drawDebugLines(markers)
            }
        }
    )

    private fun cubeWorldPosition(): Vec3 = Vec3(0f, CUBE_REST_HEIGHT, 0f)

    private const val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()
    private const val HOURS_TO_DEGREES = 360f / 24f
}
