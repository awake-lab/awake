// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass

class ComponentStore<T : Any>(
    internal val type: KClass<T>,
) {
    private val sparse = EntityIndexMap()
    private var denseEntities = LongArray(DEFAULT_CAPACITY)
    private var denseComponents = newComponentArray(type, DEFAULT_CAPACITY)
    private var count = 0

    val size: Int get() = count

    /** Live view over the dense entity array -- backed by a single instance created once
     * per store instead of a fresh `object : AbstractList<...>()` on every access. The view
     * still reads [count]/[denseEntities] through the enclosing instance, so it stays live;
     * only the wrapper allocation itself is what's cached here. [QueryCollector.collect] is
     * the main caller and used to allocate one of these per query recomputation. */
    private val entitiesView: List<Entity> = object : AbstractList<Entity>() {
        override val size: Int get() = count

        override fun get(index: Int): Entity = Entity(denseEntities[index])
    }

    val entities: List<Entity> get() = entitiesView

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
        if (denseIndex >= 0 && denseIndex < count) {
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
        return if (denseIndex >= 0 && denseIndex < count) {
            typedDenseComponents[denseIndex]
        } else {
            null
        }
    }

    fun contains(entity: Entity): Boolean = get(entity) != null

    fun remove(entity: Entity): T? {
        val denseIndex = sparse.get(entity.id)
        return if (denseIndex >= 0 && denseIndex < count) {
            removeAt(denseIndex)
        } else {
            null
        }
    }

    private fun removeAt(denseIndex: Int): T {
        val removed = typedDenseComponents[denseIndex]
        val removedEntityId = denseEntities[denseIndex].toInt()
        val lastIndex = count - 1
        if (denseIndex != lastIndex) {
            val lastEntity = denseEntities[lastIndex]
            denseEntities[denseIndex] = lastEntity
            denseComponents[denseIndex] = denseComponents[lastIndex]
            sparse.set(lastEntity.toInt(), denseIndex)
        }

        denseComponents[lastIndex] = null
        count -= 1
        sparse.remove(removedEntityId)
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
