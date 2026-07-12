// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld

/**
 * Constructs this platform's real [PhysicsWorld], or `null` where none exists yet -- same
 * "expect/actual returns null on an unsupported platform" shape as
 * `awake:scene`'s `createDemoNavMesh()`. `awake:backend:jolt`'s desktop+Android
 * `JoltPhysicsWorld` is real (jolt-jni backed); iOS (JoltC cinterop) and wasmJs
 * (JoltPhysics.js) are stub-only there (`TODO()`-throwing), so this returns `null` on those
 * two platforms rather than constructing a `JoltPhysicsWorld` that would crash immediately --
 * see docs/MVP_PLAN.md's decision log for the Jolt Physics integration entry.
 */
expect fun createPhysicsWorld(): PhysicsWorld?
