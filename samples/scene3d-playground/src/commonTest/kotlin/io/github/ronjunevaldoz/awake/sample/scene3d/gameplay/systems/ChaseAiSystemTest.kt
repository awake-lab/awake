// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.gameplay.systems

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.navigation.NavMesh
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChaseAiSystemTest {
    private class FakeNavMesh(private val path: List<Vec3>) : NavMesh {
        var findPathCallCount = 0
            private set

        override fun findPath(start: Vec3, end: Vec3): List<Vec3> {
            findPathCallCount++
            return path
        }
    }

    @Test
    fun stepsTowardEachWaypointInTurn() {
        val world = World()
        val npcTransform = Transform(position = Vec3(0f, 0f, 0f))
        val targetTransform = Transform(position = Vec3(10f, 0f, 0f))
        val navMesh = FakeNavMesh(listOf(Vec3(1f, 0f, 0f), Vec3(2f, 0f, 0f)))
        // repathInterval huge so the fixed path above isn't reset mid-test.
        val system = ChaseAiSystem(
            npcTransform,
            targetTransform,
            navMesh,
            speed = 1f,
            repathInterval = 100f,
            waypointRadius = 0.1f,
        )

        system.update(world, 0.1f)
        assertEquals(1, navMesh.findPathCallCount)
        assertTrue(
            npcTransform.position.x > 0f,
            "Expected the NPC to step toward the first waypoint.",
        )

        repeat(30) { system.update(world, 0.1f) }
        assertTrue(
            npcTransform.position.x > 1.1f,
            "Expected the NPC to pass the first waypoint (1,0,0) and head toward the " +
                "second (2,0,0) by now (x=${npcTransform.position.x}).",
        )
    }

    @Test
    fun repathsAfterIntervalElapses() {
        val world = World()
        val npcTransform = Transform(position = Vec3(0f, 0f, 0f))
        val targetTransform = Transform(position = Vec3(10f, 0f, 0f))
        val navMesh = FakeNavMesh(listOf(Vec3(1f, 0f, 0f)))
        val system = ChaseAiSystem(npcTransform, targetTransform, navMesh, repathInterval = 0.5f)

        system.update(world, 0.1f)
        assertEquals(1, navMesh.findPathCallCount)

        system.update(world, 0.5f)
        assertEquals(2, navMesh.findPathCallCount)
    }

    @Test
    fun doesNothingWhenNoPathExists() {
        val world = World()
        val npcTransform = Transform(position = Vec3(0f, 0f, 0f))
        val targetTransform = Transform(position = Vec3(10f, 0f, 0f))
        val system = ChaseAiSystem(npcTransform, targetTransform, FakeNavMesh(emptyList()))

        system.update(world, 0.1f)

        assertEquals(0f, npcTransform.position.x)
    }
}
