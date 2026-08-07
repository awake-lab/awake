// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass

internal actual fun <T : Any> newComponentArray(type: KClass<T>, capacity: Int): Array<Any?> = arrayOfNulls<Any>(capacity)

internal actual fun <T : Any> createComponentInstance(type: KClass<T>): T {
    error("Automatic component instantiation not supported on this platform. Register a factory via registerPool.")
}

// No `java.lang.Class` on Kotlin/Native, and no equivalent compiler idiom to bypass `T::class`
// -- this platform doesn't have the reflection-wrapper cost the JVM/Android actuals are dodging
// here (Kotlin/Native's reified `T::class` isn't backed by `Reflection.getOrCreateKotlinClass`),
// so there's nothing to optimize; just return the `KClass` itself as the key.
@PublishedApi
internal actual inline fun <reified T : Any> componentTypeKey(): Any = T::class

internal actual fun <T : Any> componentTypeKeyOf(type: KClass<T>): Any = type
