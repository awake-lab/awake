package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.TransformSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
