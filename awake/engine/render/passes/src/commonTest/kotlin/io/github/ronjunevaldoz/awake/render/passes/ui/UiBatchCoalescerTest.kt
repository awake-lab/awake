// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiBatchCoalescerTest {

    @Test
    fun testCoalesceSimpleQuads() {
        val primitives = listOf(
            UiDrawPrimitive.Quad(0f, 0f, 100f, 50f, Color(1f, 1f, 1f, 1f)),
            UiDrawPrimitive.Quad(100f, 0f, 100f, 50f, Color(1f, 0f, 0f, 1f)),
        )

        val runs = UiBatchCoalescer.coalesce(primitives)
        assertEquals(1, runs.size)
        assertTrue(runs[0] is UiStagedRun.QuadRun)

        val quadRun = runs[0] as UiStagedRun.QuadRun
        // 2 quads * 4 vertices * 10 floats = 80 floats
        assertEquals(80, quadRun.vertices.size)
        // 2 quads * 6 indices = 12 indices
        assertEquals(12, quadRun.indices.size)
    }

    @Test
    fun testCoalesceInterleavedPrimitivesAndClips() {
        val primitives = listOf(
            UiDrawPrimitive.Quad(0f, 0f, 100f, 50f, Color(1f, 1f, 1f, 1f)),
            UiDrawPrimitive.ClipPush(UiBounds(10f, 10f, 80f, 30f)),
            UiDrawPrimitive.RoundedQuad(10f, 10f, 80f, 30f, Color(0f, 0f, 1f, 1f), 4f, 1f),
            UiDrawPrimitive.ClipPop(UiBounds(0f, 0f, 200f, 200f)),
            UiDrawPrimitive.Glyph(20f, 20f, 16f, 16f, 0f, 0f, 0.5f, 0.5f, Color(1f, 1f, 1f, 1f)),
        )

        val runs = UiBatchCoalescer.coalesce(primitives)
        assertEquals(5, runs.size)
        assertTrue(runs[0] is UiStagedRun.QuadRun)
        assertTrue(runs[1] is UiStagedRun.ClipRun)
        assertTrue(runs[2] is UiStagedRun.RoundedQuadRun)
        assertTrue(runs[3] is UiStagedRun.ClipRun)
        assertTrue(runs[4] is UiStagedRun.GlyphRun)

        val clip1 = runs[1] as UiStagedRun.ClipRun
        assertEquals(UiBounds(10f, 10f, 80f, 30f), clip1.rect)

        val clip2 = runs[3] as UiStagedRun.ClipRun
        assertEquals(UiBounds(0f, 0f, 200f, 200f), clip2.rect)
    }

    @Test
    fun testShadowQuadCoalescing() {
        val primitives = listOf(
            UiDrawPrimitive.ShadowQuad(
                x = 10f,
                y = 10f,
                w = 100f,
                h = 50f,
                radius = 8f,
                color = Color(0f, 0f, 0f, 1f),
                blurRadius = 4f,
                spread = 2f,
                offsetX = 0f,
                offsetY = 4f,
            ),
        )

        val runs = UiBatchCoalescer.coalesce(primitives)
        assertEquals(1, runs.size)
        assertTrue(runs[0] is UiStagedRun.RoundedQuadRun)

        val shadowRun = runs[0] as UiStagedRun.RoundedQuadRun
        // 1 quad * 4 vertices * 16 floats = 64 floats
        assertEquals(64, shadowRun.vertices.size)
        assertEquals(6, shadowRun.indices.size)
    }

    @Test
    fun testChunkingExceedingMaxQuads() {
        val primitives = (0 until 10).map { i ->
            UiDrawPrimitive.Quad(i * 10f, 0f, 10f, 10f, Color(1f, 1f, 1f, 1f))
        }

        // maxQuadsPerBatch = 3 -> 10 quads should produce 4 chunks (3, 3, 3, 1)
        val runs = UiBatchCoalescer.coalesce(primitives, maxQuadsPerBatch = 3)
        assertEquals(4, runs.size)
        assertTrue(runs.all { it is UiStagedRun.QuadRun })

        val q0 = runs[0] as UiStagedRun.QuadRun
        assertEquals(3 * 4 * 10, q0.vertices.size)

        val q3 = runs[3] as UiStagedRun.QuadRun
        assertEquals(1 * 4 * 10, q3.vertices.size)
    }
}
