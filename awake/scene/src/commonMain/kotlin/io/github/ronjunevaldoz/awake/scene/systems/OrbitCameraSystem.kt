// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.Transform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Catalog-tool camera mode (see docs/MVP_PLAN.md's model-viewer/camera-catalog decision
 * log): orbits [camera] around [target]'s position at a fixed [distance], driven by
 * pointer-drag (yaw/pitch) and `W`/`S` (zoom in/out) -- no scroll-wheel axis exists in
 * [Input] yet, so distance is keyboard-only for this slice.
 *
 * Same "compute frame-to-frame drag delta by diffing pointer position" approach as
 * [PlayerMovementSystem]'s drag-origin tracking, since [Input] exposes absolute pointer
 * position, not a delta.
 *
 * [autoRotateSpeed] (radians/second, default 0 = off) advances yaw on its own whenever the
 * pointer isn't actively dragging -- opt-in per instance (e.g. `sample-hello-cube`'s static
 * single-cube demo, which has nothing else to animate) rather than a default-on behavior,
 * since `awake-demo`'s interactive catalog-tool orbit shouldn't drift out from under a user
 * who has let go of the mouse.
 */
class OrbitCameraSystem(
    /** Mutable (not just constructor-set) so a caller can retarget the orbit (e.g. a
     * catalog-tool "focus on this model" dropdown) without recreating the system and
     * losing its accumulated yaw/pitch/distance state. */
    var target: Transform,
    private val camera: Camera,
    private var distance: Float = DEFAULT_DISTANCE,
    private val rotateSpeed: Float = DEFAULT_ROTATE_SPEED,
    private val zoomSpeed: Float = DEFAULT_ZOOM_SPEED,
    private val autoRotateSpeed: Float = 0f
) : System {
    private var yaw: Float = 0f
    private var pitch: Float = DEFAULT_PITCH
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
            yaw += autoRotateSpeed * delta
        }

        if (Input.isKeyDown(Key.W)) distance = (distance - zoomSpeed * delta).coerceAtLeast(MIN_DISTANCE)
        if (Input.isKeyDown(Key.S)) distance += zoomSpeed * delta

        val cosPitch = cos(pitch)
        val coreCamera = camera.camera
        coreCamera.eye.x = target.position.x + distance * cosPitch * sin(yaw)
        coreCamera.eye.y = target.position.y + distance * sin(pitch)
        coreCamera.eye.z = target.position.z + distance * cosPitch * cos(yaw)
        coreCamera.center.x = target.position.x
        coreCamera.center.y = target.position.y
        coreCamera.center.z = target.position.z
    }

    private companion object {
        const val DEFAULT_DISTANCE = 8f
        const val MIN_DISTANCE = 1f
        const val DEFAULT_ROTATE_SPEED = 0.01f
        const val DEFAULT_ZOOM_SPEED = 4f
        const val DEFAULT_PITCH = 0.4f
        val MIN_PITCH = (-89.0 * PI / 180.0).toFloat()
        val MAX_PITCH = (89.0 * PI / 180.0).toFloat()
    }
}
