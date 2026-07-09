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
        return if (types.isEmpty()) {
            val count = entities.count
            val results = ArrayList<Entity>(count)
            for (id in 0 until count) {
                if (entities.isAlive(id)) {
                    results += entities.entity(id)
                }
            }
            results
        } else {
            val queryStores = types.mapNotNull { type ->
                components.typeIdOrNull(type)?.let { components.storeOrNull<Any>(it) }
            }
            if (queryStores.size != types.size) {
                emptyList()
            } else {
                val smallestStore = queryStores.minBy { it.size }
                smallestStore.entities.filter { entity ->
                    entities.isAlive(entity) && queryStores.all { it.contains(entity) }
                }
            }
        }
    }
}
