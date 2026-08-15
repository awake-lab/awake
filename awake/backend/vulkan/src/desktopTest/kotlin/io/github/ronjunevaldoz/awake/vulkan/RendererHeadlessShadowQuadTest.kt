// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.vulkan.renderer.renderUiToTexture
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Offscreen target edge, in pixels. Top-level (not a class property) so it can't be shadowed
 * by `ByteArray.size` inside the `redAt` extension below. */
private const val TARGET = 64

/**
 * Pixel-level proof that [UiDrawPrimitive.ShadowQuad] rasterizes a real soft drop shadow --
 * this branch used to be `TODO()`, so any shadcn card/popover/dialog with a `.shadow(...)`
 * style rule crashed the Vulkan renderer with `NotImplementedError`.
 *
 * "It didn't crash" is not enough: the same primitive drawn as an opaque rect would also pass
 * that. Each test below pins one property the single-pass distance-field technique has to
 * produce -- a soft falloff band, a falloff whose WIDTH tracks `blurRadius`, and geometry
 * actually displaced by `offsetY`.
 */
class RendererHeadlessShadowQuadTest {

    /** Red channel at [x]/[y] -- the render pass clears to opaque black and the fixtures below
     * use a white shadow colour, so red reads directly as "how much shadow landed here". */
    private fun ByteArray.redAt(x: Int, y: Int): Int = this[(y * TARGET + x) * 4].toInt() and 0xFF

    private fun render(renderer: io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer, shadow: UiDrawPrimitive.ShadowQuad): ByteArray {
        val target = renderer.createRenderTarget(TARGET, TARGET)
        renderer.renderUiToTexture(target, listOf(shadow), font = null)
        return runBlocking { renderer.readPixels(target) }.data
    }

    private fun shadow(blurRadius: Float, offsetY: Float = 0f) = UiDrawPrimitive.ShadowQuad(
        // Centered 24x24 box in a 64x64 target: 12px from centre to edge, so even the widest
        // blur below still reaches full opacity at the centre and full transparency at the border.
        x = 20f,
        y = 20f,
        w = 24f,
        h = 24f,
        radius = 6f,
        offsetX = 0f,
        offsetY = offsetY,
        blurRadius = blurRadius,
        spread = 0f,
        color = Color(1f, 1f, 1f, 1f),
    )

    @Test
    fun blurredShadowQuadRendersSoftFalloffNotAHardEdge() {
        RendererHeadlessUiGlyphBaselineTest.withSharedHeadlessRenderer { renderer ->
            val data = render(renderer, shadow(blurRadius = 8f))

            assertTrue(data.redAt(32, 32) >= 250, "shadow centre should be fully opaque, got ${data.redAt(32, 32)}")
            assertTrue(data.redAt(1, 32) <= 5, "far outside the shadow should be untouched, got ${data.redAt(1, 32)}")

            // Scanline through the centre: alpha = 1 - smoothstep(-blur, blur, sdf), so the left
            // edge (12px from centre) ramps across roughly x = 12..28. A hard-edged fill would
            // give at most one or two intermediate pixels of antialiasing here.
            val ramp = (8..30).map { data.redAt(it, 32) }
            assertTrue(
                ramp.count { it in 1..254 } >= 8,
                "expected a wide soft falloff band along y=32, got $ramp",
            )
            assertTrue(
                ramp.zipWithNext().all { (left, right) -> right >= left - 2 },
                "falloff should rise monotonically toward the shadow centre, got $ramp",
            )
        }
    }

    @Test
    fun falloffWidthTracksBlurRadius() {
        RendererHeadlessUiGlyphBaselineTest.withSharedHeadlessRenderer { renderer ->
            fun softPixelCount(blurRadius: Float): Int =
                render(renderer, shadow(blurRadius)).let { data -> (0 until TARGET).count { data.redAt(it, 32) in 1..254 } }

            val sharp = softPixelCount(blurRadius = 0f)
            val soft = softPixelCount(blurRadius = 8f)

            // blurRadius 0 floors at the shader's own 1px antialias band (a handful of pixels on
            // each of the two edges the scanline crosses); blurRadius 8 must be far wider. Without
            // this, a shadow implementation that ignored blurRadius entirely would still pass the
            // soft-falloff test above purely on corner antialiasing.
            assertTrue(sharp <= 8, "blurRadius=0 should be near hard-edged, got $sharp soft pixels")
            assertTrue(soft >= sharp * 3, "blurRadius=8 should be much softer than blurRadius=0, got $soft vs $sharp")
        }
    }

    @Test
    fun offsetYDisplacesTheShadow() {
        RendererHeadlessUiGlyphBaselineTest.withSharedHeadlessRenderer { renderer ->
            val centered = render(renderer, shadow(blurRadius = 4f))
            val dropped = render(renderer, shadow(blurRadius = 4f, offsetY = 12f))

            // y=50 sits past the un-offset shadow's blur band entirely (bottom edge 44 + blur 4),
            // but a solid 6px inside the shifted one's (bottom edge 56) -- so it flips from fully
            // dark to fully lit purely because offsetY was applied.
            assertEquals(0, centered.redAt(32, 50), "un-offset shadow should not reach y=50")
            assertTrue(dropped.redAt(32, 50) >= 250, "offsetY=12 should push the shadow down over y=50, got ${dropped.redAt(32, 50)}")
        }
    }
}
