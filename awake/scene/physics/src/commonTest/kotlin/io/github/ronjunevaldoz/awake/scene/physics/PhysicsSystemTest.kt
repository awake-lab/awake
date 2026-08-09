// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.physics

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.physics.BodyHandle
import io.github.ronjunevaldoz.awake.physics.BodyTransform
import io.github.ronjunevaldoz.awake.physics.MotionType
import io.github.ronjunevaldoz.awake.physics.PhysicsShape
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.physics.RaycastHit
import io.github.ronjunevaldoz.awake.physics.SphereShape
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.physics.components.PhysicsBody
import io.github.ronjunevaldoz.awake.scene.physics.systems.PhysicsSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PhysicsSystemTest {
    /** Pure ECS-wiring fake -- no real simulation, just records calls and hands back a
     * scripted [syncTransforms] result so this test doesn't need jolt-jni. */
    private class FakePhysicsWorld : PhysicsWorld {
        var createBodyCallCount = 0
            private set
        var stepCallCount = 0
            private set
        var syncTransformsCallCount = 0
            private set
        private var nextHandleId = 0L
        var scriptedTransforms: List<BodyTransform> = emptyList()

        override fun createBody(
            shape: PhysicsShape,
            position: Vec3,
            rotation: Vec3,
            motionType: MotionType,
        ): BodyHandle {
            createBodyCallCount++
            return BodyHandle(nextHandleId++)
        }

        override fun destroyBody(handle: BodyHandle) = Unit

        override fun step(deltaTime: Float) {
            stepCallCount++
        }

        override fun syncTransforms(): List<BodyTransform> {
            syncTransformsCallCount++
            return scriptedTransforms
        }

        override fun raycast(origin: Vec3, direction: Vec3, maxDistance: Float): RaycastHit? = null

        override fun destroy() = Unit
    }

    @Test
    fun createsBodyOnceLazilyAndSyncsTransformBackToEntity() {
        val world = World()
        val entity = world.create()
        val transform = Transform(position = Vec3(0f, 10f, 0f))
        world.add(entity, transform)
        val physicsBody =
            PhysicsBody(shape = SphereShape(radius = 1f), motionType = MotionType.DYNAMIC)
        world.add(entity, physicsBody)
        val physicsWorld = FakePhysicsWorld()
        val system = PhysicsSystem(physicsWorld)

        system.update(world, 1f / 60f)

        assertEquals(1, physicsWorld.createBodyCallCount)
        assertEquals(1, physicsWorld.stepCallCount)
        assertEquals(1, physicsWorld.syncTransformsCallCount)
        val handle = physicsBody.handle
        assertNotNull(handle, "expected PhysicsSystem to assign a handle after creating the body")

        // Second update must not create a second body for the same entity.
        system.update(world, 1f / 60f)
        assertEquals(1, physicsWorld.createBodyCallCount)
        assertEquals(2, physicsWorld.stepCallCount)
        assertEquals(2, physicsWorld.syncTransformsCallCount)
        assertEquals(handle, physicsBody.handle)

        physicsWorld.scriptedTransforms = listOf(
            BodyTransform(
                handle = handle,
                position = Vec3(1f, 2f, 3f),
                rotation = Vec3(0f, 0.5f, 0f),
            ),
        )
        system.update(world, 1f / 60f)

        assertEquals(1f, transform.position.x)
        assertEquals(2f, transform.position.y)
        assertEquals(3f, transform.position.z)
        assertEquals(0.5f, transform.rotation.y)
    }

    @Test
    fun ignoresSyncedTransformsForUnknownHandles() {
        val world = World()
        val entity = world.create()
        world.add(entity, Transform(position = Vec3(0f, 0f, 0f)))
        world.add(
            entity,
            PhysicsBody(shape = SphereShape(radius = 1f), motionType = MotionType.STATIC),
        )
        val physicsWorld = FakePhysicsWorld()
        physicsWorld.scriptedTransforms = listOf(
            BodyTransform(
                handle = BodyHandle(999L),
                position = Vec3(5f, 5f, 5f),
                rotation = Vec3(0f, 0f, 0f),
            ),
        )
        val system = PhysicsSystem(physicsWorld)

        system.update(world, 1f / 60f)

        val transform = world.get<Transform>(entity)!!
        assertTrue(transform.position.x == 0f && transform.position.y == 0f && transform.position.z == 0f)
    }
}
