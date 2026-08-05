// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.LookAtControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.LookAtCameraSystem
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * LookAtCameraSystem is decoupled from hardware input -- it only reads whatever target is
 * already set on [LookAtControl]. Unlike [io.github.ronjunevaldoz.awake.scene.systems
 * .FollowCameraSystem], the eye is never touched -- only the aim direction tracks the target.
 */
class LookAtCameraSystemTest {
    private fun newCamera(eye: Vec3) = Camera(
        camera = CoreCamera(eye = eye, center = Vec3(0f, 0f, 0f), fovYRadians = 1f, near = 0.1f, far = 100f)
    )

    @Test
    fun cameraAimsAtTargetWithoutMovingTheEye() {
        val world = World()
        val target = Transform(position = Vec3(2f, 0f, 3f))
        val entity = world.create()
        val eye = Vec3(10f, 5f, -8f)
        world.add(entity, newCamera(eye))
        world.add(entity, LookAtControl().apply { this.target = target })
        val system = LookAtCameraSystem()

        system.update(world, 1f / 60f)

        val camera = world.get(entity, Camera::class)!!
        assertEquals(2f, camera.camera.center.x)
        assertEquals(0f, camera.camera.center.y)
        assertEquals(3f, camera.camera.center.z)
        // Eye is untouched -- rotation-only, unlike FollowCameraSystem's eye-chasing.
        assertEquals(eye.x, camera.camera.eye.x)
        assertEquals(eye.y, camera.camera.eye.y)
        assertEquals(eye.z, camera.camera.eye.z)
    }

    @Test
    fun entityWithoutTargetIsSkippedWithoutThrowing() {
        val world = World()
        val entity = world.create()
        world.add(entity, newCamera(Vec3(0f, 0f, 0f)))
        world.add(entity, LookAtControl())
        val system = LookAtCameraSystem()

        system.update(world, 1f / 60f)
    }

    @Test
    fun cameraTracksTargetAsItMovesEveryFrame() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val entity = world.create()
        world.add(entity, newCamera(Vec3(0f, 5f, -10f)))
        world.add(entity, LookAtControl().apply { this.target = target })
        val system = LookAtCameraSystem()
        val camera = world.get(entity, Camera::class)!!

        repeat(10) {
            target.position.x += 1f
            system.update(world, 1f / 60f)
        }

        // No smoothing/lag -- always exactly at the target's current position, unlike
        // FollowCameraSystem's eye, which trails behind.
        assertEquals(10f, camera.camera.center.x)
    }
}
