// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics.jolt

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.physics.MotionType
import io.github.ronjunevaldoz.awake.physics.SphereShape
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Real, falsifiable proof the jolt-jni step/readback loop actually simulates gravity, not
 * just returning whatever position [io.github.ronjunevaldoz.awake.physics.PhysicsWorld.createBody]
 * was given back unchanged -- a dynamic body in free fall (no ground, nothing to collide
 * with) must have fallen by roughly the expected kinematic amount after a known number of
 * fixed-timestep updates. Physics under constant gravity with no other forces is
 * deterministic enough for a tolerance-based assertion.
 */
class JoltPhysicsWorldTest {
    @Test
    fun dynamicBodyFallsUnderGravity() {
        val gravity = -9.81f
        val world = JoltPhysicsWorld(gravity = Vec3(0f, gravity, 0f))
        try {
            val startY = 10f
            val handle = world.createBody(
                shape = SphereShape(radius = 0.5f),
                position = Vec3(0f, startY, 0f),
                rotation = Vec3(0f, 0f, 0f),
                motionType = MotionType.DYNAMIC,
            )

            val deltaTime = 1f / 60f
            val steps = 60
            repeat(steps) { world.step(deltaTime) }

            val transform = world.syncTransforms().single { it.handle == handle }
            val fallDistance = startY - transform.position.y

            // Semi-implicit ("symplectic") Euler integration over `steps` fixed ticks of
            // `deltaTime` gives fallDistance = g * dt^2 * steps * (steps + 1) / 2, which is
            // slightly larger than the continuous 0.5 * g * t^2 kinematic formula -- use a
            // generous +/-20% tolerance around that discrete estimate rather than assuming
            // Jolt's exact internal integration scheme.
            val expectedFallDistance = -gravity * deltaTime * deltaTime * steps * (steps + 1) / 2f
            assertTrue(
                abs(fallDistance - expectedFallDistance) < expectedFallDistance * 0.2f,
                "Expected the sphere to fall about $expectedFallDistance m over $steps steps " +
                    "of $deltaTime s each, but it fell $fallDistance m " +
                    "(from y=$startY to y=${transform.position.y}).",
            )
        } finally {
            world.destroy()
        }
    }
}
