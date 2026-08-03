// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Grid
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A real cube spinning in place over a real reference ground grid. The cube's mesh comes from
 * [rotatingCubeGeometry] (copied out of the now-retired hello-cube sample, see that file's doc
 * comment); the ground grid is [Grid.lines] -- an existing, already-unit-tested
 * `awake-base` helper for exactly this ("a square reference/floor grid", see its own doc
 * comment) -- drawn as world-space debug lines rather than a second mesh+material, since a
 * flat reference grid has no fill to shade. Both are driven onto the screen via
 * [io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime.provideDrawCalls]/
 * `drawDebugLines`, the hooks that let a [io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime]
 * (a UI-only game runtime, with no 3D scene/camera of its own) still submit real geometry to
 * the one [Renderer.draw] call that drives the swapchain frame every frame.
 *
 * Every camera parameter is a live-draggable slider in real spherical coordinates -- see
 * [cameraAndDrawCalls] -- rather than a hand-picked eye-position ratio someone has to
 * edit-compile-run-screenshot to retune: "Orbit"/"Pitch"/"Zoom" orbit/tilt/dolly around
 * [targetCenter]; "Pan X"/"Pan Z"/"Elevation" move that orbit target itself; "Near"/"Far"/"FOV"
 * are the projection's clip planes and vertical field of view; "Roll" tilts the camera's up
 * vector around its own view axis. "Lock Target" freezes [targetCenter] at the origin default
 * (ignoring Pan X/Z/Elevation) so the other sliders can be tuned without the target drifting.
 * The cube's own spin is a separate, continuous auto-rotation advanced every frame by [update]
 * -- independent of any slider, so the demo is visibly "rotating" the moment it's selected, not
 * only once a slider is dragged.
 */
internal object RotatingCubeDemo {
    private var orbitDegrees = 35f
    private var pitchDegrees = 70f
    private var zoom = 6f
    private var near = 0.1f
    private var far = 100f
    private var fovDegrees = 45f
    private var rollDegrees = 0f
    private var panX = 0f
    private var panZ = 0f
    private var elevation = 0.5f
    private var lockTarget = false
    private var wireframe = false
    private var spinRadians = 0f

    private var cubeMesh: Mesh? = null
    private var material: Material? = null

    private const val SPIN_RADIANS_PER_SECOND = 0.8f
    private const val GRID_SIZE = 10f
    private const val GRID_DIVISIONS = 10

    // Half the unit cube's height (rotatingCubeGeometry spans -0.5..0.5 on every axis) --
    // rests the cube's bottom face exactly on the grid's y=0 plane instead of centering the
    // cube AT y=0, which sank it half beneath the floor. Also why 0.5f is targetCenter()'s
    // and elevation's own default: the camera's orbit target now genuinely matches where the
    // cube actually sits, not an arbitrary offset that happened to look plausible.
    private const val CUBE_REST_HEIGHT = 0.5f

    val entry = Scene3DDemo(
        id = "rotating-cube",
        title = "Rotating cube",
        renderViewport = {
            // Real geometry is drawn by the 3D pass Scene3DPlaygroundFeature wires up (see
            // its own doc comment) -- this viewport column only needs to exist so the shell
            // lays out a center pane for it; the demo has nothing UI-authored to put here.
        },
        renderControls = {
            orbitDegrees = shadcnSlider(id = "cube-orbit", min = 0f, max = 360f, value = orbitDegrees, label = "Orbit")
            pitchDegrees = shadcnSlider(id = "cube-pitch", min = 0f, max = 89f, value = pitchDegrees, label = "Pitch")
            zoom = shadcnSlider(id = "cube-zoom", min = 2f, max = 15f, value = zoom, label = "Zoom")
            rollDegrees = shadcnSlider(id = "cube-roll", min = -180f, max = 180f, value = rollDegrees, label = "Roll")
            near = shadcnSlider(id = "cube-near", min = 0.01f, max = 5f, value = near, label = "Near")
            far = shadcnSlider(id = "cube-far", min = 10f, max = 500f, value = far, label = "Far")
            fovDegrees = shadcnSlider(id = "cube-fov", min = 10f, max = 120f, value = fovDegrees, label = "FOV")
            lockTarget = shadcnSwitch(id = "cube-lock-target", checked = lockTarget, label = "Lock target")
            panX = shadcnSlider(id = "cube-pan-x", min = -5f, max = 5f, value = panX, label = "Pan X", enabled = !lockTarget)
            panZ = shadcnSlider(id = "cube-pan-z", min = -5f, max = 5f, value = panZ, label = "Pan Z", enabled = !lockTarget)
            elevation = shadcnSlider(id = "cube-elevation", min = -3f, max = 5f, value = elevation, label = "Elevation", enabled = !lockTarget)
            wireframe = shadcnSwitch(id = "cube-wireframe", checked = wireframe, label = "Wireframe")
            text(label = "Cube spins automatically; sliders move the camera (Pitch 0 = side-on, 89 = top-down).")
        }
    )

    fun isActive(activeDemoId: String) = activeDemoId == entry.id

    /** Builds this demo's mesh/material the first time it's needed (a UI-only game has no
     * upfront asset list to build these from, see [Renderer.createMesh]'s own doc comment),
     * advances the cube's own spin by [deltaSeconds], and stages this frame's debug lines --
     * the ground grid always, plus (while [wireframe] is on) the cube's own edges instead of
     * its solid mesh. Call once per frame, only while this demo is active; the solid draw
     * calls come from [cameraAndDrawCalls] afterwards. */
    fun update(renderer: Renderer, deltaSeconds: Float) {
        if (cubeMesh == null) cubeMesh = renderer.createMesh(rotatingCubeGeometry)
        if (material == null) material = renderer.createMaterial()

        spinRadians += deltaSeconds * SPIN_RADIANS_PER_SECOND

        val gridLines = Grid.lines(size = GRID_SIZE, divisions = GRID_DIVISIONS)
            .map { (a, b) -> LineSegment(a, b, GRID_COLOR) }
        val lines = if (wireframe) gridLines + wireframeCubeEdges() else gridLines
        renderer.drawDebugLines(lines)
    }

    /** The orbit target -- what [orbitDegrees]/[pitchDegrees]/[zoom] orbit around. Frozen at
     * the origin default while [lockTarget] is on, ignoring [panX]/[panZ]/[elevation] so the
     * other sliders can be tuned without the target drifting underneath them. */
    private fun targetCenter(): Vec3 =
        if (lockTarget) Vec3(0f, 0.5f, 0f) else Vec3(panX, elevation, panZ)

    /** Camera orbiting [targetCenter] in real spherical coordinates -- [orbitDegrees] is yaw
     * (compass direction around the target), [pitchDegrees] is the angle up from the horizon
     * (0 = eye-level side-on, 90 = straight down), [zoom] is the distance from the target.
     * Live-draggable, unlike a hand-picked eye-position ratio: dragging Pitch to 89 gives a
     * true top-down view, dragging it to 0 gives a side-on view, with everything in between
     * actually reachable. [rollDegrees] tilts the camera's up vector around its own view axis
     * (the eye-to-target line) rather than the world's; [near]/[far]/[fovDegrees] are the
     * projection's clip planes and vertical field of view. The solid cube draw call is included
     * only when [wireframe] is off (its edges are drawn as debug lines instead, see [update]);
     * the ground grid has no solid draw call at all, see [update]. */
    fun cameraAndDrawCalls(): Pair<Camera, List<DrawCall>> {
        val orbitRad = orbitDegrees * DEGREES_TO_RADIANS
        val pitchRad = pitchDegrees * DEGREES_TO_RADIANS
        val horizontalRadius = zoom * cos(pitchRad)
        val center = targetCenter()
        val eye = center + Vec3(
            horizontalRadius * sin(orbitRad),
            zoom * sin(pitchRad),
            horizontalRadius * cos(orbitRad)
        )
        val camera = Camera(
            eye = eye,
            center = center,
            up = rolledUpVector(eye, center),
            fovYRadians = fovDegrees * DEGREES_TO_RADIANS,
            near = near,
            far = far
        )
        val cube = if (!wireframe) {
            cubeMesh?.let { mesh ->
                material?.let { mat ->
                    DrawCall(mesh, mat, Mat4().translate(0f, CUBE_REST_HEIGHT, 0f).rotateY(spinRadians))
                }
            }
        } else {
            null
        }
        return camera to listOfNotNull(cube)
    }

    /** World-up `(0, 1, 0)` rotated by [rollDegrees] around the eye-to-[center] view axis --
     * standard camera-roll construction (right = forward x worldUp, trueUp = right x forward,
     * rolledUp = trueUp*cos(roll) + right*sin(roll)). Degenerates gracefully to whatever
     * `Camera`'s own view-matrix construction does with a near-parallel up/forward pair at the
     * extreme top-down Pitch (89) -- an existing, pre-this-feature limitation of a look-at
     * camera in general, not something this roll addition introduces. */
    private fun rolledUpVector(eye: Vec3, center: Vec3): Vec3 {
        val forward = (center - eye).normalize()
        val worldUp = Vec3(0f, 1f, 0f)
        val right = forward.cross(worldUp).normalize()
        val trueUp = right.cross(forward).normalize()
        val rollRad = rollDegrees * DEGREES_TO_RADIANS
        return (trueUp * cos(rollRad)) + (right * sin(rollRad))
    }

    private fun wireframeCubeEdges(): List<LineSegment> {
        val sin = sin(spinRadians)
        val cos = cos(spinRadians)
        val worldCorners = rotatingCubeLocalCorners.map { (x, y, z) ->
            // Same rotateY + CUBE_REST_HEIGHT convention as cameraAndDrawCalls' cube model
            // matrix -- both rotate the unit cube in place then lift it to rest on the grid.
            Vec3(x * cos + z * sin, y + CUBE_REST_HEIGHT, -x * sin + z * cos)
        }
        return rotatingCubeEdgeIndices.map { (startIndex, endIndex) ->
            LineSegment(worldCorners[startIndex], worldCorners[endIndex], WIREFRAME_COLOR)
        }
    }

    private val WIREFRAME_COLOR = floatArrayOf(1f, 1f, 1f, 1f)
    private val GRID_COLOR = floatArrayOf(0.55f, 0.55f, 0.6f, 1f)
    private const val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()
}
