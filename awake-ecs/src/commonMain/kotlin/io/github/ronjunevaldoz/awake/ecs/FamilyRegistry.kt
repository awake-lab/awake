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

import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * Owns every maintained [FamilyCache] -- the typed [Family1Cache]/[Family2Cache] instances
 * plus the arbitrary-arity [GeneralFamilyCache] ones -- and keeps them all in sync with
 * [World]'s structural changes (entity destroy, component add/replace/remove).
 *
 * Extracted out of [World] so entity/component lifecycle and family-cache bookkeeping don't
 * live in the same 400+ line file; this class needs read access to [World]'s stores and
 * type-id assignment, exposed via `internal` members on [World].
 */
internal class FamilyRegistry(private val world: World) {
    private val families = mutableMapOf<FamilyKey, FamilyCache>()
    private val generalFamilies = mutableMapOf<FamilySpec, GeneralFamilyCache>()

    fun clear() {
        families.clear()
        generalFamilies.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun <A : Any> familyCache(type: KClass<A>): Family1Cache<A> {
        val key = FamilyKey.single(world.typeId(type))
        return families.getOrPut(key) { buildFamily(type) } as Family1Cache<A>
    }

    @Suppress("UNCHECKED_CAST")
    fun <A : Any, B : Any> familyCache(typeA: KClass<A>, typeB: KClass<B>): Family2Cache<A, B> {
        val key = FamilyKey.pair(world.typeId(typeA), world.typeId(typeB))
        return families.getOrPut(key) { buildFamily(typeA, typeB) } as Family2Cache<A, B>
    }

    fun generalFamilyCache(spec: FamilySpec): GeneralFamilyCache {
        return generalFamilies.getOrPut(spec) { buildGeneralFamily(spec) }
    }

    fun removeEntity(entity: Entity) {
        allCaches().forEach { it.remove(entity) }
    }

    fun <T : Any> addComponent(entity: Entity, type: KClass<T>, component: T) {
        allCaches().forEach { it.addComponent(world, entity, type, component) }
    }

    fun <T : Any> replaceComponent(entity: Entity, type: KClass<T>, component: T) {
        allCaches().forEach { it.replaceComponent(world, entity, type, component) }
    }

    fun removeComponent(entity: Entity, type: KClass<out Any>) {
        allCaches().forEach { it.removeComponent(world, entity, type) }
    }

    private fun allCaches(): Sequence<FamilyCache> {
        return families.values.asSequence() + generalFamilies.values.asSequence()
    }

    private fun <A : Any> buildFamily(type: KClass<A>): Family1Cache<A> {
        val cache = Family1Cache(type)
        world.storeOrNull(type)?.forEach { entity, component ->
            cache.add(entity, component)
        }
        return cache
    }

    private fun <A : Any, B : Any> buildFamily(
        typeA: KClass<A>,
        typeB: KClass<B>
    ): Family2Cache<A, B> {
        val cache = Family2Cache(typeA, typeB)
        val storeA = world.storeOrNull(typeA)
        val storeB = world.storeOrNull(typeB)
        if (storeA != null && storeB != null) {
            fillFamily(cache, storeA, storeB)
        }
        return cache
    }

    private fun buildGeneralFamily(spec: FamilySpec): GeneralFamilyCache {
        val cache = GeneralFamilyCache(spec)
        world.collectQuery(emptySet()).forEach { entity ->
            if (cache.matches(world, entity)) {
                cache.add(entity)
            }
        }
        return cache
    }

    private fun <A : Any, B : Any> fillFamily(
        cache: Family2Cache<A, B>,
        storeA: ComponentStore<A>,
        storeB: ComponentStore<B>
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
        storeB: ComponentStore<B>
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
        storeB: ComponentStore<B>
    ) {
        storeB.forEach { entity, componentB ->
            storeA.get(entity)?.let { componentA ->
                cache.add(entity, componentA, componentB)
            }
        }
    }
}

@JvmInline
internal value class ComponentTypeId(
    val value: Int
)

@JvmInline
internal value class FamilyKey(
    val packed: Long
) {
    companion object {
        private const val SINGLE_SENTINEL = -1
        private const val INT_BITS = 32
        private const val LOW_INT_MASK = 0xFFFF_FFFFL

        fun single(type: ComponentTypeId): FamilyKey {
            return FamilyKey(pack(type.value, SINGLE_SENTINEL))
        }

        fun pair(typeA: ComponentTypeId, typeB: ComponentTypeId): FamilyKey {
            return FamilyKey(pack(typeA.value, typeB.value))
        }

        private fun pack(first: Int, second: Int): Long {
            return (first.toLong() shl INT_BITS) or (second.toLong() and LOW_INT_MASK)
        }
    }
}
