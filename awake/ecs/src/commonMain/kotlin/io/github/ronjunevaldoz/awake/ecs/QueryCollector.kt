// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass

/**
 * Collects the current matching entity list for a query.
 *
 * Kept separate from [World] so query collection can evolve independently from the public
 * facade while [QueryCache] still owns caching and invalidation.
 */
internal class QueryCollector(
    private val entities: EntityArena,
    private val components: ComponentRegistry
) {
    fun collect(types: Set<KClass<out Any>>): List<Entity> {
        if (types.isEmpty()) {
            val count = entities.count
            val results = ArrayList<Entity>(count)
            for (id in 0 until count) {
                if (entities.isAlive(id)) {
                    results += entities.entity(id)
                }
            }
            return results
        }

        // Plain array instead of `types.mapNotNull { ... }` to avoid the intermediate List
        // and re-walking it once per candidate entity via `filter { queryStores.all { ... } }`.
        val queryStores = arrayOfNulls<ComponentStore<Any>>(types.size)
        var resolvedIndex = 0
        for (type in types) {
            val typeId = components.typeIdOrNull(type) ?: return emptyList()
            val store = components.storeOrNull<Any>(typeId) ?: return emptyList()
            queryStores[resolvedIndex] = store
            resolvedIndex += 1
        }

        var smallestStore = queryStores[0]!!
        for (index in 1 until queryStores.size) {
            val store = queryStores[index]!!
            if (store.size < smallestStore.size) {
                smallestStore = store
            }
        }

        // `forEach` walks the typed dense arrays directly instead of
        // `smallestStore.entities.filter { ... }`, which would box every Entity (a value
        // class) through the `List<Entity>` interface and allocate a `filter` iterator.
        val results = ArrayList<Entity>(smallestStore.size)
        smallestStore.forEach { entity, _ ->
            if (entities.isAlive(entity) && matchesAll(queryStores, entity)) {
                results += entity
            }
        }
        return results
    }

    private fun matchesAll(stores: Array<ComponentStore<Any>?>, entity: Entity): Boolean {
        for (index in stores.indices) {
            if (!stores[index]!!.contains(entity)) {
                return false
            }
        }
        return true
    }
}
