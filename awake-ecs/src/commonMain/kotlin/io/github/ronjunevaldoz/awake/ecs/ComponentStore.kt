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

class ComponentStore<T : Any> {
    private val sparse = mutableListOf<Int>()
    private val denseEntities = mutableListOf<Entity>()
    private val denseComponents = mutableListOf<T>()

    val size: Int get() = denseEntities.size

    val entities: List<Entity> get() = denseEntities

    fun add(entity: Entity, component: T): T? {
        ensureSparseCapacity(entity.id)
        val denseIndex = sparse[entity.id]
        if (denseIndex in denseEntities.indices && denseEntities[denseIndex].id == entity.id) {
            val previous = denseComponents[denseIndex]
            denseEntities[denseIndex] = entity
            denseComponents[denseIndex] = component
            return previous
        }

        sparse[entity.id] = denseEntities.size
        denseEntities += entity
        denseComponents += component
        return null
    }

    fun get(entity: Entity): T? {
        val denseIndex = sparse.getOrNull(entity.id) ?: return null
        return if (denseIndex in denseEntities.indices && denseEntities[denseIndex] == entity) {
            denseComponents[denseIndex]
        } else {
            null
        }
    }

    fun contains(entity: Entity): Boolean {
        return get(entity) != null
    }

    fun remove(entity: Entity): T? {
        val denseIndex = sparse.getOrNull(entity.id) ?: return null
        return if (denseIndex in denseEntities.indices && denseEntities[denseIndex] == entity) {
            removeAt(entity, denseIndex)
        } else {
            null
        }
    }

    private fun removeAt(entity: Entity, denseIndex: Int): T {
        val removed = denseComponents[denseIndex]
        val lastIndex = denseEntities.lastIndex
        val lastEntity = denseEntities[lastIndex]
        val lastComponent = denseComponents[lastIndex]

        denseEntities[denseIndex] = lastEntity
        denseComponents[denseIndex] = lastComponent
        sparse[lastEntity.id] = denseIndex

        denseEntities.removeAt(lastIndex)
        denseComponents.removeAt(lastIndex)
        sparse[entity.id] = ABSENT
        return removed
    }

    fun clear() {
        denseEntities.clear()
        denseComponents.clear()
        sparse.fill(ABSENT)
    }

    fun forEach(block: (Entity, T) -> Unit) {
        for (index in denseEntities.indices) {
            block(denseEntities[index], denseComponents[index])
        }
    }

    private fun ensureSparseCapacity(id: Int) {
        while (id >= sparse.size) {
            sparse += ABSENT
        }
    }

    private companion object {
        const val ABSENT = -1
    }
}
