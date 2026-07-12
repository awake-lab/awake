// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld

// awake:backend:jolt's wasmJsMain JoltPhysicsWorld is TODO()-stub-only (JoltPhysics.js
// deferred, see docs/MVP_PLAN.md's decision log) -- null here means PhysicsDemo simply
// skips physics stepping on this platform instead of crashing.
actual fun createPhysicsWorld(): PhysicsWorld? = null
