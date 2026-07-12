// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics

import io.github.ronjunevaldoz.awake.core.math.Vec3

data class RaycastHit(val handle: BodyHandle, val point: Vec3, val distance: Float)
