/*
 * Awake
 * Awake.awake-ecs.commonTest
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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class EcsOptimizationTest {

    @Test
    fun componentsArePooledAndReused() {
        val world = World()
        world.registerPool(PooledComponent::class) { PooledComponent() }

        val entity1 = world.create()
        val comp1 = world.add<PooledComponent>(entity1)
        comp1.value = 42

        world.destroy(entity1) // Returns comp1 to pool

        val entity2 = world.create()
        val comp2 = world.add<PooledComponent>(entity2)

        // Verify it's the same instance and was reset
        assertSame(comp1, comp2)
        assertEquals(0, comp2.value)
    }

    @Test
    fun spawnDslCorrectlyInitializesPooledEntities() {
        val world = World()
        world.registerPool(PooledComponent::class) { PooledComponent() }

        val entity = world.spawn<PooledComponent> {
            it.value = 100
        }

        assertTrue(world.isAlive(entity))
        assertEquals(100, world.get<PooledComponent>(entity)?.value)
    }

    @Test
    fun signaturesCorrectlyMatchMultiComponentFamilies() {
        val world = World()
        val entity = world.create()
        
        val family = world.family<CompA, CompB>()
        assertEquals(0, family.size)

        world.add(entity, CompA())
        assertEquals(0, family.size)

        world.add(entity, CompB())
        assertEquals(1, family.size)

        world.remove<CompA>(entity)
        assertEquals(0, family.size)
    }

    @Test
    fun creatingEntityDoesNotInvalidateCachedQueries() {
        val world = World()
        val entity1 = world.create()
        world.add(entity1, CompA())

        val query = world.query(CompA::class)
        assertEquals(1, query.size)

        // Create a new entity - should not dirty the query cache
        world.create()
        
        // This should hit the cache if our logic is correct
        val queryAfterCreate = world.query(CompA::class)
        assertSame(query, queryAfterCreate) 
    }

    private class PooledComponent : Poolable {
        var value: Int = 0
        override fun reset() {
            value = 0
        }
    }

    private class CompA
    private class CompB
}
