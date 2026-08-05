// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.FollowControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.FollowCameraSystem
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * FollowCameraSystem is decoupled from hardware input -- it only reads target/offset/smoothing
 * already set on [FollowControl]. Gameplay code decides how the followed target itself moves.
 */
class FollowCameraSystemTest {
    private fun newCamera() = Camera(
        camera = CoreCamera(eye = Vec3(0f, 0f, 0f), center = Vec3(0f, 0f, 0f), fovYRadians = 1f, near = 0.1f, far = 100f)
    )

    @Test
    fun cameraAlwaysLooksAtTarget() {
        val world = World()
        val target = Transform(position = Vec3(2f, 0f, 3f))
        val entity = world.create()
        world.add(entity, newCamera())
        world.add(entity, FollowControl().apply { this.target = target })
        val system = FollowCameraSystem()

        system.update(world, 1f / 60f)

        val camera = world.get(entity, Camera::class)!!
        assertEquals(2f, camera.camera.center.x)
        assertEquals(0f, camera.camera.center.y)
        assertEquals(3f, camera.camera.center.z)
    }

    @Test
    fun entityWithoutTargetIsSkippedWithoutThrowing() {
        val world = World()
        val entity = world.create()
        world.add(entity, newCamera())
        world.add(entity, FollowControl())
        val system = FollowCameraSystem()

        system.update(world, 1f / 60f)
    }

    @Test
    fun cameraConvergesTowardMovingTargetOverSeveralFrames() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val entity = world.create()
        world.add(entity, newCamera())
        val offset = Vec3(0f, 3f, 6f)
        world.add(entity, FollowControl().apply {
            this.target = target
            this.offset = offset
            this.smoothing = 8f
        })
        val system = FollowCameraSystem()
        val camera = world.get(entity, Camera::class)!!

        // Drive several frames while the target keeps moving away, like WASD-driven movement.
        repeat(120) {
            target.position.x += 0.05f
            system.update(world, 1f / 60f)
        }

        val desiredX = target.position.x + offset.x
        assertTrue(
            abs(camera.camera.eye.x - desiredX) < 0.5f,
            "expected eye.x (${camera.camera.eye.x}) to converge near desired ($desiredX)"
        )
        // Still trailing behind, not teleported exactly onto the target.
        assertTrue(camera.camera.eye.x < desiredX)
    }
}
