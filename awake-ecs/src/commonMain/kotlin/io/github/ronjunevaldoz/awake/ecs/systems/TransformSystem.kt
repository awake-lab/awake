/*
 * Awake
 * Awake.awake-ecs.commonMain
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

package io.github.ronjunevaldoz.awake.ecs.systems

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.components.Transform

class TransformSystem : System {
    override fun update(world: World, delta: Float) {
        val transforms = world.query(Transform::class)
            .associateWith { entity -> world.get<Transform>(entity) ?: error("Missing Transform for $entity") }
        val visited = mutableSetOf<Entity>()
        val visiting = mutableSetOf<Entity>()

        transforms.keys.forEach { entity ->
            propagate(entity, transforms, visited, visiting)
        }
    }

    private fun propagate(
        entity: Entity,
        transforms: Map<Entity, Transform>,
        visited: MutableSet<Entity>,
        visiting: MutableSet<Entity>
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
