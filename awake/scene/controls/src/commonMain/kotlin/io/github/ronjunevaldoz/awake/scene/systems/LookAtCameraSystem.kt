// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.LookAtControl

/**
 * Rotation-only tracking camera (see [FollowCameraSystem]'s own doc comment for the sibling
 * mode that also chases the target's position): keeps any [Camera] that has a [LookAtControl]
 * component aimed at its target every frame -- the eye is left exactly where it already is,
 * only [io.github.ronjunevaldoz.awake.core.math.Camera.center] moves.
 *
 * Decoupled from hardware input, same shape as [OrbitCameraSystem]/[FreeFlyCameraSystem]/
 * [FollowCameraSystem]: [LookAtControl] carries the target, this system only re-aims; gameplay
 * code decides how the target itself moves and how the eye itself gets positioned.
 */
class LookAtCameraSystem : System {

    override fun update(world: World, delta: Float) {
        world.queryEach(Camera::class, LookAtControl::class) { entity, camera, control ->
            val target = control.target ?: return@queryEach
            camera.camera.center.x = target.position.x
            camera.camera.center.y = target.position.y
            camera.camera.center.z = target.position.z
        }
    }
}
