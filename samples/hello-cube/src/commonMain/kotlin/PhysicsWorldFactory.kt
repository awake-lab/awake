// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld

/**
 * Constructs this platform's real [PhysicsWorld], or `null` where none exists yet -- same
 * "expect/actual returns null on an unsupported platform" shape as
 * `awake:scene`'s `createDemoNavMesh()`. `awake:backend:jolt`'s desktop/Android (jolt-jni),
 * iOS (JoltC cinterop) and wasmJs (JoltPhysics.js) backends are all real now -- this stays
 * `expect`/`actual` per-platform (rather than a single commonMain implementation) since each
 * backend is a separate native binding with zero shared code, see docs/MVP_PLAN.md's decision
 * log for the Jolt Physics integration entry.
 *
 * `suspend` (not a plain function) across every platform: wasmJs's `JoltPhysicsWorld` needs
 * `jolt-physics`'s own async Emscripten module bootstrap to resolve before it can be
 * constructed (see that class's doc comment) -- the other three platforms' `actual`s are
 * still plain synchronous constructor calls under the hood, `suspend` costs them nothing.
 * `PhysicsDemo.ready()` was already `suspend`, so this ripples no further than that one call
 * site.
 */
expect suspend fun createPhysicsWorld(): PhysicsWorld?
