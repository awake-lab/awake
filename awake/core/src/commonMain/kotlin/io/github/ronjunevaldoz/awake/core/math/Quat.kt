// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** `[x, y, z, w]` rotation quaternion -- glTF's own component order. Same
 * quaternion-to-rotation-matrix formula [GltfParser][io.github.ronjunevaldoz.awake.asset.gltf.GltfParser]'s
 * `trsMatrix` already used inline; factored out here so animation playback (which only has a
 * rotation, no translation/scale to compose alongside it) doesn't need to fake a full TRS call. */
data class Quat(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f, var w: Float = 1f) {
    fun toMat4(): Mat4 {
        val xx = x * x
        val yy = y * y
        val zz = z * z
        val xy = x * y
        val xz = x * z
        val yz = y * z
        val wx = w * x
        val wy = w * y
        val wz = w * z

        return Mat4().apply {
            m00 = 1f - 2f * (yy + zz)
            m10 = 2f * (xy + wz)
            m20 = 2f * (xz - wy)
            m30 = 0f
            m01 = 2f * (xy - wz)
            m11 = 1f - 2f * (xx + zz)
            m21 = 2f * (yz + wx)
            m31 = 0f
            m02 = 2f * (xz + wy)
            m12 = 2f * (yz - wx)
            m22 = 1f - 2f * (xx + yy)
            m32 = 0f
            m03 = 0f
            m13 = 0f
            m23 = 0f
            m33 = 1f
        }
    }

    /**
     * `this` then [other] -- the rotation you get by applying this one first.
     *
     * Quaternion multiplication is not commutative, and this is the Hamilton product with its
     * operands SWAPPED, so that `(a * b)` means "a then b" and agrees with `a.toMat4() *
     * b.toMat4()` -- [Mat4.times] itself computes the reversed product (`A * B` is the
     * conventional `B * A`), so an unswapped product here would rotate in the opposite order
     * from the matrices built out of the same quaternions. QuatOpsTest pins it down.
     */
    operator fun times(other: Quat): Quat = Quat(
        other.w * x + other.x * w + other.y * z - other.z * y,
        other.w * y - other.x * z + other.y * w + other.z * x,
        other.w * z + other.x * y - other.y * x + other.z * w,
        other.w * w - other.x * x - other.y * y - other.z * z,
    )

    /** The opposite rotation. Equal to the inverse for a unit quaternion, which every quaternion
     * this class produces is -- [inverse] is the one to use when that is not guaranteed. */
    fun conjugate(): Quat = Quat(-x, -y, -z, w)

    /** The true inverse, or `null` for a zero quaternion (which encodes no rotation to undo). */
    fun inverse(): Quat? {
        val lengthSquared = x * x + y * y + z * z + w * w
        if (lengthSquared < DEGENERATE_LENGTH_SQUARED) return null
        val inverseLengthSquared = 1f / lengthSquared
        return Quat(
            -x * inverseLengthSquared,
            -y * inverseLengthSquared,
            -z * inverseLengthSquared,
            w * inverseLengthSquared,
        )
    }

    /** [point] rotated by this quaternion, allocating one result -- the pure form, per [Vec3]'s
     * naming contract. */
    fun rotate(point: Vec3): Vec3 {
        // v + 2 * cross(q.xyz, cross(q.xyz, v) + q.w * v): the standard expansion, which avoids
        // building a rotation matrix to turn a single point.
        val tx = 2f * (y * point.z - z * point.y)
        val ty = 2f * (z * point.x - x * point.z)
        val tz = 2f * (x * point.y - y * point.x)
        return Vec3(
            point.x + w * tx + (y * tz - z * ty),
            point.y + w * ty + (z * tx - x * tz),
            point.z + w * tz + (x * ty - y * tx),
        )
    }

    /**
     * This rotation as the `(x, y, z)` radians [io.github.ronjunevaldoz.awake.scene.core]'s
     * `Transform.rotation` stores, in the same order it composes them.
     *
     * Round-trips with [fromEuler] except at the poles, where pitch reaches +/-90 degrees and
     * yaw and roll describe the same axis (gimbal lock): infinitely many Euler triples encode
     * that rotation, and this returns one of them with roll folded into yaw.
     */
    fun toEuler(): Vec3 {
        // Read back off this rotation's own matrix rather than from a hand-expanded formula:
        // that way the extraction cannot drift out of step with [fromEuler]'s composition order,
        // which is the only thing here that can silently be wrong.
        val m = toMat4()
        val pitch = -m.m20
        return if (pitch >= 1f - GIMBAL_EPSILON || pitch <= -1f + GIMBAL_EPSILON) {
            // At a pole X and Z turn about the same axis: infinitely many triples describe this
            // rotation, and this returns the one with X folded into Z.
            Vec3(0f, if (pitch > 0f) HALF_PI else -HALF_PI, atan2(-m.m01, m.m11))
        } else {
            Vec3(atan2(m.m21, m.m22), asin(pitch.coerceIn(-1f, 1f)), atan2(m.m10, m.m00))
        }
    }

    companion object {
        val IDENTITY = Quat(0f, 0f, 0f, 1f)

        /** A quaternion encodes half the rotation angle. */
        private const val HALF_TURN_SCALE = 0.5f

        private const val DEGENERATE_LENGTH_SQUARED = 1e-12f

        /** How close to a pole counts as gimbal lock -- see [toEuler]. */
        private const val GIMBAL_EPSILON = 1e-6f

        private val HALF_PI = (PI / 2.0).toFloat()

        /**
         * The rotation `Transform.rotation`'s `(x, y, z)` radians describe, applied in the same
         * order `Transform`'s own matrix applies them: X, then Y, then Z. (Its builder reads
         * `rotateZ().rotateY().rotateX()`, which is the reverse spelling of the same thing --
         * [Mat4.times] composes backwards.)
         *
         * Built from three axis rotations rather than a hand-expanded formula: the order is the
         * only thing that can be wrong here, and spelling it out means it matches `Transform` by
         * construction rather than by a comment claiming it does.
         */
        fun fromEuler(radians: Vec3): Quat =
            fromAxisAngle(Vec3(1f, 0f, 0f), radians.x) *
                fromAxisAngle(Vec3(0f, 1f, 0f), radians.y) *
                fromAxisAngle(Vec3(0f, 0f, 1f), radians.z)

        /** Below this an axis carries no direction to rotate about. */
        private const val DEGENERATE_AXIS_LENGTH = 1e-6f

        /**
         * A rotation of [radians] about [axis], right-handed (counter-clockwise looking down the
         * axis toward the origin).
         *
         * [axis] is normalized here rather than required to be: a caller composing an axis from
         * a difference of two points has an unnormalized one, and silently producing a rotation
         * that also scales is the kind of bug that surfaces three transforms away. Returns
         * [IDENTITY] for a degenerate axis, which is the only rotation a zero axis can mean.
         */
        fun fromAxisAngle(axis: Vec3, radians: Float): Quat {
            val length = sqrt(axis.x * axis.x + axis.y * axis.y + axis.z * axis.z)
            if (length < DEGENERATE_AXIS_LENGTH) return Quat(IDENTITY.x, IDENTITY.y, IDENTITY.z, IDENTITY.w)
            val half = radians * HALF_TURN_SCALE
            val sinHalf = sin(half) / length
            return Quat(axis.x * sinHalf, axis.y * sinHalf, axis.z * sinHalf, cos(half))
        }

        /** Normalized-lerp between [a] and [b] -- the standard approximation for glTF's `LINEAR`
         * rotation-sampler interpolation (true `slerp` isn't worth its extra trig cost for the
         * small per-keyframe angular deltas real animation clips use). Picks the shorter arc by
         * flipping [b]'s sign when the quaternions' dot product is negative -- otherwise lerping
         * between two equivalent-but-oppositely-signed quaternions spins the long way around. */
        fun nlerp(a: Quat, b: Quat, t: Float): Quat {
            val dot = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w
            val bx: Float
            val by: Float
            val bz: Float
            val bw: Float
            if (dot < 0f) {
                bx = -b.x
                by = -b.y
                bz = -b.z
                bw = -b.w
            } else {
                bx = b.x
                by = b.y
                bz = b.z
                bw = b.w
            }
            val rx = a.x + (bx - a.x) * t
            val ry = a.y + (by - a.y) * t
            val rz = a.z + (bz - a.z) * t
            val rw = a.w + (bw - a.w) * t
            val length = sqrt(rx * rx + ry * ry + rz * rz + rw * rw)
            return if (length == 0f) IDENTITY else Quat(rx / length, ry / length, rz / length, rw / length)
        }
    }
}
