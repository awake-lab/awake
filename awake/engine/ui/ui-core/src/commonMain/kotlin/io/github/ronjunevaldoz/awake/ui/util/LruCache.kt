// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.util

/**
 * A bounded least-recently-used cache for memoizing pure functions -- see
 * docs/tasks/2026-08-03-text-layout-measure-cache.md for the motivating case (text-layout
 * measurement). Not thread-safe: every piece of Awake's UI state (`rememberStateValue`,
 * `UiWeightCache`, this) assumes single-threaded measure+draw on one [K]-typed key path, matching
 * the rest of the UI layer.
 *
 * [V] must be non-null: this uses "value present" to distinguish a cache hit from a miss, which a
 * nullable value type would make ambiguous.
 */
class LruCache<K, V : Any>(private val maxSize: Int) {
    init {
        require(maxSize > 0) { "maxSize must be > 0, was $maxSize" }
    }

    // LinkedHashMap's default iteration order is insertion order; getOrPut below re-inserts on
    // every hit to move that key to the most-recently-used (last) position, so the first key
    // returned by keys.iterator() is always the least-recently-used one to evict.
    private val map = LinkedHashMap<K, V>(maxSize)

    fun getOrPut(key: K, compute: () -> V): V {
        val existing = map.remove(key)
        if (existing != null) {
            map[key] = existing
            return existing
        }
        val value = compute()
        map[key] = value
        if (map.size > maxSize) {
            map.remove(map.keys.iterator().next())
        }
        return value
    }

    /** True if [key] would currently be served without computing -- lets a caller that wants
     * hit/miss stats (see `textLayoutCacheStats` in `ui-unstyled`) observe that without this
     * generic cache needing to track it itself. Does not affect LRU order (unlike [getOrPut]'s
     * hit path) -- call [getOrPut] itself to actually record the access. */
    fun containsKey(key: K): Boolean = map.containsKey(key)

    val size: Int get() = map.size

    fun clear() = map.clear()
}
