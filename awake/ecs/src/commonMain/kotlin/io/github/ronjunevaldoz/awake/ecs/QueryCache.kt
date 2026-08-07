// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass

internal class QueryCache(
    private val collector: (Set<KClass<out Any>>) -> List<Entity>,
) {
    private val cachedQueries = mutableMapOf<QueryKey, CachedQuery>()
    private var typedQueryVersion = 0
    private var emptyQueryVersion = 0
    private var hasQueryCache = false

    fun query(types: Set<KClass<out Any>>): List<Entity> {
        val key = QueryKey(types)
        val cached = cachedQueries.getOrPut(key) { CachedQuery() }
        hasQueryCache = true
        val currentVersion = if (types.isEmpty()) emptyQueryVersion else typedQueryVersion
        if (cached.version != currentVersion) {
            cached.replaceWith(collector(key.types))
            cached.version = currentVersion
        }
        return cached.entities
    }

    /** Called when an entity is created or destroyed. */
    fun markEmptyQueriesDirty() {
        if (hasQueryCache) {
            emptyQueryVersion += 1
        }
    }

    /** Called when an entity is destroyed or its component signature changes. */
    fun markAllQueriesDirty() {
        if (hasQueryCache) {
            emptyQueryVersion += 1
            typedQueryVersion += 1
        }
    }

    fun clear() {
        cachedQueries.clear()
        typedQueryVersion = 0
        emptyQueryVersion = 0
        hasQueryCache = false
    }

    private data class QueryKey(
        val types: Set<KClass<out Any>>,
    )

    private class CachedQuery {
        private val _entities: MutableList<Entity> = mutableListOf()
        val entities: List<Entity> get() = _entities
        var version: Int = -1

        fun replaceWith(newEntities: List<Entity>) {
            _entities.clear()
            _entities.addAll(newEntities)
        }
    }
}
