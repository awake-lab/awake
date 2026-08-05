// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics

import io.github.ronjunevaldoz.awake.core.math.Vec3

/**
 * Jolt Physics integration, slice 1 (see docs/MVP_PLAN.md's decision log): mirrors
 * `awake:engine:render-api`/`awake:backend:vulkan`'s own module split -- a neutral
 * commonMain-only interface here, a concrete implementation per native binding in its own
 * backend module (`awake:backend:jolt` today, using jolt-jni on desktop+Android; a JoltC
 * cinterop backend for iOS and a JoltPhysics.js backend for wasmJs are deferred, since
 * jolt-jni/JoltC/JoltPhysics.js share zero code. Demo navmesh bootstrap follows the same
 * unsupported-platform-null pattern in sample code, not in this reusable physics contract.
 *
 * There is deliberately no factory function in this module -- platform bootstrap code
 * (a game's `Application`/`View` implementation) constructs the concrete
 * `JoltPhysicsWorld` from `awake:backend:jolt` directly, the same way `Renderer`'s concrete
 * type is constructed by platform bootstrap code rather than by `awake:engine:render-api`
 * itself.
 */
interface PhysicsWorld {
    fun createBody(shape: PhysicsShape, position: Vec3, rotation: Vec3, motionType: MotionType): BodyHandle

    fun destroyBody(handle: BodyHandle)

    fun step(deltaTime: Float)

    /** Batched readback of every tracked body's current pose -- one call per frame, never one
     * call per body per frame. Physics steps happen once per frame regardless of how many
     * bodies exist, so this API mirrors that: a per-body-per-frame JNI/cinterop crossing
     * would scale with body count instead of staying constant. */
    fun syncTransforms(): List<BodyTransform>

    fun raycast(origin: Vec3, direction: Vec3, maxDistance: Float): RaycastHit?

    fun destroy()
}
