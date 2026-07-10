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
 * Public ECS facade. Lifecycle state, component storage/pooling, query collection, and
 * query caching live in [EntityArena], [ComponentRegistry], [QueryCollector], and
 * [QueryCache]; [FamilyRegistry] keeps maintained families in sync with structural changes.
 */
class World {
    private val entities = EntityArena()
    private val components = ComponentRegistry()
    private val queryCollector = QueryCollector(entities, components)
    private val queries = QueryCache(queryCollector::collect)
    private val familyRegistry = FamilyRegistry(this, queryCollector)

    fun create(): Entity {
        val entity = entities.create()
        queries.markDirty()
        return entity
    }

    /** Creates a new entity and initializes a pooled component of type [T]. */
    inline fun <reified T : Any> spawn(block: (T) -> Unit = {}): Entity {
        val entity = create()
        val component = add<T>(entity)
        block(component)
        return entity
    }

    /** Fast path for callers spawning many entities of the same pooled component type in a
     * loop -- takes an already-resolved [typeId] (see [typeId]) instead of the reified
     * [spawn], so the caller can hoist `T::class`'s resolution outside the loop instead of
     * paying a fresh `KClass` lookup (and, without `kotlin-reflect`, a fresh `ClassReference`
     * allocation) on every entity. */
    fun <T : Any> spawn(typeId: ComponentTypeId, block: (T) -> Unit = {}): Entity {
        val entity = create()
        val component = add<T>(entity, typeId)
        block(component)
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
            queries.markDirty()
        }
        return destroyed
    }

    fun isAlive(entity: Entity): Boolean {
        return entities.isAlive(entity)
    }

    internal fun getSignature(id: Int): Long {
        return entities.signature(id)
    }

    /** Reified sugar for [add] with pooling support. */
    inline fun <reified T : Any> add(entity: Entity): T {
        return add(entity, T::class)
    }

    /** Adds a component of [type] to [entity], obtaining an instance from the pool if available.
     * Requires the component to have been registered with a factory via [registerPool] or
     * to have a zero-arg constructor (JVM only). */
    fun <T : Any> add(entity: Entity, type: KClass<T>): T {
        val pool = components.pool(type)
        @Suppress("UNCHECKED_CAST")
        val instance = pool.obtain() as T
        add(entity, type, instance)
        return instance
    }

    /** Reified sugar for [add] -- resolves the type id via [componentTypeKey] instead of a fresh
     * `KClass` per call (see that function's doc comment for why this matters: without
     * `kotlin-reflect`, a bare `T::class` inside a reified inline function re-derives a
     * `ClassReference` wrapper on every single call, confirmed by decompiling the generated
     * bytecode). This gives the default reified API every caller already uses the same
     * steady-state cost as a caller hand-hoisting a `ComponentTypeId` (see "Hot-path
     * performance" in this module's agent doc) -- no opt-in required. */
    inline fun <reified T : Any> add(entity: Entity, component: T): T? {
        val typeId = resolveReifiedTypeId(componentTypeKey<T>()) { T::class }
        return addReified(entity, typeId, { T::class }, component)
    }

    fun <T : Any> add(entity: Entity, type: KClass<T>, component: T): T? {
        requireAlive(entity)
        val typeId = components.typeId(type)
        val previous = components.store(typeId, type).add(entity, component)
        if (previous == null) {
            entities.markComponentAdded(entity.id, typeId)
            queries.markDirty()
            familyRegistry.addComponent(entity, typeId, component)
        } else {
            components.recycle(typeId, previous)
            familyRegistry.replaceComponent(entity, typeId, component)
        }
        return previous
    }

    /** Shared body for the reified [add] sugar above -- kept as a regular (non-inline) member so
     * it can touch private state, and marked `@PublishedApi internal` so the public inline
     * function above can call it from any module. Unlike the `ComponentTypeId` overload of [add]
     * (which requires the store to already exist), this lazily creates it via [type] if missing,
     * since the reified sugar must work on a type's very first `add` with no prior setup. */
    @PublishedApi
    internal fun <T : Any> addReified(
        entity: Entity,
        typeId: ComponentTypeId,
        type: () -> KClass<T>,
        component: T
    ): T? {
        requireAlive(entity)
        val store = components.storeForKey(typeId, type)
        val previous = store.add(entity, component)
        if (previous == null) {
            entities.markComponentAdded(entity.id, typeId)
            queries.markDirty()
            familyRegistry.addComponent(entity, typeId, component)
        } else {
            components.recycle(typeId, previous)
            familyRegistry.replaceComponent(entity, typeId, component)
        }
        return previous
    }

    /** Resolves (or registers) a reified type's id via its [componentTypeKey] token -- shared by
     * every reified sugar function below. `@PublishedApi internal` so it's callable from the
     * public inline functions that use it. */
    @PublishedApi
    internal fun <T : Any> resolveReifiedTypeId(key: Any, type: () -> KClass<T>): ComponentTypeId {
        return components.typeIdForKey(key, type)
    }

    /** [resolveReifiedTypeId]'s `get`/`has` counterpart -- doesn't register a new type on a miss,
     * matching [typeIdOrNull]-style "unknown type means null" semantics for reads. */
    @PublishedApi
    internal fun resolveReifiedTypeIdOrNull(key: Any): ComponentTypeId? {
        return components.typeIdForKeyOrNull(key)
    }

    /** Fast path for callers that already cached [typeId] for a component whose store has
     * been initialized already through the [KClass]-based API. Avoids the per-call
     * [KClass] lookup in the hot path. */
    fun <T : Any> add(entity: Entity, typeId: ComponentTypeId, component: T): T? {
        requireAlive(entity)
        val store = components.storeOrNull<T>(typeId)
            ?: error("Component store for type id ${typeId.value} is not initialized.")
        val previous = store.add(entity, component)
        if (previous == null) {
            entities.markComponentAdded(entity.id, typeId)
            queries.markDirty()
            familyRegistry.addComponent(entity, typeId, component)
        } else {
            components.recycle(typeId, previous)
            familyRegistry.replaceComponent(entity, typeId, component)
        }
        return previous
    }

    /** Fast path for pooled components when the caller already cached [typeId]. */
    fun <T : Any> add(entity: Entity, typeId: ComponentTypeId): T {
        val pool = components.pool(typeId)
            ?: error("Component pool for type id ${typeId.value} is not registered.")
        @Suppress("UNCHECKED_CAST")
        val instance = pool.obtain() as T
        add(entity, typeId, instance)
        return instance
    }

    /** Reified sugar for [get] -- see [add]'s reified overload for why this avoids a per-call
     * `T::class` derivation. */
    inline fun <reified T : Any> get(entity: Entity): T? {
        val typeId = resolveReifiedTypeIdOrNull(componentTypeKey<T>()) ?: return null
        return getReified(entity, typeId)
    }

    @PublishedApi
    internal fun <T : Any> getReified(entity: Entity, typeId: ComponentTypeId): T? {
        if (!entities.isAlive(entity)) {
            return null
        }
        return components.storeOrNull<T>(typeId)?.get(entity)
    }

    fun <T : Any> get(entity: Entity, type: KClass<T>): T? {
        if (!entities.isAlive(entity)) {
            return null
        }
        val typeId = components.typeIdOrNull(type) ?: return null
        return components.storeOrNull<T>(typeId)?.get(entity)
    }

    fun <T : Any> get(entity: Entity, typeId: ComponentTypeId): T? {
        if (!entities.isAlive(entity)) {
            return null
        }
        return components.storeOrNull<T>(typeId)?.get(entity)
    }

    /** Reified sugar for [remove] -- see [add]'s reified overload for why this avoids a per-call
     * `T::class` derivation. */
    inline fun <reified T : Any> remove(entity: Entity): T? {
        val typeId = resolveReifiedTypeId(componentTypeKey<T>()) { T::class }
        return removeReified(entity, typeId)
    }

    @PublishedApi
    internal fun <T : Any> removeReified(entity: Entity, typeId: ComponentTypeId): T? {
        if (!entities.isAlive(entity)) {
            return null
        }
        val removed = components.storeOrNull<T>(typeId)?.remove(entity)
        if (removed != null) {
            entities.markComponentRemoved(entity.id, typeId)
            queries.markDirty()
            familyRegistry.removeComponent(entity, typeId)
            components.recycle(typeId, removed)
        }
        return removed
    }

    fun <T : Any> remove(entity: Entity, type: KClass<T>): T? {
        if (!entities.isAlive(entity)) {
            return null
        }
        val typeId = components.typeId(type)
        val removed = components.storeOrNull<T>(typeId)?.remove(entity)
        if (removed != null) {
            entities.markComponentRemoved(entity.id, typeId)
            queries.markDirty()
            familyRegistry.removeComponent(entity, typeId)
            components.recycle(typeId, removed)
        }
        return removed
    }

    fun <T : Any> remove(entity: Entity, typeId: ComponentTypeId): T? {
        if (!entities.isAlive(entity)) {
            return null
        }
        val removed = components.storeOrNull<T>(typeId)?.remove(entity)
        if (removed != null) {
            entities.markComponentRemoved(entity.id, typeId)
            queries.markDirty()
            familyRegistry.removeComponent(entity, typeId)
            components.recycle(typeId, removed)
        }
        return removed
    }

    /** Reified sugar for [has] -- see [add]'s reified overload for why this avoids a per-call
     * `T::class` derivation. */
    inline fun <reified T : Any> has(entity: Entity): Boolean {
        val typeId = resolveReifiedTypeIdOrNull(componentTypeKey<T>()) ?: return false
        return has(entity, typeId)
    }

    fun has(entity: Entity, type: KClass<out Any>): Boolean {
        if (!entities.isAlive(entity)) return false
        val typeId = components.typeIdOrNull(type) ?: return false
        return entities.has(entity, typeId)
    }

    fun has(entity: Entity, typeId: ComponentTypeId): Boolean {
        return entities.has(entity, typeId)
    }

    fun query(vararg types: KClass<out Any>): List<Entity> {
        return queries.query(types.toSet())
    }

    fun queryEach(vararg types: KClass<out Any>, block: (Entity) -> Unit) {
        query(*types).forEach(block)
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

    /** An arbitrary-arity family via `all`/`one`/`exclude` -- e.g.
     * `world.family { all(Transform::class, MeshRenderer::class, Light::class) }`. Prefer
     * the typed [family] overloads above for a 1-2-component query (they return matched
     * components directly, not just entities) -- reach for this when a query needs 3+
     * types or `one`/`exclude` semantics neither of those support. See [Family]'s doc
     * comment for why this is a separate mechanism from [Family1]/[Family2] rather than a
     * generalized replacement for them. */
    fun family(configure: FamilySpecBuilder.() -> Unit): Family {
        val spec = FamilySpecBuilder().apply(configure).build()
        return Family(familyRegistry.familySpecCache(spec))
    }

    inline fun <reified T : Any> query(): List<Entity> {
        return query(T::class)
    }

    /** Registers a factory for [type] to enable component pooling. */
    fun <T : Any> registerPool(type: KClass<T>, factory: () -> T) {
        components.registerPool(type, factory)
    }

    /** Automatically returns [component] to its type's pool. */
    fun recycle(component: Any) {
        components.recycle(component)
    }

    internal fun <T : Any> recycle(type: KClass<T>, component: T) {
        components.recycle(type, component)
    }

    internal fun recycle(typeId: ComponentTypeId, component: Any) {
        components.recycle(typeId, component)
    }

    fun clear() {
        entities.clear()
        components.clear()
        familyRegistry.clear()
        queries.clear()
    }

    fun componentCount(type: KClass<out Any>): Int {
        return components.componentCount(type)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> store(type: KClass<T>): ComponentStore<T> {
        return store(typeId(type), type)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> store(typeId: ComponentTypeId, type: KClass<T>): ComponentStore<T> {
        return components.store(typeId, type)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> storeOrNull(typeId: ComponentTypeId): ComponentStore<T>? {
        return components.storeOrNull(typeId)
    }

    private fun requireAlive(entity: Entity) {
        require(entities.isAlive(entity)) { "Entity is not alive: $entity" }
    }

    /** Returns the stable component id assigned to [type] within this world. */
    fun typeId(type: KClass<out Any>): ComponentTypeId {
        return components.typeId(type)
    }
}
