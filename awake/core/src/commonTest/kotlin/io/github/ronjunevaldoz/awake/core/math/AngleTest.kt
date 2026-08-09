// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [Angle] stores degrees internally whichever factory built it, so [Angle.fromRadians] converts
 * on the way *in* and [Angle.toRadians] converts back on the way *out*. That round trip is the
 * only thing here that can silently go wrong, so it is what these assert.
 */
class AngleTest {

    @Test
    fun fromDegreesRoundTripsThroughToDegrees() {
        assertEquals(90f, Angle.fromDegrees(90f).toDegrees(), TOLERANCE)
        assertEquals(-45f, Angle.fromDegrees(-45f).toDegrees(), TOLERANCE)
        assertEquals(0f, Angle.fromDegrees(0f).toDegrees(), TOLERANCE)
    }

    @Test
    fun fromDegreesConvertsToRadians() {
        assertEquals(PI_F, Angle.fromDegrees(180f).toRadians(), TOLERANCE)
        assertEquals(PI_F / 2f, Angle.fromDegrees(90f).toRadians(), TOLERANCE)
        assertEquals(PI_F * 2f, Angle.fromDegrees(360f).toRadians(), TOLERANCE)
    }

    @Test
    fun fromRadiansStoresDegreesInternally() {
        assertEquals(180f, Angle.fromRadians(PI_F).toDegrees(), TOLERANCE)
        assertEquals(90f, Angle.fromRadians(PI_F / 2f).toDegrees(), TOLERANCE)
    }

    @Test
    fun fromRadiansRoundTripsThroughToRadians() {
        assertEquals(PI_F, Angle.fromRadians(PI_F).toRadians(), TOLERANCE)
        assertEquals(0.7f, Angle.fromRadians(0.7f).toRadians(), TOLERANCE)
    }

    @Test
    fun intExtensionsReadDegreeLiterals() {
        assertEquals(90f, 90.angleDeg, TOLERANCE)
        assertEquals(PI_F / 2f, 90.angleRad, TOLERANCE)
        assertEquals(0f, 0.angleRad, TOLERANCE)
        assertEquals(PI_F, 180.angleRad, TOLERANCE)
    }

    @Test
    fun floatExtensionsReadDegreeLiterals() {
        assertEquals(45.5f, 45.5f.angleDeg, TOLERANCE)
        assertEquals(PI_F / 4f, 45f.angleRad, TOLERANCE)
        assertEquals(-PI_F / 2f, (-90f).angleRad, TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
        val PI_F = PI.toFloat()
    }
}
