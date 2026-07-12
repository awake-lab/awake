// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics.jolt

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.physics.BodyHandle
import io.github.ronjunevaldoz.awake.physics.BodyTransform
import io.github.ronjunevaldoz.awake.physics.MotionType
import io.github.ronjunevaldoz.awake.physics.PhysicsShape
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.physics.RaycastHit

/**
 * No Jolt Physics backend on iOS yet (see docs/MVP_PLAN.md's Jolt Physics decision log
 * entry) -- jolt-jni is JVM-only (desktop+Android). iOS's real path is a JoltC cinterop
 * binding (mirrors `awake:backend:vulkan`'s MoltenVK cinterop precedent), deliberately
 * deferred to a later slice. Exists only so this module compiles on every
 * `awake:physics:api` target -- every method throws, matching how `Material`/`Texture` are
 * `TODO()`-only on the WebGPU backend already.
 */
class JoltPhysicsWorld(gravity: Vec3 = Vec3(0f, -9.81f, 0f)) : PhysicsWorld {
    override fun createBody(
        shape: PhysicsShape,
        position: Vec3,
        rotation: Vec3,
        motionType: MotionType
    ): BodyHandle = TODO("Jolt iOS backend not yet implemented, see docs/MVP_PLAN.md's Jolt Physics decision log entry")

    override fun destroyBody(handle: BodyHandle): Unit =
        TODO("Jolt iOS backend not yet implemented, see docs/MVP_PLAN.md's Jolt Physics decision log entry")

    override fun step(deltaTime: Float): Unit =
        TODO("Jolt iOS backend not yet implemented, see docs/MVP_PLAN.md's Jolt Physics decision log entry")

    override fun syncTransforms(): List<BodyTransform> =
        TODO("Jolt iOS backend not yet implemented, see docs/MVP_PLAN.md's Jolt Physics decision log entry")

    override fun raycast(origin: Vec3, direction: Vec3, maxDistance: Float): RaycastHit? =
        TODO("Jolt iOS backend not yet implemented, see docs/MVP_PLAN.md's Jolt Physics decision log entry")

    override fun destroy(): Unit =
        TODO("Jolt iOS backend not yet implemented, see docs/MVP_PLAN.md's Jolt Physics decision log entry")
}
