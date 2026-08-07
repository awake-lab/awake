// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

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
        val first = TestComponent(value = 1)
        val second = TestComponent(value = 2)

        assertNull(world.add(entity, first))
        assertEquals(first, world.get<TestComponent>(entity))
        assertEquals(first, world.add(entity, second))
        assertEquals(second, world.get<TestComponent>(entity))
        assertEquals(second, world.remove<TestComponent>(entity))
        assertNull(world.get<TestComponent>(entity))
    }

    @Test
    fun queryReturnsOnlyEntitiesWithAllRequestedComponents() {
        val world = World()
        val oneComponent = world.create()
        val twoComponents = world.create()

        world.add(oneComponent, TestComponent())
        world.add(twoComponents, TestComponent())
        world.add(twoComponents, MarkerComponent)

        assertEquals(listOf(oneComponent, twoComponents), world.query(TestComponent::class))
        assertEquals(listOf(twoComponents), world.query(TestComponent::class, MarkerComponent::class))
    }

    @Test
    fun cachedQueryUpdatesAfterComponentAndEntityChanges() {
        val world = World()
        val first = world.create()
        val second = world.create()
        world.add(first, TestComponent())

        assertEquals(listOf(first), world.query(TestComponent::class))

        world.add(second, TestComponent())
        assertEquals(listOf(first, second), world.query(TestComponent::class))

        world.remove<TestComponent>(first)
        assertEquals(listOf(second), world.query(TestComponent::class))

        world.destroy(second)
        assertEquals(emptyList(), world.query(TestComponent::class))
    }

    @Test
    fun queryEachSkipsDestroyedEntities() {
        val world = World()
        val destroyed = world.create()
        val alive = world.create()
        world.add(destroyed, TestComponent())
        world.add(destroyed, MarkerComponent)
        world.add(alive, TestComponent())
        world.add(alive, MarkerComponent)

        world.destroy(destroyed)

        val visited = mutableListOf<Entity>()
        world.queryEach<TestComponent, MarkerComponent> { entity, _, _ ->
            visited += entity
        }

        assertEquals(listOf(alive), visited)
    }

    @Test
    fun familyUpdatesAfterStructuralChanges() {
        val world = World()
        val first = world.create()
        val second = world.create()
        world.add(first, TestComponent(value = 1))
        world.add(first, MarkerComponent)

        val family = world.family<TestComponent, MarkerComponent>()
        assertEquals(listOf(first to 1), family.values())

        world.add(second, TestComponent(value = 2))
        assertEquals(listOf(first to 1), family.values())

        world.add(second, MarkerComponent)
        assertEquals(setOf(first to 1, second to 2), family.values().toSet())

        world.add(second, TestComponent(value = 3))
        assertEquals(setOf(first to 1, second to 3), family.values().toSet())
        assertEquals(listOf(1, 3), family.componentValues().sorted())

        world.remove<MarkerComponent>(first)
        assertEquals(listOf(second to 3), family.values())

        world.destroy(second)
        assertEquals(emptyList(), family.values())
    }

    @Test
    fun destroyingEntityRemovesComponentsAndRejectsStaleHandle() {
        val world = World()
        val entity = world.create()
        world.add(entity, TestComponent())

        assertTrue(world.destroy(entity))

        assertNull(world.get<TestComponent>(entity))
        assertEquals(emptyList(), world.query(TestComponent::class))
        assertFalse(world.destroy(entity))
    }
}

private data class TestComponent(
    val value: Int = 0,
)

private data object MarkerComponent

private fun Family2<TestComponent, MarkerComponent>.values(): List<Pair<Entity, Int>> {
    val values = mutableListOf<Pair<Entity, Int>>()
    forEach { entity, component, _ ->
        values += entity to component.value
    }
    return values
}

private fun Family2<TestComponent, MarkerComponent>.componentValues(): List<Int> {
    val values = mutableListOf<Int>()
    forEachComponents { component, _ ->
        values += component.value
    }
    return values
}
