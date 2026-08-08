// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.dsl

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.ensure
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import kotlin.reflect.KClass
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/**
 * A flat configurator for entities that stores lambdas to be applied to pooled components.
 */
class EntityModifier {
    @PublishedApi
    internal val actions = mutableListOf<(World, Entity) -> Unit>()

    /**
     * Appends a pre-existing component instance to the entity.
     * Mutates and returns this modifier; it is a builder step, not a pure combinator.
     */
    fun with(component: Any): EntityModifier = apply {
        actions.add { world, entity ->
            // component::class is always the exact runtime type of `component`, so pairing
            // them as World.add(entity, KClass<Any>, Any) is type-correct; only the static
            // KClass<out Any> to KClass<Any> widening needs the cast, not the value itself.
            @Suppress("UNCHECKED_CAST")
            world.add(entity, component::class as KClass<Any>, component)
        }
    }

    /**
     * Adds (or reuses) a [T] on the entity and applies [setup] to it.
     *
     * [factory] is required rather than reflected: `world.add<T>()` resolves a no-arg
     * constructor at runtime, which Kotlin does not emit for a component holding a value
     * class (`Transform.parent: Entity?` is the live example), and which is unsupported
     * outright on iOS and wasmJs. Passing `::Transform` keeps this path allocation-explicit
     * and identical on every target.
     */
    inline fun <reified T : Any> configure(
        noinline factory: () -> T,
        crossinline setup: T.() -> Unit,
    ) = apply {
        actions.add { world, entity ->
            world.ensure(entity, factory).setup()
        }
    }
}

/**
 * Global entry point for building an [EntityModifier].
 *
 * Capitalised on purpose: it mirrors Compose's `Modifier` so entity authoring reads the same
 * way UI authoring does. Note this is a *function*, unlike `ui.modifier.Modifier`, which is a
 * `val` -- both names can be imported into one file without clashing.
 */
// Capitalized to mirror Compose's Modifier entry point by design (see the doc comment
// above), not an accidental violation of function-naming convention.
@Suppress("FunctionNaming", "ktlint:standard:function-naming")
fun Modifier() = EntityModifier()

// --- Semantic Extensions ---

/**
 * Configures a [Transform] component.
 */
fun EntityModifier.transform(
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
 * Makes the entity a working camera.
 *
 * Attaches BOTH halves, because either one alone is silently useless: [Camera] is the lens
 * that `RenderSystem` looks for when picking the view to draw from, and [CameraComponent] is
 * the control state `CameraSystem` drives. An entity with only the latter is never rendered
 * from and never moves.
 */
fun EntityModifier.camera(
    mode: CameraMode = CameraMode.FirstPerson,
    target: Entity? = null,
    lens: CoreCamera = defaultLens(),
    primary: Boolean = true,
    setup: CameraComponent.() -> Unit = {},
): EntityModifier = with(Camera(lens, isPrimary = primary)).configure(::CameraComponent) {
    this.mode = mode
    this.targetEntity = target
    this.setup()
}

private fun defaultLens() = CoreCamera.perspective()

/**
 * Configures a [MeshRenderer] component.
 */
fun EntityModifier.meshRenderer(
    mesh: Mesh,
    material: Material,
) = with(MeshRenderer(mesh, material))
