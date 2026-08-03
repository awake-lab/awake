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
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
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
 * Every camera parameter is a live-draggable slider -- see [cameraAndDrawCalls] -- rather than
 * a hand-picked eye-position ratio someone has to edit-compile-run-screenshot to retune. Two
 * distinct camera modes, both driven by the same sliders with different meanings (see
 * [freeLook]'s own doc comment for why "Free look" -- not "Look At Target" -- is the accurate
 * name for the second mode): by default, "Orbit"/"Pitch"/"Zoom" orbit/tilt/dolly the eye around
 * a look-at target positioned by "Pan X"/"Pan Z"/"Elevation". With "Free look" on, the eye
 * itself is fixed at "Pan X"/"Pan Z"/"Elevation" instead, and "Orbit"/"Pitch" rotate which
 * direction that fixed eye is looking (an FPS-style look-around instead of an orbit) --
 * "Zoom" has no meaning in this mode and is disabled. "Near"/"Far"/"FOV" are the projection's
 * clip planes and vertical field of view in both modes; "Roll" tilts the camera's up vector
 * around its own view axis. The cube's own spin is a separate, continuous auto-rotation
 * advanced every frame by [update] -- independent of any slider, so the demo is visibly
 * "rotating" the moment it's selected, not only once a slider is dragged.
 */
internal object RotatingCubeDemo {
    private var orbitDegrees = 0f
    private var pitchDegrees = 0f
    private var zoom = 15f
    private var near = 0.1f
    private var far = 100f
    private var fovDegrees = 45f
    private var rollDegrees = 0f
    private var panX = 0f
    private var panZ = 0f
    private var elevation = 2.2f

    /** `false` (default): orbit mode -- [orbitDegrees]/[pitchDegrees]/[zoom] orbit the eye
     * around a look-at target positioned by [panX]/[panZ]/[elevation]; the target stays fixed
     * regardless of where the eye orbits to.
     *
     * `true`: free-look mode -- the eye itself is fixed at [panX]/[panZ]/[elevation] instead
     * of a look-at target, and [orbitDegrees]/[pitchDegrees] instead rotate which direction
     * that fixed eye looks (an FPS-style look-around). Named "Free look", not "Look At Target"
     * -- despite `Camera` always having a `center` field the view matrix looks at (every mode
     * is technically "look-at"), this mode has no fixed point being looked AT; the eye is what's
     * fixed, and the look direction rotates freely around it. [zoom] has no meaning here (no
     * orbit radius to control) and is disabled in the controls panel while this is on. */
    private var freeLook = false
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
            orbitDegrees = shadcnFieldSliderWithValue(id = "cube-orbit", label = "Orbit", min = 0f, max = 360f, value = orbitDegrees)
            pitchDegrees = shadcnFieldSliderWithValue(id = "cube-pitch", label = "Pitch", min = 0f, max = 89f, value = pitchDegrees)
            rollDegrees = shadcnFieldSliderWithValue(id = "cube-roll", label = "Roll", min = -180f, max = 180f, value = rollDegrees)
            near = shadcnFieldSliderWithValue(id = "cube-near", label = "Near", min = 0.01f, max = 5f, value = near)
            far = shadcnFieldSliderWithValue(id = "cube-far", label = "Far", min = 10f, max = 500f, value = far)
            fovDegrees = shadcnFieldSliderWithValue(id = "cube-fov", label = "FOV", min = 10f, max = 120f, value = fovDegrees)
            freeLook = shadcnSwitch(id = "cube-free-look", checked = freeLook, label = "Free look")
            zoom = shadcnFieldSliderWithValue(id = "cube-zoom", label = "Zoom", min = 2f, max = 15f, value = zoom, enabled = !freeLook)
            panX = shadcnFieldSliderWithValue(id = "cube-pan-x", label = "Pan X", min = -5f, max = 5f, value = panX)
            panZ = shadcnFieldSliderWithValue(id = "cube-pan-z", label = "Pan Z", min = -5f, max = 5f, value = panZ)
            elevation = shadcnFieldSliderWithValue(id = "cube-elevation", label = "Elevation", min = -3f, max = 5f, value = elevation)
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
        val lines = axisLines() + gridLines + if (wireframe) wireframeCubeEdges() else emptyList()
        renderer.drawDebugLines(lines)
    }

    /** Orbit mode's look-at target -- see [freeLook]'s doc comment. */
    private fun targetCenter(): Vec3 = Vec3(panX, elevation, panZ)

    /** Unit look direction for free-look mode -- [orbitDegrees] is yaw (compass heading),
     * [pitchDegrees] is elevation angle above the horizon, matching the same slider ranges
     * orbit mode uses so switching modes doesn't require re-tuning the sliders to sane values. */
    private fun freeLookDirection(): Vec3 {
        val orbitRad = orbitDegrees * DEGREES_TO_RADIANS
        val pitchRad = pitchDegrees * DEGREES_TO_RADIANS
        return Vec3(
            cos(pitchRad) * sin(orbitRad),
            sin(pitchRad),
            cos(pitchRad) * cos(orbitRad)
        )
    }

    /** Builds this frame's [Camera] in whichever mode [freeLook] selects (see its own doc
     * comment for the two modes) -- [rollDegrees] tilts the camera's up vector around its own
     * view axis (the eye-to-center line) rather than the world's in either mode; [near]/[far]/
     * [fovDegrees] are the projection's clip planes and vertical field of view. The solid cube
     * draw call is included only when [wireframe] is off (its edges are drawn as debug lines
     * instead, see [update]); the ground grid has no solid draw call at all, see [update]. */
    fun cameraAndDrawCalls(): Pair<Camera, List<DrawCall>> {
        val eye: Vec3
        val center: Vec3
        if (freeLook) {
            eye = Vec3(panX, elevation, panZ)
            center = eye + freeLookDirection() * FREE_LOOK_DISTANCE
        } else {
            val orbitRad = orbitDegrees * DEGREES_TO_RADIANS
            val pitchRad = pitchDegrees * DEGREES_TO_RADIANS
            val horizontalRadius = zoom * cos(pitchRad)
            center = targetCenter()
            eye = center + Vec3(
                horizontalRadius * sin(orbitRad),
                zoom * sin(pitchRad),
                horizontalRadius * cos(orbitRad)
            )
        }
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
    private val AXIS_COLOR_X = floatArrayOf(0.9f, 0.15f, 0.15f, 1f)
    private val AXIS_COLOR_Y = floatArrayOf(0.15f, 0.75f, 0.15f, 1f)
    private val AXIS_COLOR_Z = floatArrayOf(0.15f, 0.35f, 0.9f, 1f)
    private const val AXIS_LENGTH = 2f

    // Arbitrary nonzero distance from eye to Camera's `center` field in free-look mode --
    // Camera's view matrix only needs eye/center/up to determine direction, this magnitude
    // has no visual effect (unlike orbit mode's zoom, which IS the eye-to-target distance).
    private const val FREE_LOOK_DISTANCE = 10f
    private const val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()
}
