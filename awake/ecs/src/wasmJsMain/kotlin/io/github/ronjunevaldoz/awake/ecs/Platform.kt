// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

import kotlin.reflect.KClass

// Same reasoning as the iOS actual -- no `java.lang.Class` (or equivalent compiler idiom
// to bypass `T::class`) on Kotlin/Wasm either, so there's nothing to optimize here.
internal actual fun <T : Any> newComponentArray(type: KClass<T>, capacity: Int): Array<Any?> = arrayOfNulls<Any>(capacity)

internal actual fun <T : Any> createComponentInstance(type: KClass<T>): T {
    error("Automatic component instantiation not supported on this platform. Register a factory via registerPool.")
}

@PublishedApi
internal actual inline fun <reified T : Any> componentTypeKey(): Any = T::class

internal actual fun <T : Any> componentTypeKeyOf(type: KClass<T>): Any = type
