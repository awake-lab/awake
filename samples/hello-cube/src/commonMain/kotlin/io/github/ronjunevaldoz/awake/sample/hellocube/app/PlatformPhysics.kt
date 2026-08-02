// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.physics.PhysicsWorld

/**
 * Constructs the sample's [PhysicsWorld], same "platform bootstrap wires the concrete
 * backend" shape as [platformBackendPreference] -- `awake:physics:api` deliberately has no
 * factory of its own (see that module's `PhysicsWorld` doc comment).
 *
 * `null` on platforms where a synchronous factory doesn't exist yet (today: wasmJs, whose
 * `JoltPhysicsWorld` needs `jolt-physics`'s async WASM bootstrap -- see that backend's own
 * `suspend fun create()` -- and this call site, [helloCubeSceneSpec], runs synchronously at
 * game-construction time, before any suspend context exists). The scene spec skips adding
 * the ground/box physics bodies entirely when this returns `null`, rather than crashing.
 */
internal expect fun createPhysicsWorld(): PhysicsWorld?
