/*
 * Awake
 * Awake.awake-ecs.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass

/**
 * The public, read-only view of a maintained one-component family -- see [Family1Cache]
 * for how membership is kept up to date incrementally as [World] mutates.
 */
class Family1<A : Any> @PublishedApi internal constructor(
    @PublishedApi internal val cache: Family1Cache<A>
) {
    val size: Int get() = cache.size

    fun components(): Array<A> {
        return cache.components()
    }

    fun componentAt(index: Int): A {
        return cache.componentAt(index)
    }

    inline fun forEach(block: (Entity, A) -> Unit) {
        cache.forEach(block)
    }

    inline fun forEachComponent(block: (A) -> Unit) {
        cache.forEachComponent(block)
    }
}

/** The public, read-only view of a maintained two-component family -- see [Family2Cache]. */
class Family2<A : Any, B : Any> @PublishedApi internal constructor(
    @PublishedApi internal val cache: Family2Cache<A, B>
) {
    val size: Int get() = cache.size

    fun componentsA(): Array<A> {
        return cache.componentsA()
    }

    fun componentsB(): Array<B> {
        return cache.componentsB()
    }

    fun componentA(index: Int): A {
        return cache.componentA(index)
    }

    fun componentB(index: Int): B {
        return cache.componentB(index)
    }

    inline fun forEach(block: (Entity, A, B) -> Unit) {
        cache.forEach(block)
    }

    inline fun forEachComponents(block: (A, B) -> Unit) {
        cache.forEachComponents(block)
    }
}

/**
 * A family cache is kept in sync with [World] incrementally: [World.add]/[World.remove]
 * notify every existing family cache directly (see `addComponentToFamilies` etc. in
 * `World.kt`) instead of each family re-scanning all entities on every structural change.
 */
@PublishedApi
internal sealed class FamilyCache {
    abstract fun types(): Set<KClass<out Any>>
    abstract fun remove(entity: Entity)
    abstract fun <T : Any> addComponent(world: World, entity: Entity, typeId: ComponentTypeId, type: KClass<T>, component: T)
    abstract fun <T : Any> replaceComponent(world: World, entity: Entity, typeId: ComponentTypeId, type: KClass<T>, component: T)
    abstract fun removeComponent(world: World, entity: Entity, typeId: ComponentTypeId, type: KClass<out Any>)
}

@PublishedApi
@Suppress("TooManyFunctions")
internal class Family1Cache<A : Any>(
    private val type: KClass<A>,
    private val typeId: ComponentTypeId,
    @Suppress("unused") private val store: ComponentStore<A>
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
        @Suppress("UNCHECKED_CAST")
        return components as Array<A>
    }

    @PublishedApi
    internal fun componentAt(index: Int): A {
        @Suppress("UNCHECKED_CAST")
        return components[index] as A
    }

    fun add(entity: Entity, component: A) {
        val index = indexOf(entity)
        if (index >= 0) {
            components[index] = component
            return
        }
        ensureCapacity(count + 1)
        sparse.set(entity.id, count)
        entities[count] = entity.packed
        components[count] = component
        count += 1
    }

    @PublishedApi
    internal inline fun forEach(block: (Entity, A) -> Unit) {
        val localEntities = entities
        @Suppress("UNCHECKED_CAST")
        val localComponents = components as Array<A>
        val localCount = count
        for (index in 0 until localCount) {
            block(Entity(localEntities[index]), localComponents[index])
        }
    }

    @PublishedApi
    internal inline fun forEachComponent(block: (A) -> Unit) {
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

    override fun <T : Any> addComponent(
        world: World,
        entity: Entity,
        typeId: ComponentTypeId,
        type: KClass<T>,
        component: T
    ) {
        if (this.typeId == typeId) {
            @Suppress("UNCHECKED_CAST")
            add(entity, component as A)
        }
    }

    override fun <T : Any> replaceComponent(
        world: World,
        entity: Entity,
        typeId: ComponentTypeId,
        type: KClass<T>,
        component: T
    ) {
        if (this.typeId == typeId) {
            val index = indexOf(entity)
            if (index >= 0) {
                @Suppress("UNCHECKED_CAST")
                components[index] = component as A
            }
        }
    }

    override fun removeComponent(world: World, entity: Entity, typeId: ComponentTypeId, type: KClass<out Any>) {
        if (this.typeId == typeId) {
            remove(entity)
        }
    }

    private fun removeAt(index: Int) {
        if (index < 0) {
            return
        }

        val lastIndex = count - 1
        val lastEntity = entities[lastIndex]
        entities[index] = lastEntity
        components[index] = components[lastIndex]
        sparse.set(Entity(lastEntity).id, index)

        components[lastIndex] = null
        count -= 1
    }

    private fun indexOf(entity: Entity): Int {
        val denseIndex = sparse.get(entity.id)
        return if (denseIndex in 0 until count && entities[denseIndex] == entity.packed) {
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

@PublishedApi
@Suppress("TooManyFunctions")
internal class Family2Cache<A : Any, B : Any>(
    private val typeA: KClass<A>,
    private val typeIdA: ComponentTypeId,
    private val storeA: ComponentStore<A>,
    private val typeB: KClass<B>,
    private val typeIdB: ComponentTypeId,
    private val storeB: ComponentStore<B>,
    private val mask: Long
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
        @Suppress("UNCHECKED_CAST")
        return componentsA as Array<A>
    }

    @PublishedApi
    internal fun componentsB(): Array<B> {
        @Suppress("UNCHECKED_CAST")
        return componentsB as Array<B>
    }

    @PublishedApi
    internal fun componentA(index: Int): A {
        @Suppress("UNCHECKED_CAST")
        return componentsA[index] as A
    }

    @PublishedApi
    internal fun componentB(index: Int): B {
        @Suppress("UNCHECKED_CAST")
        return componentsB[index] as B
    }

    fun add(entity: Entity, componentA: A, componentB: B) {
        val index = indexOf(entity)
        if (index >= 0) {
            componentsA[index] = componentA
            componentsB[index] = componentB
            return
        }
        ensureCapacity(count + 1)
        sparse.set(entity.id, count)
        entities[count] = entity.packed
        componentsA[count] = componentA
        componentsB[count] = componentB
        count += 1
    }

    @PublishedApi
    internal inline fun forEach(block: (Entity, A, B) -> Unit) {
        val localEntities = entities
        @Suppress("UNCHECKED_CAST")
        val localComponentsA = componentsA as Array<A>
        @Suppress("UNCHECKED_CAST")
        val localComponentsB = componentsB as Array<B>
        val localCount = count
        for (index in 0 until localCount) {
            block(Entity(localEntities[index]), localComponentsA[index], localComponentsB[index])
        }
    }

    @PublishedApi
    internal inline fun forEachComponents(block: (A, B) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val localComponentsA = componentsA as Array<A>
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

    override fun <T : Any> addComponent(
        world: World,
        entity: Entity,
        typeId: ComponentTypeId,
        type: KClass<T>,
        component: T
    ) {
        if (indexOf(entity) < 0 && (world.getSignature(entity.id) and mask) == mask) {
            val compA = storeA.get(entity)!!
            val compB = storeB.get(entity)!!
            add(entity, compA, compB)
        }
    }

    override fun <T : Any> replaceComponent(
        world: World,
        entity: Entity,
        typeId: ComponentTypeId,
        type: KClass<T>,
        component: T
    ) {
        val index = indexOf(entity)
        if (index < 0) {
            return
        }
        if (typeIdA == typeId) {
            @Suppress("UNCHECKED_CAST")
            componentsA[index] = component as A
        } else if (typeIdB == typeId) {
            @Suppress("UNCHECKED_CAST")
            componentsB[index] = component as B
        }
    }

    override fun removeComponent(world: World, entity: Entity, typeId: ComponentTypeId, type: KClass<out Any>) {
        if (typeIdA == typeId || typeIdB == typeId) {
            remove(entity)
        }
    }

    private fun removeAt(index: Int) {
        if (index < 0) {
            return
        }

        val lastIndex = count - 1
        val lastEntity = entities[lastIndex]
        entities[index] = lastEntity
        componentsA[index] = componentsA[lastIndex]
        componentsB[index] = componentsB[lastIndex]
        sparse.set(Entity(lastEntity).id, index)

        componentsA[lastIndex] = null
        componentsB[lastIndex] = null
        count -= 1
    }

    private fun indexOf(entity: Entity): Int {
        val denseIndex = sparse.get(entity.id)
        return if (denseIndex in 0 until count && entities[denseIndex] == entity.packed) {
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
