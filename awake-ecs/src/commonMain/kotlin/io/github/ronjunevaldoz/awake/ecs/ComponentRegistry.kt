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

internal class ComponentRegistry {
    private var stores = arrayOfNulls<ComponentStore<Any>>(16)

    private val componentPools = mutableMapOf<KClass<out Any>, ComponentPool<Any>>()
    private var componentPoolsById = arrayOfNulls<ComponentPool<Any>>(16)

    private val typeIds = mutableMapOf<KClass<out Any>, ComponentTypeId>()
    private var hasComponentPools = false

    fun clear() {
        forEachStore { it.clear() }
        stores.fill(null)
        typeIds.clear()
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
            ComponentTypeId(id)
        }
    }

    fun typeIdOrNull(type: KClass<out Any>): ComponentTypeId? {
        return typeIds[type]
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
