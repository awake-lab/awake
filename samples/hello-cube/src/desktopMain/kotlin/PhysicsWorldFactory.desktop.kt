// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.physics.jolt.JoltPhysicsWorld

actual fun createPhysicsWorld(): PhysicsWorld? = JoltPhysicsWorld()
