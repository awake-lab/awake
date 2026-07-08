package io.github.ronjunevaldoz.awake.ecs

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.components.Light
import io.github.ronjunevaldoz.awake.ecs.components.Transform
import io.github.ronjunevaldoz.awake.ecs.systems.TransformSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorldTest {
    @Test
    fun recycledEntityKeepsSameIdButGetsNewGeneration() {
        val world = World()
        val first = world.create()

        assertTrue(world.destroy(first))
        val second = world.create()

        assertEquals(first.id, second.id)
        assertNotEquals(first.generation, second.generation)
        assertFalse(world.isAlive(first))
        assertTrue(world.isAlive(second))
    }

    @Test
    fun componentsCanBeAddedReplacedAndRemoved() {
        val world = World()
        val entity = world.create()
        val first = Light(intensity = 1f)
        val second = Light(intensity = 2f)

        assertNull(world.add(entity, first))
        assertEquals(first, world.get<Light>(entity))
        assertEquals(first, world.add(entity, second))
        assertEquals(second, world.get<Light>(entity))
        assertEquals(second, world.remove<Light>(entity))
        assertNull(world.get<Light>(entity))
    }

    @Test
    fun queryReturnsOnlyEntitiesWithAllRequestedComponents() {
        val world = World()
        val transformOnly = world.create()
        val renderable = world.create()

        world.add(transformOnly, Transform())
        world.add(renderable, Transform())
        world.add(renderable, Light())

        assertEquals(listOf(transformOnly, renderable), world.query(Transform::class))
        assertEquals(listOf(renderable), world.query(Transform::class, Light::class))
    }

    @Test
    fun cachedQueryUpdatesAfterComponentAndEntityChanges() {
        val world = World()
        val first = world.create()
        val second = world.create()
        world.add(first, Transform())

        assertEquals(listOf(first), world.query(Transform::class))

        world.add(second, Transform())
        assertEquals(listOf(first, second), world.query(Transform::class))

        world.remove<Transform>(first)
        assertEquals(listOf(second), world.query(Transform::class))

        world.destroy(second)
        assertEquals(emptyList(), world.query(Transform::class))
    }

    @Test
    fun queryEachSkipsDestroyedEntities() {
        val world = World()
        val destroyed = world.create()
        val alive = world.create()
        world.add(destroyed, Transform())
        world.add(destroyed, Light())
        world.add(alive, Transform())
        world.add(alive, Light())

        world.destroy(destroyed)

        val visited = mutableListOf<Entity>()
        world.queryEach<Transform, Light> { entity, _, _ ->
            visited += entity
        }

        assertEquals(listOf(alive), visited)
    }

    @Test
    fun destroyingEntityRemovesComponentsAndRejectsStaleHandle() {
        val world = World()
        val entity = world.create()
        world.add(entity, Transform())

        assertTrue(world.destroy(entity))

        assertNull(world.get<Transform>(entity))
        assertEquals(emptyList(), world.query(Transform::class))
        assertFalse(world.destroy(entity))
    }

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
}
