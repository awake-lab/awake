// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.physics.PhysicsWorld

// jolt-physics (JoltPhysics.js)'s JoltPhysicsWorld needs an async WASM module bootstrap
// (`suspend fun create()`), which doesn't fit this synchronous call site -- see
// PlatformPhysics.kt's doc comment. The hello-cube wasmJs target renders the same scene
// minus the physics-driven box/ground bodies until that's wired.
internal actual fun createPhysicsWorld(): PhysicsWorld? = null
