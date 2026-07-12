// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics

import io.github.ronjunevaldoz.awake.core.math.Vec3

/** Backend-neutral collision shape description -- a plain data description, not a live
 * native handle, so `PhysicsWorld.createBody` (the only place a [PhysicsShape] is consumed)
 * can build whatever native shape object its own backend needs (jolt-jni's `BoxShape`/
 * `SphereShape`, eventually JoltC's C shape structs) without this module knowing any of
 * that exists. */
sealed interface PhysicsShape

/** [halfExtents] matches Jolt Physics' own `BoxShape` convention (half the box's total
 * width/height/depth along each axis), not a full-extent size. */
data class BoxShape(val halfExtents: Vec3) : PhysicsShape

data class SphereShape(val radius: Float) : PhysicsShape
