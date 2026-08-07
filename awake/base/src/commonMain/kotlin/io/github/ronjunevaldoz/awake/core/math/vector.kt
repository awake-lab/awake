// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.sqrt

data class Vec2(var x: Float, var y: Float) {
    constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())
}

/**
 * A mutable 3-component vector.
 *
 * Naming contract -- every member here follows it, and so should anything added later:
 * - **Bare imperative verb** ([set], [add], [sub], [scale], [lerp], [normalize]) **mutates
 *   `this`** and returns `this` so calls can be chained. Nothing is allocated.
 * - **`-ed` suffix** ([normalized]) and the **operators** ([plus], [minus], [times]) are
 *   pure: they allocate a new [Vec3] and leave the receiver untouched.
 * - **Products and queries** ([dot], [cross], [length3]) are pure by nature -- they answer a
 *   question about two vectors rather than transforming one.
 *
 * The mutating forms exist so per-frame systems can do vector math without allocating; the
 * pure forms exist so expression-style code reads naturally. Picking the wrong one is a
 * silent bug in one direction (`v.normalized()` whose result is dropped does nothing), so
 * prefer the mutating form inside `System.update` and the pure form everywhere else.
 */
data class Vec3(var x: Float = 1f, var y: Float = 1f, var z: Float = 1f) {
    fun set(x: Float, y: Float, z: Float): Vec3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(other: Vec3): Vec3 {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        return this
    }

    fun add(other: Vec3): Vec3 {
        this.x += other.x
        this.y += other.y
        this.z += other.z
        return this
    }

    fun sub(other: Vec3): Vec3 {
        this.x -= other.x
        this.y -= other.y
        this.z -= other.z
        return this
    }

    fun scale(scalar: Float): Vec3 {
        this.x *= scalar
        this.y *= scalar
        this.z *= scalar
        return this
    }

    fun lerp(target: Vec3, factor: Float): Vec3 {
        this.x += (target.x - this.x) * factor
        this.y += (target.y - this.y) * factor
        this.z += (target.z - this.z) * factor
        return this
    }

    /**
     * sqrt(x * x + y * y + z * z)
     */
    fun length3(): Float {
        return sqrt(x * x + y * y + z * z)
    }

    /**
     * Scales this vector to unit length **in place**. A zero-length vector is left alone
     * rather than producing NaN. Returns `this`, so `dir.normalize()` on its own line is a
     * complete, meaningful statement -- see [normalized] for the allocating variant.
     */
    fun normalize(): Vec3 {
        val length = length3()
        if (length != 0.0f) {
            val invLength = 1.0f / length
            x *= invLength
            y *= invLength
            z *= invLength
        }
        return this
    }

    /** Allocating counterpart of [normalize]: returns a new unit-length copy, leaving `this`
     * unchanged. Dropping the result of this call is always a no-op bug. */
    fun normalized(): Vec3 = Vec3(x, y, z).normalize()

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

    operator fun plus(other: Vec3): Vec3 {
        return Vec3(
            x + other.x,
            y + other.y,
            z + other.z,
        )
    }

    operator fun times(scalar: Float): Vec3 {
        return Vec3(x * scalar, y * scalar, z * scalar)
    }

    /**
     * Awake's world-space direction convention. Each is a `get()` property rather than a
     * stored `val`, so every read hands back a **fresh** vector: [Vec3] is mutable, and a
     * shared instance could be aliased into a [set]/[add] chain and silently corrupt the
     * constant for every other caller in the process.
     *
     * Right-handed, Y-up, facing -Z -- the same convention `CameraSystem` builds its aim from
     * (yaw 0 looks along [FORWARD], +yaw turns toward [RIGHT], +pitch tilts toward [UP]).
     */
    companion object {
        val ZERO: Vec3 get() = Vec3(0f, 0f, 0f)
        val ONE: Vec3 get() = Vec3(1f, 1f, 1f)

        val UP: Vec3 get() = Vec3(0f, 1f, 0f)
        val DOWN: Vec3 get() = Vec3(0f, -1f, 0f)
        val RIGHT: Vec3 get() = Vec3(1f, 0f, 0f)
        val LEFT: Vec3 get() = Vec3(-1f, 0f, 0f)
        val FORWARD: Vec3 get() = Vec3(0f, 0f, -1f)
        val BACK: Vec3 get() = Vec3(0f, 0f, 1f)
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
