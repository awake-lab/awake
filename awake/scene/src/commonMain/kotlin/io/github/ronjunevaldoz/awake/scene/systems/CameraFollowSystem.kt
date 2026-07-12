// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * MVP1a's first real per-frame writer of [Camera.camera]'s `eye`/`center` -- every other
 * scene today authors these once in `scenes/mvp.scene.json` and never touches them again.
 * Fixed third-person offset, no collision/occlusion handling (matches [PlayerMovementSystem]'s
 * "deliberately simple" scope for this slice).
 */
class CameraFollowSystem(
    private val playerTransform: Transform,
    private val cameraComponent: Camera,
    private val offset: Vec3 = Vec3(0f, 3f, 6f)
) : System {
    override fun update(world: World, delta: Float) {
        val playerPosition = playerTransform.position
        val camera = cameraComponent.camera
        camera.eye.x = playerPosition.x + offset.x
        camera.eye.y = playerPosition.y + offset.y
        camera.eye.z = playerPosition.z + offset.z
        camera.center.x = playerPosition.x
        camera.center.y = playerPosition.y
        camera.center.z = playerPosition.z
    }
}
