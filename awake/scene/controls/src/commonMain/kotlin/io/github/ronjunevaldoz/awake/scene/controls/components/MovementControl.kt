// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.controls.components

import io.github.ronjunevaldoz.awake.ecs.Poolable

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
