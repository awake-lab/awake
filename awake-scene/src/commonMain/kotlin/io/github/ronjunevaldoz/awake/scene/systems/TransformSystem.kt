/*
 * Awake
 * Awake.awake-scene.commonMain
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

package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * Propagates world matrices parent-before-child every frame via a memoized DFS (with cycle
 * detection). [transforms]/[visited]/[visiting] are reused instance state, not allocated
 * fresh each [update] call -- `update()` runs every frame in a real game, so a fresh
 * `Map`+two `Set`s per call was real per-frame allocation/rehashing pressure (see
 * docs/ecs-benchmark-scorecard.md). Recomputes from scratch every frame regardless (still
 * correct if a caller reparents an entity mid-game by mutating `Transform.parent` directly,
 * since there's no change-notification hook for that) -- only the *buffers* are reused, not
 * a cached traversal order, so this stays correct without needing `World` to expose a
 * structural-change version this system could check.
 */
class TransformSystem : System {
    private val transforms = mutableMapOf<Entity, Transform>()
    private val visited = mutableSetOf<Entity>()
    private val visiting = mutableSetOf<Entity>()

    override fun update(world: World, delta: Float) {
        transforms.clear()
        world.queryEach<Transform> { entity, transform ->
            transforms[entity] = transform
        }
        visited.clear()
        visiting.clear()

        transforms.keys.forEach { entity ->
            propagate(entity)
        }
    }

    private fun propagate(entity: Entity): Mat4 {
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
