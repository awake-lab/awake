// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Replaces the component of type [T] on [entity]. If it exists, it's recycled.
 * Returns the new component instance.
 */
inline fun <reified T : Any> World.replace(entity: Entity, component: T): T {
    remove<T>(entity)
    add(entity, component)
    return component
}

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
 * Toggles a component on an [entity]. 
 * If [enabled] is true and component is missing, it's added via [factory].
 * If [enabled] is false and component exists, it's removed.
 */
inline fun <reified T : Any> World.toggle(entity: Entity, enabled: Boolean, factory: () -> T) {
    if (enabled) {
        if (!has<T>(entity)) {
            add(entity, factory())
        }
    } else {
        remove<T>(entity)
    }
}

/**
 * Property delegate that fetches a component on demand from the ECS world.
 */
class ComponentDelegate<T : Any>(
    @PublishedApi internal val world: World,
    @PublishedApi internal val entity: Entity,
    @PublishedApi internal val type: KClass<T>
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return world.get(entity, type) ?: error("Component ${type.simpleName} missing on $entity!")
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        world.add(entity, type, value)
    }
}

/**
 * Property delegate that fetches a component on demand from the ECS world.
 */
inline fun <reified T : Any> World.component(entity: Entity): ComponentDelegate<T> =
    ComponentDelegate(this, entity, T::class)
