// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.core.components

import io.github.ronjunevaldoz.awake.ecs.Poolable

/**
 * Stores the current pose for an entity whose [Transform.worldMatrix] is a translate-then-
 * rotate-around-Y composition instead of the usual position/rotation/scale TRS
 * ([TransformSystem] deliberately doesn't touch an entity that has this component -- see
 * [SpinSystem]). Gameplay/UI code (a slider, an auto-play clock, anything) sets [radians]
 * directly every frame it wants a new angle; [SpinSystem] only composes the matrix.
 */
class SpinControl : Poolable {
    var radians: Float = 0f
    var speed: Float = 1f

    override fun reset() {
        radians = 0f
        speed = 1f
    }
}
