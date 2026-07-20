// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.MovementControl
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.PlayerMovementSystem
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * PlayerMovementSystem is now decoupled from hardware input -- it only reads intent already
 * accumulated onto [MovementControl] (by PlayerControlSystem, in a real frame; see
 * PlayerControlSystemTest for that half). These tests drive [MovementControl] directly.
 */
class PlayerMovementSystemTest {
    @Test
    fun movementControlMovesPlayerOnXzPlane() {
        val world = World()
        val transform = Transform(position = Vec3(0f, 1f, 0f))
        val entity = world.create()
        world.add(entity, transform)
        val control = MovementControl().apply { moveX = 1f; moveZ = 1f }
        world.add(entity, control)
        val system = PlayerMovementSystem(transform, speed = 2f)

        system.update(world, 0.5f)

        assertEquals(1f, transform.position.x)
        assertEquals(1f, transform.position.y) // untouched
        assertEquals(1f, transform.position.z)
    }

    @Test
    fun noMovementControlLeavesPositionUnchanged() {
        val world = World()
        val transform = Transform(position = Vec3(5f, 0f, 5f))
        val entity = world.create()
        world.add(entity, transform)
        world.add(entity, MovementControl())
        val system = PlayerMovementSystem(transform)

        system.update(world, 1f)

        assertEquals(5f, transform.position.x)
        assertEquals(5f, transform.position.z)
    }

    @Test
    fun onlyTheTrackedTransformMoves() {
        val world = World()
        val trackedTransform = Transform(position = Vec3(0f, 0f, 0f))
        val trackedEntity = world.create()
        world.add(trackedEntity, trackedTransform)
        world.add(trackedEntity, MovementControl())

        val otherTransform = Transform(position = Vec3(0f, 0f, 0f))
        val otherEntity = world.create()
        world.add(otherEntity, otherTransform)
        world.add(otherEntity, MovementControl().apply { moveX = 1f; moveZ = 1f })

        val system = PlayerMovementSystem(trackedTransform, speed = 2f)
        system.update(world, 1f)

        assertEquals(0f, trackedTransform.position.x)
        assertEquals(0f, trackedTransform.position.z)
        // Confirms the system really is scoped to trackedTransform, not "whichever entity
        // has a MovementControl" -- otherEntity's nonzero control must have no effect here.
        assertEquals(0f, otherTransform.position.x)
        assertEquals(0f, otherTransform.position.z)
    }
}
