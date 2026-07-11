// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Catalog-tool camera mode (see docs/MVP_PLAN.md's model-viewer/camera-catalog decision
 * log): a spectator/noclip camera -- `W`/`A`/`S`/`D` moves [camera] freely along its own
 * forward/right vectors (derived from [yaw]/[pitch], same spherical-angle approach
 * [OrbitCameraSystem] uses), pointer-drag looks around. Moves `eye` and `center` together
 * so the look direction doesn't change from translation alone.
 */
class FreeFlyCameraSystem(
    private val camera: Camera,
    private val moveSpeed: Float = DEFAULT_MOVE_SPEED,
    private val rotateSpeed: Float = DEFAULT_ROTATE_SPEED
) : System {
    private var yaw: Float = 0f
    private var pitch: Float = 0f
    private var lastPointerX = 0f
    private var lastPointerY = 0f
    private var wasDragging = false

    override fun update(world: World, delta: Float) {
        if (Input.pointerDown) {
            if (wasDragging) {
                val dx = Input.pointerX - lastPointerX
                val dy = Input.pointerY - lastPointerY
                yaw -= dx * rotateSpeed
                pitch = (pitch - dy * rotateSpeed).coerceIn(MIN_PITCH, MAX_PITCH)
            }
            lastPointerX = Input.pointerX
            lastPointerY = Input.pointerY
            wasDragging = true
        } else {
            wasDragging = false
        }

        val cosPitch = cos(pitch)
        // yaw = 0, pitch = 0 points along -Z -- matches this codebase's existing camera
        // convention (see CameraTest's identity-view setup: eye at origin, center at
        // (0, 0, -1)).
        val forwardX = cosPitch * sin(yaw)
        val forwardY = sin(pitch)
        val forwardZ = -cosPitch * cos(yaw)
        // Horizontal-only right vector (perpendicular to forward's XZ projection) -- strafe
        // doesn't tilt with pitch, matching a typical spectator camera.
        val rightX = cos(yaw)
        val rightZ = sin(yaw)

        var moveX = 0f
        var moveY = 0f
        var moveZ = 0f
        val step = moveSpeed * delta
        if (Input.isKeyDown(Key.W) || Input.isKeyDown(Key.ArrowUp)) {
            moveX += forwardX * step; moveY += forwardY * step; moveZ += forwardZ * step
        }
        if (Input.isKeyDown(Key.S) || Input.isKeyDown(Key.ArrowDown)) {
            moveX -= forwardX * step; moveY -= forwardY * step; moveZ -= forwardZ * step
        }
        if (Input.isKeyDown(Key.D) || Input.isKeyDown(Key.ArrowRight)) {
            moveX += rightX * step; moveZ += rightZ * step
        }
        if (Input.isKeyDown(Key.A) || Input.isKeyDown(Key.ArrowLeft)) {
            moveX -= rightX * step; moveZ -= rightZ * step
        }

        val coreCamera = camera.camera
        coreCamera.eye.x += moveX
        coreCamera.eye.y += moveY
        coreCamera.eye.z += moveZ
        coreCamera.center.x = coreCamera.eye.x + forwardX
        coreCamera.center.y = coreCamera.eye.y + forwardY
        coreCamera.center.z = coreCamera.eye.z + forwardZ
    }

    private companion object {
        const val DEFAULT_MOVE_SPEED = 5f
        const val DEFAULT_ROTATE_SPEED = 0.01f
        val MIN_PITCH = (-89.0 * PI / 180.0).toFloat()
        val MAX_PITCH = (89.0 * PI / 180.0).toFloat()
    }
}
