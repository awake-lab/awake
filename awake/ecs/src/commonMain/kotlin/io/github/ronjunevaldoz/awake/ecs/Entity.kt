// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.jvm.JvmInline

/**
 * Stable entity handle. The low 32 bits carry the sparse-set id; the high 32 bits carry
 * the generation, so a recycled id cannot alias an older handle.
 */
@JvmInline
value class Entity(val packed: Long) {
    val id: Int get() = packed.toInt()
    val generation: Int get() = (packed ushr GENERATION_SHIFT).toInt()

    override fun toString(): String = "Entity(id=$id, generation=$generation)"

    companion object {
        private const val GENERATION_SHIFT = 32
        private const val ID_MASK = 0xFFFF_FFFFL

        fun of(id: Int, generation: Int): Entity {
            require(id >= 0) { "Entity id must be non-negative." }
            require(generation >= 0) { "Entity generation must be non-negative." }
            return Entity((generation.toLong() shl GENERATION_SHIFT) or (id.toLong() and ID_MASK))
        }
    }
}
