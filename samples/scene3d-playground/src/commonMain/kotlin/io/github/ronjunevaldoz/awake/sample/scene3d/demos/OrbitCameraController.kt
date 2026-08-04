// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Reusable default camera control -- every demo with a 3D viewport gets this same Camera/
 * Projection panel via [renderOrbitCameraControls] instead of hand-rolling its own (previously
 * [RotatingCubeDemo] had the full set and `GltfViewerDemo` had a smaller ad-hoc one; a demo now
 * only adds controls for what's actually specific to it -- e.g. glTF viewer's own "Auto-rotate"
 * switch -- on top of this shared panel, not a second camera implementation).
 *
 * Two modes, both driven by the same fields (see [freeLook]'s own doc comment for why "Free
 * look" -- not "Look At Target" -- is the accurate name for the second mode): by default,
 * [orbitDegrees]/[pitchDegrees]/[zoom] orbit/tilt/dolly the eye around a look-at target
 * positioned by [panX]/[panZ]/[elevation]. With [freeLook] on, the eye itself is fixed at
 * [panX]/[panZ]/[elevation] instead, and [orbitDegrees]/[pitchDegrees] rotate which direction
 * that fixed eye is looking (an FPS-style look-around) -- [zoom] has no meaning in this mode.
 * [near]/[far]/[fovDegrees] are the projection's clip planes and vertical field of view in both
 * modes; [rollDegrees] tilts the camera's up vector around its own view axis.
 */
internal class OrbitCameraController(
    zoom: Float = 15f,
    var zoomMin: Float = 2f,
    var zoomMax: Float = 15f,
    var panRange: Float = 5f,
    elevation: Float = 2.2f
) {
    var orbitDegrees = 0f
    var pitchDegrees = 0f
    var zoom = zoom
    var near = 0.1f

    /** Suits a roughly-unit-scale scene by default -- a demo whose [zoomMax] goes well past
     * this (e.g. an auto-fit zoom sized to a large glTF model's real units) must scale this up
     * itself, or the eye ends up farther from its target than [far] allows and the whole scene
     * clips out silently (a real reported bug: fully blank viewport, no error -- see
     * `GltfViewerDemo.preload()`'s own `camera.far = camera.zoomMax * 2f`). */
    var far = 100f
    var fovDegrees = 45f
    var rollDegrees = 0f
    var panX = 0f
    var panZ = 0f
    var elevation = elevation
    var freeLook = false

    /** `true` (default): [computeCamera]'s look-at `center` is the caller-supplied target
     * position; `false`: the look-at `center` is [orbitAnchor] instead (the same point the eye
     * orbits around, so the eye stays centered in view without needing a separate target
     * concept). Either way the eye's own position ([orbitAnchor] + the orbit offset) is
     * unaffected by this toggle -- only what the already-positioned camera is aimed at changes,
     * not where it is (a real reported bug from an earlier version of this toggle, where the
     * look-at target *was* the orbit anchor, so retargeting it silently dragged the eye along
     * with it). Meaningless in [freeLook] mode -- disabled in the controls panel while that's on. */
    var lockTargetToPoint = true

    // Controls panel grouping -- default expanded so nothing looks like it went missing.
    var cameraGroupExpanded = true
    var projectionGroupExpanded = true

    /** Orbit mode's eye *anchor* -- [orbitDegrees]/[pitchDegrees]/[zoom] always orbit the eye
     * around this exact point, regardless of [lockTargetToPoint]. Keeping the anchor fixed to
     * [panX]/[panZ]/[elevation] (never swapped for the target's position) is what keeps the eye
     * itself from jumping when [lockTargetToPoint] is toggled -- only [computeCamera]'s separate
     * look-at `center` changes, re-aiming the same eye position instead of moving it. */
    fun orbitAnchor(): Vec3 = Vec3(panX, elevation, panZ)

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

    /** Builds this frame's [CoreCamera] in whichever mode [freeLook] selects -- [targetPosition]
     * is only used as the look-at `center` when [lockTargetToPoint] is on; the eye's position
     * never depends on it. */
    fun computeCamera(targetPosition: Vec3): CoreCamera {
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
            center = if (lockTargetToPoint) targetPosition else anchor
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

    /** World-up `(0, 1, 0)` rotated by [rollDegrees] around the eye-to-`center` view axis --
     * standard camera-roll construction (right = forward x worldUp, trueUp = right x forward,
     * rolledUp = trueUp*cos(roll) + right*sin(roll)). */
    private fun rolledUpVector(eye: Vec3, center: Vec3): Vec3 {
        val forward = (center - eye).normalize()
        val worldUp = Vec3(0f, 1f, 0f)
        val right = forward.cross(worldUp).normalize()
        val trueUp = right.cross(forward).normalize()
        val rollRad = rollDegrees * DEGREES_TO_RADIANS
        return (trueUp * cos(rollRad)) + (right * sin(rollRad))
    }

    private companion object {
        // Arbitrary nonzero distance from eye to Camera's `center` field in free-look mode --
        // Camera's view matrix only needs eye/center/up to determine direction, this magnitude
        // has no visual effect (unlike orbit mode's zoom, which IS the eye-to-target distance).
        const val FREE_LOOK_DISTANCE = 10f
        val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()
    }
}

/** Renders [controller]'s Camera + Projection groups -- [idPrefix] keeps this demo's slider ids
 * distinct from any other demo's copy of the same panel; [targetLabel] names whatever
 * [OrbitCameraController.lockTargetToPoint]'s switch is locking onto (e.g. "cube", "model"). */
internal fun ColumnScope.renderOrbitCameraControls(
    controller: OrbitCameraController,
    idPrefix: String,
    targetLabel: String
) {
    // Reassigning from shadcnCollapsible's own return value here (`cameraGroupExpanded =
    // shadcnCollapsible(...)`) is a real bug, not just style: the primary shadcnCollapsible
    // always `return`s the pre-click `expanded` it was called with, so that reassignment
    // executes *after* `onExpandedChange` already flipped the var this frame and silently
    // clobbers it back to the stale value -- collapsing a card never visibly toggled.
    // shadcnCollapsible's own sidebar-category caller (UiShowcaseChrome.kt) never reassigns
    // from the return value for exactly this reason; match that here.
    shadcnCollapsible(
        id = "$idPrefix-controls-camera",
        title = "Camera",
        expanded = controller.cameraGroupExpanded,
        onExpandedChange = { controller.cameraGroupExpanded = it },
        bordered = true
    ) {
        controller.orbitDegrees = shadcnFieldSliderWithValue(id = "$idPrefix-orbit", label = "Orbit", min = 0f, max = 360f, value = controller.orbitDegrees)
        controller.pitchDegrees = shadcnFieldSliderWithValue(id = "$idPrefix-pitch", label = "Pitch", min = 0f, max = 89f, value = controller.pitchDegrees)
        controller.rollDegrees = shadcnFieldSliderWithValue(id = "$idPrefix-roll", label = "Roll", min = -180f, max = 180f, value = controller.rollDegrees)
        controller.freeLook = shadcnSwitch(id = "$idPrefix-free-look", checked = controller.freeLook, label = "Free look")
        controller.zoom = shadcnFieldSliderWithValue(id = "$idPrefix-zoom", label = "Zoom", min = controller.zoomMin, max = controller.zoomMax, value = controller.zoom, enabled = !controller.freeLook)
        controller.lockTargetToPoint = shadcnSwitch(id = "$idPrefix-lock-target", checked = controller.lockTargetToPoint, label = "Look at $targetLabel", enabled = !controller.freeLook)
        // Meaningless combined with lockTargetToPoint only in orbit mode (the target is forced
        // to the caller's target position, these fields have nothing to affect); in freeLook
        // they're always live since they're the *eye* position there, not a look-at target.
        val targetFieldsEnabled = controller.freeLook || !controller.lockTargetToPoint
        controller.panX = shadcnFieldSliderWithValue(id = "$idPrefix-pan-x", label = "Pan X", min = -controller.panRange, max = controller.panRange, value = controller.panX, enabled = targetFieldsEnabled)
        controller.panZ = shadcnFieldSliderWithValue(id = "$idPrefix-pan-z", label = "Pan Z", min = -controller.panRange, max = controller.panRange, value = controller.panZ, enabled = targetFieldsEnabled)
        controller.elevation = shadcnFieldSliderWithValue(id = "$idPrefix-elevation", label = "Elevation", min = -controller.panRange, max = controller.panRange, value = controller.elevation, enabled = targetFieldsEnabled)
    }
    shadcnCollapsible(
        id = "$idPrefix-controls-projection",
        title = "Projection",
        expanded = controller.projectionGroupExpanded,
        onExpandedChange = { controller.projectionGroupExpanded = it },
        bordered = true
    ) {
        controller.near = shadcnFieldSliderWithValue(id = "$idPrefix-near", label = "Near", min = 0.01f, max = 5f, value = controller.near)
        controller.far = shadcnFieldSliderWithValue(id = "$idPrefix-far", label = "Far", min = 10f, max = controller.zoomMax * 40f, value = controller.far)
        controller.fovDegrees = shadcnFieldSliderWithValue(id = "$idPrefix-fov", label = "FOV", min = 10f, max = 120f, value = controller.fovDegrees)
    }
}
