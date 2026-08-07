// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class EcsOptimizationTest {

    @Test
    fun componentsArePooledAndReused() {
        val world = World()
        world.registerPool(PooledComponent::class) { PooledComponent() }

        val entity1 = world.create()
        val comp1 = world.add<PooledComponent>(entity1)
        comp1.value = 42

        world.destroy(entity1) // Returns comp1 to pool

        val entity2 = world.create()
        val comp2 = world.add<PooledComponent>(entity2)

        // Verify it's the same instance and was reset
        assertSame(comp1, comp2)
        assertEquals(0, comp2.value)
    }

    @Test
    fun removedPooledComponentIsResetAndReused() {
        val world = World()
        world.registerPool(PooledComponent::class) { PooledComponent() }
        val entity = world.create()
        val component = world.add<PooledComponent>(entity)
        component.value = 42

        val removed = world.remove<PooledComponent>(entity)
        val nextEntity = world.create()
        val reused = world.add<PooledComponent>(nextEntity)

        assertSame(component, removed)
        assertEquals(0, removed?.value)
        assertSame(component, reused)
    }

    @Test
    fun pooledTypeIdFastPathIsResetAndReused() {
        val world = World()
        world.registerPool(PooledComponent::class) { PooledComponent() }
        world.store(PooledComponent::class)

        val typeId = world.typeId(PooledComponent::class)
        val firstEntity = world.create()
        val first = world.add<PooledComponent>(firstEntity, typeId)
        first.value = 42

        val removed = world.remove<PooledComponent>(firstEntity, typeId)
        val secondEntity = world.create()
        val reused = world.add<PooledComponent>(secondEntity, typeId)

        assertSame(first, removed)
        assertEquals(0, removed?.value)
        assertSame(first, reused)
    }

    @Test
    fun replacedPooledComponentIsResetAndReused() {
        val world = World()
        world.registerPool(PooledComponent::class) { PooledComponent() }
        val entity = world.create()
        val first = world.add<PooledComponent>(entity)
        first.value = 42
        val replacement = PooledComponent().also { it.value = 7 }

        val previous = world.add(entity, replacement)
        val nextEntity = world.create()
        val reused = world.add<PooledComponent>(nextEntity)

        assertSame(first, previous)
        assertEquals(0, first.value)
        assertSame(replacement, world.get<PooledComponent>(entity))
        assertSame(first, reused)
    }

    @Test
    fun clearKeepsPoolFactoryButDropsStoredInstances() {
        var created = 0
        val world = World()
        world.registerPool(PooledComponent::class) {
            created += 1
            PooledComponent()
        }
        val firstEntity = world.create()
        val first = world.add<PooledComponent>(firstEntity)
        world.destroy(firstEntity)

        world.clear()
        val second = world.add<PooledComponent>(world.create())

        assertEquals(2, created)
        assertNotSame(first, second)
    }

    @Test
    fun spawnDslCorrectlyInitializesPooledEntities() {
        val world = World()
        world.registerPool(PooledComponent::class) { PooledComponent() }

        val entity = world.create()
        world.add<PooledComponent>(entity).value = 100

        assertTrue(world.isAlive(entity))
        assertEquals(100, world.get<PooledComponent>(entity)?.value)
    }

    @Test
    fun signaturesCorrectlyMatchMultiComponentFamilies() {
        val world = World()
        val entity = world.create()

        val family = world.family<CompA, CompB>()
        assertEquals(0, family.size)

        world.add(entity, CompA())
        assertEquals(0, family.size)

        world.add(entity, CompB())
        assertEquals(1, family.size)

        world.remove<CompA>(entity)
        assertEquals(0, family.size)
    }

    @Test
    fun typeIdFastPathKeepsFamiliesAndLookupsInSync() {
        val world = World()
        world.store(CompA::class)
        world.store(CompB::class)

        val aTypeId = world.typeId(CompA::class)
        val bTypeId = world.typeId(CompB::class)
        val family = world.family<CompA, CompB>()
        val entity = world.create()
        val firstA = CompA()
        val firstB = CompB()

        assertEquals(0, family.size)

        world.add(entity, aTypeId, firstA)
        assertEquals(0, family.size)
        assertSame(firstA, world.get<CompA>(entity, aTypeId))
        assertTrue(world.has(entity, aTypeId))

        world.add(entity, bTypeId, firstB)
        assertEquals(1, family.size)
        assertSame(firstB, world.get<CompB>(entity, bTypeId))
        assertTrue(world.has(entity, bTypeId))

        val replacement = CompA()
        val previous = world.add(entity, aTypeId, replacement)
        assertSame(firstA, previous)
        assertSame(replacement, world.get<CompA>(entity, aTypeId))
        assertEquals(1, family.size)

        val removed = world.remove<CompA>(entity, aTypeId)
        assertSame(replacement, removed)
        assertFalse(world.has(entity, aTypeId))
        assertEquals(0, family.size)
    }

    @Test
    fun pooledTypeIdFamilyChurnKeepsMembershipStableAcrossRemoveReaddAndDestroy() {
        val world = World()
        world.registerPool(PooledComponent::class) { PooledComponent() }
        world.store(PooledComponent::class)

        val typeId = world.typeId(PooledComponent::class)
        val first = world.create()
        val second = world.create()
        val third = world.create()

        val firstComponent = world.add<PooledComponent>(first, typeId)
        val secondComponent = world.add<PooledComponent>(second, typeId)
        val thirdComponent = world.add<PooledComponent>(third, typeId)
        firstComponent.value = 1
        secondComponent.value = 2
        thirdComponent.value = 3

        world.add(first, FamilyMarker)
        world.add(second, FamilyMarker)
        world.add(third, FamilyMarker)

        val family = world.family<PooledComponent, FamilyMarker>()
        assertEquals(setOf(first, second, third), family.entities())

        val removedSecond = checkNotNull(world.remove<PooledComponent>(second, typeId))
        assertSame(secondComponent, removedSecond)
        assertEquals(0, removedSecond.value)
        assertEquals(setOf(first, third), family.entities())

        val reusedSecond = world.add<PooledComponent>(second, typeId)
        assertSame(removedSecond, reusedSecond)
        assertEquals(0, reusedSecond.value)
        assertEquals(setOf(first, second, third), family.entities())

        assertTrue(world.destroy(first))
        assertEquals(setOf(second, third), family.entities())

        val recycled = world.create()
        val recycledComponent = world.add<PooledComponent>(recycled, typeId)
        world.add(recycled, FamilyMarker)
        assertEquals(0, recycledComponent.value)
        assertEquals(setOf(second, third, recycled), family.entities())
    }

    @Test
    fun componentQueryKeepsStableListInstanceAfterEntityCreation() {
        val world = World()
        val entity1 = world.create()
        world.add(entity1, CompA())

        val query = world.query(CompA::class)
        assertEquals(1, query.size)

        world.create()

        // The query is recomputed as needed, but the cached list object stays stable.
        val queryAfterCreate = world.query(CompA::class)
        assertSame(query, queryAfterCreate)
    }

    @Test
    fun emptyQueryReflectsEntityLifecycle() {
        val world = World()

        assertEquals(emptyList(), world.query(*emptyComponentTypes))
        val entity = world.create()
        assertEquals(listOf(entity), world.query(*emptyComponentTypes))

        assertTrue(world.destroy(entity))
        assertEquals(emptyList(), world.query(*emptyComponentTypes))
    }

    @Test
    fun entityStorageExpandsPastInitialCapacityAndRecyclesHighIds() {
        val world = World()
        val entities = List(130) { world.create() }
        val highEntity = entities[96]

        assertTrue(world.isAlive(highEntity))
        assertTrue(world.destroy(highEntity))
        val recycled = world.create()

        assertEquals(highEntity.id, recycled.id)
        assertFalse(world.isAlive(highEntity))
        assertTrue(world.isAlive(recycled))
    }

    @Test
    fun registeringMoreThanLongSignatureCapacityFailsClearly() {
        val world = World()
        first64ComponentTypes.forEach { world.typeId(it) }

        val failure = assertFailsWith<IllegalArgumentException> {
            world.typeId(ComponentType64::class)
        }

        assertTrue(failure.message?.contains("64 component types") == true)
    }

    private class PooledComponent : Poolable {
        var value: Int = 0
        override fun reset() {
            value = 0
        }
    }

    private class CompA
    private class CompB

    private fun Family2<PooledComponent, FamilyMarker>.entities(): Set<Entity> {
        val entities = mutableSetOf<Entity>()
        forEach { entity, _, _ ->
            entities += entity
        }
        return entities
    }

    private data object FamilyMarker
}

private val first64ComponentTypes = listOf(
    ComponentType00::class,
    ComponentType01::class,
    ComponentType02::class,
    ComponentType03::class,
    ComponentType04::class,
    ComponentType05::class,
    ComponentType06::class,
    ComponentType07::class,
    ComponentType08::class,
    ComponentType09::class,
    ComponentType10::class,
    ComponentType11::class,
    ComponentType12::class,
    ComponentType13::class,
    ComponentType14::class,
    ComponentType15::class,
    ComponentType16::class,
    ComponentType17::class,
    ComponentType18::class,
    ComponentType19::class,
    ComponentType20::class,
    ComponentType21::class,
    ComponentType22::class,
    ComponentType23::class,
    ComponentType24::class,
    ComponentType25::class,
    ComponentType26::class,
    ComponentType27::class,
    ComponentType28::class,
    ComponentType29::class,
    ComponentType30::class,
    ComponentType31::class,
    ComponentType32::class,
    ComponentType33::class,
    ComponentType34::class,
    ComponentType35::class,
    ComponentType36::class,
    ComponentType37::class,
    ComponentType38::class,
    ComponentType39::class,
    ComponentType40::class,
    ComponentType41::class,
    ComponentType42::class,
    ComponentType43::class,
    ComponentType44::class,
    ComponentType45::class,
    ComponentType46::class,
    ComponentType47::class,
    ComponentType48::class,
    ComponentType49::class,
    ComponentType50::class,
    ComponentType51::class,
    ComponentType52::class,
    ComponentType53::class,
    ComponentType54::class,
    ComponentType55::class,
    ComponentType56::class,
    ComponentType57::class,
    ComponentType58::class,
    ComponentType59::class,
    ComponentType60::class,
    ComponentType61::class,
    ComponentType62::class,
    ComponentType63::class,
)

private val emptyComponentTypes = emptyArray<kotlin.reflect.KClass<out Any>>()

private class ComponentType00
private class ComponentType01
private class ComponentType02
private class ComponentType03
private class ComponentType04
private class ComponentType05
private class ComponentType06
private class ComponentType07
private class ComponentType08
private class ComponentType09
private class ComponentType10
private class ComponentType11
private class ComponentType12
private class ComponentType13
private class ComponentType14
private class ComponentType15
private class ComponentType16
private class ComponentType17
private class ComponentType18
private class ComponentType19
private class ComponentType20
private class ComponentType21
private class ComponentType22
private class ComponentType23
private class ComponentType24
private class ComponentType25
private class ComponentType26
private class ComponentType27
private class ComponentType28
private class ComponentType29
private class ComponentType30
private class ComponentType31
private class ComponentType32
private class ComponentType33
private class ComponentType34
private class ComponentType35
private class ComponentType36
private class ComponentType37
private class ComponentType38
private class ComponentType39
private class ComponentType40
private class ComponentType41
private class ComponentType42
private class ComponentType43
private class ComponentType44
private class ComponentType45
private class ComponentType46
private class ComponentType47
private class ComponentType48
private class ComponentType49
private class ComponentType50
private class ComponentType51
private class ComponentType52
private class ComponentType53
private class ComponentType54
private class ComponentType55
private class ComponentType56
private class ComponentType57
private class ComponentType58
private class ComponentType59
private class ComponentType60
private class ComponentType61
private class ComponentType62
private class ComponentType63
private class ComponentType64
