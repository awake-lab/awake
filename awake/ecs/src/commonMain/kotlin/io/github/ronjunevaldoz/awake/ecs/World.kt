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
 * Public facade for the ECS world. Coordinates internal managers for entity lifecycle,
 * component storage, and query caching.
 *
 * Not thread-safe; this ECS is single-threaded by design (see the `game-framework-dev`/
 * `ecs-dev` agent docs).
 */
class World {
    @PublishedApi internal val entities = EntityArena()
    @PublishedApi internal val components = ComponentRegistry()
    private val collector = QueryCollector(entities, components)
    private val familyRegistry = FamilyRegistry(this, collector)
    private val queryCache = QueryCache { types -> collector.collect(types) }

    fun create(): Entity {
        val entity = entities.create()
        queryCache.markEmptyQueriesDirty()
        return entity
    }

    fun destroy(entity: Entity): Boolean {
        if (!entities.isAlive(entity)) {
            return false
        }

        familyRegistry.removeEntity(entity)
        components.removeEntity(entity, entities.signature(entity.id))
        
        val destroyed = entities.destroy(entity)
        if (destroyed) {
            queryCache.markAllQueriesDirty()
        }
        return destroyed
    }

    fun isAlive(entity: Entity): Boolean {
        return entities.isAlive(entity)
    }

    inline fun <reified T : Any> add(entity: Entity): T {
        return add(entity, T::class)
    }

    fun <T : Any> add(entity: Entity, type: KClass<T>): T {
        val instance = components.pool(type).obtain() as T
        add(entity, type, instance)
        return instance
    }

    inline fun <reified T : Any> add(entity: Entity, component: T): T? {
        val typeId = components.typeIdForKey(componentTypeKey<T>()) { T::class }
        return addInternal(entity, typeId, T::class, component)
    }

    fun <T : Any> add(entity: Entity, type: KClass<T>, component: T): T? {
        return addInternal(entity, components.typeId(type), type, component)
    }

    @PublishedApi
    internal fun <T : Any> addInternal(entity: Entity, typeId: ComponentTypeId, type: KClass<T>, component: T): T? {
        requireAlive(entity)
        val previous = components.store(typeId, type).add(entity, component)
        if (previous == null) {
            entities.markComponentAdded(entity.id, typeId)
            queryCache.markAllQueriesDirty()
            familyRegistry.addComponent(entity, typeId, component)
        } else {
            components.recycle(type, previous)
            familyRegistry.replaceComponent(entity, typeId, component)
        }
        return previous
    }

    inline fun <reified T : Any> get(entity: Entity): T? {
        val typeId = components.typeIdForKeyOrNull(componentTypeKey<T>()) ?: return null
        return getInternal<T>(entity, typeId)
    }

    fun <T : Any> get(entity: Entity, type: KClass<T>): T? {
        val typeId = components.typeIdOrNull(type) ?: return null
        return getInternal<T>(entity, typeId)
    }

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> getInternal(entity: Entity, typeId: ComponentTypeId): T? {
        if (!entities.isAlive(entity)) {
            return null
        }
        return components.storeOrNull<T>(typeId)?.get(entity)
    }

    inline fun <reified T : Any> remove(entity: Entity): T? {
        val typeId = components.typeIdForKeyOrNull(componentTypeKey<T>()) ?: return null
        return removeInternal(entity, typeId, T::class)
    }

    fun <T : Any> remove(entity: Entity, type: KClass<T>): T? {
        val typeId = components.typeIdOrNull(type) ?: return null
        return removeInternal(entity, typeId, type)
    }

    @PublishedApi
    internal fun <T : Any> removeInternal(entity: Entity, typeId: ComponentTypeId, @Suppress("unused") type: KClass<T>): T? {
        if (!entities.isAlive(entity)) {
            return null
        }
        val removed = components.storeOrNull<T>(typeId)?.remove(entity)
        if (removed != null) {
            entities.markComponentRemoved(entity.id, typeId)
            queryCache.markAllQueriesDirty()
            familyRegistry.removeComponent(entity, typeId)
            components.recycle(typeId, removed)
        }
        return removed
    }

    inline fun <reified T : Any> has(entity: Entity): Boolean {
        val typeId = components.typeIdForKeyOrNull(componentTypeKey<T>()) ?: return false
        return hasInternal(entity, typeId)
    }

    fun has(entity: Entity, type: KClass<out Any>): Boolean {
        val typeId = components.typeIdOrNull(type) ?: return false
        return hasInternal(entity, typeId)
    }

    @PublishedApi
    internal fun hasInternal(entity: Entity, typeId: ComponentTypeId): Boolean {
        return entities.has(entity, typeId)
    }

    fun query(vararg types: KClass<out Any>): List<Entity> {
        return queryCache.query(types.toSet())
    }

    fun <A : Any> queryEach(type: KClass<A>, block: (Entity, A) -> Unit) {
        familyRegistry.familyCache(type).forEach(block)
    }

    inline fun <reified A : Any> queryEach(noinline block: (Entity, A) -> Unit) {
        queryEach(A::class, block)
    }

    fun <A : Any> family(type: KClass<A>): Family1<A> {
        return Family1(familyRegistry.familyCache(type))
    }

    inline fun <reified A : Any> family(): Family1<A> {
        return family(A::class)
    }

    fun <A : Any, B : Any> queryEach(
        typeA: KClass<A>,
        typeB: KClass<B>,
        block: (Entity, A, B) -> Unit
    ) {
        familyRegistry.familyCache(typeA, typeB).forEach(block)
    }

    inline fun <reified A : Any, reified B : Any> queryEach(noinline block: (Entity, A, B) -> Unit) {
        queryEach(A::class, B::class, block)
    }

    fun <A : Any, B : Any> family(typeA: KClass<A>, typeB: KClass<B>): Family2<A, B> {
        return Family2(familyRegistry.familyCache(typeA, typeB))
    }

    inline fun <reified A : Any, reified B : Any> family(): Family2<A, B> {
        return family(A::class, B::class)
    }

    fun family(configure: FamilySpecBuilder.() -> Unit): Family {
        val spec = FamilySpecBuilder().apply(configure).build()
        return Family(familyRegistry.familySpecCache(spec))
    }

    inline fun <reified T : Any> query(): List<Entity> {
        return query(T::class)
    }

    fun registerPool(type: KClass<out Any>, factory: () -> Any) {
        @Suppress("UNCHECKED_CAST")
        components.registerPool(type as KClass<Any>, factory)
    }

    inline fun <reified T : Any> registerPool(noinline factory: () -> T) {
        registerPool(T::class, factory)
    }

    fun clear() {
        entities.clear()
        components.clear()
        familyRegistry.clear()
        queryCache.clear()
    }

    fun componentCount(type: KClass<out Any>): Int {
        return components.componentCount(type)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> store(type: KClass<T>): ComponentStore<T> {
        return components.store(typeId(type), type)
    }

    @PublishedApi
    internal fun <T : Any> store(typeId: ComponentTypeId, type: KClass<T>): ComponentStore<T> {
        return components.store(typeId, type)
    }

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> storeOrNull(typeId: ComponentTypeId): ComponentStore<T>? {
        return components.storeOrNull(typeId)
    }

    internal fun collectQuery(types: Set<KClass<out Any>>): List<Entity> {
        return collector.collect(types)
    }

    internal fun getSignature(id: Int): Long {
        return entities.signature(id)
    }

    private fun requireAlive(entity: Entity) {
        require(entities.isAlive(entity)) { "Entity is not alive: $entity" }
    }

    fun typeId(type: KClass<out Any>): ComponentTypeId {
        return components.typeId(type)
    }

    // --- Helpers for EcsOptimizationTest.kt that expects overloads taking ComponentTypeId

    inline fun <reified T : Any> add(entity: Entity, typeId: ComponentTypeId): T {
        val instance = components.pool(typeId)?.obtain() ?: components.pool(T::class).obtain()
        addInternal(entity, typeId, T::class, instance as T)
        return instance as T
    }

    inline fun <reified T : Any> add(entity: Entity, typeId: ComponentTypeId, component: T): T? {
        return addInternal(entity, typeId, T::class, component)
    }

    inline fun <reified T : Any> get(entity: Entity, typeId: ComponentTypeId): T? {
        return getInternal<T>(entity, typeId)
    }

    inline fun <reified T : Any> remove(entity: Entity, typeId: ComponentTypeId): T? {
        return removeInternal(entity, typeId, T::class)
    }

    fun has(entity: Entity, typeId: ComponentTypeId): Boolean {
        return hasInternal(entity, typeId)
    }
}
