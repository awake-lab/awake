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
        Input.pointerCapturedByUi = false
        Input.scrollDeltaY = 0f
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
    fun pinchScrollDollysEyeAlongForwardAndIsConsumed() {
        val world = World()
        val camera = newCamera()
        val system = FreeFlyCameraSystem(camera, pinchZoomSpeed = 2f)

        // Default yaw/pitch (both 0) points forward along -Z (same convention as
        // forwardKeyMovesEyeAlongLookDirection).
        Input.scrollDeltaY = 3f
        system.update(world, 0f)

        assertEquals(0f, camera.camera.eye.x, absoluteTolerance = 1e-4f)
        assertEquals(0f, camera.camera.eye.y, absoluteTolerance = 1e-4f)
        assertEquals(-6f, camera.camera.eye.z, absoluteTolerance = 1e-4f)

        // Consumed -- a leftover delta must not double-apply on the next frame.
        val eyeZAfterConsuming = camera.camera.eye.z
        system.update(world, 0f)
        assertEquals(eyeZAfterConsuming, camera.camera.eye.z, absoluteTolerance = 1e-4f)
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

    @Test
    fun pointerCapturedByUiSuppressesLookButNotWhenReleased() {
        val world = World()
        val camera = newCamera()
        val system = FreeFlyCameraSystem(camera)

        // A UI widget has claimed the pointer -- the same drag motion that would otherwise
        // change look direction (see dragChangesLookDirection) must be suppressed.
        Input.pointerCapturedByUi = true
        Input.setPointer(down = true, x = 100f, y = 100f)
        system.update(world, 0f)
        val centerBeforeDrag = Vec3(camera.camera.center.x, camera.camera.center.y, camera.camera.center.z)

        Input.setPointer(down = true, x = 150f, y = 140f)
        system.update(world, 0f)

        assertEquals(centerBeforeDrag.x, camera.camera.center.x, absoluteTolerance = 1e-6f)
        assertEquals(centerBeforeDrag.z, camera.camera.center.z, absoluteTolerance = 1e-6f)

        // Release the UI's claim -- the same kind of drag now DOES change look direction.
        Input.pointerCapturedByUi = false
        Input.setPointer(down = true, x = 150f, y = 140f)
        system.update(world, 0f)
        val centerAfterRelease = Vec3(camera.camera.center.x, camera.camera.center.y, camera.camera.center.z)

        Input.setPointer(down = true, x = 200f, y = 180f)
        system.update(world, 0f)

        assert(camera.camera.center.x != centerAfterRelease.x || camera.camera.center.z != centerAfterRelease.z) {
            "expected drag to change look direction once pointerCapturedByUi is released"
        }
    }

    @Test
    fun setOrientationMakesForwardPointOppositeOrbitEyeOffset() {
        // Simulates CubeDemo's ORBIT -> FREE_FLY handoff: an orbit camera sitting at some
        // yaw/pitch offset from a target has an eye-to-target direction that is the exact
        // negation of its own target-to-eye offset. setOrientation(-orbitYaw, -orbitPitch)
        // should make FreeFlyCameraSystem's forward vector equal that eye-to-target
        // direction, so the handoff doesn't visibly snap the look direction.
        val world = World()
        val orbitYaw = 0.7f
        val orbitPitch = 0.3f
        val cosPitch = kotlin.math.cos(orbitPitch)
        // Orbit's target-to-eye offset direction (see OrbitCameraSystem.update()).
        val offsetX = cosPitch * kotlin.math.sin(orbitYaw)
        val offsetY = kotlin.math.sin(orbitPitch)
        val offsetZ = cosPitch * kotlin.math.cos(orbitYaw)

        val camera = newCamera()
        val system = FreeFlyCameraSystem(camera)
        system.setOrientation(-orbitYaw, -orbitPitch)
        system.update(world, 0f)

        val eye = camera.camera.eye
        val center = camera.camera.center
        val forwardX = center.x - eye.x
        val forwardY = center.y - eye.y
        val forwardZ = center.z - eye.z

        assertEquals(-offsetX, forwardX, absoluteTolerance = 1e-4f)
        assertEquals(-offsetY, forwardY, absoluteTolerance = 1e-4f)
        assertEquals(-offsetZ, forwardZ, absoluteTolerance = 1e-4f)
    }

    private fun assertEquals(expected: Float, actual: Float, absoluteTolerance: Float) {
        assert(kotlin.math.abs(expected - actual) <= absoluteTolerance) {
            "expected $expected, got $actual (tolerance $absoluteTolerance)"
        }
    }
}
