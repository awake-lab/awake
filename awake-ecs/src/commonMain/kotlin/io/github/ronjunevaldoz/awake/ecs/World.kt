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
 * Owns entity allocation/recycling and one [ComponentStore] per component type. Family-cache
 * bookkeeping (the maintained [Family1Cache]/[Family2Cache]/[FamilySpecCache] instances
 * backing [family]/[queryEach]) lives in [FamilyRegistry] -- see `Families.kt`/
 * `FamilySpec.kt`/`FamilyRegistry.kt`. Not thread-safe; this ECS is single-threaded by
 * design (see the `game-framework-dev`/`ecs-dev` agent docs).
 */
class World {
    private var entityGenerations = IntArray(DEFAULT_CAPACITY)
    private var entityAlive = LongArray(DEFAULT_CAPACITY / 64 + 1)
    private var entitySignatures = LongArray(DEFAULT_CAPACITY)
    private var entitiesCount = 0

    private val recycledEntityIds = EntityIdStack()

    /** Dense array of stores indexed by [ComponentTypeId.value]. Replaces a [MutableMap]
     * so component access can skip a hash lookup once the type id is known. */
    private var stores = arrayOfNulls<ComponentStore<Any>>(16)
    
    private val componentPools = mutableMapOf<KClass<out Any>, ComponentPool<Any>>()

    private val typeIds = mutableMapOf<KClass<out Any>, ComponentTypeId>()
    private val queryCache = mutableMapOf<QueryKey, CachedQuery>()
    private val familyRegistry = FamilyRegistry(this)
    private var queryVersion = 0

    fun create(): Entity {
        val recycledId = recycledEntityIds.pop()
        if (recycledId >= 0) {
            setAlive(recycledId, true)
            return Entity.of(recycledId, entityGenerations[recycledId])
        }

        val nextId = entitiesCount++
        ensureCapacity(nextId)
        entityGenerations[nextId] = 0
        setAlive(nextId, true)
        return Entity.of(nextId, 0)
    }

    /** Creates a new entity and initializes a pooled component of type [T]. */
    inline fun <reified T : Any> spawn(block: (T) -> Unit = {}): Entity {
        val entity = create()
        val component = add<T>(entity)
        block(component)
        return entity
    }

    fun destroy(entity: Entity): Boolean {
        if (!isAlive(entity)) {
            return false
        }

        familyRegistry.removeEntity(entity)
        val id = entity.id
        
        // Only clear stores that actually contain this entity's components
        if (entitySignatures[id] != 0L) {
            forEachStore { store ->
                val removed = store.remove(entity)
                if (removed != null) {
                    recycle(removed)
                }
            }
        }
        
        setAlive(id, false)
        entityGenerations[id] += 1
        entitySignatures[id] = 0L // Clear component signature
        recycledEntityIds.push(id)
        markQueriesDirty()
        return true
    }

    fun isAlive(entity: Entity): Boolean {
        val id = entity.id
        if (id < 0 || id >= entitiesCount) return false
        return isAlive(id) && entityGenerations[id] == entity.generation
    }

    private fun isAlive(id: Int): Boolean {
        val wordIndex = id ushr 6
        return if (wordIndex < entityAlive.size) {
            (entityAlive[wordIndex] and (1L shl (id and 63))) != 0L
        } else {
            false
        }
    }

    private fun setAlive(id: Int, alive: Boolean) {
        val wordIndex = id ushr 6
        val bit = 1L shl (id and 63)
        if (alive) {
            entityAlive[wordIndex] = entityAlive[wordIndex] or bit
        } else {
            entityAlive[wordIndex] = entityAlive[wordIndex] and bit.inv()
        }
    }

    internal fun getSignature(id: Int): Long {
        return if (id in 0 until entitiesCount) entitySignatures[id] else 0L
    }

    /** Reified sugar for [add] with pooling support. */
    inline fun <reified T : Any> add(entity: Entity): T {
        return add(entity, T::class)
    }

    /** Adds a component of [type] to [entity], obtaining an instance from the pool if available.
     * Requires the component to have been registered with a factory via [registerPool] or
     * to have a zero-arg constructor. */
    fun <T : Any> add(entity: Entity, type: KClass<T>): T {
        val pool = pool(type)
        @Suppress("UNCHECKED_CAST")
        val instance = pool.obtain() as T
        add(entity, type, instance)
        return instance
    }

    inline fun <reified T : Any> add(entity: Entity, component: T): T? {
        return add(entity, T::class, component)
    }

    fun <T : Any> add(entity: Entity, type: KClass<T>, component: T): T? {
        requireAlive(entity)
        val typeId = typeId(type)
        val previous = store(typeId, type).add(entity, component)
        if (previous == null) {
            val id = entity.id
            entitySignatures[id] = entitySignatures[id] or (1L shl typeId.value)
            markQueriesDirty()
            familyRegistry.addComponent(entity, typeId, type, component)
        } else {
            recycle(previous)
            familyRegistry.replaceComponent(entity, typeId, type, component)
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
        val typeId = typeIds[type] ?: return null
        return storeOrNull<T>(typeId)?.get(entity)
    }

    inline fun <reified T : Any> remove(entity: Entity): T? {
        return remove(entity, T::class)
    }

    fun <T : Any> remove(entity: Entity, type: KClass<T>): T? {
        if (!isAlive(entity)) {
            return null
        }
        val typeId = typeId(type)
        val removed = storeOrNull<T>(typeId)?.remove(entity)
        if (removed != null) {
            val id = entity.id
            entitySignatures[id] = entitySignatures[id] and (1L shl typeId.value).inv()
            markQueriesDirty()
            familyRegistry.removeComponent(entity, typeId, type)
            recycle(removed)
        }
        return removed
    }

    inline fun <reified T : Any> has(entity: Entity): Boolean {
        return has(entity, T::class)
    }

    fun has(entity: Entity, type: KClass<out Any>): Boolean {
        if (!isAlive(entity)) return false
        val typeId = typeIds[type] ?: return false
        return (entitySignatures[entity.id] and (1L shl typeId.value)) != 0L
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
        @Suppress("UNCHECKED_CAST")
        componentPools[type] = ComponentPool(factory) as ComponentPool<Any>
    }

    /** Automatically returns [component] to its type's pool. */
    fun recycle(component: Any) {
        componentPools[component::class]?.free(component)
    }

    private fun pool(type: KClass<out Any>): ComponentPool<Any> {
        return componentPools.getOrPut(type) {
            ComponentPool { 
                @Suppress("UNCHECKED_CAST")
                val clazz = type.javaObjectType as Class<Any>
                clazz.getDeclaredConstructor().newInstance()
            }
        }
    }

    fun clear() {
        entitiesCount = 0
        entityGenerations.fill(0)
        entityAlive.fill(0L)
        entitySignatures.fill(0L)
        recycledEntityIds.clear()
        forEachStore { it.clear() }
        stores.fill(null)
        typeIds.clear()
        familyRegistry.clear()
        queryCache.clear()
        componentPools.values.forEach { it.clear() }
        markQueriesDirty()
    }

    fun componentCount(type: KClass<out Any>): Int {
        val typeId = typeIds[type] ?: return 0
        return storeOrNull<Any>(typeId)?.size ?: 0
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> store(type: KClass<T>): ComponentStore<T> {
        return store(typeId(type), type)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> store(typeId: ComponentTypeId, type: KClass<T>): ComponentStore<T> {
        val id = typeId.value
        if (id < stores.size) {
            val existing = stores[id]
            if (existing != null) return existing as ComponentStore<T>
        }

        val store: ComponentStore<T> = ComponentStore(type)
        ensureStoreCapacity(id)
        stores[id] = store as ComponentStore<Any>
        return store
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> storeOrNull(typeId: ComponentTypeId): ComponentStore<T>? {
        val id = typeId.value
        return if (id < stores.size) stores[id] as? ComponentStore<T> else null
    }

    private fun ensureCapacity(id: Int) {
        if (id >= entityGenerations.size) {
            val newCapacity = maxOf(id + 1, entityGenerations.size * 2)
            entityGenerations = entityGenerations.copyOf(newCapacity)
            entitySignatures = entitySignatures.copyOf(newCapacity)
            entityAlive = entityAlive.copyOf(newCapacity / 64 + 1)
        }
    }

    private fun ensureStoreCapacity(id: Int) {
        if (id >= stores.size) {
            stores = stores.copyOf(maxOf(id + 1, stores.size * 2))
        }
    }

    private inline fun forEachStore(action: (ComponentStore<Any>) -> Unit) {
        stores.forEach { it?.let(action) }
    }

    /** Package-visible for [FamilyRegistry], which needs to build a [FamilySpecCache]'s
     * initial membership by scanning every currently-alive entity. */
    internal fun collectQuery(types: Set<KClass<out Any>>): List<Entity> {
        return if (types.isEmpty()) {
            (0 until entitiesCount)
                .filter(::isAlive)
                .map { Entity.of(it, entityGenerations[it]) }
        } else {
            val queryStores = types.mapNotNull { type ->
                typeIds[type]?.let { storeOrNull<Any>(it) }
            }
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

    private fun requireAlive(entity: Entity) {
        require(isAlive(entity)) { "Entity is not alive: $entity" }
    }

    private fun markQueriesDirty() {
        queryVersion += 1
    }

    /** Package-visible for [FamilyRegistry]'s [FamilyKey] construction. */
    internal fun typeId(type: KClass<out Any>): ComponentTypeId {
        return typeIds.getOrPut(type) {
            ComponentTypeId(typeIds.size)
        }
    }

    private data class QueryKey(
        val types: Set<KClass<out Any>>
    )

    private data class CachedQuery(
        val entities: MutableList<Entity> = mutableListOf(),
        var version: Int = -1
    )

    private companion object {
        const val DEFAULT_CAPACITY = 16
    }
}
