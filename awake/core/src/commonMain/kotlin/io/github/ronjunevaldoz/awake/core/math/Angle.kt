// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.jvm.JvmInline
import kotlin.math.PI

@JvmInline
value class Angle(private val value: Float) {
    // Factory methods to create an Angle instance
    companion object {
        private const val DEGREES_TO_RADIANS = PI.toFloat() / 180.0f
        private const val RADIANS_TO_DEGREES = 180.0f / PI.toFloat()
        fun fromDegrees(degrees: Float): Angle = Angle(degrees)

        fun fromRadians(radians: Float): Angle = Angle(radians * RADIANS_TO_DEGREES)
    }

    // Get the angle value in degrees
    fun toDegrees(): Float = value

    // Get the angle value in radians
    fun toRadians(): Float = value * DEGREES_TO_RADIANS
}

/** Reads a degree literal as degrees. Identity -- present so call sites can state the unit. */
val Int.angleDeg: Float
    get() = Angle(this.toFloat()).toDegrees()

/** Converts a degree literal to radians, e.g. `45.angleRad`. */
val Int.angleRad: Float
    get() = Angle(this.toFloat()).toRadians()

/** Reads a degree literal as degrees. Identity -- present so call sites can state the unit. */
val Float.angleDeg: Float
    get() = Angle(this).toDegrees()

/** Converts a degree literal to radians, e.g. `45f.angleRad`. */
val Float.angleRad: Float
    get() = Angle(this).toRadians()
