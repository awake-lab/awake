// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.FreeFlyControl
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * FreeFlyCameraSystem is now decoupled from hardware input -- it only reads intent already
 * accumulated onto [FreeFlyControl] (by PlayerControlSystem, in a real frame; see
 * PlayerControlSystemTest for that half). These tests drive [FreeFlyControl] directly.
 */
class FreeFlyCameraSystemTest {
    private fun newCamera() = Camera(
        camera = CoreCamera(eye = Vec3(0f, 0f, 0f), center = Vec3(0f, 0f, -1f), fovYRadians = 1f, near = 0.1f, far = 100f)
    )

    private fun World.spawnFreeFlyCamera(control: FreeFlyControl = FreeFlyControl()): Pair<io.github.ronjunevaldoz.awake.ecs.Entity, FreeFlyControl> {
        val entity = create()
        add(entity, newCamera())
        add(entity, control)
        return entity to control
    }

    @Test
    fun noIntentLeavesEyeUnchanged() {
        val world = World()
        val (entity, _) = world.spawnFreeFlyCamera()
        val system = FreeFlyCameraSystem()

        system.update(world, 1f)

        val camera = world.get(entity, Camera::class)!!
        assertEquals(0f, camera.camera.eye.x)
        assertEquals(0f, camera.camera.eye.y)
        assertEquals(0f, camera.camera.eye.z)
    }

    @Test
    fun forwardMoveZMovesEyeAlongLookDirection() {
        val world = World()
        val (entity, control) = world.spawnFreeFlyCamera()
        val system = FreeFlyCameraSystem(moveSpeed = 2f)

        // Default yaw/pitch (both 0) points forward along -Z (matches this codebase's
        // existing camera convention, e.g. CameraTest's identity-view setup) -- the system's
        // own forward vector is (0, 0, -1) at identity, so a positive control.moveZ (not
        // negative) is what actually drives the eye toward -Z here; PlayerControlSystem is
        // the one that flips the raw W-key's -1 into FreeFlyControl.moveZ = +1 for this.
        control.moveZ = 1f
        system.update(world, 1f)

        val camera = world.get(entity, Camera::class)!!
        assertEquals(0f, camera.camera.eye.x, absoluteTolerance = 1e-4f)
        assertEquals(0f, camera.camera.eye.y, absoluteTolerance = 1e-4f)
        assertEquals(-2f, camera.camera.eye.z, absoluteTolerance = 1e-4f)
    }

    @Test
    fun centerStaysOneUnitAheadOfEyeAfterMoving() {
        val world = World()
        val (entity, control) = world.spawnFreeFlyCamera()
        val system = FreeFlyCameraSystem(moveSpeed = 3f)

        control.moveX = 1f
        system.update(world, 1f)

        val camera = world.get(entity, Camera::class)!!
        val eye = camera.camera.eye
        val center = camera.camera.center
        val dx = center.x - eye.x
        val dy = center.y - eye.y
        val dz = center.z - eye.z
        val lookDistance = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        assertEquals(1f, lookDistance, absoluteTolerance = 1e-4f)
    }

    @Test
    fun yawAndPitchDeltaApply() {
        val world = World()
        val (entity, control) = world.spawnFreeFlyCamera()
        val system = FreeFlyCameraSystem()

        control.yawDelta = 0.3f
        control.pitchDelta = 0.1f
        system.update(world, 0f)

        assertEquals(0.3f, control.yaw)
        assertEquals(0.1f, control.pitch)

        // Deltas apply again next frame if not reset by the caller (matches OrbitControl's
        // own contract: PlayerControlSystem, not this system, is responsible for resetting
        // them every real frame).
        system.update(world, 0f)
        assertEquals(0.6f, control.yaw, absoluteTolerance = 1e-5f)
    }

    @Test
    fun pitchClampsWithinNearlyVerticalBounds() {
        val world = World()
        val (_, control) = world.spawnFreeFlyCamera()
        val system = FreeFlyCameraSystem()

        control.pitchDelta = 100f
        system.update(world, 0f)

        assert(control.pitch < (PI_F / 2f)) { "pitch must clamp below vertical: ${control.pitch}" }
    }

    private companion object {
        const val PI_F = kotlin.math.PI.toFloat()
    }
}
