// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass

internal class QueryCache(
    private val collector: (Set<KClass<out Any>>) -> List<Entity>
) {
    private val queryCache = mutableMapOf<QueryKey, CachedQuery>()
    private var queryVersion = 0
    private var hasQueryCache = false

    fun query(types: Set<KClass<out Any>>): List<Entity> {
        val key = QueryKey(types)
        val cached = queryCache.getOrPut(key) { CachedQuery() }
        hasQueryCache = true
        if (cached.version != queryVersion) {
            cached.entities.clear()
            cached.entities += collector(key.types)
            cached.version = queryVersion
        }
        return cached.entities
    }

    fun markDirty() {
        if (hasQueryCache) {
            queryVersion += 1
        }
    }

    fun clear() {
        queryCache.clear()
        queryVersion = 0
        hasQueryCache = false
    }

    private data class QueryKey(
        val types: Set<KClass<out Any>>
    )

    private data class CachedQuery(
        val entities: MutableList<Entity> = mutableListOf(),
        var version: Int = -1
    )
}
