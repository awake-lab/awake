// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.MovementControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.MatrixRelativeMovementSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/**
 * Guards the defect that made the camera lose its subject: the movement basis was built from
 * an un-normalized `center - eye`, so walking speed silently scaled with the camera's distance
 * to its target. The player then outran the smoothed follow camera and left the frame.
 */
class CameraRelativeMovementTest {
    @Test
    fun forwardSpeedIsIndependentOfCameraDistance() {
        val near = distanceTravelledForward(cameraDistance = 2f)
        val far = distanceTravelledForward(cameraDistance = 50f)

        assertEquals(near, far, ABSOLUTE_TOLERANCE)
        // speed * delta, i.e. the basis really is unit length rather than merely consistent.
        assertEquals(SPEED * DELTA, far, ABSOLUTE_TOLERANCE)
    }

    @Test
    fun strafeAndForwardMoveAtTheSameSpeed() {
        val forward = distanceTravelledForward(cameraDistance = 10f)
        val strafe = travel(cameraDistance = 10f) { moveX = 1f }.let { kotlin.math.abs(it.x) }

        assertEquals(forward, strafe, ABSOLUTE_TOLERANCE)
    }

    @Test
    fun movementFollowsWhereTheCameraLooks() {
        // Camera parked on +X looking back toward the origin, so "forward" is -X, not -Z.
        val moved = travel(cameraDistance = 10f, eye = Vec3(10f, 0f, 0f)) { moveZ = 1f }

        assertTrue(moved.x < -ABSOLUTE_TOLERANCE, "expected to move along -X, got $moved")
        assertEquals(0f, moved.z, ABSOLUTE_TOLERANCE)
    }

    private fun distanceTravelledForward(cameraDistance: Float): Float =
        kotlin.math.abs(travel(cameraDistance) { moveZ = 1f }.z)

    /** Runs one movement step and returns how far the subject moved. */
    private fun travel(
        cameraDistance: Float,
        eye: Vec3 = Vec3(0f, 0f, cameraDistance),
        intent: MovementControl.() -> Unit,
    ): Vec3 {
        val world = World()

        val cameraEntity = world.create()
        world.add(
            cameraEntity,
            Camera(
                CoreCamera.perspective(eye = eye, center = Vec3.ZERO),
            ),
        )
        world.add(cameraEntity, ActiveCamera())

        val subject = world.create()
        world.add(subject, Transform())
        world.add(subject, MovementControl().apply(intent))

        MatrixRelativeMovementSystem(speed = SPEED).update(world, DELTA)

        return world.get(subject, Transform::class)!!.position
    }

    private companion object {
        const val SPEED = 5f
        const val DELTA = 0.1f
        const val ABSOLUTE_TOLERANCE = 0.0001f
    }
}
