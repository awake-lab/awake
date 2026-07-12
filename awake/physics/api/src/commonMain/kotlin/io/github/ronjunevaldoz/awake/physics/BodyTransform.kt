// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics

import io.github.ronjunevaldoz.awake.core.math.Vec3

/** One body's read-back pose, as returned in bulk by [PhysicsWorld.syncTransforms] --
 * [rotation] is Euler angles (radians) rather than a quaternion, matching the rest of this
 * engine's `Transform`/`Camera` convention (see awake:scene's `Transform` component)
 * instead of introducing a new quaternion type into a coarse, backend-neutral API. */
data class BodyTransform(val handle: BodyHandle, val position: Vec3, val rotation: Vec3)
