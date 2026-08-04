// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.core.math.Grid
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.Camera as SceneCamera
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A real cube spinning in place over a real reference ground grid, driven entirely through the
 * real ECS [io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime.world] -- one entity
 * carries the cube's [Transform]/[MeshRenderer], one carries its [SceneCamera], both spawned in
 * [Scene3DDemo.onActivate] and destroyed in [Scene3DDemo.onDeactivate] so switching playground
 * pages never leaves a stale cube behind (see [Scene3DDemos]'s doc comment). The ground grid is
 * still [Grid.lines] drawn as world-space debug lines via `renderer.drawDebugLines` -- unrelated
 * to the ECS, a flat reference grid has no fill to shade so it never needed a mesh/material.
 *
 * Every camera parameter is a live-draggable slider -- see [computeCamera] -- rather than
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
 * advanced every frame by [Scene3DDemo.onUpdate] -- independent of any slider, so the demo is
 * visibly "rotating" the moment it's selected, not only once a slider is dragged.
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
     * around a fixed anchor point ([orbitAnchor], positioned by [panX]/[panZ]/[elevation]);
     * the eye's position depends only on that anchor and these three params, never on
     * [lockTargetToCube] -- toggling what the camera looks at must never move the eye itself
     * (a real reported bug from an earlier version of this toggle, where the look-at target
     * *was* the orbit anchor, so retargeting it silently dragged the eye along with it).
     *
     * `true`: free-look mode -- the eye itself is fixed at [panX]/[panZ]/[elevation] instead
     * of orbiting an anchor, and [orbitDegrees]/[pitchDegrees] instead rotate which direction
     * that fixed eye looks (an FPS-style look-around). Named "Free look", not "Look At Target"
     * -- despite `Camera` always having a `center` field the view matrix looks at (every mode
     * is technically "look-at"), this mode has no fixed point being looked AT; the eye is what's
     * fixed, and the look direction rotates freely around it. [zoom] has no meaning here (no
     * orbit radius to control) and [lockTargetToCube] has no meaning either (there is no
     * look-at target in this mode) -- both are disabled in the controls panel while this is on. */
    private var freeLook = false

    /** `true` (default): orbit mode's look-at `center` ([computeCamera]) is the cube's own
     * fixed world position; `false`: the look-at `center` is [orbitAnchor] instead (the same
     * point the eye orbits around, so the eye stays centered in view without needing a separate
     * target concept). Either way the eye's own position ([orbitAnchor] + the orbit offset) is
     * unaffected by this toggle -- only what the already-positioned camera is aimed at changes,
     * not where it is. Meaningless in [freeLook] mode (see its own doc comment) -- disabled in
     * the controls panel while that's on. */
    private var lockTargetToCube = true
    private var wireframe = false
    private var spinRadians = 0f

    /** Drives [spinRadians] -- see [ManualTimeController]'s own doc comment for the auto/manual
     * shape. Not spin-specific itself (a generic 0..24-hour clock any demo could reuse); this
     * demo is the one that maps `hours -> a full 360-degree turn` ([HOURS_TO_DEGREES]) below. */
    private val timeController = ManualTimeController()

    // Controls panel grouping -- default expanded so nothing looks like it went missing.
    private var cameraGroupExpanded = true
    private var projectionGroupExpanded = true
    private var displayGroupExpanded = true

    private var cubeMesh: Mesh? = null
    private var material: Material? = null
    private var cubeEntity: Entity? = null
    private var cameraEntity: Entity? = null

    private const val GRID_SIZE = 10f
    private const val GRID_DIVISIONS = 10

    // Half the unit cube's height (rotatingCubeGeometry spans -0.5..0.5 on every axis) --
    // rests the cube's bottom face exactly on the grid's y=0 plane instead of centering the
    // cube AT y=0, which sank it half beneath the floor. Also why 0.5f is the camera's own
    // target-center and elevation default: the camera's orbit target now genuinely matches
    // where the cube actually sits, not an arbitrary offset that happened to look plausible.
    private const val CUBE_REST_HEIGHT = 0.5f

    val entry = Scene3DDemo(
        id = "rotating-cube",
        title = "Rotating cube",
        renderViewport = {
            // Real geometry is drawn by RenderSystem via this demo's own ECS entities (see
            // onActivate/onUpdate below) -- this viewport column only needs to exist so the
            // shell lays out a center pane for it; the demo has nothing UI-authored to put here.
        },
        renderControls = {
            // Reassigning from shadcnCollapsible's own return value here (`cameraGroupExpanded =
            // shadcnCollapsible(...)`) is a real bug, not just style: the primary shadcnCollapsible
            // always `return`s the pre-click `expanded` it was called with, so that reassignment
            // executes *after* `onExpandedChange` already flipped the var this frame and silently
            // clobbers it back to the stale value -- collapsing a card never visibly toggled.
            // shadcnCollapsible's own sidebar-category caller (UiShowcaseChrome.kt) never
            // reassigns from the return value for exactly this reason; match that here.
            shadcnCollapsible(
                id = "cube-controls-camera",
                title = "Camera",
                expanded = cameraGroupExpanded,
                onExpandedChange = { cameraGroupExpanded = it },
                bordered = true
            ) {
                orbitDegrees = shadcnFieldSliderWithValue(id = "cube-orbit", label = "Orbit", min = 0f, max = 360f, value = orbitDegrees)
                pitchDegrees = shadcnFieldSliderWithValue(id = "cube-pitch", label = "Pitch", min = 0f, max = 89f, value = pitchDegrees)
                rollDegrees = shadcnFieldSliderWithValue(id = "cube-roll", label = "Roll", min = -180f, max = 180f, value = rollDegrees)
                freeLook = shadcnSwitch(id = "cube-free-look", checked = freeLook, label = "Free look")
                zoom = shadcnFieldSliderWithValue(id = "cube-zoom", label = "Zoom", min = 2f, max = 15f, value = zoom, enabled = !freeLook)
                lockTargetToCube = shadcnSwitch(id = "cube-lock-target", checked = lockTargetToCube, label = "Look at cube", enabled = !freeLook)
                // Meaningless combined with lockTargetToCube only in orbit mode (the target is
                // forced to the cube, these fields have nothing to affect); in freeLook they're
                // always live since they're the *eye* position there, not a look-at target.
                val targetFieldsEnabled = freeLook || !lockTargetToCube
                panX = shadcnFieldSliderWithValue(id = "cube-pan-x", label = "Pan X", min = -5f, max = 5f, value = panX, enabled = targetFieldsEnabled)
                panZ = shadcnFieldSliderWithValue(id = "cube-pan-z", label = "Pan Z", min = -5f, max = 5f, value = panZ, enabled = targetFieldsEnabled)
                elevation = shadcnFieldSliderWithValue(id = "cube-elevation", label = "Elevation", min = -3f, max = 5f, value = elevation, enabled = targetFieldsEnabled)
            }
            shadcnCollapsible(
                id = "cube-controls-projection",
                title = "Projection",
                expanded = projectionGroupExpanded,
                onExpandedChange = { projectionGroupExpanded = it },
                bordered = true
            ) {
                near = shadcnFieldSliderWithValue(id = "cube-near", label = "Near", min = 0.01f, max = 5f, value = near)
                far = shadcnFieldSliderWithValue(id = "cube-far", label = "Far", min = 10f, max = 500f, value = far)
                fovDegrees = shadcnFieldSliderWithValue(id = "cube-fov", label = "FOV", min = 10f, max = 120f, value = fovDegrees)
            }
            shadcnCollapsible(
                id = "cube-controls-display",
                title = "Display",
                expanded = displayGroupExpanded,
                onExpandedChange = { displayGroupExpanded = it },
                bordered = true
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
            world.add(cube, Transform(worldMatrix = cubeModelMatrix()))
            if (!wireframe) world.add(cube, MeshRenderer(cubeMesh!!, material!!))
            cubeEntity = cube
            val camera = world.create()
            world.add(camera, SceneCamera(computeCamera(), isPrimary = true))
            cameraEntity = camera
        },
        onDeactivate = { world ->
            cubeEntity?.let { world.destroy(it) }
            cameraEntity?.let { world.destroy(it) }
            cubeEntity = null
            cameraEntity = null
        },
        onUpdate = { delta ->
            timeController.advance(delta)
            spinRadians = timeController.hours * HOURS_TO_DEGREES * DEGREES_TO_RADIANS

            val gridLines = Grid.lines(size = GRID_SIZE, divisions = GRID_DIVISIONS)
                .map { (a, b) -> LineSegment(a, b, GRID_COLOR) }
            val lines = axisLines() + gridLines + if (wireframe) wireframeCubeEdges() else emptyList()
            renderer.drawDebugLines(lines)

            cubeEntity?.let { entity ->
                world.get(entity, Transform::class)?.worldMatrix = cubeModelMatrix()
                val hasMeshRenderer = world.get(entity, MeshRenderer::class) != null
                if (wireframe && hasMeshRenderer) {
                    world.remove(entity, MeshRenderer::class)
                } else if (!wireframe && !hasMeshRenderer) {
                    cubeMesh?.let { mesh -> material?.let { mat -> world.add(entity, MeshRenderer(mesh, mat)) } }
                }
            }
            cameraEntity?.let { entity -> world.add(entity, SceneCamera(computeCamera(), isPrimary = true)) }
        }
    )

    private fun cubeModelMatrix(): Mat4 = Mat4().translate(0f, CUBE_REST_HEIGHT, 0f).rotateY(spinRadians)

    /** Orbit mode's eye *anchor* -- [orbitDegrees]/[pitchDegrees]/[zoom] always orbit the eye
     * around this exact point, regardless of [lockTargetToCube]. Keeping the anchor fixed to
     * [panX]/[panZ]/[elevation] (never swapped for the cube's position) is what keeps the eye
     * itself from jumping when [lockTargetToCube] is toggled -- only [computeCamera]'s
     * separate look-at `center` changes, re-aiming the same eye position instead of moving it. */
    private fun orbitAnchor(): Vec3 = Vec3(panX, elevation, panZ)

    /** The cube never translates (only [wireframeCubeEdges]/[cubeModelMatrix] rotates it in
     * place), so its world position is always this fixed point -- the same one the cube's own
     * rest-height translate uses. */
    private fun cubeWorldPosition(): Vec3 = Vec3(0f, CUBE_REST_HEIGHT, 0f)

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

    /** Builds this frame's [CoreCamera] in whichever mode [freeLook] selects (see its own doc
     * comment for the two modes) -- [rollDegrees] tilts the camera's up vector around its own
     * view axis (the eye-to-center line) rather than the world's in either mode; [near]/[far]/
     * [fovDegrees] are the projection's clip planes and vertical field of view. */
    private fun computeCamera(): CoreCamera {
        val eye: Vec3
        val center: Vec3
        if (freeLook) {
            eye = Vec3(panX, elevation, panZ)
            center = eye + freeLookDirection() * FREE_LOOK_DISTANCE
        } else {
            val orbitRad = orbitDegrees * DEGREES_TO_RADIANS
            val pitchRad = pitchDegrees * DEGREES_TO_RADIANS
            val horizontalRadius = zoom * cos(pitchRad)
            val anchor = orbitAnchor()
            eye = anchor + Vec3(
                horizontalRadius * sin(orbitRad),
                zoom * sin(pitchRad),
                horizontalRadius * cos(orbitRad)
            )
            center = if (lockTargetToCube) cubeWorldPosition() else anchor
        }
        return CoreCamera(
            eye = eye,
            center = center,
            up = rolledUpVector(eye, center),
            fovYRadians = fovDegrees * DEGREES_TO_RADIANS,
            near = near,
            far = far
        )
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

    // Arbitrary nonzero distance from eye to Camera's `center` field in free-look mode --
    // Camera's view matrix only needs eye/center/up to determine direction, this magnitude
    // has no visual effect (unlike orbit mode's zoom, which IS the eye-to-target distance).
    private const val FREE_LOOK_DISTANCE = 10f
    private const val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()

    // 24 "hours" = one full 360-degree turn -- see [timeController]'s own doc comment.
    private const val HOURS_TO_DEGREES = 360f / 24f
}
