// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.OrbitControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * OrbitCameraSystem is now decoupled from hardware input -- it only reads intent already
 * accumulated onto [OrbitControl] (by PlayerControlSystem, in a real frame; see
 * PlayerControlSystemTest for that half). These tests drive [OrbitControl] directly.
 */
class OrbitCameraSystemTest {
    private fun newCamera() = Camera(
        camera = CoreCamera(eye = Vec3(0f, 0f, 0f), center = Vec3(0f, 0f, 0f), fovYRadians = 1f, near = 0.1f, far = 100f)
    )

    @Test
    fun cameraAlwaysLooksAtTarget() {
        val world = World()
        val target = Transform(position = Vec3(2f, 0f, 3f))
        val entity = world.create()
        world.add(entity, newCamera())
        world.add(entity, OrbitControl().apply {
            this.target = target
            distance = 5f
        })
        val system = OrbitCameraSystem()

        system.update(world, 0f)

        val camera = world.get(entity, Camera::class)!!
        assertEquals(2f, camera.camera.center.x)
        assertEquals(0f, camera.camera.center.y)
        assertEquals(3f, camera.camera.center.z)
    }

    @Test
    fun noDeltaOrAutoRotateLeavesYawPitchUnchangedAcrossFrames() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val entity = world.create()
        world.add(entity, newCamera())
        world.add(entity, OrbitControl().apply {
            this.target = target
            distance = 5f
        })
        val system = OrbitCameraSystem()

        system.update(world, 0f)
        val camera = world.get(entity, Camera::class)!!
        val firstEye = Vec3(camera.camera.eye.x, camera.camera.eye.y, camera.camera.eye.z)
        system.update(world, 0f)

        assertEquals(firstEye.x, camera.camera.eye.x)
        assertEquals(firstEye.y, camera.camera.eye.y)
        assertEquals(firstEye.z, camera.camera.eye.z)
    }

    @Test
    fun distanceDeltaAppliesAndClampsAtMinDistance() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val entity = world.create()
        world.add(entity, newCamera())
        val control = OrbitControl().apply {
            this.target = target
            distance = 10f
        }
        world.add(entity, control)
        val system = OrbitCameraSystem()

        control.distanceDelta = -3f
        system.update(world, 0f)
        assertEquals(7f, control.distance)

        control.distanceDelta = -100f
        system.update(world, 0f)
        assertEquals(OrbitCameraSystem.MIN_DISTANCE, control.distance)
    }

    @Test
    fun yawAndPitchDeltaApplyAndPitchClamps() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val entity = world.create()
        world.add(entity, newCamera())
        val control = OrbitControl().apply {
            this.target = target
            yaw = 0f
            pitch = 0f
        }
        world.add(entity, control)
        val system = OrbitCameraSystem()

        control.yawDelta = 0.5f
        control.pitchDelta = 0.2f
        system.update(world, 0f)
        assertEquals(0.5f, control.yaw)
        assertEquals(0.2f, control.pitch)

        control.yawDelta = 0f
        control.pitchDelta = 100f
        system.update(world, 0f)
        assertEquals(OrbitCameraSystem.MAX_PITCH, control.pitch)
    }

    @Test
    fun autoRotateOnlyAppliesWhenNoManualDeltaThisFrame() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val entity = world.create()
        world.add(entity, newCamera())
        val control = OrbitControl().apply {
            this.target = target
            yaw = 0f
        }
        world.add(entity, control)
        val system = OrbitCameraSystem(autoRotateSpeed = 1f)

        // Manual yaw delta present this frame -- auto-rotate must not also apply.
        control.yawDelta = 0.1f
        system.update(world, 1f)
        assertEquals(0.1f, control.yaw)

        // No manual delta -- auto-rotate applies.
        control.yawDelta = 0f
        system.update(world, 1f)
        assertEquals(1.1f, control.yaw)
    }

    @Test
    fun entityWithoutTargetIsSkippedWithoutThrowing() {
        val world = World()
        val entity = world.create()
        world.add(entity, newCamera())
        world.add(entity, OrbitControl())
        val system = OrbitCameraSystem()

        system.update(world, 0f)
    }
}
