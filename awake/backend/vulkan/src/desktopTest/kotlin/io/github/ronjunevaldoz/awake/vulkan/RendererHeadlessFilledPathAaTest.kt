// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.uiPath
import io.github.ronjunevaldoz.awake.vulkan.renderer.renderUiToTexture
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Pixel-level proof that [UiDrawPrimitive.FilledPath]'s diagonal edges are antialiased --
 * regression coverage for the "dropdown chevron looks jagged" report this session fixed by
 * adding [io.github.ronjunevaldoz.awake.ui.tessellateFillAa]'s AA fringe (see its doc comment
 * for why an AA fringe ring was chosen over MSAA). A pixel-baseline snapshot alone can't prove
 * "smoother" -- this samples one scanline crossing a known diagonal edge and asserts the alpha
 * channel takes on INTERMEDIATE values there (not just hard 0/255 jumps), which is only
 * possible with the fringe ring's per-vertex alpha fade.
 */
class RendererHeadlessFilledPathAaTest {

    @Test
    fun diagonalFilledPathEdgeHasIntermediateAlphaValues() {
        RendererHeadlessUiGlyphBaselineTest.withSharedHeadlessRenderer { renderer ->
            val size = 64
            val target = renderer.createRenderTarget(size, size)

            // A downward-pointing chevron-like triangle -- same shape family as
            // UiIcons.chevronDown (Icons.kt), with a clear diagonal edge on its left side
            // (from (8, 8) to (32, 40)) that a hard-edged rasterizer would render as a jagged
            // stair-step, one pixel-row at a time, with zero antialiasing.
            val triangle = UiDrawPrimitive.FilledPath(
                path = uiPath {
                    moveTo(8f, 8f)
                    lineTo(56f, 8f)
                    lineTo(32f, 40f)
                    close()
                },
                color = Color(1f, 1f, 1f, 1f)
            )

            renderer.renderUiToTexture(target, listOf(triangle), font = null)
            val pixels = runBlocking { renderer.readPixels(target) }
            val data = pixels.data

            // The render pass clears to OPAQUE black (Renderer.clearColorValue: rgba(0,0,0,1)),
            // and the triangle is opaque white, so alpha stays ~1 everywhere post-composite --
            // it's the RGB channel (red, here, since white == (1,1,1,1)) that carries the
            // AA fringe's blend-with-black gradient, going from 0 (background) through
            // intermediate grays (fringe) to 255 (solid white interior). This is the same
            // information a screenshot-diff would show as "smoother edge", just read directly
            // off the pixel buffer instead of eyeballing a PNG.
            fun redAt(x: Int, y: Int): Int {
                val offset = (y * size + x) * 4
                return data[offset].toInt() and 0xFF
            }

            // Scanline y = 24 crosses the diagonal left edge (line from (8,8) to (32,40): at
            // y=24, x = 8 + (24-8)/(40-8)*(32-8) = 8 + 0.5*24 = 20). Sample a window of x
            // values around that crossing and require at least one strictly-intermediate red
            // value (neither fully black background nor fully white interior) -- the AA
            // fringe's signature, impossible from the old hard-edged tessellateFill path
            // (every pixel there was either exactly 0 or exactly 255, a hard stair-step).
            val scanlineReds = (14..26).map { x -> redAt(x, 24) }
            val intermediateReds = scanlineReds.filter { it in 1..254 }
            assertTrue(
                intermediateReds.isNotEmpty(),
                "expected at least one intermediate (non-0/255) red value along the " +
                    "diagonal FilledPath edge at y=24, got reds=$scanlineReds"
            )

            // Sanity: the shape actually rendered a real edge -- both a solid white interior
            // pixel AND a solid black background pixel on the same scanline -- otherwise the
            // intermediate-value assertion above would trivially pass on a broken/empty render.
            val fullScanlineReds = (0 until size).map { x -> redAt(x, 24) }
            assertTrue(fullScanlineReds.any { it >= 250 }, "expected a solid white interior pixel on the scanline")
            assertTrue(fullScanlineReds.any { it <= 5 }, "expected a solid black background pixel on the scanline")
        }
    }
}
