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
        Input.pointerCapturedByUi = false
        Input.scrollDeltaY = 0f
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
    fun pinchScrollDecreasesDistanceAndIsConsumed() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val camera = newCamera()
        val system = OrbitCameraSystem(target, camera, initialDistance = 10f, pinchZoomSpeed = 2f)

        Input.scrollDeltaY = 3f
        system.update(world, 0f)

        assertEquals(4f, system.distance)
        // Consumed -- a leftover delta must not double-apply on the next frame with no new
        // scroll input.
        val distanceAfterConsuming = system.distance
        system.update(world, 0f)
        assertEquals(distanceAfterConsuming, system.distance)
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

    @Test
    fun pointerCapturedByUiSuppressesDragButNotWhenReleased() {
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val camera = newCamera()
        val system = OrbitCameraSystem(target, camera, initialDistance = 5f)

        // A UI widget (e.g. a slider) has claimed the pointer this "frame" -- the same drag
        // motion that would otherwise orbit the camera (see dragRotatesCameraAroundTarget)
        // must NOT change yaw/pitch while Input.pointerCapturedByUi is true.
        Input.pointerCapturedByUi = true
        Input.setPointer(down = true, x = 100f, y = 100f)
        system.update(world, 0f)
        val yawBefore = system.yaw
        val pitchBefore = system.pitch

        Input.setPointer(down = true, x = 150f, y = 140f)
        system.update(world, 0f)

        assertEquals(yawBefore, system.yaw, "yaw must not change while pointer is captured by UI")
        assertEquals(pitchBefore, system.pitch, "pitch must not change while pointer is captured by UI")

        // Release the UI's claim -- the exact same kind of drag now DOES rotate the camera,
        // proving the suppression above was specifically caused by pointerCapturedByUi, not
        // some other unrelated state change.
        Input.pointerCapturedByUi = false
        Input.setPointer(down = true, x = 150f, y = 140f)
        system.update(world, 0f)
        val yawAfterRelease = system.yaw
        val pitchAfterRelease = system.pitch

        Input.setPointer(down = true, x = 200f, y = 180f)
        system.update(world, 0f)

        assert(system.yaw != yawAfterRelease || system.pitch != pitchAfterRelease) {
            "expected drag to change yaw/pitch once pointerCapturedByUi is released: " +
                "yaw before=$yawAfterRelease after=${system.yaw}, pitch before=$pitchAfterRelease after=${system.pitch}"
        }
    }

    @Test
    fun pointerCapturedByUiAlsoSuppressesAutoRotate() {
        // Regression test for a bug the previous pointerCapturedByUiSuppressesDragButNotWhenReleased
        // test missed entirely, since it used the default autoRotateSpeed = 0f (a no-op):
        // dragging a UI slider (which sets pointerCapturedByUi = true) still visibly spun the
        // camera in CubeDemo, which constructs this system with a non-zero autoRotateSpeed --
        // the old `else` branch ran auto-rotate whenever the drag path wasn't active, which
        // included "pointer down but claimed by UI," not just "pointer genuinely idle."
        val world = World()
        val target = Transform(position = Vec3(0f, 0f, 0f))
        val camera = newCamera()
        val system = OrbitCameraSystem(target, camera, initialDistance = 5f, autoRotateSpeed = 1f)

        Input.pointerCapturedByUi = true
        Input.setPointer(down = true, x = 100f, y = 100f)
        system.update(world, 1f)
        val yawAfterFirstFrame = system.yaw
        system.update(world, 1f)

        assertEquals(yawAfterFirstFrame, system.yaw, "yaw must not auto-rotate while pointer is captured by UI")

        // Once genuinely idle (no drag, not captured), auto-rotate resumes.
        Input.pointerCapturedByUi = false
        Input.setPointer(down = false, x = 100f, y = 100f)
        system.update(world, 1f)

        assert(system.yaw != yawAfterFirstFrame) {
            "expected auto-rotate to resume once the pointer is idle and no longer captured by UI"
        }
    }
}
