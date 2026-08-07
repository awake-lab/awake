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
import io.github.ronjunevaldoz.awake.scene.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.components.Light
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A real cube spinning in place over a real reference ground grid, driven entirely through the
 * real ECS [io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime.world].
 */
internal object RotatingCubeDemo {
    private var cameraEntity: Entity? = null

    private var wireframe = false
    private var spinRadians = 0f

    private var followTargetEntity: Entity? = null
    private var panningEntity: Entity? = null
    private var moveTargetWithWASD = false
    private var showAimMarkers = false

    private val timeController = ManualTimeController()
    private var displayGroupExpanded = false

    private var cubeMesh: Mesh? = null
    private var material: Material? = null
    private var cubeEntity: Entity? = null
    private var lightEntity: Entity? = null

    private const val CUBE_REST_HEIGHT = 0.5f

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
            moveTargetWithWASD = scope.shadcnSwitch(id = "cube-move-target", checked = moveTargetWithWASD, label = "WASD moves target", enabled = config?.mode == CameraMode.Cinematic)
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
            if (material == null) material = renderer.createMaterial()
            spinRadians = 0f
            timeController.reset()
            val cube = world.create()
            world.add(cube, Transform().apply { position.set(cubeWorldPosition()) })
            world.add(cube, SpinControl().apply { radians = spinRadians })
            if (!wireframe) world.add(cube, MeshRenderer(cubeMesh!!, material!!))
            cubeEntity = cube
            
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
            lightEntity?.let { world.destroy(it) }
            lightEntity = null
            followTargetEntity?.let { world.destroy(it) }
            followTargetEntity = null
            panningEntity?.let { world.destroy(it) }
            panningEntity = null
            cameraEntity?.let { world.destroy(it) }
            cameraEntity = null
        },
        onUpdate = { delta ->
            timeController.advance(delta)
            spinRadians = timeController.hours * HOURS_TO_DEGREES * DEGREES_TO_RADIANS

            renderer.drawReferenceGrid()
            if (wireframe) renderer.drawDebugLines(wireframeCubeEdges())

            cubeEntity?.let { entity ->
                world.get(entity, SpinControl::class)?.radians = spinRadians
                val hasMeshRenderer = world.get(entity, MeshRenderer::class) != null
                if (wireframe && hasMeshRenderer) {
                    world.remove(entity, MeshRenderer::class)
                } else if (!wireframe && !hasMeshRenderer) {
                    cubeMesh?.let { mesh -> material?.let { mat -> world.add(entity, MeshRenderer(mesh, mat)) } }
                }
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
                val ft = followTargetEntity?.let { world.get<Transform>(it)?.position } ?: cubeWorldPosition()
                markers += LineSegment(ft, ft + Vec3(0.1f, 0f, 0f), floatArrayOf(1f, 1f, 0f, 1f))
                markers += LineSegment(ft, ft + Vec3(0f, 0.1f, 0f), floatArrayOf(1f, 1f, 0f, 1f))
                markers += LineSegment(ft, ft + Vec3(0f, 0f, 0.1f), floatArrayOf(1f, 1f, 0f, 1f))
                renderer.drawDebugLines(markers)
            }
        }
    )

    private fun cubeWorldPosition(): Vec3 = Vec3(0f, CUBE_REST_HEIGHT, 0f)

    private fun wireframeCubeEdges(): List<LineSegment> {
        val s = sin(spinRadians).toFloat()
        val c = cos(spinRadians).toFloat()
        val worldCorners = rotatingCubeLocalCorners.map { (x, y, z) ->
            Vec3(x * c + z * s, y + CUBE_REST_HEIGHT, -x * s + z * c)
        }
        return rotatingCubeEdgeIndices.map { (startIndex, endIndex) ->
            LineSegment(worldCorners[startIndex], worldCorners[endIndex], floatArrayOf(1f, 1f, 1f, 1f))
        }
    }

    private const val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()
    private const val HOURS_TO_DEGREES = 360f / 24f
}
