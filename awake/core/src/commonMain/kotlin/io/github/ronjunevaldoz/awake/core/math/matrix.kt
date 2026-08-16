// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import io.github.ronjunevaldoz.awake.core.memory.FloatBuf
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Mat4 {
    val data: FloatArray = FloatArray(16)

    var m00: Float
        get() = data[0]
        set(value) {
            data[0] = value
        }

    var m10: Float
        get() = data[1]
        set(value) {
            data[1] = value
        }

    var m20: Float
        get() = data[2]
        set(value) {
            data[2] = value
        }

    var m30: Float
        get() = data[3]
        set(value) {
            data[3] = value
        }

    var m01: Float
        get() = data[4]
        set(value) {
            data[4] = value
        }

    var m11: Float
        get() = data[5]
        set(value) {
            data[5] = value
        }

    var m21: Float
        get() = data[6]
        set(value) {
            data[6] = value
        }

    var m31: Float
        get() = data[7]
        set(value) {
            data[7] = value
        }

    var m02: Float
        get() = data[8]
        set(value) {
            data[8] = value
        }

    var m12: Float
        get() = data[9]
        set(value) {
            data[9] = value
        }

    var m22: Float
        get() = data[10]
        set(value) {
            data[10] = value
        }

    var m32: Float
        get() = data[11]
        set(value) {
            data[11] = value
        }

    var m03: Float
        get() = data[12]
        set(value) {
            data[12] = value
        }

    var m13: Float
        get() = data[13]
        set(value) {
            data[13] = value
        }

    var m23: Float
        get() = data[14]
        set(value) {
            data[14] = value
        }

    var m33: Float
        get() = data[15]
        set(value) {
            data[15] = value
        }

    init {
        identity()
    }

    fun identity() {
        m00 = 1f
        m11 = 1f
        m22 = 1f
        m33 = 1f
        m01 = 0f
        m02 = 0f
        m03 = 0f
        m10 = 0f
        m12 = 0f
        m13 = 0f
        m20 = 0f
        m21 = 0f
        m23 = 0f
        m30 = 0f
        m31 = 0f
        m32 = 0f
    }

    fun set(other: Mat4): Mat4 {
        m00 = other.m00
        m01 = other.m01
        m02 = other.m02
        m03 = other.m03
        m10 = other.m10
        m11 = other.m11
        m12 = other.m12
        m13 = other.m13
        m20 = other.m20
        m21 = other.m21
        m22 = other.m22
        m23 = other.m23
        m30 = other.m30
        m31 = other.m31
        m32 = other.m32
        m33 = other.m33
        return this
    }

    fun scale(value: Float): Mat4 = scale(value, value, value)

    fun scale(x: Float, y: Float, z: Float): Mat4 {
        val scale = Mat4()
        scale.m00 = x
        scale.m11 = y
        scale.m22 = z
        return scale * this
    }

    fun translate(x: Float, y: Float, z: Float): Mat4 {
        val translation = Mat4()
        translation.m03 = x
        translation.m13 = y
        translation.m23 = z
        return translation * this
    }

    /**
     * Right-handed rotation of [angle] radians about [axis] -- counter-clockwise looking down
     * the axis toward the origin, which is what [rotateZ] and [Quat.fromAxisAngle] have always
     * done.
     *
     * This, [rotateX] and [rotateY] used to store the TRANSPOSE of that, i.e. they rotated the
     * opposite way, while [rotateZ] did not. Nothing caught it because the CPU never transformed
     * a point on the render path and a cube spinning backwards still looks like a spinning cube;
     * it surfaced when quaternions and matrices had to agree. Objects with an authored X or Y
     * rotation now turn the other way -- that is this fix, not a regression.
     */
    fun rotate(angle: Float, axis: Vec3): Mat4 {
        val rotationMatrix = Mat4()
        val normalizedAxis = axis.normalized()

        val cosTheta = cos(angle)
        val sinTheta = sin(angle)
        val oneMinusCosTheta = 1.0f - cosTheta

        val x = normalizedAxis.x
        val y = normalizedAxis.y
        val z = normalizedAxis.z

        // Calculate elements of the rotation matrix
        rotationMatrix.m00 = cosTheta + x * x * oneMinusCosTheta
        rotationMatrix.m01 = x * y * oneMinusCosTheta - z * sinTheta
        rotationMatrix.m02 = x * z * oneMinusCosTheta + y * sinTheta

        rotationMatrix.m10 = x * y * oneMinusCosTheta + z * sinTheta
        rotationMatrix.m11 = cosTheta + y * y * oneMinusCosTheta
        rotationMatrix.m12 = y * z * oneMinusCosTheta - x * sinTheta

        rotationMatrix.m20 = x * z * oneMinusCosTheta - y * sinTheta
        rotationMatrix.m21 = y * z * oneMinusCosTheta + x * sinTheta
        rotationMatrix.m22 = cosTheta + z * z * oneMinusCosTheta

        return rotationMatrix * this
    }

    // Rotate around the x-axis
    fun rotateX(angleRad: Float): Mat4 {
        val rotationMatrix = Mat4()
        val sin = sin(angleRad)
        val cos = cos(angleRad)

        rotationMatrix.m11 = cos
        rotationMatrix.m12 = -sin
        rotationMatrix.m21 = sin
        rotationMatrix.m22 = cos

        return rotationMatrix * this
    }

    // Rotate around the y-axis
    fun rotateY(angleRad: Float): Mat4 {
        val rotationMatrix = Mat4()
        val sin = sin(angleRad)
        val cos = cos(angleRad)

        rotationMatrix.m00 = cos
        rotationMatrix.m02 = sin
        rotationMatrix.m20 = -sin
        rotationMatrix.m22 = cos

        return rotationMatrix * this
    }

    // Rotate around the z-axis
    fun rotateZ(angleRad: Float): Mat4 {
        val rotationMatrix = Mat4()
        val sin = sin(angleRad)
        val cos = cos(angleRad)

        rotationMatrix.m00 = cos
        rotationMatrix.m01 = -sin
        rotationMatrix.m10 = sin
        rotationMatrix.m11 = cos

        return rotationMatrix * this
    }

    companion object {

        /**
         * Creates an orthographic projection matrix.
         *
         * This projection is best suited for 2D rendering, UI elements, and when a uniform depth range is desired.
         *
         * @param left   The leftmost coordinate of the projection volume.
         * @param right  The rightmost coordinate of the projection volume.
         * @param bottom The bottom coordinate of the projection volume.
         * @param top    The top coordinate of the projection volume.
         * @param near   The distance to the near plane of the projection volume.
         * @param far    The distance to the far plane of the projection volume.
         * @param clipSpace The target API's NDC convention -- supplies the depth range.
         * @return The orthographic projection matrix.
         */
        // Mirrors the canonical GL entry point of the same name; collapsing the
        // parameters into an object would break that correspondence.
        @Suppress("LongParameterList")
        fun orthographic(
            left: Float,
            right: Float,
            bottom: Float,
            top: Float,
            near: Float,
            far: Float,
            clipSpace: ClipSpace,
        ): Mat4 {
            val width = right - left
            val height = top - bottom
            val depth = far - near

            val tx = -(right + left) / width
            val ty = -(top + bottom) / height

            return Mat4().apply {
                m00 = 2f / width
                m11 = 2f / height
                m22 = if (clipSpace.depthZeroToOne) -1f / depth else -2f / depth
                m03 = tx
                m13 = ty
                m23 = if (clipSpace.depthZeroToOne) -near / depth else -(far + near) / depth
            }
        }

        /**
         * Creates a frustum projection matrix.
         *
         * This projection is suitable for 3D rendering, where the view volume is a truncated pyramid (frustum).
         * Commonly used in frustum culling
         * val ratio = width / height
         * val projection = Mat4f.frustum(-ratio, ratio, -1f, 1f, 0.1f, 100.0f)
         *
         * @param left   The leftmost coordinate of the frustum.
         * @param right  The rightmost coordinate of the frustum.
         * @param bottom The bottom coordinate of the frustum.
         * @param top    The top coordinate of the frustum.
         * @param near   The distance to the near plane of the frustum.
         * @param far    The distance to the far plane of the frustum.
         * @param clipSpace The target API's NDC convention -- supplies the depth range.
         * @return The frustum projection matrix.
         */
        // Mirrors the canonical GL entry point of the same name; collapsing the
        // parameters into an object would break that correspondence.
        @Suppress("LongParameterList")
        fun frustum(
            left: Float,
            right: Float,
            bottom: Float,
            top: Float,
            near: Float,
            far: Float,
            clipSpace: ClipSpace,
        ): Mat4 {
            val width = right - left
            val height = top - bottom
            val depth = far - near
            return Mat4().apply {
                m00 = 2.0f * near / width
                m11 = 2.0f * near / height
                m02 = (right + left) / width
                m12 = (top + bottom) / height
                m22 = if (clipSpace.depthZeroToOne) -far / depth else -(far + near) / depth
                m32 = -1.0f
                m23 = if (clipSpace.depthZeroToOne) -far * near / depth else -2.0f * far * near / depth
                m33 = 0f
            }
        }

        /**
         * Creates a perspective projection matrix.
         *
         * This projection is commonly used for 3D rendering, where a perspective effect is desired.
         *
         * @param fovY   The vertical field of view angle in radians.
         * @param aspect The aspect ratio of the projection volume (width / height).
         * @param near   The distance to the near plane of the projection volume.
         * @param far    The distance to the far plane of the projection volume.
         * @param clipSpace The target API's NDC convention -- supplies the depth range.
         * @return The perspective projection matrix.
         */
        fun perspective(fovY: Float, aspect: Float, near: Float, far: Float, clipSpace: ClipSpace): Mat4 {
            val tanHalfFovY = tan(fovY / 2f)
            val scaleY = 1f / tanHalfFovY
            val rangeInv = near - far
            return Mat4().apply {
                m00 = scaleY / aspect
                m11 = scaleY
                m22 = if (clipSpace.depthZeroToOne) far / rangeInv else (near + far) / rangeInv
                m32 = -1f
                m23 = if (clipSpace.depthZeroToOne) near * far / rangeInv else 2f * near * far / rangeInv
                m33 = 0f
            }
        }

        // Mirrors the canonical GL entry point of the same name; collapsing the
        // parameters into an object would break that correspondence.
        @Suppress("LongParameterList")
        fun setLookAt(
            eyeX: Float,
            eyeY: Float,
            eyeZ: Float,
            centerX: Float,
            centerY: Float,
            centerZ: Float,
            upX: Float,
            upY: Float,
            upZ: Float,
        ): Mat4 = setLookAt(
            Vec3(eyeX, eyeY, eyeZ),
            Vec3(centerX, centerY, centerZ),
            Vec3(upX, upY, upZ),
        )

        fun setLookAt(
            eye: Vec3,
            center: Vec3,
            up: Vec3,
        ): Mat4 = Mat4().apply {
            val f = (center - eye).normalized() // forward
            val s = f.cross(up).normalized() // side
            val u = s.cross(f) // up

            // Rotation goes in ROWS, matching the row-form translation below. Writing it
            // into columns instead transposes (and for an orthonormal basis, inverts) the
            // rotation, which is invisible whenever the basis happens to be the identity
            // -- eye on +Z looking down -Z -- and mirrors the view everywhere else.
            m00 = s.x
            m01 = s.y
            m02 = s.z
            m10 = u.x
            m11 = u.y
            m12 = u.z
            m20 = -f.x
            m21 = -f.y
            m22 = -f.z
            m03 = -s.dot(eye)
            m13 = -u.dot(eye)
            m23 = f.dot(eye)
        }

        /** Standard T * R * S composition -- glTF node transforms (and any other TRS-driven
         * transform, e.g. joint local transforms during animation playback) all reduce to this;
         * factored out so nothing outside [core.math] hand-rolls quaternion-to-matrix math
         * inline (see [Quat.toMat4]). */
        fun fromTrs(translation: Vec3, rotation: Quat, scale: Vec3): Mat4 {
            val r = rotation.toMat4()
            return Mat4().apply {
                m00 = r.m00 * scale.x
                m10 = r.m10 * scale.x
                m20 = r.m20 * scale.x
                m30 = 0f
                m01 = r.m01 * scale.y
                m11 = r.m11 * scale.y
                m21 = r.m21 * scale.y
                m31 = 0f
                m02 = r.m02 * scale.z
                m12 = r.m12 * scale.z
                m22 = r.m22 * scale.z
                m32 = 0f
                m03 = translation.x
                m13 = translation.y
                m23 = translation.z
                m33 = 1f
            }
        }

        /** `a * b` in the standard column-major row/col sense (applies [b] first, then [a]) --
         * unlike the [Mat4.times] operator above, whose chained-matrix convention doesn't match
         * the row/col semantics the `mRC` accessors use (see that operator's callers for the
         * difference). Node-hierarchy world-transform composition (glTF scene graph, joint
         * palettes) needs this exact convention. */
        fun multiplyColumnMajor(a: Mat4, b: Mat4): Mat4 {
            val result = Mat4()
            for (col in 0 until 4) {
                for (row in 0 until 4) {
                    var sum = 0f
                    for (k in 0 until 4) {
                        sum += a.data[k * 4 + row] * b.data[col * 4 + k]
                    }
                    result.data[col * 4 + row] = sum
                }
            }
            return result
        }
    }

    override fun toString(): String = buildString {
        append("| $m00 $m01 $m02 $m03 |\n")
        append("| $m10 $m11 $m12 $m13 |\n")
        append("| $m20 $m21 $m22 $m23 |\n")
        append("| $m30 $m31 $m32 $m33 |")
    }
}

operator fun Mat4.get(i: Int, buffer: FloatBuf) {
    for (j in 0 until 16) {
        buffer[16 * i + j] = data[j]
    }
}

operator fun Mat4.plus(other: Mat4): Mat4 {
    val result = Mat4()
    for (i in 0 until 4) {
        for (j in 0 until 4) {
            var sum = 0.0f
            for (k in 0 until 4) {
                sum += data[i * 4 + k] + other.data[k * 4 + j]
            }
            result.data[i * 4 + j] = sum
        }
    }
    return result
}

operator fun Mat4.times(other: Mat4): Mat4 {
    val result = Mat4()
    for (i in 0 until 4) {
        for (j in 0 until 4) {
            var sum = 0.0f
            for (k in 0 until 4) {
                sum += data[i * 4 + k] * other.data[k * 4 + j]
            }
            result.data[i * 4 + j] = sum
        }
    }
    return result
}

/**
 * In-place [transformPosition] -- see it for the convention this follows and why it changed.
 */
fun Mat4.transformToPosition(posDest: Vec4) {
    val x = posDest.x
    val y = posDest.y
    val z = posDest.z
    val w = posDest.w
    val mat = this
    val resultX = mat.m00 * x + mat.m01 * y + mat.m02 * z + mat.m03 * w
    val resultY = mat.m10 * x + mat.m11 * y + mat.m12 * z + mat.m13 * w
    val resultZ = mat.m20 * x + mat.m21 * y + mat.m22 * z + mat.m23 * w
    val resultW = mat.m30 * x + mat.m31 * y + mat.m32 * z + mat.m33 * w
    posDest[resultX, resultY, resultZ] = resultW
}

/**
 * `matrix * position`, the same order every shader in this repo uses
 * (`uniforms.mvp * vec4f(inPosition, 1.0)`) and the one [Mat4.translate]/[Mat4.fromTrs] store
 * for: translation lives in `m03/m13/m23`, and it has to be read across a row.
 *
 * This used to read down the columns instead, i.e. it transformed by the TRANSPOSE. Nothing in
 * the engine noticed, because the CPU never transforms a point on the render path -- matrices go
 * to the GPU as raw column-major floats and the multiply happens there. It surfaced the moment
 * something needed a point transformed on the CPU (a bounding box, a gizmo handle): a translated
 * box did not move.
 */
fun Mat4.transformPosition(position: Vec4): Vec4 {
    val x = position.x
    val y = position.y
    val z = position.z
    val w = position.w

    val resultX = m00 * x + m01 * y + m02 * z + m03 * w
    val resultY = m10 * x + m11 * y + m12 * z + m13 * w
    val resultZ = m20 * x + m21 * y + m22 * z + m23 * w
    val resultW = m30 * x + m31 * y + m32 * z + m33 * w
    return Vec4(resultX, resultY, resultZ, resultW)
}

/**
 * The determinant, via cofactor expansion on the first row.
 *
 * Shares its 2x2 sub-determinants with [inverse] -- the same numbers appear there, so a caller
 * that needs both pays for them twice. Fine at the rate a matrix is actually inverted (a picking
 * ray per click, not per draw call); if that changes, fuse them.
 */
fun Mat4.determinant(): Float {
    val s0 = m00 * m11 - m01 * m10
    val s1 = m00 * m12 - m02 * m10
    val s2 = m00 * m13 - m03 * m10
    val s3 = m01 * m12 - m02 * m11
    val s4 = m01 * m13 - m03 * m11
    val s5 = m02 * m13 - m03 * m12

    val c5 = m22 * m33 - m23 * m32
    val c4 = m21 * m33 - m23 * m31
    val c3 = m21 * m32 - m22 * m31
    val c2 = m20 * m33 - m23 * m30
    val c1 = m20 * m32 - m22 * m30
    val c0 = m20 * m31 - m21 * m30

    return s0 * c5 - s1 * c4 + s2 * c3 + s3 * c2 - s4 * c1 + s5 * c0
}

/**
 * The inverse, or `null` when this matrix is singular (a zero scale collapses an axis, and no
 * inverse exists -- returning identity or garbage there would silently produce wrong positions
 * far from the call that caused it).
 *
 * Unprojection is what this exists for: turning a screen pixel back into a world-space ray needs
 * the inverse view-projection, and without it a picking implementation has to approximate by
 * projecting every candidate forward instead (see the studio gizmo's own doc comment).
 *
 * Convention-free by construction: it inverts the matrix, so it composes with whichever
 * multiplication order the caller already uses. [Mat4Test] pins that down by round-tripping
 * through this codebase's own [transformPosition] rather than asserting a layout.
 */
fun Mat4.inverse(): Mat4? {
    val s0 = m00 * m11 - m01 * m10
    val s1 = m00 * m12 - m02 * m10
    val s2 = m00 * m13 - m03 * m10
    val s3 = m01 * m12 - m02 * m11
    val s4 = m01 * m13 - m03 * m11
    val s5 = m02 * m13 - m03 * m12

    val c5 = m22 * m33 - m23 * m32
    val c4 = m21 * m33 - m23 * m31
    val c3 = m21 * m32 - m22 * m31
    val c2 = m20 * m33 - m23 * m30
    val c1 = m20 * m32 - m22 * m30
    val c0 = m20 * m31 - m21 * m30

    val determinant = s0 * c5 - s1 * c4 + s2 * c3 + s3 * c2 - s4 * c1 + s5 * c0
    if (determinant == 0f || !determinant.isFinite()) return null
    val invDet = 1f / determinant

    return Mat4().also { out ->
        out.m00 = (m11 * c5 - m12 * c4 + m13 * c3) * invDet
        out.m01 = (-m01 * c5 + m02 * c4 - m03 * c3) * invDet
        out.m02 = (m31 * s5 - m32 * s4 + m33 * s3) * invDet
        out.m03 = (-m21 * s5 + m22 * s4 - m23 * s3) * invDet

        out.m10 = (-m10 * c5 + m12 * c2 - m13 * c1) * invDet
        out.m11 = (m00 * c5 - m02 * c2 + m03 * c1) * invDet
        out.m12 = (-m30 * s5 + m32 * s2 - m33 * s1) * invDet
        out.m13 = (m20 * s5 - m22 * s2 + m23 * s1) * invDet

        out.m20 = (m10 * c4 - m11 * c2 + m13 * c0) * invDet
        out.m21 = (-m00 * c4 + m01 * c2 - m03 * c0) * invDet
        out.m22 = (m30 * s4 - m31 * s2 + m33 * s0) * invDet
        out.m23 = (-m20 * s4 + m21 * s2 - m23 * s0) * invDet

        out.m30 = (-m10 * c3 + m11 * c1 - m12 * c0) * invDet
        out.m31 = (m00 * c3 - m01 * c1 + m02 * c0) * invDet
        out.m32 = (-m30 * s3 + m31 * s1 - m32 * s0) * invDet
        out.m33 = (m20 * s3 - m21 * s1 + m22 * s0) * invDet
    }
}

/** Rows and columns swapped. */
fun Mat4.transpose(): Mat4 = Mat4().also { out ->
    out.m00 = m00
    out.m01 = m10
    out.m02 = m20
    out.m03 = m30
    out.m10 = m01
    out.m11 = m11
    out.m12 = m21
    out.m13 = m31
    out.m20 = m02
    out.m21 = m12
    out.m22 = m22
    out.m23 = m32
    out.m30 = m03
    out.m31 = m13
    out.m32 = m23
    out.m33 = m33
}

/**
 * The inverse-transpose of this model matrix -- what normals must be transformed by, and `null`
 * when the model matrix is singular.
 *
 * Transforming a normal by the model matrix itself is only correct under uniform scale. Under
 * non-uniform scale it skews the normal off the surface, and the lighting goes subtly wrong with
 * nothing crashing: squash an object on one axis and it lights as though it were still round.
 * The shaders in this repo are handed `model` today, so this is the fix for that, not a
 * speculative helper.
 */
fun Mat4.normalMatrix(): Mat4? = inverse()?.transpose()
