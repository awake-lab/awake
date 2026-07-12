// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics.jolt

import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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

/**
 * Inverse of [quatToEulerVec3]: builds the same `Qz(z) * Qy(y) * Qx(x)` quaternion jolt-jni's
 * own `Quat.sEulerAngles(x, y, z)` constructs. JoltC (the iOS cinterop backend) has no
 * equivalent `sEulerAngles`-style helper exposed in its C API -- [PhysicsWorld.createBody]
 * takes a `rotation: Vec3` of Euler angles, so this reconstructs the quaternion by hand
 * (standard Z*Y*X intrinsic-rotation composition) before handing it to
 * `JPC_BodyCreationSettings.Rotation`. Pure function, no JoltC/cinterop type in its signature,
 * so it's testable the same way [quatToEulerVec3] is. Returns (w, x, y, z).
 */
internal fun eulerVec3ToQuatWxyz(rotation: Vec3): FloatArray {
    val halfX = rotation.x * 0.5f
    val halfY = rotation.y * 0.5f
    val halfZ = rotation.z * 0.5f
    val cx = cos(halfX)
    val sx = sin(halfX)
    val cy = cos(halfY)
    val sy = sin(halfY)
    val cz = cos(halfZ)
    val sz = sin(halfZ)

    // Qz(z) * Qy(y) * Qx(x), expanded -- matches quatToEulerVec3's own inverse formula.
    val w = cz * cy * cx + sz * sy * sx
    val x = cz * cy * sx - sz * sy * cx
    val y = cz * sy * cx + sz * cy * sx
    val z = sz * cy * cx - cz * sy * sx
    return floatArrayOf(w, x, y, z)
}
