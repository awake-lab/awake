// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.components

import io.github.ronjunevaldoz.awake.core.math.Vec3
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
 * Stores the target/offset/smoothing for a third-person follow (POV) camera.
 */
class FollowControl : Poolable {
    var target: Transform? = null
    var offset: Vec3 = Vec3(0f, 3f, 6f)

    /** Exponential-decay rate (higher = snappier). See [FollowCameraSystem]. */
    var smoothing: Float = 8f

    override fun reset() {
        target = null
        offset = Vec3(0f, 3f, 6f)
        smoothing = 8f
    }
}

/**
 * Stores the target for a rotation-only (look-at/tracking) camera -- the eye stays wherever it
 * already is (set independently, e.g. by [OrbitControl]/[FreeFlyControl] or gameplay code), only
 * the aim direction tracks [target] every frame. See [FollowControl]'s own doc comment for the
 * distinction: that one also chases the target's position, this one doesn't.
 */
class LookAtControl : Poolable {
    var target: Transform? = null

    override fun reset() {
        target = null
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
