// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform
import kotlin.test.Test
import kotlin.test.assertEquals

class RendererVertexWritersTest {

    @Test
    fun testWriteVertex() {
        val out = FloatArray(10)
        val color = Color(0.1f, 0.2f, 0.3f, 0.4f)
        val transform = UiPrimitiveTransform(scaleX = 2f, scaleY = 3f, pivotX = 4f, pivotY = 5f)

        writeVertex(out, 0, 10f, 20f, color, transform)

        assertEquals(10f, out[0])
        assertEquals(20f, out[1])
        assertEquals(0.1f, out[2])
        assertEquals(0.2f, out[3])
        assertEquals(0.3f, out[4])
        assertEquals(0.4f, out[5])
        assertEquals(2f, out[6])
        assertEquals(3f, out[7])
        assertEquals(4f, out[8])
        assertEquals(5f, out[9])
    }

    @Test
    fun testWriteGlyphVertex() {
        val out = FloatArray(12)
        val color = Color.White

        writeGlyphVertex(out, 0, 10f, 20f, 0.5f, 0.75f, color)

        assertEquals(10f, out[0])
        assertEquals(20f, out[1])
        assertEquals(0.5f, out[2])
        assertEquals(0.75f, out[3])
        assertEquals(1f, out[4])
        assertEquals(1f, out[8]) // default scaleX
    }

    @Test
    fun testWriteRoundedQuadVertex() {
        val out = FloatArray(16)
        val color = Color(1f, 0f, 0f, 1f)

        writeRoundedQuadVertex(
            out = out,
            offset = 0,
            x = 100f,
            y = 200f,
            localX = 10f,
            localY = 20f,
            halfW = 50f,
            halfH = 60f,
            radius = 8f,
            smoothing = 1.5f,
            color = color,
        )

        assertEquals(100f, out[0])
        assertEquals(200f, out[1])
        assertEquals(10f, out[2])
        assertEquals(20f, out[3])
        assertEquals(50f, out[4])
        assertEquals(60f, out[5])
        assertEquals(8f, out[6])
        assertEquals(1.5f, out[7])
        assertEquals(1f, out[8]) // Red channel
    }
}
