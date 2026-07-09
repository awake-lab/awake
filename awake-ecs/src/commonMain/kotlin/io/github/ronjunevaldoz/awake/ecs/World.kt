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

import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

@Suppress("TooManyFunctions")
class World {
    private val slots = mutableListOf<EntitySlot>()
    private val freeIds = mutableListOf<Int>()
    private val stores = mutableMapOf<KClass<out Any>, ComponentStore<Any>>()
    private val typeIds = mutableMapOf<KClass<out Any>, ComponentTypeId>()
    private val families = mutableMapOf<FamilyKey, FamilyCache>()
    private val queryCache = mutableMapOf<QueryKey, CachedQuery>()
    private var queryVersion = 0

    fun create(): Entity {
        val id = freeIds.removeLastOrNull()
        if (id != null) {
            val slot = slots[id]
            slot.alive = true
            markQueriesDirty()
            return Entity.of(id, slot.generation)
        }

        val nextId = slots.size
        slots += EntitySlot(generation = 0, alive = true)
        markQueriesDirty()
        return Entity.of(nextId, 0)
    }

    fun destroy(entity: Entity): Boolean {
        if (!isAlive(entity)) {
            return false
        }

        removeEntityFromFamilies(entity)
        stores.values.forEach { it.remove(entity) }
        val slot = slots[entity.id]
        slot.alive = false
        slot.generation += 1
        freeIds += entity.id
        markQueriesDirty()
        return true
    }

    fun isAlive(entity: Entity): Boolean {
        val slot = slots.getOrNull(entity.id) ?: return false
        return slot.alive && slot.generation == entity.generation
    }

    inline fun <reified T : Any> add(entity: Entity, component: T): T? {
        return add(entity, T::class, component)
    }

    fun <T : Any> add(entity: Entity, type: KClass<T>, component: T): T? {
        requireAlive(entity)
        typeId(type)
        val previous = store(type).add(entity, component)
        if (previous == null) {
            markQueriesDirty()
            addComponentToFamilies(entity, type, component)
        } else {
            replaceComponentInFamilies(entity, type, component)
        }
        return previous
    }

    inline fun <reified T : Any> get(entity: Entity): T? {
        return get(entity, T::class)
    }

    fun <T : Any> get(entity: Entity, type: KClass<T>): T? {
        if (!isAlive(entity)) {
            return null
        }
        return storeOrNull(type)?.get(entity)
    }

    inline fun <reified T : Any> remove(entity: Entity): T? {
        return remove(entity, T::class)
    }

    fun <T : Any> remove(entity: Entity, type: KClass<T>): T? {
        if (!isAlive(entity)) {
            return null
        }
        val removed = storeOrNull(type)?.remove(entity)
        if (removed != null) {
            markQueriesDirty()
            removeComponentFromFamilies(entity, type)
        }
        return removed
    }

    inline fun <reified T : Any> has(entity: Entity): Boolean {
        return has(entity, T::class)
    }

    fun has(entity: Entity, type: KClass<out Any>): Boolean {
        return isAlive(entity) && (stores[type]?.contains(entity) == true)
    }

    fun query(vararg types: KClass<out Any>): List<Entity> {
        val key = QueryKey(types.toSet())
        val cached = queryCache.getOrPut(key) { CachedQuery() }
        if (cached.version != queryVersion) {
            cached.entities.clear()
            cached.entities += collectQuery(key.types)
            cached.version = queryVersion
        }
        return cached.entities
    }

    fun queryEach(vararg types: KClass<out Any>, block: (Entity) -> Unit) {
        query(*types).forEach(block)
    }

    fun <A : Any> queryEach(type: KClass<A>, block: (Entity, A) -> Unit) {
        familyCache(type).forEach(block)
    }

    inline fun <reified A : Any> queryEach(noinline block: (Entity, A) -> Unit) {
        queryEach(A::class, block)
    }

    fun <A : Any> family(type: KClass<A>): Family1<A> {
        return Family1(familyCache(type))
    }

    inline fun <reified A : Any> family(): Family1<A> {
        return family(A::class)
    }

    fun <A : Any, B : Any> queryEach(
        typeA: KClass<A>,
        typeB: KClass<B>,
        block: (Entity, A, B) -> Unit
    ) {
        familyCache(typeA, typeB).forEach(block)
    }

    inline fun <reified A : Any, reified B : Any> queryEach(noinline block: (Entity, A, B) -> Unit) {
        queryEach(A::class, B::class, block)
    }

    fun <A : Any, B : Any> family(typeA: KClass<A>, typeB: KClass<B>): Family2<A, B> {
        return Family2(familyCache(typeA, typeB))
    }

    inline fun <reified A : Any, reified B : Any> family(): Family2<A, B> {
        return family(A::class, B::class)
    }

    private fun collectQuery(types: Set<KClass<out Any>>): List<Entity> {
        return if (types.isEmpty()) {
            slots.indices
                .map { Entity.of(it, slots[it].generation) }
                .filter(::isAlive)
        } else {
            val queryStores = types.mapNotNull(stores::get)
            if (queryStores.size != types.size) {
                emptyList()
            } else {
                val smallestStore = queryStores.minBy { it.size }
                smallestStore.entities.filter { entity ->
                    isAlive(entity) && queryStores.all { it.contains(entity) }
                }
            }
        }
    }

    inline fun <reified T : Any> query(): List<Entity> {
        return query(T::class)
    }

    fun clear() {
        slots.clear()
        freeIds.clear()
        stores.clear()
        typeIds.clear()
        families.clear()
        queryCache.clear()
        markQueriesDirty()
    }

    fun componentCount(type: KClass<out Any>): Int {
        return stores[type]?.size ?: 0
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> store(type: KClass<T>): ComponentStore<T> {
        return stores.getOrPut(type) { ComponentStore() } as ComponentStore<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> storeOrNull(type: KClass<T>): ComponentStore<T>? {
        return stores[type] as? ComponentStore<T>
    }

    private fun requireAlive(entity: Entity) {
        require(isAlive(entity)) { "Entity is not alive: $entity" }
    }

    private fun markQueriesDirty() {
        queryVersion += 1
    }

    private fun typeId(type: KClass<out Any>): ComponentTypeId {
        return typeIds.getOrPut(type) {
            ComponentTypeId(typeIds.size)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : Any> familyCache(type: KClass<A>): Family1Cache<A> {
        val key = FamilyKey.single(typeId(type))
        return families.getOrPut(key) { buildFamily(type) } as Family1Cache<A>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : Any, B : Any> familyCache(
        typeA: KClass<A>,
        typeB: KClass<B>
    ): Family2Cache<A, B> {
        val key = FamilyKey.pair(typeId(typeA), typeId(typeB))
        return families.getOrPut(key) { buildFamily(typeA, typeB) } as Family2Cache<A, B>
    }

    private fun <A : Any> buildFamily(type: KClass<A>): Family1Cache<A> {
        val cache = Family1Cache(type)
        storeOrNull(type)?.forEach { entity, component ->
            cache.add(entity, component)
        }
        return cache
    }

    private fun <A : Any, B : Any> buildFamily(
        typeA: KClass<A>,
        typeB: KClass<B>
    ): Family2Cache<A, B> {
        val cache = Family2Cache(typeA, typeB)
        val storeA = storeOrNull(typeA)
        val storeB = storeOrNull(typeB)
        if (storeA != null && storeB != null) {
            fillFamily(cache, storeA, storeB)
        }
        return cache
    }

    private fun <A : Any, B : Any> fillFamily(
        cache: Family2Cache<A, B>,
        storeA: ComponentStore<A>,
        storeB: ComponentStore<B>
    ) {
        if (storeA.size <= storeB.size) {
            addMatchesFromA(cache, storeA, storeB)
        } else {
            addMatchesFromB(cache, storeA, storeB)
        }
    }

    private fun <A : Any, B : Any> addMatchesFromA(
        cache: Family2Cache<A, B>,
        storeA: ComponentStore<A>,
        storeB: ComponentStore<B>
    ) {
        storeA.forEach { entity, componentA ->
            storeB.get(entity)?.let { componentB ->
                cache.add(entity, componentA, componentB)
            }
        }
    }

    private fun <A : Any, B : Any> addMatchesFromB(
        cache: Family2Cache<A, B>,
        storeA: ComponentStore<A>,
        storeB: ComponentStore<B>
    ) {
        storeB.forEach { entity, componentB ->
            storeA.get(entity)?.let { componentA ->
                cache.add(entity, componentA, componentB)
            }
        }
    }

    private fun removeEntityFromFamilies(entity: Entity) {
        families.values.forEach { it.remove(entity) }
    }

    private fun <T : Any> addComponentToFamilies(
        entity: Entity,
        type: KClass<T>,
        component: T
    ) {
        families.values.forEach { family ->
            family.addComponent(this, entity, type, component)
        }
    }

    private fun <T : Any> replaceComponentInFamilies(
        entity: Entity,
        type: KClass<T>,
        component: T
    ) {
        families.values.forEach { family ->
            family.replaceComponent(this, entity, type, component)
        }
    }

    private fun removeComponentFromFamilies(entity: Entity, type: KClass<out Any>) {
        families.values.forEach { family ->
            family.removeComponent(entity, type)
        }
    }

    private data class EntitySlot(
        var generation: Int,
        var alive: Boolean
    )

    private data class QueryKey(
        val types: Set<KClass<out Any>>
    )

    private data class CachedQuery(
        val entities: MutableList<Entity> = mutableListOf(),
        var version: Int = -1
    )
}

class Family1<A : Any> @PublishedApi internal constructor(
    @PublishedApi internal val cache: Family1Cache<A>
) {
    val size: Int get() = cache.size

    inline fun forEach(block: (Entity, A) -> Unit) {
        cache.forEach(block)
    }

    inline fun forEachComponent(block: (A) -> Unit) {
        cache.forEachComponent(block)
    }
}

class Family2<A : Any, B : Any> @PublishedApi internal constructor(
    @PublishedApi internal val cache: Family2Cache<A, B>
) {
    val size: Int get() = cache.size

    inline fun forEach(block: (Entity, A, B) -> Unit) {
        cache.forEach(block)
    }

    inline fun forEachComponents(block: (A, B) -> Unit) {
        cache.forEachComponents(block)
    }
}

@JvmInline
private value class ComponentTypeId(
    val value: Int
)

@JvmInline
private value class FamilyKey(
    val packed: Long
) {
    companion object {
        private const val SINGLE_SENTINEL = -1

        fun single(type: ComponentTypeId): FamilyKey {
            return FamilyKey(pack(type.value, SINGLE_SENTINEL))
        }

        fun pair(typeA: ComponentTypeId, typeB: ComponentTypeId): FamilyKey {
            return FamilyKey(pack(typeA.value, typeB.value))
        }

        private fun pack(first: Int, second: Int): Long {
            return (first.toLong() shl INT_BITS) or (second.toLong() and LOW_INT_MASK)
        }
    }
}

@PublishedApi
internal sealed class FamilyCache {
    abstract fun remove(entity: Entity)
    abstract fun <T : Any> addComponent(world: World, entity: Entity, type: KClass<T>, component: T)
    abstract fun <T : Any> replaceComponent(world: World, entity: Entity, type: KClass<T>, component: T)
    abstract fun removeComponent(entity: Entity, type: KClass<out Any>)
}

@PublishedApi
@Suppress("TooManyFunctions")
internal class Family1Cache<A : Any>(
    private val type: KClass<A>
) : FamilyCache() {
    @PublishedApi
    internal var entities = LongArray(DEFAULT_FAMILY_CAPACITY)
    @PublishedApi
    internal var components = arrayOfNulls<Any>(DEFAULT_FAMILY_CAPACITY)
    @PublishedApi
    internal var count: Int = 0
    // Sparse index (entity id -> dense index) so remove/replace don't need a linear scan --
    // same trick ComponentStore already uses. Family caches previously only had the dense
    // arrays above, making indexOf() an O(n) scan on every structural change.
    private var sparse = IntArray(0)

    val size: Int get() = count

    fun add(entity: Entity, component: A) {
        ensureSparseCapacity(entity.id)
        ensureCapacity(count + 1)
        sparse[entity.id] = count
        entities[count] = entity.packed
        components[count] = component
        count += 1
    }

    @PublishedApi
    internal inline fun forEach(block: (Entity, A) -> Unit) {
        val localEntities = entities
        val localComponents = components
        val localCount = count
        for (index in 0 until localCount) {
            @Suppress("UNCHECKED_CAST")
            block(Entity(localEntities[index]), localComponents[index] as A)
        }
    }

    @PublishedApi
    internal inline fun forEachComponent(block: (A) -> Unit) {
        val localComponents = components
        val localCount = count
        for (index in 0 until localCount) {
            @Suppress("UNCHECKED_CAST")
            block(localComponents[index] as A)
        }
    }

    override fun remove(entity: Entity) {
        removeAt(indexOf(entity))
    }

    override fun <T : Any> addComponent(
        world: World,
        entity: Entity,
        type: KClass<T>,
        component: T
    ) {
        if (this.type == type) {
            @Suppress("UNCHECKED_CAST")
            add(entity, component as A)
        }
    }

    override fun <T : Any> replaceComponent(
        world: World,
        entity: Entity,
        type: KClass<T>,
        component: T
    ) {
        if (this.type == type) {
            @Suppress("UNCHECKED_CAST")
            replace(entity, component as A)
        }
    }

    override fun removeComponent(entity: Entity, type: KClass<out Any>) {
        if (this.type == type) {
            remove(entity)
        }
    }

    private fun replace(entity: Entity, component: A) {
        val index = indexOf(entity)
        if (index >= 0) {
            components[index] = component
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
        sparse[Entity(lastEntity).id] = index
        components[lastIndex] = null
        count -= 1
    }

    private fun indexOf(entity: Entity): Int {
        val denseIndex = sparse.getOrNull(entity.id) ?: return -1
        return if (denseIndex in 0 until count && entities[denseIndex] == entity.packed) {
            denseIndex
        } else {
            -1
        }
    }

    private fun ensureSparseCapacity(id: Int) {
        if (id < sparse.size) {
            return
        }
        val previousSize = sparse.size
        val newSize = maxOf(id + 1, maxOf(DEFAULT_FAMILY_CAPACITY, previousSize * CAPACITY_GROWTH_FACTOR))
        sparse = sparse.copyOf(newSize)
        sparse.fill(ABSENT, fromIndex = previousSize, toIndex = newSize)
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
    private val typeB: KClass<B>
) : FamilyCache() {
    @PublishedApi
    internal var entities = LongArray(DEFAULT_FAMILY_CAPACITY)
    @PublishedApi
    internal var componentsA = arrayOfNulls<Any>(DEFAULT_FAMILY_CAPACITY)
    @PublishedApi
    internal var componentsB = arrayOfNulls<Any>(DEFAULT_FAMILY_CAPACITY)
    @PublishedApi
    internal var count: Int = 0
    // Sparse index (entity id -> dense index) so remove/replace don't need a linear scan --
    // same trick ComponentStore/Family1Cache use.
    private var sparse = IntArray(0)

    val size: Int get() = count

    fun add(entity: Entity, componentA: A, componentB: B) {
        ensureSparseCapacity(entity.id)
        ensureCapacity(count + 1)
        sparse[entity.id] = count
        entities[count] = entity.packed
        componentsA[count] = componentA
        componentsB[count] = componentB
        count += 1
    }

    @PublishedApi
    internal inline fun forEach(block: (Entity, A, B) -> Unit) {
        val localEntities = entities
        val localComponentsA = componentsA
        val localComponentsB = componentsB
        val localCount = count
        for (index in 0 until localCount) {
            @Suppress("UNCHECKED_CAST")
            block(Entity(localEntities[index]), localComponentsA[index] as A, localComponentsB[index] as B)
        }
    }

    @PublishedApi
    internal inline fun forEachComponents(block: (A, B) -> Unit) {
        val localComponentsA = componentsA
        val localComponentsB = componentsB
        val localCount = count
        for (index in 0 until localCount) {
            @Suppress("UNCHECKED_CAST")
            block(localComponentsA[index] as A, localComponentsB[index] as B)
        }
    }

    override fun remove(entity: Entity) {
        removeAt(indexOf(entity))
    }

    override fun <T : Any> addComponent(
        world: World,
        entity: Entity,
        type: KClass<T>,
        component: T
    ) {
        if (typeA == type || typeB == type) {
            upsertIfMatched(world, entity)
        }
    }

    override fun <T : Any> replaceComponent(
        world: World,
        entity: Entity,
        type: KClass<T>,
        component: T
    ) {
        val index = indexOf(entity)
        if (index < 0) {
            return
        }
        if (typeA == type) {
            @Suppress("UNCHECKED_CAST")
            componentsA[index] = component as A
        } else if (typeB == type) {
            @Suppress("UNCHECKED_CAST")
            componentsB[index] = component as B
        }
    }

    override fun removeComponent(entity: Entity, type: KClass<out Any>) {
        if (typeA == type || typeB == type) {
            remove(entity)
        }
    }

    private fun upsertIfMatched(world: World, entity: Entity) {
        val componentA = world.get(entity, typeA) ?: return
        val componentB = world.get(entity, typeB) ?: return
        val index = indexOf(entity)
        if (index >= 0) {
            componentsA[index] = componentA
            componentsB[index] = componentB
        } else {
            add(entity, componentA, componentB)
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
        sparse[Entity(lastEntity).id] = index
        componentsA[lastIndex] = null
        componentsB[lastIndex] = null
        count -= 1
    }

    private fun indexOf(entity: Entity): Int {
        val denseIndex = sparse.getOrNull(entity.id) ?: return -1
        return if (denseIndex in 0 until count && entities[denseIndex] == entity.packed) {
            denseIndex
        } else {
            -1
        }
    }

    private fun ensureSparseCapacity(id: Int) {
        if (id < sparse.size) {
            return
        }
        val previousSize = sparse.size
        val newSize = maxOf(id + 1, maxOf(DEFAULT_FAMILY_CAPACITY, previousSize * CAPACITY_GROWTH_FACTOR))
        sparse = sparse.copyOf(newSize)
        sparse.fill(ABSENT, fromIndex = previousSize, toIndex = newSize)
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

private const val INT_BITS = 32
private const val LOW_INT_MASK = 0xFFFF_FFFFL
private const val DEFAULT_FAMILY_CAPACITY = 16
private const val CAPACITY_GROWTH_FACTOR = 2
private const val ABSENT = -1
