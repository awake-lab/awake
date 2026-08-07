// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

/**
 * Components can implement this to be automatically reset when returned to the pool.
 */
interface Poolable {
    fun reset()
}

/**
 * High-performance object pool for components.
 *
 * Backed by a plain growable array used strictly as a LIFO stack (push/pop from one end
 * only), not [ArrayDeque] -- profiling `awakeFamilyChurn` with async-profiler showed
 * `ArrayDeque.removeLast`/`positiveMod`/`ensureCapacity`/`getSize` together costing ~11% of
 * CPU samples on this pool's `obtain`/`free` round trip. `ArrayDeque`'s circular-buffer
 * design (division-based index wraparound via `positiveMod`) pays for supporting
 * both-end operations this pool never uses; a plain array + `size` counter needs no index
 * wraparound at all for single-end push/pop.
 */
@PublishedApi
internal class ComponentPool<T : Any>(
    private val factory: () -> T,
) {
    @PublishedApi
    internal var items = arrayOfNulls<Any?>(DEFAULT_CAPACITY)

    @PublishedApi
    internal var size = 0

    @PublishedApi
    internal fun obtain(): T {
        if (size == 0) {
            return factory()
        }
        size -= 1
        @Suppress("UNCHECKED_CAST")
        val instance = items[size] as T
        items[size] = null
        return instance
    }

    fun free(instance: T) {
        if (instance is Poolable) {
            instance.reset()
        }
        ensureCapacity(size + 1)
        items[size] = instance
        size += 1
    }

    fun clear() {
        items.fill(null, fromIndex = 0, toIndex = size)
        size = 0
    }

    private fun ensureCapacity(required: Int) {
        if (required <= items.size) {
            return
        }
        items = items.copyOf(maxOf(required, items.size * CAPACITY_GROWTH_FACTOR))
    }

    private companion object {
        const val DEFAULT_CAPACITY = 16
        const val CAPACITY_GROWTH_FACTOR = 2
    }
}
