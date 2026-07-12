// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.navigation.NavMesh

/**
 * MVP1a's first ECS-coupled AI behavior (see docs/MMORPG_ROADMAP.md): [npcTransform] chases
 * [targetTransform] by periodically re-querying [navMesh] for a path and steering
 * kinematically toward the next waypoint -- same "deliberately simple, no physics" bar as
 * [PlayerMovementSystem]. Chase only, not chase-*and*-avoid; dodge/avoid behavior is a
 * follow-up, not required to prove the navmesh pipeline works.
 *
 * Re-paths every [repathInterval] seconds rather than every frame -- `recast4j`'s
 * `findPath`/`findStraightPath` aren't cheap enough to redo at 60Hz for a single NPC, let
 * alone many.
 */
class ChaseAiSystem(
    private val npcTransform: Transform,
    private val targetTransform: Transform,
    private val navMesh: NavMesh,
    private val speed: Float = DEFAULT_SPEED,
    private val repathInterval: Float = DEFAULT_REPATH_INTERVAL,
    private val waypointRadius: Float = DEFAULT_WAYPOINT_RADIUS
) : System {
    private var path: List<Vec3> = emptyList()
    private var waypointIndex = 0
    private var timeSinceRepath = repathInterval // repath immediately on the first call

    override fun update(world: World, delta: Float) {
        timeSinceRepath += delta
        if (timeSinceRepath >= repathInterval) {
            timeSinceRepath = 0f
            path = navMesh.findPath(npcTransform.position, targetTransform.position)
            waypointIndex = 0
        }

        val waypoint = path.getOrNull(waypointIndex) ?: return
        val dx = waypoint.x - npcTransform.position.x
        val dz = waypoint.z - npcTransform.position.z
        val distance = kotlin.math.sqrt(dx * dx + dz * dz)
        if (distance <= waypointRadius) {
            waypointIndex++
            return
        }
        val invDistance = 1f / distance
        npcTransform.position.x += dx * invDistance * speed * delta
        npcTransform.position.z += dz * invDistance * speed * delta
    }

    private companion object {
        const val DEFAULT_SPEED = 2.5f
        const val DEFAULT_REPATH_INTERVAL = 0.5f
        const val DEFAULT_WAYPOINT_RADIUS = 0.3f
    }
}
