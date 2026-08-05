// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.components

import io.github.ronjunevaldoz.awake.physics.BodyHandle
import io.github.ronjunevaldoz.awake.physics.MotionType
import io.github.ronjunevaldoz.awake.physics.PhysicsShape

/**
 * Declares that an entity should have a live physics body -- same "declared in scene JSON,
 * backed by a live GPU/native resource" shape as [MeshRenderer] (a plain, non-[Poolable]
 * data class carrying just enough to describe the resource before it exists), except the
 * live resource here is a jolt-jni body rather than a GPU handle.
 *
 * [handle] starts `null` and stays that way until [io.github.ronjunevaldoz.awake.scene.systems.PhysicsSystem]
 * actually creates the backing body -- ECS components (including ones loaded straight from
 * `scene.json` via `SceneLoader`) are constructed long before any `PhysicsWorld` exists, so
 * this can't be a constructor-required, non-null field the way [shape]/[motionType] are.
 */
data class PhysicsBody(
    val shape: PhysicsShape,
    val motionType: MotionType,
    var handle: BodyHandle? = null
)
