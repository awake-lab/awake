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
