package io.github.ronjunevaldoz.awake.ecs

import kotlin.test.Test
import kotlin.test.assertEquals

class GeneralFamilyTest {
    @Test
    fun allMatchesEntitiesWithEveryRequestedComponentIncludingThreeTypes() {
        val world = World()
        val allThree = world.create()
        val missingOne = world.create()
        world.add(allThree, ComponentA)
        world.add(allThree, ComponentB)
        world.add(allThree, ComponentC)
        world.add(missingOne, ComponentA)
        world.add(missingOne, ComponentB)

        val family = world.family { all(ComponentA::class, ComponentB::class, ComponentC::class) }

        assertEquals(setOf(allThree), family.entitySet())
    }

    @Test
    fun oneMatchesEntitiesWithAtLeastOneRequestedComponent() {
        val world = World()
        val hasA = world.create()
        val hasB = world.create()
        val hasNeither = world.create()
        world.add(hasA, ComponentA)
        world.add(hasB, ComponentB)

        val family = world.family { one(ComponentA::class, ComponentB::class) }

        assertEquals(setOf(hasA, hasB), family.entitySet())
        assertEquals(false, family.entitySet().contains(hasNeither))
    }

    @Test
    fun excludeRejectsEntitiesWithTheExcludedComponent() {
        val world = World()
        val plain = world.create()
        val excluded = world.create()
        world.add(plain, ComponentA)
        world.add(excluded, ComponentA)
        world.add(excluded, ComponentC)

        val family = world.family { all(ComponentA::class); exclude(ComponentC::class) }

        assertEquals(setOf(plain), family.entitySet())
    }

    @Test
    fun familyStaysInSyncAsComponentsAreAddedAndRemoved() {
        val world = World()
        val entity = world.create()
        world.add(entity, ComponentA)

        val family = world.family { all(ComponentA::class, ComponentB::class) }
        assertEquals(emptySet(), family.entitySet())

        world.add(entity, ComponentB)
        assertEquals(setOf(entity), family.entitySet())

        world.remove<ComponentB>(entity)
        assertEquals(emptySet(), family.entitySet())
    }

    @Test
    fun familyDropsEntityOnceItGainsAnExcludedComponent() {
        val world = World()
        val entity = world.create()
        world.add(entity, ComponentA)

        val family = world.family { all(ComponentA::class); exclude(ComponentC::class) }
        assertEquals(setOf(entity), family.entitySet())

        world.add(entity, ComponentC)
        assertEquals(emptySet(), family.entitySet())

        world.remove<ComponentC>(entity)
        assertEquals(setOf(entity), family.entitySet())
    }

    @Test
    fun familyRemovesDestroyedEntities() {
        val world = World()
        val entity = world.create()
        world.add(entity, ComponentA)

        val family = world.family { all(ComponentA::class) }
        assertEquals(setOf(entity), family.entitySet())

        world.destroy(entity)
        assertEquals(emptySet(), family.entitySet())
    }
}

private data object ComponentA
private data object ComponentB
private data object ComponentC

private fun Family.entitySet(): Set<Entity> {
    val result = mutableSetOf<Entity>()
    forEach { entity -> result += entity }
    return result
}
