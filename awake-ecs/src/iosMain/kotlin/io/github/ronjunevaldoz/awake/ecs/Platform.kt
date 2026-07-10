/*
 * Awake
 * Awake.awake-ecs.iosMain
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

internal actual fun <T : Any> newComponentArray(type: KClass<T>, capacity: Int): Array<Any?> {
    return arrayOfNulls<Any>(capacity)
}

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
