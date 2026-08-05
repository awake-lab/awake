// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Reusable orbit/free-look camera rig -- builds a [Camera] from slider/UI-friendly fields
 * (orbit/pitch/roll/zoom/pan/elevation) instead of a raw eye/center/up triple, so a debug/
 * inspector-style viewport (any game or sample with a 3D scene to look at) doesn't need to
 * hand-roll the same orbit math. Pure math, no GPU/UI dependency -- lives alongside [Camera]
 * itself for the same reason that class does (see its own doc comment).
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
class OrbitCameraController(
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

    /** Suits a roughly-unit-scale scene by default -- a caller whose [zoomMax] goes well past
     * this (e.g. an auto-fit zoom sized to a large glTF model's real units) must scale this up
     * itself, or the eye ends up farther from its target than [far] allows and the whole scene
     * clips out silently (a real reported bug: fully blank viewport, no error). */
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
     * with it). Meaningless in [freeLook] mode. */
    var lockTargetToPoint = true

    // Controls-panel grouping state -- purely presentational bookkeeping a UI layer built on
    // top of this class can bind to (e.g. a collapsible "Camera"/"Projection" panel); this class
    // itself never reads these fields.
    var cameraGroupExpanded = false
    var projectionGroupExpanded = false

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

    /** Builds this frame's [Camera] in whichever mode [freeLook] selects -- [targetPosition]
     * is only used as the look-at `center` when [lockTargetToPoint] is on; the eye's position
     * never depends on it. */
    fun computeCamera(targetPosition: Vec3): Camera {
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
        return Camera(
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
