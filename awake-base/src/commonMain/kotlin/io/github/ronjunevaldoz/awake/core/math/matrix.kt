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

    fun scale(value: Float): Mat4 {
        return scale(value, value, value)
    }

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

    fun rotate(angle: Float, axis: Vec3): Mat4 {
        val rotationMatrix = Mat4()
        val normalizedAxis = axis.normalize()

        val cosTheta = cos(angle)
        val sinTheta = sin(angle)
        val oneMinusCosTheta = 1.0f - cosTheta

        val x = normalizedAxis.x
        val y = normalizedAxis.y
        val z = normalizedAxis.z

        // Calculate elements of the rotation matrix
        rotationMatrix.m00 = cosTheta + x * x * oneMinusCosTheta
        rotationMatrix.m10 = x * y * oneMinusCosTheta - z * sinTheta
        rotationMatrix.m20 = x * z * oneMinusCosTheta + y * sinTheta

        rotationMatrix.m01 = x * y * oneMinusCosTheta + z * sinTheta
        rotationMatrix.m11 = cosTheta + y * y * oneMinusCosTheta
        rotationMatrix.m21 = y * z * oneMinusCosTheta - x * sinTheta

        rotationMatrix.m02 = x * z * oneMinusCosTheta - y * sinTheta
        rotationMatrix.m12 = y * z * oneMinusCosTheta + x * sinTheta
        rotationMatrix.m22 = cosTheta + z * z * oneMinusCosTheta

        return rotationMatrix * this
    }

    // Rotate around the x-axis
    fun rotateX(angleRad: Float): Mat4 {
        val rotationMatrix = Mat4()
        val sin = sin(angleRad)
        val cos = cos(angleRad)

        rotationMatrix.m11 = cos
        rotationMatrix.m12 = sin
        rotationMatrix.m21 = -sin
        rotationMatrix.m22 = cos

        return rotationMatrix * this
    }

    // Rotate around the y-axis
    fun rotateY(angleRad: Float): Mat4 {
        val rotationMatrix = Mat4()
        val sin = sin(angleRad)
        val cos = cos(angleRad)

        rotationMatrix.m00 = cos
        rotationMatrix.m02 = -sin
        rotationMatrix.m20 = sin
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
         * @return The orthographic projection matrix.
         */
        fun orthographic(
            left: Float,
            right: Float,
            bottom: Float,
            top: Float,
            near: Float,
            far: Float
        ): Mat4 {
            val width = right - left
            val height = top - bottom
            val depth = far - near

            val tx = -(right + left) / width
            val ty = -(top + bottom) / height
            val tz = -(far + near) / depth

            return Mat4().apply {
                m00 = 2f / width
                m11 = 2f / height
                m22 = -2f / depth
                m03 = tx
                m13 = ty
                m23 = tz
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
         * @return The frustum projection matrix.
         */
        fun frustum(
            left: Float,
            right: Float,
            bottom: Float,
            top: Float,
            near: Float,
            far: Float
        ): Mat4 {
            val width = right - left
            val height = top - bottom
            val depth = far - near
            return Mat4().apply {
                m00 = 2.0f * near / width
                m11 = 2.0f * near / height
                m02 = (right + left) / width
                m12 = (top + bottom) / height
                m22 = -(far + near) / depth
                m32 = -1.0f
                m23 = -2.0f * far * near / depth
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
         * @return The perspective projection matrix.
         */
        fun perspective(fovY: Float, aspect: Float, near: Float, far: Float): Mat4 {
            val tanHalfFovY = tan(fovY / 2f)
            val scaleY = 1f / tanHalfFovY
            val rangeInv = near - far
            return Mat4().apply {
                m00 = scaleY / aspect
                m11 = scaleY
                m22 = (near + far) / rangeInv
                m32 = -1f
                m23 = 2f * near * far / rangeInv
                m33 = 0f
            }
        }

        fun setLookAt(
            eyeX: Float,
            eyeY: Float,
            eyeZ: Float,
            centerX: Float,
            centerY: Float,
            centerZ: Float,
            upX: Float,
            upY: Float,
            upZ: Float
        ): Mat4 {
            return setLookAt(
                Vec3(eyeX, eyeY, eyeZ),
                Vec3(centerX, centerY, centerZ),
                Vec3(upX, upY, upZ)
            )
        }

        fun setLookAt(
            eye: Vec3, center: Vec3, up: Vec3
        ): Mat4 {
            return Mat4().apply {
                val f = (center - eye).normalize() // forward
                val s = f.cross(up).normalize() // side
                val u = s.cross(f) // up

                m00 = s.x
                m10 = s.y
                m20 = s.z
                m01 = u.x
                m11 = u.y
                m21 = u.z
                m02 = -f.x
                m12 = -f.y
                m22 = -f.z
                m03 = -s.dot(eye)
                m13 = -u.dot(eye)
                m23 = f.dot(eye)
            }
        }
    }


    override fun toString(): String {
        return buildString {
            append("| $m00 $m01 $m02 $m03 |\n")
            append("| $m10 $m11 $m12 $m13 |\n")
            append("| $m20 $m21 $m22 $m23 |\n")
            append("| $m30 $m31 $m32 $m33 |")
        }
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

fun Mat4.transformToPosition(posDest: Vec4) {
    val x = posDest.x
    val y = posDest.y
    val z = posDest.z
    val w = posDest.w
    val mat = this
    val resultX = mat.m00 * x + mat.m10 * y + mat.m20 * z + mat.m30 * w
    val resultY = mat.m01 * x + mat.m11 * y + mat.m21 * z + mat.m31 * w
    val resultZ = mat.m02 * x + mat.m12 * y + mat.m22 * z + mat.m32 * w
    val resultW = mat.m03 * x + mat.m13 * y + mat.m23 * z + mat.m33 * w
    posDest[resultX, resultY, resultZ] = resultW
}

fun Mat4.transformPosition(position: Vec4): Vec4 {
    val x = position.x
    val y = position.y
    val z = position.z
    val w = position.w

    val resultX = m00 * x + m10 * y + m20 * z + m30 * w
    val resultY = m01 * x + m11 * y + m21 * z + m31 * w
    val resultZ = m02 * x + m12 * y + m22 * z + m32 * w
    val resultW = m03 * x + m13 * y + m23 * z + m33 * w
    return Vec4(resultX, resultY, resultZ, resultW)
}