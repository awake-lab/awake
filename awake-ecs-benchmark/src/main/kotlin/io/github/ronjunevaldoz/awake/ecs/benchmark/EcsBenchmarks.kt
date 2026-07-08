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

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity as FleksEntity
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World as FleksWorld
import com.github.quillraven.fleks.configureWorld
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.ecs.Entity as AwakeEntity
import io.github.ronjunevaldoz.awake.ecs.World as AwakeWorld
import io.github.ronjunevaldoz.awake.ecs.components.MeshRenderer
import io.github.ronjunevaldoz.awake.ecs.components.Transform
import io.github.ronjunevaldoz.awake.ecs.systems.TransformSystem
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import sun.misc.Unsafe
import java.util.concurrent.TimeUnit

@Fork(1)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
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
    fun awakeTransformMeshQuery(state: AwakeQueryState): Int {
        var count = 0
        state.world.query(Transform::class, MeshRenderer::class).forEach { entity ->
            state.world.get<Transform>(entity)
            state.world.get<MeshRenderer>(entity)
            count++
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
    fun awakeTransformHierarchyPropagation(state: AwakeHierarchyState): Float {
        state.system.update(state.world, FRAME_DELTA)
        return state.lastTransform().worldMatrix.m23
    }

    @Benchmark
    fun fleksTransformHierarchyPropagation(state: FleksHierarchyState): Float {
        state.world.update(FRAME_DELTA)
        return with(state.world) { state.lastEntity[FleksTransform].worldMatrix.m23 }
    }
}

private const val FRAME_DELTA = 1f / 60f

@State(Scope.Thread)
open class EntityCountState {
    @Param("10000", "100000")
    var entityCount: Int = 0
}

@State(Scope.Benchmark)
open class AwakeQueryState {
    @Param("10000", "100000")
    var entityCount: Int = 0
    lateinit var world: AwakeWorld

    @Setup(Level.Iteration)
    fun setup() {
        world = AwakeWorld()
        repeat(entityCount) {
            val entity = world.create()
            world.add(entity, Transform())
            world.add(entity, MeshRenderer(FakeGpuObjects.mesh, FakeGpuObjects.material))
        }
    }
}

@State(Scope.Benchmark)
open class FleksQueryState {
    @Param("10000", "100000")
    var entityCount: Int = 0
    lateinit var world: FleksWorld
    lateinit var family: com.github.quillraven.fleks.Family

    @Setup(Level.Iteration)
    fun setup() {
        world = configureWorld(entityCount) { }
        family = world.family { all(FleksTransform, FleksMeshRenderer) }
        repeat(entityCount) {
            world.entity {
                it += FleksTransform()
                it += FleksMeshRenderer(FakeGpuObjects.mesh, FakeGpuObjects.material)
            }
        }
    }
}

@State(Scope.Benchmark)
open class AwakeHierarchyState {
    @Param("10", "50")
    var depth: Int = 0
    lateinit var world: AwakeWorld
    var lastEntity: AwakeEntity = AwakeEntity.of(0, 0)
    val system = TransformSystem()

    @Setup(Level.Iteration)
    fun setup() {
        world = AwakeWorld()
        var parent: AwakeEntity? = null
        repeat(depth) { index ->
            val entity = world.create()
            world.add(entity, Transform(position = Vec3(0f, 0f, 1f), parent = parent))
            parent = entity
            if (index == depth - 1) {
                lastEntity = entity
            }
        }
    }

    fun lastTransform(): Transform {
        return world.get(lastEntity, Transform::class) ?: error("Missing last transform.")
    }
}

@State(Scope.Benchmark)
open class FleksHierarchyState {
    @Param("10", "50")
    var depth: Int = 0
    lateinit var world: FleksWorld
    lateinit var lastEntity: FleksEntity

    @Setup(Level.Iteration)
    fun setup() {
        world = configureWorld(depth) {
            systems {
                add(FleksTransformSystem())
            }
        }
        var parent: FleksEntity? = null
        repeat(depth) { index ->
            val currentParent = parent
            val entity = world.entity {
                it += FleksTransform(parent = currentParent)
            }
            parent = entity
            if (index == depth - 1) {
                lastEntity = entity
            }
        }
    }
}

data class FleksTransform(
    var position: Vec3 = Vec3(0f, 0f, 1f),
    var rotation: Vec3 = Vec3(0f, 0f, 0f),
    var scale: Vec3 = Vec3(1f, 1f, 1f),
    var parent: FleksEntity? = null,
    var worldMatrix: Mat4 = Mat4()
) : Component<FleksTransform> {
    override fun type(): ComponentType<FleksTransform> = FleksTransform

    fun localMatrix(): Mat4 {
        return Mat4()
            .translate(position.x, position.y, position.z)
            .rotateZ(rotation.z)
            .rotateY(rotation.y)
            .rotateX(rotation.x)
            .scale(scale.x, scale.y, scale.z)
    }

    companion object : ComponentType<FleksTransform>()
}

data class FleksMeshRenderer(
    val mesh: Mesh,
    val material: Material
) : Component<FleksMeshRenderer> {
    override fun type(): ComponentType<FleksMeshRenderer> = FleksMeshRenderer

    companion object : ComponentType<FleksMeshRenderer>()
}

class FleksTransformSystem : IntervalSystem() {
    private val family = world.family { all(FleksTransform) }

    override fun onTick() {
        val transforms = linkedMapOf<FleksEntity, FleksTransform>()
        with(world) {
            family.forEach { entity ->
                transforms[entity] = entity[FleksTransform]
            }
        }

        val visited = mutableSetOf<FleksEntity>()
        val visiting = mutableSetOf<FleksEntity>()
        transforms.keys.forEach { entity ->
            propagate(entity, transforms, visited, visiting)
        }
    }

    private fun propagate(
        entity: FleksEntity,
        transforms: Map<FleksEntity, FleksTransform>,
        visited: MutableSet<FleksEntity>,
        visiting: MutableSet<FleksEntity>
    ): Mat4 {
        if (entity in visited) {
            return transforms.getValue(entity).worldMatrix
        }
        check(visiting.add(entity)) { "Transform hierarchy contains a cycle at $entity." }

        val transform = transforms.getValue(entity)
        val local = transform.localMatrix()
        val parent = transform.parent
        transform.worldMatrix = if (parent != null && transforms.containsKey(parent)) {
            local * propagate(parent, transforms, visited, visiting)
        } else {
            local
        }

        visiting.remove(entity)
        visited += entity
        return transform.worldMatrix
    }
}

private object FakeGpuObjects {
    val mesh: Mesh = UnsafeAllocator.allocate(Mesh::class.java)
    val material: Material = UnsafeAllocator.allocate(Material::class.java)
}

private object UnsafeAllocator {
    private val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe")
        .apply { isAccessible = true }
        .get(null) as Unsafe

    fun <T : Any> allocate(type: Class<T>): T {
        return type.cast(unsafe.allocateInstance(type))
    }
}
