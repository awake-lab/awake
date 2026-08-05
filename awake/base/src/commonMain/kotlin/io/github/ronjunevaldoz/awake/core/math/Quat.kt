// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.sqrt

/** `[x, y, z, w]` rotation quaternion -- glTF's own component order. Same
 * quaternion-to-rotation-matrix formula [GltfParser][io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser]'s
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
            m00 = 1f - 2f * (yy + zz); m10 = 2f * (xy + wz); m20 = 2f * (xz - wy); m30 = 0f
            m01 = 2f * (xy - wz); m11 = 1f - 2f * (xx + zz); m21 = 2f * (yz + wx); m31 = 0f
            m02 = 2f * (xz + wy); m12 = 2f * (yz - wx); m22 = 1f - 2f * (xx + yy); m32 = 0f
            m03 = 0f; m13 = 0f; m23 = 0f; m33 = 1f
        }
    }

    companion object {
        val IDENTITY = Quat(0f, 0f, 0f, 1f)

        /** Normalized-lerp between [a] and [b] -- the standard approximation for glTF's `LINEAR`
         * rotation-sampler interpolation (true `slerp` isn't worth its extra trig cost for the
         * small per-keyframe angular deltas real animation clips use). Picks the shorter arc by
         * flipping [b]'s sign when the quaternions' dot product is negative -- otherwise lerping
         * between two equivalent-but-oppositely-signed quaternions spins the long way around. */
        fun nlerp(a: Quat, b: Quat, t: Float): Quat {
            val dot = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w
            val bx: Float; val by: Float; val bz: Float; val bw: Float
            if (dot < 0f) {
                bx = -b.x; by = -b.y; bz = -b.z; bw = -b.w
            } else {
                bx = b.x; by = b.y; bz = b.z; bw = b.w
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
