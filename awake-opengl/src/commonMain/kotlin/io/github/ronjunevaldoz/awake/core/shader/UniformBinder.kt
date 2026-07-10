package io.github.ronjunevaldoz.awake.core.shader

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Vec4

interface UniformBinder {
    fun getLocation(name: String): Int
    fun set(location: Int, value: Int)
    fun set(location: Int, value: Float)
    fun set(location: Int, x: Float, y: Float, z: Float)
    fun set(location: Int, x: Float, y: Float, z: Float, w: Float)
    fun set(location: Int, value: Vec3)
    fun set(location: Int, value: Vec4)
    fun set(location: Int, value: Boolean)
    fun set(location: Int, value: Mat4)
    fun set(location: Int, matrices: List<Mat4>)
}