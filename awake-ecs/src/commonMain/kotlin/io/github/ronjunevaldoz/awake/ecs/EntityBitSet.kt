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

/**
 * High-performance primitive bitset for entity IDs.
 * Used for O(1) membership checks in families.
 */
internal class EntityBitSet(initialCapacity: Int = 16) {
    private var bits = LongArray(initialCapacity / 64 + 1)

    fun contains(id: Int): Boolean {
        val wordIndex = id ushr 6
        return if (wordIndex < bits.size) {
            (bits[wordIndex] and (1L shl (id and 63))) != 0L
        } else {
            false
        }
    }

    fun add(id: Int) {
        val wordIndex = id ushr 6
        ensureCapacity(wordIndex)
        bits[wordIndex] = bits[wordIndex] or (1L shl (id and 63))
    }

    fun remove(id: Int) {
        val wordIndex = id ushr 6
        if (wordIndex < bits.size) {
            bits[wordIndex] = bits[wordIndex] and (1L shl (id and 63)).inv()
        }
    }

    fun clear() {
        bits.fill(0L)
    }

    private fun ensureCapacity(wordIndex: Int) {
        if (wordIndex >= bits.size) {
            bits = bits.copyOf(maxOf(wordIndex + 1, bits.size * 2))
        }
    }
}
