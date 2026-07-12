// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics.jolt

import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.math.asin
import kotlin.math.atan2

/**
 * Converts a unit quaternion (w, x, y, z) to Euler angles (radians), for
 * [PhysicsWorld.syncTransforms]'s [io.github.ronjunevaldoz.awake.physics.BodyTransform.rotation].
 * Pure function (no jolt-jni type in its signature) so it's usable/testable from `commonMain`
 * -- both `desktopMain`/`androidMain` extract a jolt-jni `Quat`'s raw `(w, x, y, z)` first,
 * then call this.
 *
 * Matches the inverse of jolt-jni's own `Quat.sEulerAngles(x, y, z)`, i.e. this assumes the
 * quaternion was built as `Qz(z) * Qy(y) * Qx(x)` (confirmed by symbolically expanding
 * `sEulerAngles`'s formula against jolt-jni's quaternion-multiply operator) -- so a body
 * created with `createBody(..., rotation = Vec3(x, y, z), ...)` and read back via
 * [syncTransforms] round-trips the same (x, y, z) up to floating-point error.
 */
internal fun quatToEulerVec3(w: Float, x: Float, y: Float, z: Float): Vec3 {
    val roll = atan2(2f * (w * x + y * z), 1f - 2f * (x * x + y * y))
    val sinPitch = (2f * (w * y - z * x)).coerceIn(-1f, 1f)
    val pitch = asin(sinPitch)
    val yaw = atan2(2f * (w * z + x * y), 1f - 2f * (y * y + z * z))
    return Vec3(roll, pitch, yaw)
}
