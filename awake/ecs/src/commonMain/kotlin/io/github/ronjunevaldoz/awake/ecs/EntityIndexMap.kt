// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

/**
 * A growable `entity id -> dense index` map: the sparse half of the sparse-set pattern.
 * Extracted because `ComponentStore`, `Family1Cache`, and `Family2Cache` each used to
 * hand-roll an identical copy of this (sparse array + grow-on-demand + `ABSENT` sentinel).
 *
 * Owns only the sparse array -- callers own their own dense array(s) (entities, plus
 * whatever component payload(s) they're indexing) and are responsible for calling [set]
 * whenever they place or move an entity in their dense storage, mirroring their own
 * swap-remove logic.
 */
internal class EntityIndexMap {
    private var sparse = IntArray(0)

    /** Returns the dense index for [id], or [ABSENT] if never set (or out of range).
     * Deliberately not `sparse.getOrNull(id) ?: ABSENT` -- [IntArray.getOrNull] boxes to
     * `Int?` on every call, which profiling showed as the single largest cost (36% of CPU
     * samples) in family-cache churn, since this runs on every add/remove. */
    fun get(id: Int): Int = if (id >= 0 && id < sparse.size) sparse[id] else ABSENT

    /** Records that [id] now lives at [denseIndex] in the caller's dense storage. */
    fun set(id: Int, denseIndex: Int) {
        ensureCapacity(id)
        sparse[id] = denseIndex
    }

    /** Clears any dense index recorded for [id]. */
    fun remove(id: Int) {
        if (id >= 0 && id < sparse.size) {
            sparse[id] = ABSENT
        }
    }

    fun clear() {
        sparse.fill(ABSENT)
    }

    private fun ensureCapacity(id: Int) {
        if (id < sparse.size) {
            return
        }
        val previousSize = sparse.size
        val newSize = maxOf(id + 1, maxOf(DEFAULT_CAPACITY, previousSize * CAPACITY_GROWTH_FACTOR))
        sparse = sparse.copyOf(newSize)
        sparse.fill(ABSENT, fromIndex = previousSize, toIndex = newSize)
    }

    companion object {
        const val ABSENT = -1
        private const val DEFAULT_CAPACITY = 16
        private const val CAPACITY_GROWTH_FACTOR = 2
    }
}
