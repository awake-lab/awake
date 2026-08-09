// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.core

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.core.systems.TransformSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class TransformSystemTest {
    @Test
    fun transformSystemPropagatesParentsBeforeChildren() {
        val world = World()
        val root = world.create()
        val child = world.create()
        val grandchild = world.create()

        world.add(grandchild, Transform(position = Vec3(0f, 0f, 3f), parent = child))
        world.add(root, Transform(position = Vec3(1f, 0f, 0f)))
        world.add(child, Transform(position = Vec3(0f, 2f, 0f), parent = root))

        TransformSystem().update(world, 0f)

        val rootTransform = world.get<Transform>(root) ?: error("root missing")
        val childTransform = world.get<Transform>(child) ?: error("child missing")
        val grandchildTransform = world.get<Transform>(grandchild) ?: error("grandchild missing")
        assertEquals(1f, rootTransform.worldMatrix.m03)
        assertEquals(1f, childTransform.worldMatrix.m03)
        assertEquals(2f, childTransform.worldMatrix.m13)
        assertEquals(1f, grandchildTransform.worldMatrix.m03)
        assertEquals(2f, grandchildTransform.worldMatrix.m13)
        assertEquals(3f, grandchildTransform.worldMatrix.m23)
    }

    @Test
    fun transformSystemThrowsOnCyclicParenting() {
        val world = World()
        val a = world.create()
        val b = world.create()

        world.add(a, Transform(parent = b))
        world.add(b, Transform(parent = a))

        assertFailsWith<IllegalStateException> {
            TransformSystem().update(world, 0f)
        }
    }

    @Test
    fun transformPoolResetsAndReusesState() {
        val world = World()
        world.registerPool(Transform::class) { Transform() }

        val firstEntity = world.create()
        val first = world.add<Transform>(firstEntity)
        val parent = world.create()
        first.position.x = 4f
        first.rotation.y = 2f
        first.scale.z = 3f
        first.parent = parent
        first.worldMatrix.m03 = 9f

        val removed = world.remove<Transform>(firstEntity)
        val reused = world.add<Transform>(world.create())

        assertSame(first, removed)
        assertSame(first, reused)
        assertSame(first.position, reused.position)
        assertSame(first.rotation, reused.rotation)
        assertSame(first.scale, reused.scale)
        assertSame(first.worldMatrix, reused.worldMatrix)
        assertEquals(0f, reused.position.x)
        assertEquals(0f, reused.position.y)
        assertEquals(0f, reused.position.z)
        assertEquals(0f, reused.rotation.x)
        assertEquals(0f, reused.rotation.y)
        assertEquals(0f, reused.rotation.z)
        assertEquals(1f, reused.scale.x)
        assertEquals(1f, reused.scale.y)
        assertEquals(1f, reused.scale.z)
        assertEquals(null, reused.parent)
        assertEquals(1f, reused.worldMatrix.m00)
        assertEquals(1f, reused.worldMatrix.m11)
        assertEquals(1f, reused.worldMatrix.m22)
        assertEquals(1f, reused.worldMatrix.m33)
    }

    @Test
    fun transformSystemReusesInstanceStateAcrossMultipleUpdates() {
        val world = World()
        val root = world.create()
        val child = world.create()

        world.add(root, Transform(position = Vec3(1f, 0f, 0f)))
        world.add(child, Transform(position = Vec3(0f, 2f, 0f), parent = root))

        val system = TransformSystem()
        system.update(world, 0f)
        system.update(world, 0f)

        val childTransform = world.get<Transform>(child) ?: error("child missing")
        assertEquals(1f, childTransform.worldMatrix.m03)
        assertEquals(2f, childTransform.worldMatrix.m13)
    }
}
