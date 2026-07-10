package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.sqrt

data class Vec2(var x: Float, var y: Float) {
    constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())
}

data class Vec3(var x: Float = 1f, var y: Float = 1f, var z: Float = 1f) {
    /**
     * sqrt(x * x + y * y + z * z)
     */
    fun length3(): Float {
        return sqrt(x * x + y * y + z * z)
    }

    fun normalize(): Vec3 {
        return Vec3(x, y, z).apply {
            val length = length3()
            if (length != 0.0f) {
                val invLength = 1.0f / length
                x *= invLength
                y *= invLength
                z *= invLength
            }
        }
    }

    fun dot(other: Vec3): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun cross(other: Vec3): Vec3 {
        return Vec3(
            x = y * other.z - z * other.y,
            y = z * other.x - x * other.z,
            z = x * other.y - y * other.x
        )
    }

    operator fun minus(other: Vec3): Vec3 {
        return Vec3(
            x - other.x,
            y - other.y,
            z - other.z,
        )
    }

    companion object {
        val UP = Vec3(0f, 1f, 0f)
    }
}

data class Vec4(var x: Float = 1f, var y: Float = 1f, var z: Float = 1f, var w: Float = 1f) {
    operator fun set(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    fun dot(other: Vec4): Float {
        return x * other.x + y * other.y + z * other.z + w * other.w
    }

    operator fun minus(other: Vec4): Vec4 {
        return Vec4(x - other.x, y - other.y, z - other.z, w - other.w)
    }

    operator fun times(other: Mat4): Vec4 {
        return Vec4(
            x * other.m00 + y * other.m10 + z * other.m20 + w * other.m30,
            x * other.m01 + y * other.m11 + z * other.m21 + w * other.m31,
            x * other.m02 + y * other.m12 + z * other.m22 + w * other.m32,
            x * other.m03 + y * other.m13 + z * other.m23 + w * other.m33
        )
    }

    operator fun times(scalar: Float): Vec4 {
        return Vec4(x * scalar, y * scalar, z * scalar, w * scalar)
    }

    operator fun plus(other: Vec4): Vec4 {
        return Vec4(x + other.x, y + other.y, z + other.z, w + other.w)
    }

    fun length3(): Float {
        return sqrt(x * x + y * y + z * z)
    }

    fun pixelCoords(screenWidth: Int, screenHeight: Int): Vec2 {
        // Convert clip coordinates to normalized device coordinates (NDC)
        val ndcX = x / w
        val ndcY = y / w

        // Convert NDC to pixel coordinates
        val pixelX = (0.5f * (ndcX + 1f) * screenWidth).toInt()
        val pixelY = (0.5f * (1f - ndcY) * screenHeight).toInt()
        return Vec2(pixelX, pixelY)
    }

    fun makePixelCoords(viewportWidth: Int, viewportHeight: Int) {
        // Make coordinates as homogeneous
        x /= w
        y /= w
        z /= w
        w = 1.0f

        // Normalize values into NDC.
        x = 0.5f + 0.5f * x
        y = 0.5f + 0.5f * y
        z = 0.5f + 0.5f * z
        w = 1.0f

        // Move coordinates into window space (in pixels)
        x *= viewportWidth.toFloat()
        y *= viewportHeight.toFloat()
    }
}