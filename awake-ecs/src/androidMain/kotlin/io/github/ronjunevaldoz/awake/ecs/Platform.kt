/*
 * Awake
 * Awake.awake-ecs.jvmMain
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
    return java.lang.reflect.Array.newInstance(type.java, capacity) as Array<Any?>
}

internal actual fun <T : Any> createComponentInstance(type: KClass<T>): T {
    @Suppress("UNCHECKED_CAST")
    val clazz = type.javaObjectType as Class<T>
    return clazz.getDeclaredConstructor().newInstance()
}

// Same reasoning as the desktop actual -- see the commonMain expect declaration's doc comment.
@PublishedApi
internal actual inline fun <reified T : Any> componentTypeKey(): Any = T::class.java

internal actual fun <T : Any> componentTypeKeyOf(type: KClass<T>): Any = type.java
