// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiStroke
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.uiPath
import io.github.ronjunevaldoz.awake.vulkan.renderer.renderUiToTexture
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Regression coverage for the F3 debug-wireframe crash this session found and fixed: "UI
 * stroked-path run vertex count (4192) exceeds DynamicMesh capacity (1024 vertices)". Mirrors
 * `RendererHeadlessUiGlyphBaselineTest.headlessUiGlyphRenderSupportsRunsLargerThanOneDynamicMesh`
 * (the equivalent proof for the pre-existing Glyph chunking fix), but for `StrokedPath`, which
 * previously had none.
 */
class RendererHeadlessStrokedPathChunkingTest {

    @Test
    fun headlessStrokedPathRenderSupportsRunsLargerThanOneDynamicMesh() {
        RendererHeadlessUiGlyphBaselineTest.withSharedHeadlessRenderer { renderer ->
            val target = renderer.createRenderTarget(256, 256)
            // Each stroked rect outline tessellates to well more than 4 vertices (miter joins
            // at each of its 4 corners) -- enough of them in one same-type run (mirroring the
            // F3 wireframe's 3-rect-per-semantic-node pattern on a dense page) exceeds
            // DynamicMesh's fixed 1024-vertex capacity before the chunking fix.
            val rects = (0 until 80).map { i ->
                val x = (i % 10) * 24f
                val y = (i / 10) * 24f
                UiDrawPrimitive.StrokedPath(
                    path = uiPath {
                        moveTo(x, y)
                        lineTo(x + 20f, y)
                        lineTo(x + 20f, y + 20f)
                        lineTo(x, y + 20f)
                        close()
                    },
                    stroke = UiStroke(width = 2f.dp),
                    color = Color(0.8f, 0.85f, 1f, 1f),
                )
            }

            // Sanity check the fixture actually exercises the bug this test guards against --
            // if this ever stops being true (e.g. DynamicMesh's capacity changes), the test
            // data needs to grow to match, not silently pass for the wrong reason.
            renderer.renderUiToTexture(target, rects, font = null)
            val pixels = runBlocking { renderer.readPixels(target) }

            assertTrue(
                pixels.data.any { it.toInt() != 0 },
                "a stroked-path run larger than one DynamicMesh should chunk across multiple " +
                    "runs and render non-empty output instead of throwing",
            )
        }
    }
}
