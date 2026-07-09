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

import kotlin.reflect.KClass

/**
 * An arbitrary-arity family predicate: match entities that have **all** of [all], **any**
 * (at least one) of [one] (if non-empty), and **none** of [exclude]. This is the same
 * `all()/one()/exclude()` shape Ashley's ECS uses, adopted deliberately for the API
 * ergonomics (one generic mechanism instead of hand-writing `Family1Cache`, `Family2Cache`,
 * `Family3Cache`, ... for every arity) -- but backed by [FamilySpecCache]'s own
 * sparse-set + incremental-maintenance approach, not Ashley's listener-based internals
 * (which this project's own benchmark showed to be the slowest of the four ECS libraries
 * measured; see `docs/ecs-benchmark-scorecard.md`).
 *
 * Data class (not a data-less builder result) specifically so it works as a `Map` key in
 * `World`'s family cache -- two `family { ... }` calls describing the same predicate return
 * the same underlying [FamilySpecCache] instance, same convention [FamilyKey] already
 * uses for [Family1]/[Family2].
 */
data class FamilySpec(
    val all: Set<KClass<out Any>> = emptySet(),
    val one: Set<KClass<out Any>> = emptySet(),
    val exclude: Set<KClass<out Any>> = emptySet()
)

/** Builds a [FamilySpec] via `world.family { all(A::class); exclude(B::class) }`. */
class FamilySpecBuilder @PublishedApi internal constructor() {
    private val all = mutableSetOf<KClass<out Any>>()
    private val one = mutableSetOf<KClass<out Any>>()
    private val exclude = mutableSetOf<KClass<out Any>>()

    fun all(vararg types: KClass<out Any>): FamilySpecBuilder = apply { all += types }
    fun one(vararg types: KClass<out Any>): FamilySpecBuilder = apply { one += types }
    fun exclude(vararg types: KClass<out Any>): FamilySpecBuilder = apply { exclude += types }

    @PublishedApi
    internal fun build(): FamilySpec = FamilySpec(all.toSet(), one.toSet(), exclude.toSet())
}

/**
 * The public, read-only view of a maintained arbitrary-arity family. Unlike [Family1]/
 * [Family2], this only exposes matched [Entity] handles, not their component values
 * directly -- an arbitrary number of heterogeneous component types can't be returned as a
 * typed tuple in Kotlin without per-arity codegen, so callers read components back via
 * `world.get<T>(entity)` (an O(1) sparse-set lookup, not free but not linear either). Prefer
 * [Family1]/[Family2] for the common 1-2-component hot path, where direct typed component
 * access avoids that extra lookup per entity per frame; reach for this when a query
 * genuinely needs 3+ types or `one`/`exclude` semantics neither of those support.
 */
class Family @PublishedApi internal constructor(
    @PublishedApi internal val cache: FamilySpecCache
) {
    val size: Int get() = cache.size

    inline fun forEach(block: (Entity) -> Unit) {
        cache.forEach(block)
    }
}

/**
 * Maintains the entity set matching a [FamilySpec] incrementally: [World]'s structural-
 * change hooks call [addComponent]/[removeComponent] here directly instead of this cache
 * re-scanning every entity on every query, the same incremental-maintenance principle
 * [Family1Cache]/[Family2Cache] already use. Backed by [EntityIndexMap] for O(1)
 * membership add/remove, same as those two.
 */
@PublishedApi
internal class FamilySpecCache(
    private val world: World,
    private val spec: FamilySpec
) : FamilyCache() {
    @PublishedApi
    internal var entities = LongArray(DEFAULT_FAMILY_CAPACITY)
    @PublishedApi
    internal var count: Int = 0
    private val sparse = EntityIndexMap()
    
    private val allMask = spec.all.fold(0L) { acc, type -> acc or (1L shl world.typeId(type).value) }
    private val oneMask = spec.one.fold(0L) { acc, type -> acc or (1L shl world.typeId(type).value) }
    private val excludeMask = spec.exclude.fold(0L) { acc, type -> acc or (1L shl world.typeId(type).value) }

    override fun types(): Set<KClass<out Any>> = spec.all + spec.one + spec.exclude

    val size: Int get() = count

    @PublishedApi
    internal inline fun forEach(block: (Entity) -> Unit) {
        val localEntities = entities
        val localCount = count
        for (index in 0 until localCount) {
            block(Entity(localEntities[index]))
        }
    }

    /** Whether [entity] currently satisfies this family's `all`/`one`/`exclude` predicate,
     * read fresh from [world] (not from this cache's own membership) -- used both to decide
     * initial membership when the cache is first built and to re-check membership after a
     * structural change. */
    fun matches(world: World, entity: Entity): Boolean {
        val signature = world.getSignature(entity.id)
        if (allMask != 0L && (signature and allMask) != allMask) {
            return false
        }
        if (oneMask != 0L && (signature and oneMask) == 0L) {
            return false
        }
        if (excludeMask != 0L && (signature and excludeMask) != 0L) {
            return false
        }
        return true
    }

    fun add(entity: Entity) {
        if (indexOf(entity) >= 0) {
            return
        }
        ensureCapacity(count + 1)
        sparse.set(entity.id, count)
        entities[count] = entity.packed
        count += 1
    }

    override fun remove(entity: Entity) {
        removeAt(indexOf(entity))
    }

    override fun addComponent(world: World, entity: Entity, typeId: ComponentTypeId, component: Any) {
        sync(world, entity)
    }

    override fun replaceComponent(world: World, entity: Entity, typeId: ComponentTypeId, component: Any) {
        // Replacing a component's value (not adding/removing it) can't change which
        // all/one/exclude branch it satisfies -- the type is present either way -- so
        // membership can't change here. Nothing to do.
    }

    override fun removeComponent(world: World, entity: Entity, typeId: ComponentTypeId) {
        sync(world, entity)
    }

    /** Re-checks [entity] against [matches] and adds/removes it to match -- called after a
     * structural change to a component type this family's spec actually references. */
    private fun sync(world: World, entity: Entity) {
        val currentIndex = indexOf(entity)
        val shouldBeMember = matches(world, entity)
        if (shouldBeMember && currentIndex < 0) {
            add(entity)
        } else if (!shouldBeMember && currentIndex >= 0) {
            removeAt(currentIndex)
        }
    }

    private fun indexOf(entity: Entity): Int {
        val denseIndex = sparse.get(entity.id)
        return if (denseIndex >= 0 && denseIndex < count) {
            denseIndex
        } else {
            -1
        }
    }

    private fun removeAt(index: Int) {
        if (index < 0) {
            return
        }
        val removedEntityId = entities[index].toInt()
        val lastIndex = count - 1
        if (index != lastIndex) {
            val lastEntity = entities[lastIndex]
            entities[index] = lastEntity
            sparse.set(lastEntity.toInt(), index)
        }
        count -= 1
        sparse.remove(removedEntityId)
    }

    private fun ensureCapacity(requiredCapacity: Int) {
        if (requiredCapacity <= entities.size) {
            return
        }
        val newCapacity = maxOf(requiredCapacity, entities.size * CAPACITY_GROWTH_FACTOR)
        entities = entities.copyOf(newCapacity)
    }

    private companion object {
        const val DEFAULT_FAMILY_CAPACITY = 16
        const val CAPACITY_GROWTH_FACTOR = 2
    }
}
