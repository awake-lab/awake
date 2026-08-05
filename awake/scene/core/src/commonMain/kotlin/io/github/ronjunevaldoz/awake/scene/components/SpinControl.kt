// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.components

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Poolable

/**
 * Stores the current pose for an entity whose [Transform.worldMatrix] is a translate-then-
 * rotate-around-Y composition instead of the usual position/rotation/scale TRS
 * ([TransformSystem] deliberately doesn't touch an entity that has this component -- see
 * [SpinSystem]). Gameplay/UI code (a slider, an auto-play clock, anything) sets [radians]
 * directly every frame it wants a new angle; [SpinSystem] only composes the matrix.
 */
class SpinControl : Poolable {
    var offset: Vec3 = Vec3(0f, 0f, 0f)
    var radians: Float = 0f

    override fun reset() {
        offset = Vec3(0f, 0f, 0f)
        radians = 0f
    }
}
