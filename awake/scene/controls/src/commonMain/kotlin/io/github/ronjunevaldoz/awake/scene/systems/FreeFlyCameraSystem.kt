// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.FreeFlyControl
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Catalog-tool camera mode (see docs/MVP_PLAN.md's model-viewer/camera-catalog decision
 * log): a spectator/noclip camera.
 *
 * Decoupled from hardware input: reads movement intents from [FreeFlyControl].
 */
class FreeFlyCameraSystem(
    private val moveSpeed: Float = DEFAULT_MOVE_SPEED
) : System {

    override fun update(world: World, delta: Float) {
        world.queryEach(Camera::class, FreeFlyControl::class) { entity, camera, control ->
            // Apply rotation intents
            control.yaw += control.yawDelta
            control.pitch = (control.pitch + control.pitchDelta).coerceIn(MIN_PITCH, MAX_PITCH)

            val cosPitch = cos(control.pitch)
            val forwardX = cosPitch * sin(control.yaw)
            val forwardY = sin(control.pitch)
            val forwardZ = -cosPitch * cos(control.yaw)
            
            val rightX = cos(control.yaw)
            val rightZ = sin(control.yaw)

            // Translate the "raw" WASD movement into camera-relative movement
            val step = moveSpeed * delta
            val moveX = (control.moveX * rightX + control.moveZ * forwardX) * step
            val moveY = (control.moveZ * forwardY) * step
            val moveZ = (control.moveX * rightZ + control.moveZ * forwardZ) * step

            val coreCamera = camera.camera
            coreCamera.eye.x += moveX
            coreCamera.eye.y += moveY
            coreCamera.eye.z += moveZ
            coreCamera.center.x = coreCamera.eye.x + forwardX
            coreCamera.center.y = coreCamera.eye.y + forwardY
            coreCamera.center.z = coreCamera.eye.z + forwardZ
        }
    }

    private companion object {
        const val DEFAULT_MOVE_SPEED = 5f
        val MIN_PITCH = (-89.0 * PI / 180.0).toFloat()
        val MAX_PITCH = (89.0 * PI / 180.0).toFloat()
    }
}
