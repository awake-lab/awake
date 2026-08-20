// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.authoring.dsl

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.ensure
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.CullMode
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshRenderer
import kotlin.reflect.KClass
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/**
 * Scoped configurator for a single ECS entity and its child hierarchy.
 *
 * Exposes methods to directly attach components, configure transforms and lenses,
 * and spawn child entities via delegation to [SceneBuilder].
 *
 * @property world The backing [World] store owning this entity.
 * @property entity The current [Entity] handle being configured.
 */
@AwakeSceneDsl
class EntityScope internal constructor(
    val world: World,
    val entity: Entity,
    private val childBuilder: SceneBuilder,
) {
    /**
     * Attaches a pre-existing component instance directly to the entity.
     *
     * @param component The component instance to attach.
     */
    fun with(component: Any) {
        @Suppress("UNCHECKED_CAST")
        world.add(entity, component::class as KClass<Any>, component)
    }

    /**
     * Ensures an instance of [T] exists on the entity and applies [setup] to it.
     *
     * @param factory Explicit constructor supplier for [T], required for KMP platforms.
     * @param setup Lambda block to initialize or mutate the component.
     */
    inline fun <reified T : Any> configure(
        noinline factory: () -> T,
        crossinline setup: T.() -> Unit,
    ) {
        world.ensure(entity, factory).setup()
    }

    /**
     * Spawns a child entity parented to this entity.
     *
     * @param name The optional descriptive name for the child entity.
     * @param block The configuration block executed within an [EntityScope] for the child.
     * @return The newly spawned child [Entity] handle.
     */
    fun entity(
        name: String? = null,
        block: EntityScope.() -> Unit = {},
    ): Entity = childBuilder.entity(name, block)
}

// --- Semantic Extensions ---

/**
 * Configures the [Transform] component of this entity.
 *
 * @param x The X position offset.
 * @param y The Y position offset.
 * @param z The Z position offset.
 * @param sx The X scale factor.
 * @param sy The Y scale factor.
 * @param sz The Z scale factor.
 * @param rx The X rotation angle in radians.
 * @param ry The Y rotation angle in radians.
 * @param rz The Z rotation angle in radians.
 */
fun EntityScope.transform(
    x: Float = 0f,
    y: Float = 0f,
    z: Float = 0f,
    sx: Float = 1f,
    sy: Float = 1f,
    sz: Float = 1f,
    rx: Float = 0f,
    ry: Float = 0f,
    rz: Float = 0f,
) = configure(::Transform) {
    position.set(x, y, z)
    scale.set(sx, sy, sz)
    rotation.set(rx, ry, rz)
}

/**
 * Configures this entity as an active camera with lens and control components.
 *
 * @param mode The camera navigation mode.
 * @param target The optional target entity to track or follow.
 * @param lens The core camera lens specification.
 * @param primary Whether this camera is the primary render viewpoint.
 * @param setup Configuration lambda for the [CameraComponent].
 */
fun EntityScope.camera(
    mode: CameraMode = CameraMode.FirstPerson,
    target: Entity? = null,
    lens: CoreCamera = defaultLens(),
    primary: Boolean = true,
    setup: CameraComponent.() -> Unit = {},
) {
    with(Camera(lens, isPrimary = primary))
    configure(::CameraComponent) {
        this.mode = mode
        this.targetEntity = target
        this.setup()
    }
}

private fun defaultLens() = CoreCamera.perspective()

/**
 * Configures a [MeshRenderer] component on this entity.
 *
 * @param mesh The mesh asset to render.
 * @param material The material to shade the mesh with.
 * @param cullMode The rasterizer triangle cull mode.
 */
fun EntityScope.meshRenderer(
    mesh: Mesh,
    material: Material,
    cullMode: CullMode = CullMode.None,
) = with(MeshRenderer(mesh, material, cullMode))
