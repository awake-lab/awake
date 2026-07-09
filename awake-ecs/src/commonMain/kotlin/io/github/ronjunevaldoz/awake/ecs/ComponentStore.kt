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
    private val sparse = EntityIndexMap()
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

    /** View of [denseComponents] typed as non-null `T` instead of `Any?` -- casting the
     * array reference once here (a no-op at the bytecode level; array types erase to
     * `Object[]` regardless of declared nullability) avoids an `Intrinsics.checkNotNull`
     * Kotlin would otherwise insert on every single `as T` element cast. Profiling
     * `awakeTransformMeshQuery` showed that check costing ~14% of CPU samples. Only valid
     * for indices `< count` -- slots at or beyond `count` may hold a stale `null`. */
    @Suppress("UNCHECKED_CAST")
    private inline val typedDenseComponents: Array<T>
        get() = denseComponents as Array<T>

    fun add(entity: Entity, component: T): T? {
        val denseIndex = sparse.get(entity.id)
        if (denseIndex in 0 until count && denseEntities[denseIndex] == entity.packed) {
            val previous = typedDenseComponents[denseIndex]
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
            typedDenseComponents[denseIndex]
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
        val removed = typedDenseComponents[denseIndex]
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
        val localComponents = typedDenseComponents
        val localCount = count
        for (index in 0 until localCount) {
            block(Entity(localEntities[index]), localComponents[index])
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
