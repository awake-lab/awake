// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * Propagates world matrices parent-before-child every frame via a memoized DFS (with cycle
 * detection). [visitedStamp]/[visitingStamp] track per-entity DFS state by `entity.id`
 * instead of a `Map<Entity, Transform>`/two `Set<Entity>` -- `Entity` is a value class, so
 * using it as a `Map`/`Set` key forces a box allocation plus `hashCode()`/`equals()` on
 * every visit. Profiling `awakeTransformHierarchyPropagation` showed that combined
 * `HashMap`/`HashSet`/`Entity.box-impl` cost at ~19% of CPU samples (see
 * docs/ecs-benchmark-scorecard.md). A node is "visited this frame" when its stamp equals
 * [frameStamp]; incrementing [frameStamp] once per [update] call implicitly invalidates
 * every previous frame's stamps without needing to clear the arrays. `world.get<Transform>`
 * (an O(1) sparse-set lookup, not a hash lookup) replaces the old snapshot map for parent
 * lookups. Still recomputes from scratch every frame (correct if a caller reparents an
 * entity mid-game by mutating `Transform.parent` directly, since there's no
 * change-notification hook for that).
 */
class TransformSystem : System {
    private var visitedStamp = IntArray(0)
    private var visitingStamp = IntArray(0)
    private var frameStamp = 0

    override fun update(world: World, delta: Float) {
        frameStamp += 1
        world.queryEach<Transform> { entity, transform ->
            propagate(world, entity, transform)
        }
    }

    private fun propagate(world: World, entity: Entity, transform: Transform): Mat4 {
        val id = entity.id
        ensureCapacity(id)
        if (visitedStamp[id] == frameStamp) {
            return transform.worldMatrix
        }
        check(visitingStamp[id] != frameStamp) { "Transform hierarchy contains a cycle at $entity." }
        visitingStamp[id] = frameStamp

        val local = transform.localMatrix()
        val parent = transform.parent
        val parentTransform = if (parent != null) world.get<Transform>(parent) else null
        transform.worldMatrix = if (parent != null && parentTransform != null) {
            local * propagate(world, parent, parentTransform)
        } else {
            local
        }

        visitedStamp[id] = frameStamp
        return transform.worldMatrix
    }

    private fun ensureCapacity(id: Int) {
        if (id < visitedStamp.size) {
            return
        }
        val newSize = maxOf(id + 1, maxOf(DEFAULT_CAPACITY, visitedStamp.size * CAPACITY_GROWTH_FACTOR))
        visitedStamp = visitedStamp.copyOf(newSize)
        visitingStamp = visitingStamp.copyOf(newSize)
    }

    private companion object {
        const val DEFAULT_CAPACITY = 16
        const val CAPACITY_GROWTH_FACTOR = 2
    }
}
