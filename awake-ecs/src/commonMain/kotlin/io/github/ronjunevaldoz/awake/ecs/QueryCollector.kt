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

        // Resolves every requested type's store up front into a plain array instead of
        // `types.mapNotNull { ... }` -- avoids both the intermediate `List` allocation and
        // (via the `filter { queryStores.all { ... } }` this replaces below) re-walking that
        // list once per candidate entity. Bails out to `emptyList()` as soon as any type
        // has no store yet, same semantics as the old size-mismatch check.
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

        // Iterates the smallest store's own dense arrays via `forEach` instead of
        // `smallestStore.entities.filter { ... }` -- `entities` exposes a `List<Entity>`,
        // and `Entity` (a value class) boxes on every access through that interface, plus
        // `filter`/`Iterable.all` allocate their own iterators. `forEach` walks the typed
        // dense arrays directly (see `ComponentStore.forEach`), so entities here are never
        // boxed until they're actually added to `results`.
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
