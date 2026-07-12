// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OrbitCameraSystemTest {
    @AfterTest
    fun resetInput() {
        Input.clearKeys()
        Input.setPointer(down = false, x = 0f, y = 0f)
    }

    private fun newCamera() = Camera(
        camera = CoreCamera(eye = Vec3(0f, 0f, 0f), center = Vec3(0f, 0f, 0f), fovYRadians = 1f, near = 0.1f, far = 100f)
    )

    @Test
    fun cameraAlwaysLooksAtTarget() {
        val world = World()
        val target = Transform(position = Vec3(2f, 0f, 3f))
        val camera = newCamera()
        val system = OrbitCameraSystem(target, camera, initialDistance = 5f)

        system.update(world, 0f)

        assertEquals(2f, camera.camera.center.x)
        assertEquals(0f, camera.camera.center.y)
        assertEquals(3f, camera.camera.center.z)
    }

    @Test
    fun noDragOrKeysLeavesYawPitchUnchangedAcrossFrames() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val camera = newCamera()
        val system = OrbitCameraSystem(target, camera, initialDistance = 5f)

        system.update(world, 0f)
        val firstEye = Vec3(camera.camera.eye.x, camera.camera.eye.y, camera.camera.eye.z)
        system.update(world, 0f)

        assertEquals(firstEye.x, camera.camera.eye.x)
        assertEquals(firstEye.y, camera.camera.eye.y)
        assertEquals(firstEye.z, camera.camera.eye.z)
    }

    @Test
    fun keyDownDecreasesDistanceKeyUpIncreasesIt() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val camera = newCamera()
        val system = OrbitCameraSystem(target, camera, initialDistance = 10f, zoomSpeed = 2f)

        // 'W' zooms in -- distance shrinks, so the eye moves closer to the target (smaller
        // distance from origin, since the camera starts directly on the +Z axis at pitch's
        // default, non-zero value keeps this a strict distance check via length, not a
        // single axis).
        Input.setKeyDown(Key.W, true)
        system.update(world, 1f)
        val distanceAfterZoomIn = camera.camera.eye.length3()

        Input.clearKeys()
        Input.setKeyDown(Key.S, true)
        system.update(world, 1f)
        val distanceAfterZoomOut = camera.camera.eye.length3()

        assert(distanceAfterZoomOut > distanceAfterZoomIn) {
            "expected zoom-out distance ($distanceAfterZoomOut) > zoom-in distance ($distanceAfterZoomIn)"
        }
    }

    @Test
    fun dragRotatesCameraAroundTarget() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val camera = newCamera()
        val system = OrbitCameraSystem(target, camera, initialDistance = 5f)

        // First frame with the pointer down only records the drag origin, no rotation yet.
        Input.setPointer(down = true, x = 100f, y = 100f)
        system.update(world, 0f)
        val eyeBeforeDrag = Vec3(camera.camera.eye.x, camera.camera.eye.y, camera.camera.eye.z)

        Input.setPointer(down = true, x = 150f, y = 100f)
        system.update(world, 0f)

        assert(camera.camera.eye.x != eyeBeforeDrag.x || camera.camera.eye.z != eyeBeforeDrag.z) {
            "expected dragging to change the eye position"
        }
        // Still the same distance from the target -- dragging orbits, doesn't zoom.
        val distanceBefore = eyeBeforeDrag.length3()
        val distanceAfter = camera.camera.eye.length3()
        assert(kotlin.math.abs(distanceBefore - distanceAfter) < 1e-3f) {
            "expected orbit distance to stay constant: before=$distanceBefore after=$distanceAfter"
        }
    }
}
