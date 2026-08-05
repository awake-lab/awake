// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * Composes [Transform.worldMatrix] as `translate(offset) * rotateY(radians)` for any entity
 * with a [SpinControl] -- the same shape a rotating-in-place object (a spinning cube, a coin, a
 * portal) always needs, generalized out of duplicating this exact matrix build in every demo
 * that wants it. Doesn't read `delta` itself: [SpinControl.radians] is expected to already be
 * current (whoever owns the spin rate -- auto-play clock, UI scrub slider, gameplay code --
 * advances it before this system runs), matching [io.github.ronjunevaldoz.awake.scene.systems
 * .FollowCameraSystem]'s "control carries state, system only composes" shape.
 */
class SpinSystem : System {
    override fun update(world: World, delta: Float) {
        world.queryEach(Transform::class, SpinControl::class) { _, transform, spin ->
            transform.worldMatrix = Mat4()
                .translate(spin.offset.x, spin.offset.y, spin.offset.z)
                .rotateY(spin.radians)
        }
    }
}
