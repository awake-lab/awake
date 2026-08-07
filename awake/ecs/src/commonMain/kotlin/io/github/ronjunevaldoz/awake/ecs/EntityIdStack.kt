// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

/**
 * Primitive LIFO stack for recycled entity ids.
 *
 * Used by [World] so spawn/despawn doesn't pay boxing costs from `MutableList<Int>`.
 */
internal class EntityIdStack {
    private var ids = IntArray(DEFAULT_CAPACITY)
    private var size = 0

    fun pop(): Int = if (size > 0) ids[--size] else ABSENT

    fun push(id: Int) {
        ensureCapacity(size + 1)
        ids[size] = id
        size += 1
    }

    fun clear() {
        size = 0
    }

    private fun ensureCapacity(requiredCapacity: Int) {
        if (requiredCapacity <= ids.size) {
            return
        }
        val newCapacity = maxOf(requiredCapacity, ids.size * CAPACITY_GROWTH_FACTOR)
        ids = ids.copyOf(newCapacity)
    }

    private companion object {
        const val ABSENT = -1
        const val DEFAULT_CAPACITY = 16
        const val CAPACITY_GROWTH_FACTOR = 2
    }
}
