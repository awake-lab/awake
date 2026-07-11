// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs.benchmark

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
open class SceneBenchmarks {
    @Benchmark
    fun awakeTransformMeshQuery(state: AwakeQueryState, bh: org.openjdk.jmh.infra.Blackhole): Int {
        var count = 0
        val family = state.family
        val componentsA = family.componentsA()
        val componentsB = family.componentsB()
        val size = family.size
        var index = 0
        while (index < size) {
            bh.consume(componentsA[index])
            bh.consume(componentsB[index])
            count += 1
            index += 1
        }
        return count
    }

    @Benchmark
    fun fleksTransformMeshQuery(state: FleksQueryState): Int {
        var count = 0
        with(state.world) {
            state.family.forEach { entity ->
                entity[FleksTransform]
                entity[FleksMeshRenderer]
                count++
            }
        }
        return count
    }

    @Benchmark
    fun artemisTransformMeshQuery(state: ArtemisQueryState): Int {
        var count = 0
        val entities = state.subscription.entities
        for (index in 0 until entities.size()) {
            state.transformMapper.get(entities[index])
            state.meshMapper.get(entities[index])
            count++
        }
        return count
    }

    @Benchmark
    fun ashleyTransformMeshQuery(state: AshleyQueryState): Int {
        var count = 0
        val entities = state.engine.getEntitiesFor(state.family)
        for (index in 0 until entities.size()) {
            state.transformMapper.get(entities[index])
            state.meshMapper.get(entities[index])
            count++
        }
        return count
    }

    @Benchmark
    fun awakeTransformHierarchyPropagation(state: AwakeHierarchyState): Float {
        state.system.update(state.world, FRAME_DELTA)
        return state.lastTransform().worldMatrix.m23
    }

    @Benchmark
    fun fleksTransformHierarchyPropagation(state: FleksHierarchyState): Float {
        state.world.update(FRAME_DELTA)
        return with(state.world) { state.lastEntity[FleksTransform].worldMatrix.m23 }
    }

    @Benchmark
    fun artemisTransformHierarchyPropagation(state: ArtemisHierarchyState): Float {
        state.system.propagate()
        return state.transformMapper.get(state.lastEntity).worldMatrix.m23
    }

    @Benchmark
    fun ashleyTransformHierarchyPropagation(state: AshleyHierarchyState): Float {
        state.propagate()
        return state.transformMapper.get(state.lastEntity).worldMatrix.m23
    }
}

private const val FRAME_DELTA = 1f / 60f
