// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.PlayerMovementSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerMovementSystemTest {
    @AfterTest
    fun resetInput() {
        Input.clearKeys()
        Input.setPointer(down = false, x = 0f, y = 0f)
    }

    @Test
    fun keyboardAxisMovesPlayerOnXzPlane() {
        val world = World()
        val transform = Transform(position = Vec3(0f, 1f, 0f))
        val system = PlayerMovementSystem(transform, speed = 2f)

        Input.setKeyDown(Key.D, true)
        Input.setKeyDown(Key.S, true)
        system.update(world, 0.5f)

        assertEquals(1f, transform.position.x)
        assertEquals(1f, transform.position.y) // untouched
        assertEquals(1f, transform.position.z)
    }

    @Test
    fun noInputLeavesPositionUnchanged() {
        val world = World()
        val transform = Transform(position = Vec3(5f, 0f, 5f))
        val system = PlayerMovementSystem(transform)

        system.update(world, 1f)

        assertEquals(5f, transform.position.x)
        assertEquals(5f, transform.position.z)
    }

    @Test
    fun touchDragMovesPlayerRelativeToDragOrigin() {
        val world = World()
        val transform = Transform(position = Vec3(0f, 0f, 0f))
        val system = PlayerMovementSystem(transform, speed = 1f)

        // First frame with the pointer down only records the drag origin, no movement yet.
        Input.setPointer(down = true, x = 100f, y = 100f)
        system.update(world, 1f)
        assertEquals(0f, transform.position.x)
        assertEquals(0f, transform.position.z)

        // Dragging 80px right (== DRAG_RADIUS) should yield a full +1 axis on x.
        Input.setPointer(down = true, x = 180f, y = 100f)
        system.update(world, 1f)
        assertEquals(1f, transform.position.x)
        assertEquals(0f, transform.position.z)
    }
}
