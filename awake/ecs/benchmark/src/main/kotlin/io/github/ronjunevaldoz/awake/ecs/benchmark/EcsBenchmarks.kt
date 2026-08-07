// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs.benchmark

import com.artemis.WorldConfigurationBuilder
import com.github.quillraven.fleks.configureWorld
import io.github.ronjunevaldoz.awake.ecs.ComponentTypeId
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
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
import com.artemis.World as ArtemisWorld
import com.badlogic.ashley.core.Engine as AshleyEngine
import com.badlogic.ashley.core.Entity as AshleyEntity
import com.github.quillraven.fleks.Entity as FleksEntity
import io.github.ronjunevaldoz.awake.ecs.Entity as AwakeEntity
import io.github.ronjunevaldoz.awake.ecs.World as AwakeWorld

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

    // The four benchmarks below stress structural churn on entities already in a *built*
    // family (the family cache's indexOf()/remove()/add() path), unlike the
    // awake*ComponentAddRemove benchmarks above which never build a family to churn against.
    // Pooled type-id fast path.
    @Benchmark
    fun awakeFamilyChurn(state: AwakeFamilyChurnState): Int {
        val world = state.world
        val transformTypeId = state.transformTypeId
        val entities = state.entities
        for (entity in entities) {
            world.remove<Transform>(entity, transformTypeId)
        }
        for (entity in entities) {
            world.add<Transform>(entity, transformTypeId)
        }
        return state.family.size
    }

    // Diagnostic only: same family-churn path, but bypasses pooling so we can isolate the
    // cost of component construction from the ECS hot path.
    @Benchmark
    fun awakeFamilyChurnTypeIdDirect(state: AwakeFamilyChurnState): Int {
        val world = state.world
        val transformTypeId = state.transformTypeId
        val entities = state.entities
        for (entity in entities) {
            world.remove<Transform>(entity, transformTypeId)
        }
        for (entity in entities) {
            world.add(entity, transformTypeId, Transform())
        }
        return state.family.size
    }

    // Diagnostic only: keeps the old class-based path around so we can compare it with the
    // cached type-id fast path above.
    @Benchmark
    fun awakeFamilyChurnCachedClass(state: AwakeFamilyChurnState): Int {
        val world = state.world
        val transformClass = Transform::class
        val entities = state.entities
        for (entity in entities) {
            world.remove(entity, transformClass)
        }
        for (entity in entities) {
            world.add(entity, transformClass, Transform())
        }
        return state.family.size
    }

    // Isolates QueryCollector.collect's recompute cost (the vararg world.query path, not
    // the maintained Family caches). Toggles a scratch component every call to force a
    // QueryCache miss, since a cache hit is just a map lookup and wouldn't exercise collect.
    @Benchmark
    fun awakeGeneralQueryIteration(state: AwakeGeneralQueryState): Int {
        val world = state.world
        if (state.scratchPresent) {
            world.remove<QueryChurnMarker>(state.scratch, state.scratchTypeId)
            state.scratchPresent = false
        } else {
            world.add(state.scratch, state.scratchTypeId, QueryChurnMarker())
            state.scratchPresent = true
        }
        return world.query(Transform::class, MeshRenderer::class).size
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

@State(Scope.Benchmark)
open class AwakeGeneralQueryState {
    @Param("10000", "100000")
    var entityCount: Int = 0
    lateinit var world: AwakeWorld
    var scratch: AwakeEntity = AwakeEntity.of(0, 0)
    var scratchTypeId: ComponentTypeId = ComponentTypeId(0)
    var scratchPresent = false

    @Setup(Level.Iteration)
    fun setup() {
        world = AwakeWorld()
        repeat(entityCount) {
            val entity = world.create()
            world.add(entity, Transform())
            world.add(entity, MeshRenderer(FakeGpuObjects.mesh, FakeGpuObjects.material))
        }
        world.store(QueryChurnMarker::class)
        scratch = world.create()
        scratchTypeId = world.typeId(QueryChurnMarker::class)
        scratchPresent = false
    }
}

private class QueryChurnMarker
