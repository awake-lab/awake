// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass

/**
 * The public, read-only view of a maintained one-component family -- see [Family1Cache]
 * for how membership is kept up to date incrementally as [World] mutates.
 */
class Family1<A : Any> @PublishedApi internal constructor(
    @PublishedApi internal val cache: Family1Cache<A>,
) {
    val size: Int get() = cache.size

    fun components(): Array<A> = cache.components()

    fun componentAt(index: Int): A = cache.componentAt(index)

    inline fun forEach(block: (Entity, A) -> Unit) {
        cache.forEach(block)
    }

    inline fun forEachComponent(block: (A) -> Unit) {
        cache.forEachComponent(block)
    }
}

/** The public, read-only view of a maintained two-component family -- see [Family2Cache]. */
class Family2<A : Any, B : Any> @PublishedApi internal constructor(
    @PublishedApi internal val cache: Family2Cache<A, B>,
) {
    val size: Int get() = cache.size

    fun componentsA(): Array<A> = cache.componentsA()

    fun componentsB(): Array<B> = cache.componentsB()

    fun componentA(index: Int): A = cache.componentA(index)

    fun componentB(index: Int): B = cache.componentB(index)

    inline fun forEach(block: (Entity, A, B) -> Unit) {
        cache.forEach(block)
    }

    inline fun forEachComponents(block: (A, B) -> Unit) {
        cache.forEachComponents(block)
    }
}

/**
 * A family cache is kept in sync with [World] incrementally: [World.add]/[World.remove]
 * notify every existing family cache directly through [FamilyRegistry] instead of each
 * family re-scanning all entities on every structural change.
 */
@PublishedApi
internal sealed class FamilyCache {
    abstract fun types(): Set<KClass<out Any>>
    abstract fun remove(entity: Entity)
    abstract fun addComponent(world: World, entity: Entity, typeId: ComponentTypeId, component: Any)
    abstract fun replaceComponent(world: World, entity: Entity, typeId: ComponentTypeId, component: Any)
    abstract fun removeComponent(world: World, entity: Entity, typeId: ComponentTypeId)
}

// Implements FamilyCache's lifecycle callbacks plus the dense-array accessors Family1
// delegates to -- all tightly coupled to this one cache's own storage, so splitting them
// across classes would only separate code that has to change together.
@PublishedApi
@Suppress("TooManyFunctions")
internal class Family1Cache<A : Any>(
    private val type: KClass<A>,
    private val typeId: ComponentTypeId,
    // Kept for constructor-shape symmetry with Family2Cache's buildFamily() call site;
    // unlike Family2Cache, a one-component cache never needs to look up a sibling store.
    @Suppress("unused") private val store: ComponentStore<A>,
) : FamilyCache() {
    @PublishedApi
    internal var entities = LongArray(DEFAULT_FAMILY_CAPACITY)

    @PublishedApi
    internal var components = newComponentArray(type, DEFAULT_FAMILY_CAPACITY)

    @PublishedApi
    internal var count: Int = 0
    private val sparse = EntityIndexMap()

    override fun types(): Set<KClass<out Any>> = setOf(type)

    val size: Int get() = count

    @PublishedApi
    internal fun components(): Array<A> {
        // components is allocated via newComponentArray(type, ...) with this same `type:
        // KClass<A>`, so its runtime element type is always A.
        @Suppress("UNCHECKED_CAST")
        return components as Array<A>
    }

    @PublishedApi
    internal fun componentAt(index: Int): A {
        // Every populated slot below `count` was written by add()/addComponent() with an A;
        // components is allocated for type A (see components() above).
        @Suppress("UNCHECKED_CAST")
        return components[index] as A
    }

    fun add(entity: Entity, component: A) {
        ensureCapacity(count + 1)
        sparse.set(entity.id, count)
        entities[count] = entity.packed
        components[count] = component
        count += 1
    }

    /**
     * Iterates the live cache. Adding or removing a component that changes THIS family's
     * membership from inside [block] corrupts the walk: removal swaps the last entry into the
     * freed slot, so the swapped-in entity is skipped and the final slot is read after being
     * cleared. Collect the entities first, then mutate outside the loop.
     */
    @PublishedApi
    internal inline fun forEach(block: (Entity, A) -> Unit) {
        val localEntities = entities

        // components is allocated for type A (see components() above).
        @Suppress("UNCHECKED_CAST")
        val localComponents = components as Array<A>
        val localCount = count
        for (index in 0 until localCount) {
            block(Entity(localEntities[index]), localComponents[index])
        }
    }

    @PublishedApi
    internal inline fun forEachComponent(block: (A) -> Unit) {
        // components is allocated for type A (see components() above).
        @Suppress("UNCHECKED_CAST")
        val localComponents = components as Array<A>
        val localCount = count
        for (index in 0 until localCount) {
            block(localComponents[index])
        }
    }

    override fun remove(entity: Entity) {
        removeAt(indexOf(entity))
    }

    override fun addComponent(world: World, entity: Entity, typeId: ComponentTypeId, component: Any) {
        if (this.typeId == typeId) {
            // Guarded by the typeId == this.typeId check above: World only ever routes a
            // structural change to this cache with a component matching the A it was built for.
            @Suppress("UNCHECKED_CAST")
            val typedComponent = component as A
            ensureCapacity(count + 1)
            sparse.set(entity.id, count)
            entities[count] = entity.packed
            components[count] = typedComponent
            count += 1
        }
    }

    override fun replaceComponent(world: World, entity: Entity, typeId: ComponentTypeId, component: Any) {
        if (this.typeId == typeId) {
            val index = indexOf(entity)
            if (index >= 0) {
                // Guarded by the typeId == this.typeId check above (same invariant as
                // addComponent()).
                @Suppress("UNCHECKED_CAST")
                components[index] = component as A
            }
        }
    }

    override fun removeComponent(world: World, entity: Entity, typeId: ComponentTypeId) {
        if (this.typeId == typeId) {
            remove(entity)
        }
    }

    private fun removeAt(index: Int) {
        if (index < 0) {
            return
        }

        val removedEntityId = entities[index].toInt()
        val lastIndex = count - 1
        if (index != lastIndex) {
            val lastEntity = entities[lastIndex]
            entities[index] = lastEntity
            components[index] = components[lastIndex]
            sparse.set(lastEntity.toInt(), index)
        }

        components[lastIndex] = null
        count -= 1
        sparse.remove(removedEntityId)
    }

    private fun indexOf(entity: Entity): Int {
        val denseIndex = sparse.get(entity.id)
        return if (denseIndex >= 0 && denseIndex < count) {
            denseIndex
        } else {
            -1
        }
    }

    private fun ensureCapacity(requiredCapacity: Int) {
        if (requiredCapacity <= entities.size) {
            return
        }
        val newCapacity = maxOf(requiredCapacity, entities.size * CAPACITY_GROWTH_FACTOR)
        entities = entities.copyOf(newCapacity)
        components = components.copyOf(newCapacity)
    }
}

// Same cohesion reasoning as Family1Cache above, doubled for the two-component (A, B)
// dense-array bookkeeping and its own FamilyCache lifecycle callbacks.
@PublishedApi
@Suppress("TooManyFunctions")
internal class Family2Cache<A : Any, B : Any>(
    private val typeA: KClass<A>,
    private val typeIdA: ComponentTypeId,
    private val storeA: ComponentStore<A>,
    private val typeB: KClass<B>,
    private val typeIdB: ComponentTypeId,
    private val storeB: ComponentStore<B>,
) : FamilyCache() {
    @PublishedApi
    internal var entities = LongArray(DEFAULT_FAMILY_CAPACITY)

    @PublishedApi
    internal var componentsA = newComponentArray(typeA, DEFAULT_FAMILY_CAPACITY)

    @PublishedApi
    internal var componentsB = newComponentArray(typeB, DEFAULT_FAMILY_CAPACITY)

    @PublishedApi
    internal var count: Int = 0
    private val sparse = EntityIndexMap()

    override fun types(): Set<KClass<out Any>> = setOf(typeA, typeB)

    val size: Int get() = count

    @PublishedApi
    internal fun componentsA(): Array<A> {
        // componentsA is allocated via newComponentArray(typeA, ...), so its runtime element
        // type is always A.
        @Suppress("UNCHECKED_CAST")
        return componentsA as Array<A>
    }

    @PublishedApi
    internal fun componentsB(): Array<B> {
        // componentsB is allocated via newComponentArray(typeB, ...), so its runtime element
        // type is always B.
        @Suppress("UNCHECKED_CAST")
        return componentsB as Array<B>
    }

    @PublishedApi
    internal fun componentA(index: Int): A {
        // Every populated slot below `count` was written with an A (see componentsA() above).
        @Suppress("UNCHECKED_CAST")
        return componentsA[index] as A
    }

    @PublishedApi
    internal fun componentB(index: Int): B {
        // Every populated slot below `count` was written with a B (see componentsB() above).
        @Suppress("UNCHECKED_CAST")
        return componentsB[index] as B
    }

    fun add(entity: Entity, componentA: A, componentB: B) {
        ensureCapacity(count + 1)
        sparse.set(entity.id, count)
        entities[count] = entity.packed
        componentsA[count] = componentA
        componentsB[count] = componentB
        count += 1
    }

    /**
     * Iterates the live cache. Adding or removing a component that changes THIS family's
     * membership from inside [block] corrupts the walk: removal swaps the last entry into the
     * freed slot, so the swapped-in entity is skipped and the final slot is read after being
     * cleared. Collect the entities first, then mutate outside the loop.
     */
    @PublishedApi
    internal inline fun forEach(block: (Entity, A, B) -> Unit) {
        val localEntities = entities

        // componentsA is allocated for type A (see componentsA() above).
        @Suppress("UNCHECKED_CAST")
        val localComponentsA = componentsA as Array<A>

        // componentsB is allocated for type B (see componentsB() above).
        @Suppress("UNCHECKED_CAST")
        val localComponentsB = componentsB as Array<B>
        val localCount = count
        for (index in 0 until localCount) {
            block(Entity(localEntities[index]), localComponentsA[index], localComponentsB[index])
        }
    }

    @PublishedApi
    internal inline fun forEachComponents(block: (A, B) -> Unit) {
        // componentsA is allocated for type A (see componentsA() above).
        @Suppress("UNCHECKED_CAST")
        val localComponentsA = componentsA as Array<A>

        // componentsB is allocated for type B (see componentsB() above).
        @Suppress("UNCHECKED_CAST")
        val localComponentsB = componentsB as Array<B>
        val localCount = count
        for (index in 0 until localCount) {
            block(localComponentsA[index], localComponentsB[index])
        }
    }

    override fun remove(entity: Entity) {
        removeAt(indexOf(entity))
    }

    override fun addComponent(world: World, entity: Entity, typeId: ComponentTypeId, component: Any) {
        if (typeId == typeIdA) {
            // Guarded by the typeId == typeIdA check above: World only ever routes this call
            // with a component matching A; storeB.get(entity) is already typed B and needs
            // no cast, only `component as A` below relies on this guard.
            @Suppress("UNCHECKED_CAST")
            val componentB = storeB.get(entity) ?: return
            val typedComponentA = component as A
            ensureCapacity(count + 1)
            sparse.set(entity.id, count)
            entities[count] = entity.packed
            componentsA[count] = typedComponentA
            componentsB[count] = componentB
            count += 1
        } else if (typeId == typeIdB) {
            // Guarded by the typeId == typeIdB check above: World only ever routes this call
            // with a component matching B; storeA.get(entity) is already typed A and needs
            // no cast, only `component as B` below relies on this guard.
            @Suppress("UNCHECKED_CAST")
            val componentA = storeA.get(entity) ?: return
            val typedComponentB = component as B
            ensureCapacity(count + 1)
            sparse.set(entity.id, count)
            entities[count] = entity.packed
            componentsA[count] = componentA
            componentsB[count] = typedComponentB
            count += 1
        }
    }

    override fun replaceComponent(world: World, entity: Entity, typeId: ComponentTypeId, component: Any) {
        val index = indexOf(entity)
        if (index < 0) {
            return
        }
        if (typeIdA == typeId) {
            // Guarded by the typeIdA == typeId check above (same invariant as addComponent()).
            @Suppress("UNCHECKED_CAST")
            componentsA[index] = component as A
        } else if (typeIdB == typeId) {
            // Guarded by the typeIdB == typeId check above (same invariant as addComponent()).
            @Suppress("UNCHECKED_CAST")
            componentsB[index] = component as B
        }
    }

    override fun removeComponent(world: World, entity: Entity, typeId: ComponentTypeId) {
        if (typeIdA == typeId || typeIdB == typeId) {
            remove(entity)
        }
    }

    private fun removeAt(index: Int) {
        if (index < 0) {
            return
        }

        val removedEntityId = entities[index].toInt()
        val lastIndex = count - 1
        if (index != lastIndex) {
            val lastEntity = entities[lastIndex]
            entities[index] = lastEntity
            componentsA[index] = componentsA[lastIndex]
            componentsB[index] = componentsB[lastIndex]
            sparse.set(lastEntity.toInt(), index)
        }

        componentsA[lastIndex] = null
        componentsB[lastIndex] = null
        count -= 1
        sparse.remove(removedEntityId)
    }

    private fun indexOf(entity: Entity): Int {
        val denseIndex = sparse.get(entity.id)
        return if (denseIndex >= 0 && denseIndex < count) {
            denseIndex
        } else {
            -1
        }
    }

    private fun ensureCapacity(requiredCapacity: Int) {
        if (requiredCapacity <= entities.size) {
            return
        }
        val newCapacity = maxOf(requiredCapacity, entities.size * CAPACITY_GROWTH_FACTOR)
        entities = entities.copyOf(newCapacity)
        componentsA = componentsA.copyOf(newCapacity)
        componentsB = componentsB.copyOf(newCapacity)
    }
}

private const val DEFAULT_FAMILY_CAPACITY = 16
private const val CAPACITY_GROWTH_FACTOR = 2
