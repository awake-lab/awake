// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.state

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Turns a [StudioContract.CameraState] into a lens -- eye/up plus the projection -- with no
 * `World` dependency, so Orbit/Front/Top are testable without a scene. `center` is always just
 * the orbit target for every preset in v1 (no pan), so callers own that directly.
 *
 * Orbit shares its forward basis with `awake:scene:controls`' own `CameraSystem` (core-math
 * skill Rule 5: yaw 0 faces -Z, +yaw turns right, +pitch looks up) so this preset rig doesn't
 * disagree with the engine's own camera modes. Front/Top are fixed axis-aligned presets, not
 * run through that basis -- a literal top-down view is exactly the pole `forwardFrom`/
 * `setLookAt` degenerate at (Rule 5), so Top sets `eye`/`up` directly instead of clamping pitch
 * toward it.
 */
internal object CameraPresetMath {

    /** Writes the forward vector for [yaw]/[pitch] into [out]. No allocation -- safe to call
     * every frame from a `System.update`. */
    fun forwardFrom(yaw: Float, pitch: Float, out: Vec3) {
        val cp = cos(pitch)
        out.set(sin(yaw) * cp, sin(pitch), -cos(yaw) * cp)
    }

    /**
     * Writes this frame's eye/up/projection for [state] orbiting [target] into [camera].
     * [forwardScratch] is caller-owned scratch reused across frames (core-math skill Rule 2:
     * no allocation inside `System.update`) -- only [CameraPresetMode.Orbit] touches it.
     */
    fun applyPreset(
        state: StudioContract.CameraState,
        target: Vec3,
        camera: Camera,
        forwardScratch: Vec3,
    ) {
        val eyeOut = camera.eye
        val upOut = camera.up
        when (state.mode) {
            StudioContract.CameraPresetMode.Orbit -> {
                forwardFrom(state.yaw, state.pitch, forwardScratch)
                eyeOut.set(target).sub(forwardScratch.scale(state.distance))
                upOut.set(0f, 1f, 0f)
            }

            StudioContract.CameraPresetMode.Front -> {
                eyeOut.set(target.x, target.y, target.z + state.distance)
                upOut.set(0f, 1f, 0f)
            }

            StudioContract.CameraPresetMode.Top -> {
                eyeOut.set(target.x, target.y + state.distance, target.z)
                // up=(0,1,0) is parallel to a straight-down forward -- setLookAt's cross
                // product degenerates there (core-math skill Rule 5). -Z reads as "north up".
                upOut.set(0f, 0f, -1f)
            }
        }
        camera.projection = when (state.projection) {
            StudioContract.Projection.Perspective -> Camera.Projection.Perspective
            StudioContract.Projection.Orthographic -> Camera.Projection.Orthographic
        }
        // Ortho has no perspective divide, so its size comes from here alone: frame what the
        // lens already frames at the orbit target, and toggling projection doesn't resize.
        camera.orthoHalfHeight = state.distance * tan(camera.fovYRadians / 2f)
    }
}
