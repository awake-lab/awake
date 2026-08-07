// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.dsl

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Name
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * Structural orchestrator for building an ECS scene graph.
 */
@AwakeSceneDsl
class SceneBuilder internal constructor(
    private val world: World,
    val parentEntity: Entity? = null
) {
    /**
     * Spawns an entity with an optional name, modifier, and child block.
     */
    fun entity(
        name: String? = null,
        modifier: EntityModifier = Modifier(),
        block: SceneBuilder.() -> Unit = {}
    ): Entity {
        val currentEntity = world.create()

        if (name != null) {
            world.add(currentEntity, Name(name))
        }

        // Flush modifier actions to world storage
        modifier.actions.forEach { action -> action(world, currentEntity) }

        if (parentEntity != null) {
            world.get<Transform>(currentEntity)?.parent = parentEntity
        }

        // Process children
        val childContext = SceneBuilder(world, parentEntity = currentEntity)
        childContext.block()

        return currentEntity
    }
}

/**
 * Entry point for building a scene against a [World].
 */
fun World.scene(block: SceneBuilder.() -> Unit) {
    SceneBuilder(this).block()
}
