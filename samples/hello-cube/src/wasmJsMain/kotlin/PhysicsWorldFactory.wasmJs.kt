// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.physics.jolt.JoltPhysicsWorld

// awake:backend:jolt's wasmJsMain JoltPhysicsWorld is now a real JoltPhysics.js-backed
// implementation (see that class's doc comment) -- constructed via its suspend `create()`
// factory, which awaits jolt-physics's own async WASM module bootstrap first.
actual suspend fun createPhysicsWorld(): PhysicsWorld? = JoltPhysicsWorld.create()
