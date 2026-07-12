// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.physics.jolt.JoltPhysicsWorld

// Jolt Physics integration slice 2 (see docs/MVP_PLAN.md's decision log): awake:backend:jolt's
// iosMain JoltPhysicsWorld is now a real JoltC cinterop backend, not a TODO() stub -- mirrors
// desktop/Android's own factory.
actual fun createPhysicsWorld(): PhysicsWorld? = JoltPhysicsWorld()
