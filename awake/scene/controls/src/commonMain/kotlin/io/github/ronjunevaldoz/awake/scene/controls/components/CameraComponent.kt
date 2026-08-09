// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.controls.components

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.Poolable

/**
 * Tag component to mark an entity as the active camera.
 * Delta inputs are consumed exclusively by the active camera entity.
 */
class ActiveCamera : Poolable {
    /** Pure tag: no state to clear when the pool recycles it. */
    override fun reset() = Unit
}

/**
 * Unified camera configuration and state.
 */
class CameraComponent : Poolable {
    var mode: CameraMode = CameraMode.FirstPerson
        set(value) {
            if (field != value) {
                field = value
                needsReset = true
            }
        }

    var needsReset: Boolean = false

    // Target Tracking (Used by ThirdPerson, Cinematic, and TopDown)
    var targetEntity: Entity? = null

    // Orbit and Offset configurations
    /** Current zoom distance from the target. */
    var distance: Float = DEFAULT_DISTANCE
    var minDistance: Float = DEFAULT_MIN_DISTANCE
    var maxDistance: Float = DEFAULT_MAX_DISTANCE

    var pitch: Float = 0f // Vertical orbit angle (radians)
    var yaw: Float = 0f // Horizontal orbit angle (radians)

    // Mode-specific offsets (e.g., eye-height for FirstPerson, or focal point offset)
    var offsetPosition: Vec3 = Vec3(0f, DEFAULT_EYE_HEIGHT, 0f)

    override fun reset() {
        mode = CameraMode.FirstPerson
        needsReset = false
        targetEntity = null
        distance = DEFAULT_DISTANCE
        minDistance = DEFAULT_MIN_DISTANCE
        maxDistance = DEFAULT_MAX_DISTANCE
        pitch = 0f
        yaw = 0f
        offsetPosition.set(0f, DEFAULT_EYE_HEIGHT, 0f)
    }

    private companion object {
        const val DEFAULT_DISTANCE = 5f
        const val DEFAULT_MIN_DISTANCE = 2f
        const val DEFAULT_MAX_DISTANCE = 20f

        /** Roughly human eye height, so a first-person view sits at the head, not the feet. */
        const val DEFAULT_EYE_HEIGHT = 1.8f
    }
}

/**
 * Which drag/scroll inputs a mode actually consumes. A mode that ignores an axis must not
 * accumulate it: otherwise the hidden value keeps drifting while the mode looks frozen, and
 * the camera snaps the moment you switch to a mode that does read it.
 */
enum class CameraMode(
    internal val usesYaw: Boolean,
    internal val usesPitch: Boolean,
    internal val usesZoom: Boolean,
) {
    FirstPerson(usesYaw = true, usesPitch = true, usesZoom = false),
    ThirdPerson(usesYaw = true, usesPitch = true, usesZoom = true),

    /** Eye is parked at mode-switch time and only the aim tracks the target. */
    Cinematic(usesYaw = false, usesPitch = false, usesZoom = false),

    /** Fixed downward tilt; drag orbits the target horizontally, scroll changes height. */
    TopDown(usesYaw = true, usesPitch = false, usesZoom = true),
}
