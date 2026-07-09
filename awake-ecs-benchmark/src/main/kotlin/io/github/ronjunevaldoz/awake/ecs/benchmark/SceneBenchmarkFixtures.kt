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

import com.artemis.Aspect
import com.artemis.Component as ArtemisComponent
import com.artemis.World as ArtemisWorld
import com.artemis.WorldConfigurationBuilder
import com.badlogic.ashley.core.Component as AshleyComponent
import com.badlogic.ashley.core.Engine as AshleyEngine
import com.badlogic.ashley.core.Entity as AshleyEntity
import com.badlogic.ashley.core.Family as AshleyFamily
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
import io.github.ronjunevaldoz.awake.ecs.Family2 as AwakeFamily2
import io.github.ronjunevaldoz.awake.ecs.World as AwakeWorld
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.TransformSystem
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import sun.misc.Unsafe

@State(Scope.Benchmark)
open class AwakeQueryState {
    @Param("10000", "100000")
    var entityCount: Int = 0
    lateinit var world: AwakeWorld
    lateinit var family: AwakeFamily2<Transform, MeshRenderer>

    @Setup(Level.Iteration)
    fun setup() {
        world = AwakeWorld()
        repeat(entityCount) {
            val entity = world.create()
            world.add(entity, Transform())
            world.add(entity, MeshRenderer(FakeGpuObjects.mesh, FakeGpuObjects.material))
        }
        family = world.family<Transform, MeshRenderer>()
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
open class ArtemisQueryState {
    @Param("10000", "100000")
    var entityCount: Int = 0
    lateinit var world: ArtemisWorld
    lateinit var subscription: com.artemis.EntitySubscription
    lateinit var transformMapper: com.artemis.ComponentMapper<ArtemisTransform>
    lateinit var meshMapper: com.artemis.ComponentMapper<ArtemisMeshRenderer>

    @Setup(Level.Iteration)
    fun setup() {
        world = ArtemisWorld(WorldConfigurationBuilder().build())
        transformMapper = world.getMapper(ArtemisTransform::class.java)
        meshMapper = world.getMapper(ArtemisMeshRenderer::class.java)
        subscription = world.aspectSubscriptionManager.get(
            Aspect.all(ArtemisTransform::class.java, ArtemisMeshRenderer::class.java)
        )
        repeat(entityCount) {
            val entity = world.create()
            transformMapper.create(entity)
            meshMapper.create(entity).apply {
                mesh = FakeGpuObjects.mesh
                material = FakeGpuObjects.material
            }
        }
        world.process()
    }
}

@State(Scope.Benchmark)
open class AshleyQueryState {
    @Param("10000", "100000")
    var entityCount: Int = 0
    lateinit var engine: AshleyEngine
    lateinit var family: AshleyFamily
    lateinit var transformMapper: com.badlogic.ashley.core.ComponentMapper<AshleyTransform>
    lateinit var meshMapper: com.badlogic.ashley.core.ComponentMapper<AshleyMeshRenderer>

    @Setup(Level.Iteration)
    fun setup() {
        engine = AshleyEngine()
        transformMapper = com.badlogic.ashley.core.ComponentMapper.getFor(AshleyTransform::class.java)
        meshMapper = com.badlogic.ashley.core.ComponentMapper.getFor(AshleyMeshRenderer::class.java)
        family = AshleyFamily.all(AshleyTransform::class.java, AshleyMeshRenderer::class.java).get()
        repeat(entityCount) {
            val entity = engine.createEntity()
            entity.add(AshleyTransform())
            entity.add(AshleyMeshRenderer(FakeGpuObjects.mesh, FakeGpuObjects.material))
            engine.addEntity(entity)
        }
    }
}

@State(Scope.Benchmark)
open class AwakeFamilyChurnState {
    @Param("10000", "100000")
    var entityCount: Int = 0
    lateinit var world: AwakeWorld
    lateinit var entities: List<AwakeEntity>
    lateinit var family: AwakeFamily2<Transform, MeshRenderer>

    @Setup(Level.Iteration)
    fun setup() {
        world = AwakeWorld()
        val list = ArrayList<AwakeEntity>(entityCount)
        repeat(entityCount) {
            val entity = world.create()
            world.add(entity, Transform())
            world.add(entity, MeshRenderer(FakeGpuObjects.mesh, FakeGpuObjects.material))
            list += entity
        }
        // Build the family cache BEFORE churning -- this is the whole point of this
        // benchmark: exercise indexOf()/remove()/add() on an already-built family cache,
        // not just ComponentStore's.
        family = world.family<Transform, MeshRenderer>()
        entities = list
    }
}

@State(Scope.Benchmark)
open class FleksFamilyChurnState {
    @Param("10000", "100000")
    var entityCount: Int = 0
    lateinit var world: FleksWorld
    lateinit var entities: List<FleksEntity>
    lateinit var family: com.github.quillraven.fleks.Family

    @Setup(Level.Iteration)
    fun setup() {
        world = configureWorld(entityCount) { }
        family = world.family { all(FleksTransform, FleksMeshRenderer) }
        val list = ArrayList<FleksEntity>(entityCount)
        repeat(entityCount) {
            list += world.entity {
                it += FleksTransform()
                it += FleksMeshRenderer(FakeGpuObjects.mesh, FakeGpuObjects.material)
            }
        }
        entities = list
    }
}

@State(Scope.Benchmark)
open class ArtemisFamilyChurnState {
    @Param("10000", "100000")
    var entityCount: Int = 0
    lateinit var world: ArtemisWorld
    lateinit var entities: IntArray
    lateinit var subscription: com.artemis.EntitySubscription
    lateinit var transformMapper: com.artemis.ComponentMapper<ArtemisTransform>

    @Setup(Level.Iteration)
    fun setup() {
        world = ArtemisWorld(WorldConfigurationBuilder().build())
        transformMapper = world.getMapper(ArtemisTransform::class.java)
        val meshMapper = world.getMapper(ArtemisMeshRenderer::class.java)
        subscription = world.aspectSubscriptionManager.get(
            Aspect.all(ArtemisTransform::class.java, ArtemisMeshRenderer::class.java)
        )
        entities = IntArray(entityCount) {
            val entity = world.create()
            transformMapper.create(entity)
            meshMapper.create(entity).apply {
                mesh = FakeGpuObjects.mesh
                material = FakeGpuObjects.material
            }
            entity
        }
        world.process()
    }
}

@State(Scope.Benchmark)
open class AshleyFamilyChurnState {
    @Param("10000", "100000")
    var entityCount: Int = 0
    lateinit var engine: AshleyEngine
    lateinit var family: AshleyFamily
    lateinit var entities: List<AshleyEntity>

    @Setup(Level.Iteration)
    fun setup() {
        engine = AshleyEngine()
        family = AshleyFamily.all(AshleyTransform::class.java, AshleyMeshRenderer::class.java).get()
        val list = ArrayList<AshleyEntity>(entityCount)
        repeat(entityCount) {
            val entity = engine.createEntity()
            entity.add(AshleyTransform())
            entity.add(AshleyMeshRenderer(FakeGpuObjects.mesh, FakeGpuObjects.material))
            engine.addEntity(entity)
            list += entity
        }
        entities = list
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

@State(Scope.Benchmark)
open class ArtemisHierarchyState {
    @Param("10", "50")
    var depth: Int = 0
    lateinit var world: ArtemisWorld
    lateinit var transformMapper: com.artemis.ComponentMapper<ArtemisTransform>
    lateinit var system: ArtemisTransformSystem
    var lastEntity: Int = -1

    @Setup(Level.Iteration)
    fun setup() {
        world = ArtemisWorld(WorldConfigurationBuilder().build())
        transformMapper = world.getMapper(ArtemisTransform::class.java)
        system = ArtemisTransformSystem(world, transformMapper)
        var parent = -1
        repeat(depth) { index ->
            val entity = world.create()
            transformMapper.create(entity).apply {
                position = Vec3(0f, 0f, 1f)
                this.parent = parent
            }
            parent = entity
            if (index == depth - 1) {
                lastEntity = entity
            }
        }
        world.process()
    }
}

@State(Scope.Benchmark)
open class AshleyHierarchyState {
    @Param("10", "50")
    var depth: Int = 0
    lateinit var engine: AshleyEngine
    lateinit var transformMapper: com.badlogic.ashley.core.ComponentMapper<AshleyTransform>
    lateinit var lastEntity: AshleyEntity
    private val entities = mutableListOf<AshleyEntity>()

    @Setup(Level.Iteration)
    fun setup() {
        engine = AshleyEngine()
        transformMapper = com.badlogic.ashley.core.ComponentMapper.getFor(AshleyTransform::class.java)
        entities.clear()
        var parent: AshleyEntity? = null
        repeat(depth) { index ->
            val entity = engine.createEntity()
            entity.add(AshleyTransform(position = Vec3(0f, 0f, 1f), parent = parent))
            engine.addEntity(entity)
            entities += entity
            parent = entity
            if (index == depth - 1) {
                lastEntity = entity
            }
        }
    }

    // Reused across calls, same reasoning as Awake's TransformSystem (see
    // docs/ecs-benchmark-scorecard.md) -- keeps this a fair comparison of the ECS's own
    // per-entity access cost, not "which benchmark shim allocates less."
    private val visited = mutableSetOf<AshleyEntity>()
    private val visiting = mutableSetOf<AshleyEntity>()

    fun propagate() {
        visited.clear()
        visiting.clear()
        entities.forEach { entity -> propagateOne(entity) }
    }

    private fun propagateOne(entity: AshleyEntity): Mat4 {
        val transform = transformMapper.get(entity)
        if (entity in visited) {
            return transform.worldMatrix
        }
        check(visiting.add(entity)) { "Transform hierarchy contains a cycle." }

        val local = transform.localMatrix()
        val parent = transform.parent
        transform.worldMatrix = if (parent != null) {
            local * propagateOne(parent)
        } else {
            local
        }

        visiting.remove(entity)
        visited += entity
        return transform.worldMatrix
    }
}

class ArtemisTransformSystem(
    private val world: ArtemisWorld,
    private val mapper: com.artemis.ComponentMapper<ArtemisTransform>
) {
    // Reused across calls -- see AshleyHierarchyState's identical comment.
    private val visited = mutableSetOf<Int>()
    private val visiting = mutableSetOf<Int>()

    fun propagate() {
        val subscription = world.aspectSubscriptionManager.get(Aspect.all(ArtemisTransform::class.java))
        val entities = subscription.entities
        visited.clear()
        visiting.clear()
        for (index in 0 until entities.size()) {
            propagateOne(entities[index])
        }
    }

    private fun propagateOne(entity: Int): Mat4 {
        val transform = mapper.get(entity)
        if (entity in visited) {
            return transform.worldMatrix
        }
        check(visiting.add(entity)) { "Transform hierarchy contains a cycle." }

        val local = transform.localMatrix()
        val parent = transform.parent
        transform.worldMatrix = if (parent >= 0) {
            local * propagateOne(parent)
        } else {
            local
        }

        visiting.remove(entity)
        visited += entity
        return transform.worldMatrix
    }
}

class ArtemisTransform : ArtemisComponent() {
    var position: Vec3 = Vec3(0f, 0f, 1f)
    var rotation: Vec3 = Vec3(0f, 0f, 0f)
    var scale: Vec3 = Vec3(1f, 1f, 1f)
    var parent: Int = -1
    var worldMatrix: Mat4 = Mat4()

    fun localMatrix(): Mat4 {
        return Mat4()
            .translate(position.x, position.y, position.z)
            .rotateZ(rotation.z)
            .rotateY(rotation.y)
            .rotateX(rotation.x)
            .scale(scale.x, scale.y, scale.z)
    }
}

class ArtemisMeshRenderer : ArtemisComponent() {
    lateinit var mesh: Mesh
    lateinit var material: Material
}

data class AshleyTransform(
    var position: Vec3 = Vec3(0f, 0f, 1f),
    var rotation: Vec3 = Vec3(0f, 0f, 0f),
    var scale: Vec3 = Vec3(1f, 1f, 1f),
    var parent: AshleyEntity? = null,
    var worldMatrix: Mat4 = Mat4()
) : AshleyComponent {
    fun localMatrix(): Mat4 {
        return Mat4()
            .translate(position.x, position.y, position.z)
            .rotateZ(rotation.z)
            .rotateY(rotation.y)
            .rotateX(rotation.x)
            .scale(scale.x, scale.y, scale.z)
    }
}

data class AshleyMeshRenderer(
    val mesh: Mesh,
    val material: Material
) : AshleyComponent

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
    // Reused across calls -- see AshleyHierarchyState's identical comment.
    private val transforms = linkedMapOf<FleksEntity, FleksTransform>()
    private val visited = mutableSetOf<FleksEntity>()
    private val visiting = mutableSetOf<FleksEntity>()

    override fun onTick() {
        transforms.clear()
        with(world) {
            family.forEach { entity ->
                transforms[entity] = entity[FleksTransform]
            }
        }

        visited.clear()
        visiting.clear()
        transforms.keys.forEach { entity ->
            propagate(entity)
        }
    }

    private fun propagate(entity: FleksEntity): Mat4 {
        if (entity in visited) {
            return transforms.getValue(entity).worldMatrix
        }
        check(visiting.add(entity)) { "Transform hierarchy contains a cycle at $entity." }

        val transform = transforms.getValue(entity)
        val local = transform.localMatrix()
        val parent = transform.parent
        transform.worldMatrix = if (parent != null && transforms.containsKey(parent)) {
            local * propagate(parent)
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
