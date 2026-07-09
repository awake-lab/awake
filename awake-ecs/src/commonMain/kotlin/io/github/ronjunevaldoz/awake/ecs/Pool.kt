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

/**
 * Components can implement this to be automatically reset when returned to the pool.
 */
interface Poolable {
    fun reset()
}

/**
 * High-performance object pool for components.
 */
internal class ComponentPool<T : Any>(
    private val factory: () -> T
) {
    private val pool = ArrayDeque<T>()

    fun obtain(): T {
        return pool.removeLastOrNull() ?: factory()
    }

    fun free(instance: T) {
        if (instance is Poolable) {
            instance.reset()
        }
        pool.addLast(instance)
    }
    
    fun clear() {
        pool.clear()
    }
}
