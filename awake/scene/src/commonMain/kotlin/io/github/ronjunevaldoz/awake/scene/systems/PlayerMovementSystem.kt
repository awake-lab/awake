// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.MovementControl
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * MVP1a (see docs/MMORPG_ROADMAP.md): moves [playerTransform] directly from 
 * [MovementControl] intents. Decoupled from hardware input.
 */
class PlayerMovementSystem(
    private val playerTransform: Transform,
    private val speed: Float = DEFAULT_SPEED
) : System {

    override fun update(world: World, delta: Float) {
        // We find the specific MovementControl component associated with this system's target.
        // In a full ECS, we might iterate all entities, but PlayerMovementSystem usually 
        // tracks a specific player.
        world.queryEach(Transform::class, MovementControl::class) { entity, transform, control ->
            // Only move if this is the transform we were told to move
            if (transform !== playerTransform) return@queryEach
            if (control.moveX == 0f && control.moveZ == 0f) return@queryEach
            
            transform.position.x += control.moveX * speed * delta
            transform.position.z += control.moveZ * speed * delta
        }
    }

    private companion object {
        const val DEFAULT_SPEED = 3f
    }
}
