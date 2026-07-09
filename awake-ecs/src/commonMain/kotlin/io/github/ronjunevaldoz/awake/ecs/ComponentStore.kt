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
    private val sparse = SparseIndex()
    private var denseEntities = LongArray(DEFAULT_CAPACITY)
    private var denseComponents = arrayOfNulls<Any>(DEFAULT_CAPACITY)
    private var count = 0

    val size: Int get() = count

    val entities: List<Entity>
        get() = object : AbstractList<Entity>() {
            override val size: Int get() = count

            override fun get(index: Int): Entity {
                return Entity(denseEntities[index])
            }
        }

    fun add(entity: Entity, component: T): T? {
        val denseIndex = sparse.get(entity.id)
        if (denseIndex in 0 until count && denseEntities[denseIndex] == entity.packed) {
            @Suppress("UNCHECKED_CAST")
            val previous = denseComponents[denseIndex] as T
            denseComponents[denseIndex] = component
            return previous
        }

        ensureDenseCapacity(count + 1)
        sparse.set(entity.id, count)
        denseEntities[count] = entity.packed
        denseComponents[count] = component
        count += 1
        return null
    }

    fun get(entity: Entity): T? {
        val denseIndex = sparse.get(entity.id)
        return if (denseIndex in 0 until count && denseEntities[denseIndex] == entity.packed) {
            @Suppress("UNCHECKED_CAST")
            denseComponents[denseIndex] as T
        } else {
            null
        }
    }

    fun contains(entity: Entity): Boolean {
        return get(entity) != null
    }

    fun remove(entity: Entity): T? {
        val denseIndex = sparse.get(entity.id)
        return if (denseIndex in 0 until count && denseEntities[denseIndex] == entity.packed) {
            removeAt(denseIndex)
        } else {
            null
        }
    }

    private fun removeAt(denseIndex: Int): T {
        @Suppress("UNCHECKED_CAST")
        val removed = denseComponents[denseIndex] as T
        val lastIndex = count - 1
        val lastEntity = denseEntities[lastIndex]

        denseEntities[denseIndex] = lastEntity
        denseComponents[denseIndex] = denseComponents[lastIndex]
        sparse.set(Entity(lastEntity).id, denseIndex)

        denseComponents[lastIndex] = null
        count -= 1
        return removed
    }

    fun clear() {
        denseComponents.fill(null, fromIndex = 0, toIndex = count)
        count = 0
        sparse.clear()
    }

    fun forEach(block: (Entity, T) -> Unit) {
        val localEntities = denseEntities
        val localComponents = denseComponents
        val localCount = count
        for (index in 0 until localCount) {
            @Suppress("UNCHECKED_CAST")
            block(Entity(localEntities[index]), localComponents[index] as T)
        }
    }

    private fun ensureDenseCapacity(requiredCapacity: Int) {
        if (requiredCapacity <= denseEntities.size) {
            return
        }
        val newCapacity = maxOf(requiredCapacity, denseEntities.size * CAPACITY_GROWTH_FACTOR)
        denseEntities = denseEntities.copyOf(newCapacity)
        denseComponents = denseComponents.copyOf(newCapacity)
    }

    private companion object {
        const val DEFAULT_CAPACITY = 16
        const val CAPACITY_GROWTH_FACTOR = 2
    }
}
