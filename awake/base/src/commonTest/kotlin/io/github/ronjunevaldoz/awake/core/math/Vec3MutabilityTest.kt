// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * Locks in [Vec3]'s naming contract: a bare imperative verb mutates the receiver, the `-ed`
 * form allocates. Getting this backwards is silent -- a dropped `normalized()` result compiles
 * and does nothing -- so it is worth asserting rather than trusting the name.
 */
class Vec3MutabilityTest {
    @Test
    fun normalizeMutatesInPlaceAndReturnsTheSameInstance() {
        val v = Vec3(0f, 3f, 0f)
        val returned = v.normalize()

        assertSame(v, returned)
        assertEquals(1f, v.y, TOLERANCE)
        assertEquals(1f, v.length3(), TOLERANCE)
    }

    @Test
    fun normalizedLeavesTheReceiverUntouched() {
        val v = Vec3(0f, 3f, 0f)
        val unit = v.normalized()

        assertNotSame(v, unit)
        assertEquals(3f, v.y, TOLERANCE)
        assertEquals(1f, unit.y, TOLERANCE)
    }

    @Test
    fun normalizingAZeroVectorLeavesItAloneRatherThanProducingNaN() {
        val zero = Vec3(0f, 0f, 0f).normalize()

        assertEquals(0f, zero.x, TOLERANCE)
        assertEquals(0f, zero.y, TOLERANCE)
        assertEquals(0f, zero.z, TOLERANCE)
    }

    @Test
    fun mutatingHelpersChainAndReturnTheReceiver() {
        val v = Vec3(1f, 1f, 1f)
        val returned = v.set(2f, 4f, 6f).add(Vec3(1f, 1f, 1f)).sub(Vec3(1f, 1f, 1f)).scale(0.5f)

        assertSame(v, returned)
        assertEquals(1f, v.x, TOLERANCE)
        assertEquals(2f, v.y, TOLERANCE)
        assertEquals(3f, v.z, TOLERANCE)
    }

    @Test
    fun operatorsAllocateInsteadOfMutating() {
        val a = Vec3(1f, 2f, 3f)
        val sum = a + Vec3(1f, 1f, 1f)

        assertNotSame(a, sum)
        assertEquals(1f, a.x, TOLERANCE)
        assertEquals(2f, sum.x, TOLERANCE)
    }

    @Test
    fun upIsAFreshInstanceSoCallersCannotCorruptWorldUp() {
        val first = Vec3.up()
        first.set(9f, 9f, 9f)

        assertEquals(1f, Vec3.up().y, TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
