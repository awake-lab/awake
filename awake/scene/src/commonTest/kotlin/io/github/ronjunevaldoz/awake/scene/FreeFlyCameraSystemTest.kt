// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FreeFlyCameraSystemTest {
    @AfterTest
    fun resetInput() {
        Input.clearKeys()
        Input.setPointer(down = false, x = 0f, y = 0f)
    }

    private fun newCamera() = Camera(
        camera = CoreCamera(eye = Vec3(0f, 0f, 0f), center = Vec3(0f, 0f, -1f), fovYRadians = 1f, near = 0.1f, far = 100f)
    )

    @Test
    fun noInputLeavesEyeUnchanged() {
        val world = World()
        val camera = newCamera()
        val system = FreeFlyCameraSystem(camera)

        system.update(world, 1f)

        assertEquals(0f, camera.camera.eye.x)
        assertEquals(0f, camera.camera.eye.y)
        assertEquals(0f, camera.camera.eye.z)
    }

    @Test
    fun forwardKeyMovesEyeAlongLookDirection() {
        val world = World()
        val camera = newCamera()
        val system = FreeFlyCameraSystem(camera, moveSpeed = 2f)

        Input.setKeyDown(Key.W, true)
        system.update(world, 1f)

        // Default yaw/pitch (both 0) points forward along -Z (matches this codebase's
        // existing camera convention, e.g. CameraTest's identity-view setup).
        assertEquals(0f, camera.camera.eye.x, absoluteTolerance = 1e-4f)
        assertEquals(0f, camera.camera.eye.y, absoluteTolerance = 1e-4f)
        assertEquals(-2f, camera.camera.eye.z, absoluteTolerance = 1e-4f)
    }

    @Test
    fun centerStaysOneUnitAheadOfEyeAfterMoving() {
        val world = World()
        val camera = newCamera()
        val system = FreeFlyCameraSystem(camera, moveSpeed = 3f)

        Input.setKeyDown(Key.D, true)
        system.update(world, 1f)

        val eye = camera.camera.eye
        val center = camera.camera.center
        val dx = center.x - eye.x
        val dy = center.y - eye.y
        val dz = center.z - eye.z
        val lookDistance = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        assertEquals(1f, lookDistance, absoluteTolerance = 1e-4f)
    }

    @Test
    fun dragChangesLookDirection() {
        val world = World()
        val camera = newCamera()
        val system = FreeFlyCameraSystem(camera)

        // First frame with the pointer down only records the drag origin, no rotation yet.
        Input.setPointer(down = true, x = 100f, y = 100f)
        system.update(world, 0f)
        val centerBeforeDrag = Vec3(camera.camera.center.x, camera.camera.center.y, camera.camera.center.z)

        Input.setPointer(down = true, x = 150f, y = 100f)
        system.update(world, 0f)

        assert(camera.camera.center.x != centerBeforeDrag.x || camera.camera.center.z != centerBeforeDrag.z) {
            "expected dragging to change the look direction"
        }
    }

    private fun assertEquals(expected: Float, actual: Float, absoluteTolerance: Float) {
        assert(kotlin.math.abs(expected - actual) <= absoluteTolerance) {
            "expected $expected, got $actual (tolerance $absoluteTolerance)"
        }
    }
}
