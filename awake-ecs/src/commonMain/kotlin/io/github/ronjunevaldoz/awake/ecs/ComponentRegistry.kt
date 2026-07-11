// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass

internal class ComponentRegistry {
    private var stores = arrayOfNulls<ComponentStore<Any>>(16)

    private val componentPools = mutableMapOf<KClass<out Any>, ComponentPool<Any>>()
    private var componentPoolsById = arrayOfNulls<ComponentPool<Any>>(16)

    private val typeIds = mutableMapOf<KClass<out Any>, ComponentTypeId>()

    /** Mirrors [typeIds], but keyed by [componentTypeKey]'s platform token instead of a `KClass`
     * -- see [typeIdForKey] for why this exists and how it stays in sync with [typeIds]. */
    private val typeIdsByKey = mutableMapOf<Any, ComponentTypeId>()
    private var hasComponentPools = false

    fun clear() {
        forEachStore { it.clear() }
        stores.fill(null)
        typeIds.clear()
        typeIdsByKey.clear()
        componentPoolsById.fill(null)
        componentPools.values.forEach { it.clear() }
        hasComponentPools = componentPools.isNotEmpty()
    }

    fun componentCount(type: KClass<out Any>): Int {
        val typeId = typeIds[type] ?: return 0
        return storeOrNull<Any>(typeId)?.size ?: 0
    }

    fun <T : Any> registerPool(type: KClass<T>, factory: () -> T) {
        @Suppress("UNCHECKED_CAST")
        val pool = ComponentPool(factory) as ComponentPool<Any>
        componentPools[type] = pool
        hasComponentPools = true
        typeIds[type]?.let { typeId ->
            ensurePoolCapacity(typeId.value)
            componentPoolsById[typeId.value] = pool
        }
    }

    fun recycle(component: Any) {
        if (!hasComponentPools) {
            return
        }
        componentPools[component::class]?.free(component)
    }

    fun <T : Any> recycle(type: KClass<T>, component: T) {
        if (!hasComponentPools) {
            return
        }
        typeIds[type]?.let { recycle(it, component) } ?: componentPools[type]?.free(component)
    }

    fun recycle(typeId: ComponentTypeId, component: Any) {
        if (!hasComponentPools) {
            return
        }
        val id = typeId.value
        if (id < componentPoolsById.size) {
            componentPoolsById[id]?.free(component)
        }
    }

    fun pool(type: KClass<out Any>): ComponentPool<Any> {
        return componentPools.getOrPut(type) {
            ComponentPool {
                createComponentInstance(type)
            }
        }
    }

    fun pool(typeId: ComponentTypeId): ComponentPool<Any>? {
        val id = typeId.value
        return if (id < componentPoolsById.size) componentPoolsById[id] else null
    }

    fun <T : Any> typeId(type: KClass<T>): ComponentTypeId {
        return typeIds.getOrPut(type) {
            require(typeIds.size < MAX_COMPONENT_TYPES) {
                "Awake ECS currently supports up to $MAX_COMPONENT_TYPES component types per World."
            }
            val id = typeIds.size
            ensurePoolCapacity(id)
            componentPools[type]?.let { pool ->
                componentPoolsById[id] = pool
            }
            val typeId = ComponentTypeId(id)
            typeIdsByKey[componentTypeKeyOf(type)] = typeId
            typeId
        }
    }

    fun typeIdOrNull(type: KClass<out Any>): ComponentTypeId? {
        return typeIds[type]
    }

    /** Fast path for [World]'s reified `add`/`remove`/`get`/`has` sugar -- resolves (or
     * registers) a type id from [key] (see [componentTypeKey]) instead of a `KClass`, so the
     * reified call site never needs to derive one on the steady-state (already-registered) path.
     * [type] is only invoked -- and only ever pays a `KClass` derivation cost -- the first time
     * this component type is registered in this [ComponentRegistry], from either this method or
     * [typeId]; every call after that is a single hash lookup keyed by a cheap platform token. */
    fun <T : Any> typeIdForKey(key: Any, type: () -> KClass<T>): ComponentTypeId {
        typeIdsByKey[key]?.let { return it }
        return typeId(type())
    }

    /** Same lookup as [typeIdOrNull], but keyed by [componentTypeKey]'s token -- doesn't register
     * a new type on a miss, matching [typeIdOrNull]'s "unknown type means null, not a fresh id"
     * contract for `get`/`has`. */
    fun typeIdForKeyOrNull(key: Any): ComponentTypeId? {
        return typeIdsByKey[key]
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> store(typeId: ComponentTypeId, type: KClass<T>): ComponentStore<T> {
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
    fun <T : Any> storeOrNull(typeId: ComponentTypeId): ComponentStore<T>? {
        val id = typeId.value
        return if (id < stores.size) stores[id] as? ComponentStore<T> else null
    }

    /** Fast path for [World]'s reified `add` sugar -- unlike the strict [storeOrNull]-or-`error`
     * combination the `ComponentTypeId` overload of `World.add` uses (which requires the store to
     * already exist), this lazily creates the store via [type] if it's missing, since the reified
     * sugar has to work correctly on a brand-new component type's very first `add` with no prior
     * setup. [type] is only invoked on that first-ever miss. */
    fun <T : Any> storeForKey(typeId: ComponentTypeId, type: () -> KClass<T>): ComponentStore<T> {
        return storeOrNull(typeId) ?: store(typeId, type())
    }

    fun removeEntity(entity: Entity, signature: Long) {
        if (signature == 0L) {
            return
        }
        forEachStore { store ->
            val removed = store.remove(entity)
            if (removed != null) {
                recycle(store.type, removed)
            }
        }
    }

    private inline fun forEachStore(action: (ComponentStore<Any>) -> Unit) {
        stores.forEach { it?.let(action) }
    }

    private fun ensureStoreCapacity(id: Int) {
        if (id >= stores.size) {
            stores = stores.copyOf(maxOf(id + 1, stores.size * 2))
        }
    }

    private fun ensurePoolCapacity(id: Int) {
        if (id >= componentPoolsById.size) {
            componentPoolsById = componentPoolsById.copyOf(maxOf(id + 1, componentPoolsById.size * 2))
        }
    }

    private companion object {
        private const val MAX_COMPONENT_TYPES = Long.SIZE_BITS
    }
}
