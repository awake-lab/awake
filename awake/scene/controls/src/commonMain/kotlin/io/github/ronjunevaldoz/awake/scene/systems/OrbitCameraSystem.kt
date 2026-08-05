// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.OrbitControl
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Catalog-tool camera mode (see docs/MVP_PLAN.md's model-viewer/camera-catalog decision
 * log): orbits any [Camera] that has an [OrbitControl] component around its target.
 *
 * Decoupled from hardware input: reads rotation/zoom intents from [OrbitControl].
 */
class OrbitCameraSystem(
    private val autoRotateSpeed: Float = 0f
) : System {

    override fun update(world: World, delta: Float) {
        // Find entities that are being controlled by the player via OrbitControl
        world.queryEach(Camera::class, OrbitControl::class) { entity, camera, control ->
            val target = control.target ?: return@queryEach

            // Apply intents accumulated by PlayerControlSystem this frame
            control.yaw += control.yawDelta
            control.pitch = (control.pitch + control.pitchDelta).coerceIn(MIN_PITCH, MAX_PITCH)
            control.distance = (control.distance + control.distanceDelta).coerceAtLeast(MIN_DISTANCE)

            // Apply auto-rotate only if no manual rotation is happening on this entity
            if (control.yawDelta == 0f && control.pitchDelta == 0f) {
                control.yaw += autoRotateSpeed * delta
            }

            val cosPitch = cos(control.pitch)
            val coreCamera = camera.camera
            coreCamera.eye.x = target.position.x + control.distance * cosPitch * sin(control.yaw)
            coreCamera.eye.y = target.position.y + control.distance * sin(control.pitch)
            coreCamera.eye.z = target.position.z + control.distance * cosPitch * cos(control.yaw)
            coreCamera.center.x = target.position.x
            coreCamera.center.y = target.position.y
            coreCamera.center.z = target.position.z
        }
    }

    companion object {
        const val MIN_DISTANCE = 1f
        val MIN_PITCH = (-89.0 * PI / 180.0).toFloat()
        val MAX_PITCH = (89.0 * PI / 180.0).toFloat()
    }
}
