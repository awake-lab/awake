// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * Owns every maintained [FamilyCache] -- the typed [Family1Cache]/[Family2Cache] instances
 * plus the arbitrary-arity [FamilySpecCache] ones -- and keeps them all in sync with
 * [World]'s structural changes (entity destroy, component add/replace/remove).
 *
 * Extracted out of [World] so entity/component lifecycle and family-cache bookkeeping don't
 * live in the same 400+ line file; this class needs read access to [World]'s stores and
 * type-id assignment helpers.
 */
internal class FamilyRegistry(
    private val world: World,
    private val queryCollector: QueryCollector,
) {
    private val families = mutableMapOf<FamilyKey, FamilyCache>()
    private val familySpecCaches = mutableMapOf<FamilySpec, FamilySpecCache>()

    /** Index of families that care about a specific component type, indexed by
     * [ComponentTypeId.value]. Using an array of lists avoids [KClass] map lookups
     * on the structural-change hot path. */
    private var familiesByComponentId = arrayOfNulls<MutableList<FamilyCache>>(16)

    fun clear() {
        families.clear()
        familySpecCaches.clear()
        familiesByComponentId.fill(null)
    }

    // families is keyed by FamilyKey.single(typeId), and getOrPut's builder above always
    // constructs the Family1Cache<A> for this exact `type`/typeId pair, so a cache stored
    // under this key can only ever be a Family1Cache<A>.
    @Suppress("UNCHECKED_CAST")
    fun <A : Any> familyCache(type: KClass<A>): Family1Cache<A> {
        val typeId = world.typeId(type)
        val key = FamilyKey.single(typeId)
        return families.getOrPut(key) {
            buildFamily(type, typeId).also(::indexFamily)
        } as Family1Cache<A>
    }

    // families is keyed by FamilyKey.pair(typeIdA, typeIdB), and getOrPut's builder above
    // always constructs the Family2Cache<A, B> for this exact type pair, so a cache stored
    // under this key can only ever be a Family2Cache<A, B>.
    @Suppress("UNCHECKED_CAST")
    fun <A : Any, B : Any> familyCache(typeA: KClass<A>, typeB: KClass<B>): Family2Cache<A, B> {
        val typeIdA = world.typeId(typeA)
        val typeIdB = world.typeId(typeB)
        val key = FamilyKey.pair(typeIdA, typeIdB)
        return families.getOrPut(key) {
            buildFamily(typeA, typeIdA, typeB, typeIdB).also(::indexFamily)
        } as Family2Cache<A, B>
    }

    fun familySpecCache(spec: FamilySpec): FamilySpecCache = familySpecCaches.getOrPut(spec) {
        buildFamilySpecCache(spec).also(::indexFamily)
    }

    fun removeEntity(entity: Entity) {
        // Entity destruction affects every cache (it must be removed if it was present)
        forEachCache { it.remove(entity) }
    }

    fun addComponent(entity: Entity, typeId: ComponentTypeId, component: Any) {
        forEachRelevantCache(typeId) { it.addComponent(world, entity, typeId, component) }
    }

    fun replaceComponent(entity: Entity, typeId: ComponentTypeId, component: Any) {
        forEachRelevantCache(typeId) { it.replaceComponent(world, entity, typeId, component) }
    }

    fun removeComponent(entity: Entity, typeId: ComponentTypeId) {
        forEachRelevantCache(typeId) { it.removeComponent(world, entity, typeId) }
    }

    private fun indexFamily(cache: FamilyCache) {
        cache.types().forEach { type ->
            val id = world.typeId(type).value
            ensureCapacity(id)
            val list = familiesByComponentId[id] ?: mutableListOf<FamilyCache>().also { familiesByComponentId[id] = it }
            list.add(cache)
        }
    }

    private fun ensureCapacity(id: Int) {
        if (id >= familiesByComponentId.size) {
            familiesByComponentId = familiesByComponentId.copyOf(maxOf(id + 1, familiesByComponentId.size * 2))
        }
    }

    /** Runs on every maintained cache without allocating a combined [Sequence] -- profiling
     * showed `families.values.asSequence() + familySpecCaches.values.asSequence()` costing
     * real CPU (asSequence/SequencesKt.plus wrapper allocation) on every structural change,
     * since this runs once per entity per add/remove/destroy. */
    private inline fun forEachCache(action: (FamilyCache) -> Unit) {
        families.values.forEach(action)
        familySpecCaches.values.forEach(action)
    }

    private inline fun forEachRelevantCache(typeId: ComponentTypeId, action: (FamilyCache) -> Unit) {
        val id = typeId.value
        if (id < familiesByComponentId.size) {
            val caches = familiesByComponentId[id] ?: return
            when (caches.size) {
                0 -> return
                1 -> action(caches[0])
                else -> {
                    for (index in 0 until caches.size) {
                        action(caches[index])
                    }
                }
            }
        }
    }

    private fun <A : Any> buildFamily(type: KClass<A>, typeId: ComponentTypeId): Family1Cache<A> {
        val store = world.store(typeId, type)
        val cache = Family1Cache(type, typeId, store)
        store.forEach { entity, component ->
            cache.add(entity, component)
        }
        return cache
    }

    private fun <A : Any, B : Any> buildFamily(
        typeA: KClass<A>,
        typeIdA: ComponentTypeId,
        typeB: KClass<B>,
        typeIdB: ComponentTypeId,
    ): Family2Cache<A, B> {
        val storeA = world.store(typeIdA, typeA)
        val storeB = world.store(typeIdB, typeB)
        val cache = Family2Cache(typeA, typeIdA, storeA, typeB, typeIdB, storeB)
        fillFamily(cache, storeA, storeB)
        return cache
    }

    private fun buildFamilySpecCache(spec: FamilySpec): FamilySpecCache {
        val cache = FamilySpecCache(world, spec)
        queryCollector.collect(emptySet()).forEach { entity ->
            if (cache.matches(world, entity)) {
                cache.add(entity)
            }
        }
        return cache
    }

    private fun <A : Any, B : Any> fillFamily(
        cache: Family2Cache<A, B>,
        storeA: ComponentStore<A>,
        storeB: ComponentStore<B>,
    ) {
        if (storeA.size <= storeB.size) {
            addMatchesFromA(cache, storeA, storeB)
        } else {
            addMatchesFromB(cache, storeA, storeB)
        }
    }

    private fun <A : Any, B : Any> addMatchesFromA(
        cache: Family2Cache<A, B>,
        storeA: ComponentStore<A>,
        storeB: ComponentStore<B>,
    ) {
        storeA.forEach { entity, componentA ->
            storeB.get(entity)?.let { componentB ->
                cache.add(entity, componentA, componentB)
            }
        }
    }

    private fun <A : Any, B : Any> addMatchesFromB(
        cache: Family2Cache<A, B>,
        storeA: ComponentStore<A>,
        storeB: ComponentStore<B>,
    ) {
        storeB.forEach { entity, componentB ->
            storeA.get(entity)?.let { componentA ->
                cache.add(entity, componentA, componentB)
            }
        }
    }
}

@JvmInline
value class ComponentTypeId(
    val value: Int,
)

@JvmInline
internal value class FamilyKey(
    val packed: Long,
) {
    companion object {
        private const val SINGLE_SENTINEL = -1
        private const val INT_BITS = 32
        private const val LOW_INT_MASK = 0xFFFF_FFFFL

        fun single(type: ComponentTypeId): FamilyKey = FamilyKey(pack(type.value, SINGLE_SENTINEL))

        fun pair(typeA: ComponentTypeId, typeB: ComponentTypeId): FamilyKey = FamilyKey(pack(typeA.value, typeB.value))

        private fun pack(first: Int, second: Int): Long = (first.toLong() shl INT_BITS) or (second.toLong() and LOW_INT_MASK)
    }
}
