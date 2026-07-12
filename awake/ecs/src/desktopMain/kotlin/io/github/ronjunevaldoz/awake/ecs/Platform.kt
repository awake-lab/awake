// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass

internal actual fun <T : Any> newComponentArray(type: KClass<T>, capacity: Int): Array<Any?> {
    return java.lang.reflect.Array.newInstance(type.java, capacity) as Array<Any?>
}

internal actual fun <T : Any> createComponentInstance(type: KClass<T>): T {
    @Suppress("UNCHECKED_CAST")
    val clazz = type.javaObjectType as Class<T>
    return clazz.getDeclaredConstructor().newInstance()
}

// `T::class.java` on a reified type parameter is a recognized Kotlin compiler idiom that
// compiles directly to an `LDC <Class>` constant, skipping `Reflection.getOrCreateKotlinClass`
// entirely -- see the commonMain expect declaration's doc comment for how this was verified.
@PublishedApi
internal actual inline fun <reified T : Any> componentTypeKey(): Any = T::class.java

internal actual fun <T : Any> componentTypeKeyOf(type: KClass<T>): Any = type.java
