/*
 * Awake
 * Awake.awake-ecs-benchmark
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

package io.github.ronjunevaldoz.awake.ecs.benchmark

import com.artemis.World as ArtemisWorld
import com.artemis.WorldConfigurationBuilder
import com.badlogic.ashley.core.Engine as AshleyEngine
import com.badlogic.ashley.core.Entity as AshleyEntity
import com.github.quillraven.fleks.Entity as FleksEntity
import com.github.quillraven.fleks.World as FleksWorld
import com.github.quillraven.fleks.configureWorld
import io.github.ronjunevaldoz.awake.ecs.Entity as AwakeEntity
import io.github.ronjunevaldoz.awake.ecs.World as AwakeWorld
import io.github.ronjunevaldoz.awake.scene.components.Transform
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Suppress("TooManyFunctions")
open class EcsBenchmarks {
    @Benchmark
    fun awakeCreateDestroy(state: EntityCountState): Int {
        val world = AwakeWorld()
        val entities = ArrayList<AwakeEntity>(state.entityCount)
        repeat(state.entityCount) {
            entities += world.create()
        }
        entities.forEach(world::destroy)
        return entities.count(world::isAlive)
    }

    @Benchmark
    fun fleksCreateDestroy(state: EntityCountState): Int {
        val world = configureWorld(state.entityCount) { }
        repeat(state.entityCount) {
            world.entity()
        }
        world.removeAll()
        return world.numEntities
    }

    @Benchmark
    fun artemisCreateDestroy(state: EntityCountState): Int {
        val world = ArtemisWorld(WorldConfigurationBuilder().build())
        val entities = IntArray(state.entityCount)
        repeat(state.entityCount) { index ->
            entities[index] = world.create()
        }
        entities.forEach(world::delete)
        world.process()
        return entities.count(world.entityManager::isActive)
    }

    @Benchmark
    fun ashleyCreateDestroy(state: EntityCountState): Int {
        val engine = AshleyEngine()
        val entities = ArrayList<AshleyEntity>(state.entityCount)
        repeat(state.entityCount) {
            val entity = engine.createEntity()
            engine.addEntity(entity)
            entities += entity
        }
        entities.forEach(engine::removeEntity)
        return engine.entities.size()
    }

    @Benchmark
    fun awakeComponentAddRemove(state: EntityCountState): Int {
        val world = AwakeWorld()
        val entities = ArrayList<AwakeEntity>(state.entityCount)
        repeat(state.entityCount) {
            entities += world.create()
        }
        entities.forEach { world.add(it, Transform()) }
        entities.forEach { world.remove<Transform>(it) }
        return world.componentCount(Transform::class)
    }

    @Benchmark
    fun fleksComponentAddRemove(state: EntityCountState): Int {
        val world = configureWorld(state.entityCount) { }
        val entities = ArrayList<FleksEntity>(state.entityCount)
        repeat(state.entityCount) {
            entities += world.entity()
        }
        with(world) {
            entities.forEach { entity ->
                entity.configure { it += FleksTransform() }
            }
            entities.forEach { entity ->
                entity.configure { it -= FleksTransform }
            }
        }
        return world.family { all(FleksTransform) }.numEntities
    }

    @Benchmark
    fun artemisComponentAddRemove(state: EntityCountState): Int {
        val world = ArtemisWorld(WorldConfigurationBuilder().build())
        val mapper = world.getMapper(ArtemisTransform::class.java)
        val entities = IntArray(state.entityCount) { world.create() }
        entities.forEach { mapper.create(it) }
        entities.forEach { mapper.remove(it) }
        world.process()
        return entities.count(mapper::has)
    }

    @Benchmark
    fun ashleyComponentAddRemove(state: EntityCountState): Int {
        val engine = AshleyEngine()
        val entities = ArrayList<AshleyEntity>(state.entityCount)
        repeat(state.entityCount) {
            val entity = engine.createEntity()
            engine.addEntity(entity)
            entities += entity
        }
        entities.forEach { it.add(AshleyTransform()) }
        entities.forEach { it.remove(AshleyTransform::class.java) }
        return entities.count { it.getComponent(AshleyTransform::class.java) != null }
    }

    // The four benchmarks below specifically stress structural churn (remove + re-add a
    // component) on entities that are already members of a *built* family -- i.e. the
    // family cache's own indexOf()/remove()/add() path, not just ComponentStore's. None of
    // the awake*ComponentAddRemove benchmarks above exercise this: they never call
    // world.family<...>(), so no family cache exists for them to churn against. See
    // docs/ecs-benchmark-scorecard.md for why this gap mattered.
    @Benchmark
    fun awakeFamilyChurn(state: AwakeFamilyChurnState): Int {
        state.entities.forEach { state.world.remove<Transform>(it) }
        state.entities.forEach { state.world.add(it, Transform()) }
        return state.family.size
    }

    // Diagnostic only (not a real API recommendation yet): hoists `Transform::class` once
    // instead of letting the reified `remove<Transform>()`/`add(entity, component)` sugar
    // re-derive it on every call, to test whether that's where the ClassReference.hashCode
    // /ReflectionFactory.getOrCreateKotlinClass profiler cost (~10% of samples) comes from.
    @Benchmark
    fun awakeFamilyChurnCachedClass(state: AwakeFamilyChurnState): Int {
        val transformClass = Transform::class
        state.entities.forEach { state.world.remove(it, transformClass) }
        state.entities.forEach { state.world.add(it, transformClass, Transform()) }
        return state.family.size
    }

    @Benchmark
    fun fleksFamilyChurn(state: FleksFamilyChurnState): Int {
        with(state.world) {
            state.entities.forEach { entity -> entity.configure { it -= FleksTransform } }
            state.entities.forEach { entity -> entity.configure { it += FleksTransform() } }
        }
        return state.family.numEntities
    }

    @Benchmark
    fun artemisFamilyChurn(state: ArtemisFamilyChurnState): Int {
        state.entities.forEach { state.transformMapper.remove(it) }
        state.entities.forEach { state.transformMapper.create(it) }
        state.world.process()
        return state.subscription.entities.size()
    }

    @Benchmark
    fun ashleyFamilyChurn(state: AshleyFamilyChurnState): Int {
        state.entities.forEach { it.remove(AshleyTransform::class.java) }
        state.entities.forEach { it.add(AshleyTransform()) }
        return state.engine.getEntitiesFor(state.family).size()
    }
}

@State(Scope.Thread)
open class EntityCountState {
    @Param("10000", "100000")
    var entityCount: Int = 0
}
