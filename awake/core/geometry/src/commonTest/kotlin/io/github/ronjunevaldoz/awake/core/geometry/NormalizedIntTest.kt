// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.geometry

import kotlin.test.Test
import kotlin.test.assertEquals

class NormalizedIntTest {
    @Test
    fun signedByteDecodesFullRangeToMinusOneToOne() {
        assertEquals(1f, NormalizedInt.signedByte(127))
        assertEquals(-1f, NormalizedInt.signedByte(-128), "spec: max(c / 127.0, -1.0) clamps -128 to -1")
        assertEquals(0f, NormalizedInt.signedByte(0))
    }

    @Test
    fun unsignedByteDecodesFullRangeToZeroToOne() {
        assertEquals(1f, NormalizedInt.unsignedByte(255))
        assertEquals(0f, NormalizedInt.unsignedByte(0))
    }

    @Test
    fun signedShortDecodesFullRangeToMinusOneToOne() {
        assertEquals(1f, NormalizedInt.signedShort(32767))
        assertEquals(-1f, NormalizedInt.signedShort(-32768), "spec: max(c / 32767.0, -1.0) clamps -32768 to -1")
        assertEquals(0f, NormalizedInt.signedShort(0))
    }

    @Test
    fun unsignedShortDecodesFullRangeToZeroToOne() {
        assertEquals(1f, NormalizedInt.unsignedShort(65535))
        assertEquals(0f, NormalizedInt.unsignedShort(0))
    }
}
