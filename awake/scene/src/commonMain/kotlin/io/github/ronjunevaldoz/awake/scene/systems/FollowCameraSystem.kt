// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.FollowControl
import kotlin.math.exp

/**
 * Third-person/POV camera mode (see docs/MVP_PLAN.md's model-viewer/camera-catalog decision
 * log, matching [OrbitCameraSystem]/[FreeFlyCameraSystem]'s shape): keeps any [Camera] that
 * has a [FollowControl] component trailing behind its target at a fixed offset, smoothed with
 * frame-rate-independent exponential decay so `delta` spikes don't cause overshoot.
 *
 * Decoupled from hardware input: [FollowControl] carries the target/offset/smoothing, nothing
 * here reads WASD directly (see PlayerMovementSystem/PlayerControlSystem for moving the
 * followed target itself).
 */
class FollowCameraSystem : System {

    override fun update(world: World, delta: Float) {
        world.queryEach(Camera::class, FollowControl::class) { entity, camera, control ->
            val target = control.target ?: return@queryEach

            val desiredX = target.position.x + control.offset.x
            val desiredY = target.position.y + control.offset.y
            val desiredZ = target.position.z + control.offset.z

            val factor = (1f - exp(-control.smoothing * delta)).coerceIn(0f, 1f)
            val coreCamera = camera.camera
            coreCamera.eye.x += (desiredX - coreCamera.eye.x) * factor
            coreCamera.eye.y += (desiredY - coreCamera.eye.y) * factor
            coreCamera.eye.z += (desiredZ - coreCamera.eye.z) * factor

            coreCamera.center.x = target.position.x
            coreCamera.center.y = target.position.y
            coreCamera.center.z = target.position.z
        }
    }
}
