// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Grid
import io.github.ronjunevaldoz.awake.core.math.OrbitCameraController
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.ManualTimeController
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.ensure
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.FollowControl
import io.github.ronjunevaldoz.awake.scene.components.Light
import io.github.ronjunevaldoz.awake.scene.components.LookAtControl
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.controls.PrimaryOrbitCamera
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnToggleGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A real cube spinning in place over a real reference ground grid, driven entirely through the
 * real ECS [io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime.world] -- one entity
 * carries the cube's [Transform]/[MeshRenderer], one carries its `Camera`, both spawned in
 * [Scene3DDemo.onActivate] and destroyed in [Scene3DDemo.onDeactivate] so switching playground
 * pages never leaves a stale cube behind (see [Scene3DDemos]'s doc comment). The ground grid is
 * still [Grid.lines] drawn as world-space debug lines via `renderer.drawDebugLines` -- unrelated
 * to the ECS, a flat reference grid has no fill to shade so it never needed a mesh/material.
 *
 * Camera controls are the shared [OrbitCameraController]/[renderOrbitCameraControls] panel every
 * demo with a viewport uses by default -- this demo's own addition on top is just the
 * [ManualTimeController]-driven cube spin ("Auto-spin"/"Time"), the one thing actually specific
 * to it.
 */
internal object RotatingCubeDemo {
    private val camera = OrbitCameraController(zoom = 15f, zoomMin = 2f, zoomMax = 15f)
    private val primaryCamera = PrimaryOrbitCamera(camera)

    private var wireframe = false
    private var spinRadians = 0f

    /** Which system drives [primaryCamera]'s camera entity every frame -- see the
     * [onUpdate] dispatch below. [Follow]/[LookAt] attach [FollowControl]/[LookAtControl] to
     * that SAME entity ([PrimaryOrbitCamera.entity]) instead of spawning a second camera, so
     * exactly one [io.github.ronjunevaldoz.awake.scene.components.Camera] is ever primary. */
    private enum class CameraMode { Orbit, Follow, LookAt }
    private var cameraMode = CameraMode.Orbit

    /** Plain data holder, not an ECS entity -- [FollowControl.target]/[LookAtControl.target]
     * only ever read `.position` (see [FollowCameraSystem][io.github.ronjunevaldoz.awake.scene
     * .systems.FollowCameraSystem]/[LookAtCameraSystem][io.github.ronjunevaldoz.awake.scene
     * .systems.LookAtCameraSystem]), same shape their own tests use. Kept in sync with
     * [cubeWorldPosition] every frame in [onUpdate]. */
    private val followTarget = Transform()

    /** Drives [spinRadians] -- see [ManualTimeController]'s own doc comment for the auto/manual
     * shape. Not spin-specific itself (a generic 0..24-hour clock any demo could reuse); this
     * demo is the one that maps `hours -> a full 360-degree turn` ([HOURS_TO_DEGREES]) below. */
    private val timeController = ManualTimeController()

    private var displayGroupExpanded = false

    private var cubeMesh: Mesh? = null
    private var material: Material? = null
    private var cubeEntity: Entity? = null
    private var lightEntity: Entity? = null

    private const val GRID_SIZE = 10f
    private const val GRID_DIVISIONS = 10

    // Half the unit cube's height (rotatingCubeGeometry spans -0.5..0.5 on every axis) --
    // rests the cube's bottom face exactly on the grid's y=0 plane instead of centering the
    // cube AT y=0, which sank it half beneath the floor. Also why 0.5f is the camera's own
    // target-center and elevation default: the camera's orbit target now genuinely matches
    // where the cube actually sits, not an arbitrary offset that happened to look plausible.
    private const val CUBE_REST_HEIGHT = 0.5f

    init {
        camera.elevation = 2.2f
    }

    val entry = Scene3DDemo(
        id = "rotating-cube",
        title = "Rotating cube",
        renderViewport = {
            // Real geometry is drawn by RenderSystem via this demo's own ECS entities (see
            // onActivate/onUpdate below) -- this viewport column only needs to exist so the
            // shell lays out a center pane for it; the demo has nothing UI-authored to put here.
        },
        renderControls = {
            renderOrbitCameraControls(camera, idPrefix = "cube", targetLabel = "cube")
            shadcnToggleGroup(
                id = "cube-camera-mode",
                options = listOf("Orbit", "Follow", "Look at"),
                selectedIndex = cameraMode.ordinal,
                onIndexChange = { cameraMode = CameraMode.entries[it] }
            )
            shadcnCollapsibleCard(
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
            world.add(cube, Transform())
            world.add(cube, SpinControl().apply { offset = cubeWorldPosition(); radians = spinRadians })
            if (!wireframe) world.add(cube, MeshRenderer(cubeMesh!!, material!!))
            cubeEntity = cube
            primaryCamera.spawn(world, cubeWorldPosition())
            // Light() with all defaults exactly matches DEFAULT_SCENE_LIGHT (RenderSystem's
            // fallback for a Light-less scene) -- so this demo's pixel baselines don't change
            // while still exercising the real Light-entity lookup path, not just the fallback.
            lightEntity = world.create().also { world.add(it, Light()) }
        },
        onDeactivate = { world ->
            cubeEntity?.let { world.destroy(it) }
            cubeEntity = null
            lightEntity?.let { world.destroy(it) }
            lightEntity = null
            primaryCamera.destroy(world)
        },
        onUpdate = { delta ->
            timeController.advance(delta)
            spinRadians = timeController.hours * HOURS_TO_DEGREES * DEGREES_TO_RADIANS

            val gridLines = Grid.lines(size = GRID_SIZE, divisions = GRID_DIVISIONS)
                .map { (a, b) -> LineSegment(a, b, GRID_COLOR) }
            val lines = axisLines() + gridLines + if (wireframe) wireframeCubeEdges() else emptyList()
            renderer.drawDebugLines(lines)

            cubeEntity?.let { entity ->
                world.get(entity, SpinControl::class)?.radians = spinRadians
                val hasMeshRenderer = world.get(entity, MeshRenderer::class) != null
                if (wireframe && hasMeshRenderer) {
                    world.remove(entity, MeshRenderer::class)
                } else if (!wireframe && !hasMeshRenderer) {
                    cubeMesh?.let { mesh -> material?.let { mat -> world.add(entity, MeshRenderer(mesh, mat)) } }
                }
            }
            updateCamera(world)
        }
    )

    /** Dispatches [primaryCamera]'s single camera entity to whichever mode [cameraMode]
     * currently selects -- [Orbit][CameraMode.Orbit] (the demo's original behavior) fully
     * re-projects the entity's `Camera` every frame via [primaryCamera], so any leftover
     * [FollowControl]/[LookAtControl] from a previous mode must come off first or the
     * frame-registered `FollowCameraSystem`/`LookAtCameraSystem` would keep fighting it.
     * [Follow][CameraMode.Follow]/[LookAt][CameraMode.LookAt] instead attach their control
     * component and let those systems drive the entity directly -- [primaryCamera.refresh]
     * is skipped so it doesn't overwrite what they just wrote. */
    private fun updateCamera(world: World) {
        followTarget.position = cubeWorldPosition()
        val cameraEntity = primaryCamera.entity ?: return
        when (cameraMode) {
            CameraMode.Orbit -> {
                world.remove<FollowControl>(cameraEntity)
                world.remove<LookAtControl>(cameraEntity)
                primaryCamera.refresh(world, cubeWorldPosition())
            }
            CameraMode.Follow -> {
                world.remove<LookAtControl>(cameraEntity)
                world.ensure(cameraEntity, ::FollowControl).target = followTarget
            }
            CameraMode.LookAt -> {
                world.remove<FollowControl>(cameraEntity)
                world.ensure(cameraEntity, ::LookAtControl).target = followTarget
            }
        }
    }

    /** The cube never translates (only [wireframeCubeEdges] rotates it in place), so its world
     * position is always this fixed point -- the same one [SpinControl.offset] uses. */
    private fun cubeWorldPosition(): Vec3 = Vec3(0f, CUBE_REST_HEIGHT, 0f)

    // Standard red/green/blue X/Y/Z axis convention (Blender/Unity/three.js all use this),
    // drawn from the grid's own origin so orientation is legible at a glance. Plain colored
    // lines, not full 3D arrowheads -- an arrowhead needs real cone/triangle mesh geometry per
    // axis, which is real modeling work this reference gizmo doesn't need to justify yet.
    private fun axisLines(): List<LineSegment> = listOf(
        LineSegment(Vec3(0f, 0.01f, 0f), Vec3(AXIS_LENGTH, 0.01f, 0f), AXIS_COLOR_X),
        LineSegment(Vec3(0f, 0.01f, 0f), Vec3(0f, AXIS_LENGTH, 0f), AXIS_COLOR_Y),
        LineSegment(Vec3(0f, 0.01f, 0f), Vec3(0f, 0.01f, AXIS_LENGTH), AXIS_COLOR_Z)
    )

    private fun wireframeCubeEdges(): List<LineSegment> {
        val sin = sin(spinRadians)
        val cos = cos(spinRadians)
        val worldCorners = rotatingCubeLocalCorners.map { (x, y, z) ->
            // Same rotateY + CUBE_REST_HEIGHT convention as cubeModelMatrix -- both rotate the
            // unit cube in place then lift it to rest on the grid.
            Vec3(x * cos + z * sin, y + CUBE_REST_HEIGHT, -x * sin + z * cos)
        }
        return rotatingCubeEdgeIndices.map { (startIndex, endIndex) ->
            LineSegment(worldCorners[startIndex], worldCorners[endIndex], WIREFRAME_COLOR)
        }
    }

    private val WIREFRAME_COLOR = floatArrayOf(1f, 1f, 1f, 1f)
    private val GRID_COLOR = floatArrayOf(0.55f, 0.55f, 0.6f, 1f)
    private val AXIS_COLOR_X = floatArrayOf(0.9f, 0.15f, 0.15f, 1f)
    private val AXIS_COLOR_Y = floatArrayOf(0.15f, 0.75f, 0.15f, 1f)
    private val AXIS_COLOR_Z = floatArrayOf(0.15f, 0.35f, 0.9f, 1f)
    private const val AXIS_LENGTH = 2f

    private const val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()

    // 24 "hours" = one full 360-degree turn -- see [timeController]'s own doc comment.
    private const val HOURS_TO_DEGREES = 360f / 24f
}
