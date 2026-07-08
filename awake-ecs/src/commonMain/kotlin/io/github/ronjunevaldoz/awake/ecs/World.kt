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

@Suppress("TooManyFunctions")
class World {
    private val slots = mutableListOf<EntitySlot>()
    private val freeIds = mutableListOf<Int>()
    private val stores = mutableMapOf<KClass<out Any>, ComponentStore<Any>>()
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
        val previous = store(type).add(entity, component)
        if (previous == null) {
            markQueriesDirty()
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
        val store = storeOrNull(type) ?: return
        store.forEach { entity, component ->
            block(entity, component)
        }
    }

    inline fun <reified A : Any> queryEach(noinline block: (Entity, A) -> Unit) {
        queryEach(A::class, block)
    }

    fun <A : Any, B : Any> queryEach(
        typeA: KClass<A>,
        typeB: KClass<B>,
        block: (Entity, A, B) -> Unit
    ) {
        val storeA = storeOrNull(typeA) ?: return
        val storeB = storeOrNull(typeB) ?: return
        if (storeA.size <= storeB.size) {
            storeA.forEach { entity, componentA ->
                val componentB = storeB.get(entity)
                if (componentB != null) {
                    block(entity, componentA, componentB)
                }
            }
        } else {
            storeB.forEach { entity, componentB ->
                val componentA = storeA.get(entity)
                if (componentA != null) {
                    block(entity, componentA, componentB)
                }
            }
        }
    }

    inline fun <reified A : Any, reified B : Any> queryEach(noinline block: (Entity, A, B) -> Unit) {
        queryEach(A::class, B::class, block)
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
