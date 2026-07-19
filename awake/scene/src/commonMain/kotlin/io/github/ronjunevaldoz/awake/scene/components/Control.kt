// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.components

import io.github.ronjunevaldoz.awake.ecs.Poolable

/**
 * Stores intended movement/rotation values for an orbit camera.
 * Centralizing this here decouples specialized systems from raw hardware input.
 */
class OrbitControl : Poolable {
    var target: Transform? = null
    var yaw: Float = 0f
    var pitch: Float = 0.4f
    var distance: Float = 8f

    // Internal deltas reset every frame by PlayerControlSystem
    var yawDelta: Float = 0f
    var pitchDelta: Float = 0f
    var distanceDelta: Float = 0f

    override fun reset() {
        yaw = 0f
        pitch = 0.4f
        distance = 8f
        yawDelta = 0f
        pitchDelta = 0f
        distanceDelta = 0f
    }
}

/**
 * Stores intended movement/rotation values for a free-fly camera.
 */
class FreeFlyControl : Poolable {
    var yaw: Float = 0f
    var pitch: Float = 0f

    // Internal deltas reset every frame by PlayerControlSystem
    var yawDelta: Float = 0f
    var pitchDelta: Float = 0f
    var moveX: Float = 0f
    var moveY: Float = 0f
    var moveZ: Float = 0f

    override fun reset() {
        yaw = 0f
        pitch = 0f
        yawDelta = 0f
        pitchDelta = 0f
        moveX = 0f
        moveY = 0f
        moveZ = 0f
    }
}

/**
 * Stores intended translation deltas for a character or player.
 */
class MovementControl : Poolable {
    var moveX: Float = 0f
    var moveY: Float = 0f
    var moveZ: Float = 0f

    override fun reset() {
        moveX = 0f
        moveY = 0f
        moveZ = 0f
    }
}
