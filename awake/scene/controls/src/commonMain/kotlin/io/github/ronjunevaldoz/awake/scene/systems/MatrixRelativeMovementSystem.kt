// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.MovementControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import kotlin.math.sqrt

/**
 * Matrix-Relative Movement: Dynamically transforms player input vectors based on the forward
 * and right vectors of the currently active camera. This ensures that 'W' always moves the
 * player in the direction the camera is looking (projected onto the horizontal plane).
 */
class MatrixRelativeMovementSystem(
    private val speed: Float = 5f,
) : System {
    // Scratch vectors reused every frame -- this runs once per frame forever, so allocating
    // a fresh basis here would be pure garbage.
    private val forward = Vec3(0f, 0f, -1f)
    private val right = Vec3(1f, 0f, 0f)

    override fun update(world: World, delta: Float) {
        // 1. Resolve Active Camera orientation
        forward.set(0f, 0f, -1f)
        right.set(1f, 0f, 0f)

        world.queryEach(Camera::class, ActiveCamera::class) { _, camera, _ ->
            val core = camera.camera
            // Horizontal (XZ) view direction. Kept as scalars so the basis costs no
            // allocation; `forward` MUST end up unit-length, otherwise it scales movement
            // speed by the camera's distance to its target.
            val dirX = core.center.x - core.eye.x
            val dirZ = core.center.z - core.eye.z
            val length = sqrt(dirX * dirX + dirZ * dirZ)
            if (length > MIN_DIRECTION_LENGTH) {
                forward.set(dirX / length, 0f, dirZ / length)
                // right = forward x up; for up = (0, 1, 0) that reduces to (-z, 0, x) and is
                // already unit-length because `forward` is.
                right.set(-forward.z, 0f, forward.x)
            }
        }

        // 2. Apply to entities with Transform and MovementControl
        world.queryEach(Transform::class, MovementControl::class) { _, transform, control ->
            if (control.moveX == 0f && control.moveZ == 0f) return@queryEach

            // Transform camera-relative movement into world-space movement
            val worldMoveX = right.x * control.moveX + forward.x * control.moveZ
            val worldMoveZ = right.z * control.moveX + forward.z * control.moveZ

            transform.position.x += worldMoveX * speed * delta
            transform.position.z += worldMoveZ * speed * delta
        }
    }

    private companion object {
        /** Below this the camera is looking straight down and has no usable horizontal basis. */
        const val MIN_DIRECTION_LENGTH = 1e-4f
    }
}
