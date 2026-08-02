// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.physics.jolt.JoltPhysicsWorld

internal actual fun createPhysicsWorld(): PhysicsWorld? = JoltPhysicsWorld()
