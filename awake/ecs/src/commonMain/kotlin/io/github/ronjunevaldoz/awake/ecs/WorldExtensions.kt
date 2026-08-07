// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Ensures the [entity] has a component of type [T]. If missing, [factory] is called
 * to create and add it. Returns the existing or new component instance.
 */
inline fun <reified T : Any> World.ensure(entity: Entity, factory: () -> T): T {
    val existing = get<T>(entity)
    if (existing != null) return existing
    val new = factory()
    add(entity, new)
    return new
}

/**
 * Property delegate that fetches a component on demand from the ECS world.
 *
 * Each property access is a fresh lookup, so it reads like a local but costs a `KClass` hash
 * plus a sparse-set probe every time -- `spin.radians = spin.radians + x` is three lookups.
 * Bind it once outside a hot loop, or use [World.get] directly.
 */
class ComponentDelegate<T : Any>(
    @PublishedApi internal val world: World,
    @PublishedApi internal val entity: Entity,
    @PublishedApi internal val type: KClass<T>,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = world.get(entity, type) ?: error("Component ${type.simpleName} missing on $entity!")

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        world.add(entity, type, value)
    }
}

/**
 * Property delegate that fetches a component on demand from the ECS world.
 *
 * Each property access is a fresh lookup, so it reads like a local but costs a `KClass` hash
 * plus a sparse-set probe every time -- `spin.radians = spin.radians + x` is three lookups.
 * Bind it once outside a hot loop, or use [World.get] directly.
 */
inline fun <reified T : Any> World.component(entity: Entity): ComponentDelegate<T> =
    ComponentDelegate(this, entity, T::class)
