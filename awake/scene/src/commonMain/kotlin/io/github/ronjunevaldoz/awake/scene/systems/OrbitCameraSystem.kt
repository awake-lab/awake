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
    initialDistance: Float = DEFAULT_DISTANCE,
    private val rotateSpeed: Float = DEFAULT_ROTATE_SPEED,
    private val zoomSpeed: Float = DEFAULT_ZOOM_SPEED,
    private val autoRotateSpeed: Float = 0f
) : System {
    /** Orbit yaw in radians. Public (not just internally mutated by drag/auto-rotate) so a
     * UI slider can both read it (to draw the handle's current position) and write it
     * (when dragged) -- no clamping needed here, unlike [pitch]/[distance], since yaw wraps
     * freely (no gimbal-lock-style limit exists for rotation around the vertical axis). */
    var yaw: Float = 0f

    /** Orbit pitch in radians, clamped to [MIN_PITCH]/[MAX_PITCH] on every write (both from
     * [update]'s own drag handling and from an external UI slider) so a slider can never
     * push the camera past the same gimbal-lock-avoidance limit [update] already enforces. */
    var pitch: Float = DEFAULT_PITCH
        set(value) {
            field = value.coerceIn(MIN_PITCH, MAX_PITCH)
        }

    /** Orbit distance from [target], floored at [MIN_DISTANCE] on every write, same as
     * [update]'s own `W`-key zoom-in handling -- a UI slider setting this can't push the
     * camera through/past the target either. */
    var distance: Float = initialDistance.coerceAtLeast(MIN_DISTANCE)
        set(value) {
            field = value.coerceAtLeast(MIN_DISTANCE)
        }

    private var lastPointerX = 0f
    private var lastPointerY = 0f
    private var wasDragging = false

    override fun update(world: World, delta: Float) {
        // A UI widget (button/toggle/slider) already claimed this drag -- see
        // Input.pointerCapturedByUi's doc comment. Treat the pointer as "not down" for
        // drag-derived yaw/pitch purposes only; W/S zoom and auto-rotate below are
        // independent of dragging and must keep working while a widget is being dragged.
        val draggingPointer = Input.pointerDown && !Input.pointerCapturedByUi
        if (draggingPointer) {
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

    /** Not `private` (unlike the rest of this object's constants) -- [MIN_PITCH]/[MAX_PITCH]
     * are the same clamp [pitch]'s own setter enforces, and a UI slider for pitch needs the
     * exact same range rather than inventing a separate one that could fight this system's
     * own clamping (see `CubeDemo.drawCatalogUi`'s elevation slider). */
    companion object {
        const val DEFAULT_DISTANCE = 8f
        const val MIN_DISTANCE = 1f
        private const val DEFAULT_ROTATE_SPEED = 0.01f
        private const val DEFAULT_ZOOM_SPEED = 4f
        private const val DEFAULT_PITCH = 0.4f
        val MIN_PITCH = (-89.0 * PI / 180.0).toFloat()
        val MAX_PITCH = (89.0 * PI / 180.0).toFloat()
    }
}
